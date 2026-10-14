package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.PointMode
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import com.example.model.*
import com.example.ui.theme.*
import kotlin.math.*

private val CHAIN_COLORS = listOf(
    Color(0xFF00E5FF), // Cyan
    Color(0xFFFF2A85), // Magenta
    Color(0xFFFFB300), // Amber
    Color(0xFF00E676), // Green
    Color(0xFF7C4DFF), // Purple
    Color(0xFFFF6E40), // Orange
    Color(0xFF40C4FF), // Light Blue
    Color(0xFFE040FB)  // Pink
)

data class ProjectedPoint(
    val atom: Atom,
    val residue: Residue,
    val screenX: Float,
    val screenY: Float,
    val depthZ: Float, // Higher means closer to viewer
    val color: Color,
    val radiusPx: Float
)

data class ProjectedSegment(
    val p1: ProjectedPoint,
    val p2: ProjectedPoint,
    val strokeWidth: Float,
    val color: Color,
    val secondaryStructure: SecondaryStructure
) {
    val meanDepth: Float get() = (p1.depthZ + p2.depthZ) / 2f
}

@Composable
fun MolecularViewer3D(
    structure: ProteinStructure,
    renderStyle: RenderStyle,
    colorScheme: MolecularColorScheme,
    selectedResidue: Residue?,
    measurementMode: Boolean,
    measuredAtom1: Atom?,
    measuredAtom2: Atom?,
    autoSpin: Boolean,
    onAtomSelected: (Atom, Residue) -> Unit,
    onResetCamera: Boolean,
    onCameraResetDone: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Camera Transform State
    var rotX by remember { mutableFloatStateOf(15f) }
    var rotY by remember { mutableFloatStateOf(25f) }
    var rotZ by remember { mutableFloatStateOf(0f) }
    var zoom by remember { mutableFloatStateOf(1.0f) }
    var panX by remember { mutableFloatStateOf(0f) }
    var panY by remember { mutableFloatStateOf(0f) }

    // Reset Camera Trigger
    LaunchedEffect(onResetCamera) {
        if (onResetCamera) {
            rotX = 15f
            rotY = 25f
            rotZ = 0f
            zoom = 1.0f
            panX = 0f
            panY = 0f
            onCameraResetDone()
        }
    }

    // Auto-spin transition
    val infiniteTransition = rememberInfiniteTransition(label = "autoSpinTransition")
    val spinAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "spinAngle"
    )

    val currentRotY = if (autoSpin) (rotY + spinAngle) % 360f else rotY

    // Pinch-to-zoom and Pan state
    val transformState = rememberTransformableState { zoomChange, panChange, _ ->
        zoom = (zoom * zoomChange).coerceIn(0.25f, 5.0f)
        panX += panChange.x
        panY += panChange.y
    }

    // Keep track of latest projected atoms for touch selection
    var latestProjectedPoints by remember { mutableStateOf<List<ProjectedPoint>>(emptyList()) }

    Box(
        modifier = modifier
            .testTag("molecular_3d_viewport")
            .background(BioDarkBg)
            .transformable(state = transformState)
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    // 1-finger drag = 3D rotation
                    rotY = (rotY + dragAmount.x * 0.45f) % 360f
                    rotX = (rotX - dragAmount.y * 0.45f).coerceIn(-89f, 89f)
                }
            }
            .pointerInput(measurementMode, latestProjectedPoints) {
                detectTapGestures { tapOffset ->
                    var closestPoint: ProjectedPoint? = null
                    var minDist = 48f // 48px hit target

                    for (p in latestProjectedPoints) {
                        val dx = p.screenX - tapOffset.x
                        val dy = p.screenY - tapOffset.y
                        val dist = sqrt(dx * dx + dy * dy)
                        if (dist < minDist) {
                            minDist = dist
                            closestPoint = p
                        }
                    }

                    if (closestPoint != null) {
                        onAtomSelected(closestPoint.atom, closestPoint.residue)
                    }
                }
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasW = size.width
            val canvasH = size.height
            val centerX = canvasW / 2f + panX
            val centerY = canvasH / 2f + panY

            if (structure.allAtoms.isEmpty()) return@Canvas

            // Compute Camera and Model Dimensions
            val molRadius = max(structure.radius, 10f)
            val baseScale = (min(canvasW, canvasH) * 0.45f / molRadius) * zoom
            val cameraDist = molRadius * 3.0f

            // Trigonometry for Euler angles
            val radX = Math.toRadians(rotX.toDouble()).toFloat()
            val radY = Math.toRadians(currentRotY.toDouble()).toFloat()
            val radZ = Math.toRadians(rotZ.toDouble()).toFloat()

            val cosX = cos(radX); val sinX = sin(radX)
            val cosY = cos(radY); val sinY = sin(radY)
            val cosZ = cos(radZ); val sinZ = sin(radZ)

            // Center of molecule
            val center = structure.center

            // Project all relevant atoms
            val projectedList = mutableListOf<ProjectedPoint>()

            // Fast atom-to-residue lookup map
            val atomToResidue = mutableMapOf<Int, Residue>()
            for (chain in structure.chains.values) {
                for (res in chain) {
                    for (a in res.atoms) {
                        atomToResidue[a.serial] = res
                    }
                }
            }

            // Decide which atoms to project based on render style
            val atomsToRender = when (renderStyle) {
                RenderStyle.CARTOON, RenderStyle.BACKBONE -> structure.alphaCarbons.ifEmpty { structure.allAtoms }
                RenderStyle.BALL_AND_STICK -> {
                    // Render backbone + CB for clarity, or all if small
                    if (structure.allAtoms.size < 800) structure.allAtoms
                    else structure.allAtoms.filter { it.isBackbone || it.name == "CB" }
                }
                RenderStyle.SPACEFILL -> {
                    if (structure.allAtoms.size < 1200) structure.allAtoms
                    else structure.allAtoms.filter { it.isBackbone || it.name == "CB" }
                }
            }

            val totalResidues = max(structure.totalResidues, 1)

            for (atom in atomsToRender) {
                val res = atomToResidue[atom.serial] ?: continue

                // 1. Center
                val rx = atom.position.x - center.x
                val ry = atom.position.y - center.y
                val rz = atom.position.z - center.z

                // 2. Rotate around Y (Yaw)
                val x1 = cosY * rx + sinY * rz
                val y1 = ry
                val z1 = -sinY * rx + cosY * rz

                // 3. Rotate around X (Pitch)
                val x2 = x1
                val y2 = cosX * y1 - sinX * z1
                val z2 = sinX * y1 + cosX * z1

                // 4. Perspective projection
                val depthZ = z2
                val distFromEye = cameraDist - depthZ
                val perspective = if (distFromEye > 0.1f) cameraDist / distFromEye else 1.0f

                val screenX = centerX + x2 * baseScale * perspective
                val screenY = centerY - y2 * baseScale * perspective

                // Determine atom color
                val color = resolveColor(
                    atom = atom,
                    residue = res,
                    colorScheme = colorScheme,
                    totalResidues = totalResidues,
                    chains = structure.chainIds
                )

                // Depth-adjusted sphere size
                val baseRadiusPx = when (renderStyle) {
                    RenderStyle.CARTOON -> 3.5f
                    RenderStyle.BACKBONE -> 5.0f
                    RenderStyle.BALL_AND_STICK -> (atom.vdwRadius * 4.0f)
                    RenderStyle.SPACEFILL -> (atom.vdwRadius * 9.5f)
                } * baseScale / 12f * perspective

                projectedList.add(
                    ProjectedPoint(
                        atom = atom,
                        residue = res,
                        screenX = screenX,
                        screenY = screenY,
                        depthZ = depthZ,
                        color = color,
                        radiusPx = max(baseRadiusPx, 2.5f)
                    )
                )
            }

            latestProjectedPoints = projectedList

            // Render according to style
            when (renderStyle) {
                RenderStyle.CARTOON -> drawCartoonRibbon(structure, projectedList, baseScale)
                RenderStyle.BACKBONE -> drawBackboneTrace(structure, projectedList, baseScale)
                RenderStyle.BALL_AND_STICK -> drawBallAndStick(projectedList, baseScale)
                RenderStyle.SPACEFILL -> drawSpacefill(projectedList)
            }

            // Draw Selected Residue Highlight
            if (selectedResidue != null) {
                drawSelectionHighlight(selectedResidue, projectedList)
            }

            // Draw Distance Measurement if active
            if (measurementMode && measuredAtom1 != null && measuredAtom2 != null) {
                drawDistanceMeasurement(measuredAtom1, measuredAtom2, projectedList)
            }
        }
    }
}

// ---------------- Helper Drawing Functions ----------------

private fun DrawScope.drawCartoonRibbon(
    structure: ProteinStructure,
    projectedPoints: List<ProjectedPoint>,
    baseScale: Float
) {
    val pointMap = projectedPoints.associateBy { it.atom.serial }

    for ((_, residues) in structure.chains) {
        val caPoints = mutableListOf<ProjectedPoint>()
        for (res in residues) {
            val ca = res.caAtom ?: continue
            pointMap[ca.serial]?.let { caPoints.add(it) }
        }

        if (caPoints.size < 2) continue

        // Draw smooth chain segments sorted by depth
        val segments = mutableListOf<ProjectedSegment>()
        for (i in 0 until caPoints.size - 1) {
            val p1 = caPoints[i]
            val p2 = caPoints[i + 1]

            val sec = p1.residue.secondaryStructure
            val strokeW = when (sec) {
                SecondaryStructure.HELIX -> 9.0f * (baseScale / 12f).coerceIn(0.6f, 1.8f)
                SecondaryStructure.SHEET -> 8.0f * (baseScale / 12f).coerceIn(0.6f, 1.8f)
                SecondaryStructure.LOOP -> 4.0f * (baseScale / 12f).coerceIn(0.6f, 1.8f)
            }

            segments.add(
                ProjectedSegment(
                    p1 = p1,
                    p2 = p2,
                    strokeWidth = strokeW,
                    color = p1.color,
                    secondaryStructure = sec
                )
            )
        }

        // Sort segments from back to front (Painter's algorithm)
        segments.sortBy { it.meanDepth }

        for (seg in segments) {
            val depthAlpha = ((seg.meanDepth + 40f) / 80f).coerceIn(0.45f, 1.0f)
            val litColor = seg.color.copy(alpha = depthAlpha)

            // Draw line segment
            drawLine(
                color = litColor,
                start = Offset(seg.p1.screenX, seg.p1.screenY),
                end = Offset(seg.p2.screenX, seg.p2.screenY),
                strokeWidth = seg.strokeWidth,
                cap = StrokeCap.Round
            )

            // If Beta-sheet, draw a small directional triangle arrowhead at end
            if (seg.secondaryStructure == SecondaryStructure.SHEET) {
                drawSheetArrow(seg, litColor)
            }
        }

        // Draw small alpha carbon bead nodes on top
        for (p in caPoints) {
            drawCircle(
                color = p.color,
                radius = p.radiusPx * 1.1f,
                center = Offset(p.screenX, p.screenY)
            )
        }
    }
}

private fun DrawScope.drawSheetArrow(seg: ProjectedSegment, color: Color) {
    val dx = seg.p2.screenX - seg.p1.screenX
    val dy = seg.p2.screenY - seg.p1.screenY
    val len = sqrt(dx * dx + dy * dy)
    if (len < 5f) return

    val nx = -dy / len
    val ny = dx / len
    val w = seg.strokeWidth * 0.9f

    val arrowPath = Path().apply {
        moveTo(seg.p2.screenX, seg.p2.screenY)
        lineTo(seg.p2.screenX - dx * 0.4f + nx * w, seg.p2.screenY - dy * 0.4f + ny * w)
        lineTo(seg.p2.screenX - dx * 0.4f - nx * w, seg.p2.screenY - dy * 0.4f - ny * w)
        close()
    }
    drawPath(arrowPath, color)
}

private fun DrawScope.drawBackboneTrace(
    structure: ProteinStructure,
    projectedPoints: List<ProjectedPoint>,
    baseScale: Float
) {
    val pointMap = projectedPoints.associateBy { it.atom.serial }

    for ((_, residues) in structure.chains) {
        val caPoints = mutableListOf<ProjectedPoint>()
        for (res in residues) {
            val ca = res.caAtom ?: continue
            pointMap[ca.serial]?.let { caPoints.add(it) }
        }

        if (caPoints.size < 2) continue

        // Connect trace
        for (i in 0 until caPoints.size - 1) {
            val p1 = caPoints[i]
            val p2 = caPoints[i + 1]

            drawLine(
                brush = Brush.linearGradient(
                    colors = listOf(p1.color, p2.color),
                    start = Offset(p1.screenX, p1.screenY),
                    end = Offset(p2.screenX, p2.screenY)
                ),
                start = Offset(p1.screenX, p1.screenY),
                end = Offset(p2.screenX, p2.screenY),
                strokeWidth = 4.5f * (baseScale / 12f).coerceIn(0.5f, 2.0f),
                cap = StrokeCap.Round
            )
        }

        for (p in caPoints) {
            draw3DSphere(p.screenX, p.screenY, p.radiusPx * 1.3f, p.color)
        }
    }
}

private fun DrawScope.drawBallAndStick(
    projectedPoints: List<ProjectedPoint>,
    baseScale: Float
) {
    // Sort atoms from back to front
    val sorted = projectedPoints.sortedBy { it.depthZ }

    // Connect atoms that are close in 3D (covalent bonds ~ 1.2 to 1.8 Å)
    val bondThresholdPx = 1.9f * baseScale

    for (i in 0 until minOf(sorted.size, 600)) {
        val p1 = sorted[i]
        for (j in i + 1 until minOf(sorted.size, 600)) {
            val p2 = sorted[j]
            val dx = p1.atom.position.x - p2.atom.position.x
            val dy = p1.atom.position.y - p2.atom.position.y
            val dz = p1.atom.position.z - p2.atom.position.z
            val dist3D = sqrt(dx * dx + dy * dy + dz * dz)

            if (dist3D in 1.1f..1.85f) {
                // Draw covalent bond cylinder
                drawLine(
                    color = Color.LightGray.copy(alpha = 0.7f),
                    start = Offset(p1.screenX, p1.screenY),
                    end = Offset(p2.screenX, p2.screenY),
                    strokeWidth = 2.5f * (baseScale / 12f).coerceIn(0.5f, 1.8f),
                    cap = StrokeCap.Round
                )
            }
        }
    }

    for (p in sorted) {
        draw3DSphere(p.screenX, p.screenY, p.radiusPx, p.color)
    }
}

private fun DrawScope.drawSpacefill(projectedPoints: List<ProjectedPoint>) {
    val sorted = projectedPoints.sortedBy { it.depthZ }
    for (p in sorted) {
        draw3DSphere(p.screenX, p.screenY, p.radiusPx * 1.5f, p.color)
    }
}

private fun DrawScope.draw3DSphere(x: Float, y: Float, radius: Float, color: Color) {
    // Shaded 3D sphere with diffuse specular highlight
    val highlightOffset = Offset(x - radius * 0.35f, y - radius * 0.35f)
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.85f),
                color,
                color.copy(red = color.red * 0.4f, green = color.green * 0.4f, blue = color.blue * 0.4f)
            ),
            center = highlightOffset,
            radius = radius * 1.1f
        ),
        radius = radius,
        center = Offset(x, y)
    )
}

private fun DrawScope.drawSelectionHighlight(
    selectedResidue: Residue,
    projectedPoints: List<ProjectedPoint>
) {
    val selectedPoints = projectedPoints.filter { it.residue.resSeq == selectedResidue.resSeq }
    for (p in selectedPoints) {
        // Glowing halo circle
        drawCircle(
            color = AmberTertiary.copy(alpha = 0.35f),
            radius = p.radiusPx * 2.6f,
            center = Offset(p.screenX, p.screenY)
        )
        drawCircle(
            color = Color.White,
            radius = p.radiusPx * 1.8f,
            center = Offset(p.screenX, p.screenY),
            style = Stroke(width = 2.5f)
        )
    }
}

private fun DrawScope.drawDistanceMeasurement(
    atom1: Atom,
    atom2: Atom,
    projectedPoints: List<ProjectedPoint>
) {
    val p1 = projectedPoints.firstOrNull { it.atom.serial == atom1.serial } ?: return
    val p2 = projectedPoints.firstOrNull { it.atom.serial == atom2.serial } ?: return

    val dist = atom1.position.distanceTo(atom2.position)

    // Dashed line connecting measured atoms
    drawLine(
        color = NeonGreen,
        start = Offset(p1.screenX, p1.screenY),
        end = Offset(p2.screenX, p2.screenY),
        strokeWidth = 3f,
        pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f)
    )

    // Markers on atoms
    drawCircle(color = NeonGreen, radius = 6f, center = Offset(p1.screenX, p1.screenY))
    drawCircle(color = NeonGreen, radius = 6f, center = Offset(p2.screenX, p2.screenY))

    // Midpoint marker
    val midX = (p1.screenX + p2.screenX) / 2f
    val midY = (p1.screenY + p2.screenY) / 2f
    drawCircle(color = Color.Black.copy(alpha = 0.7f), radius = 18f, center = Offset(midX, midY))
    drawCircle(color = NeonGreen, radius = 18f, center = Offset(midX, midY), style = Stroke(width = 2f))
}

private fun resolveColor(
    atom: Atom,
    residue: Residue,
    colorScheme: MolecularColorScheme,
    totalResidues: Int,
    chains: List<String>
): Color {
    return when (colorScheme) {
        MolecularColorScheme.PLDDT_CONFIDENCE -> {
            val score = atom.tempFactor
            when {
                score >= 90f -> AlphaFoldVeryHigh  // > 90 (Very High)
                score >= 70f -> AlphaFoldConfident // 70-90 (Confident)
                score >= 50f -> AlphaFoldLow       // 50-70 (Low)
                else -> AlphaFoldVeryLow          // < 50 (Very Low)
            }
        }
        MolecularColorScheme.SECONDARY_STRUCTURE -> {
            when (residue.secondaryStructure) {
                SecondaryStructure.HELIX -> ColorHelix
                SecondaryStructure.SHEET -> ColorSheet
                SecondaryStructure.LOOP -> ColorLoop
            }
        }
        MolecularColorScheme.RAINBOW_N_TO_C -> {
            val ratio = (residue.resSeq.toFloat() / totalResidues).coerceIn(0f, 1f)
            // Blue (240°) to Red (0°)
            val hue = (1f - ratio) * 240f
            Color.hsl(hue, 0.95f, 0.55f)
        }
        MolecularColorScheme.RESIDUE_TYPE -> {
            when (residue.polarityType) {
                "Hydrophobic" -> Color(0xFFFFD54F) // Yellow
                "Polar Uncharged" -> Color(0xFF81C784) // Green
                "Acidic (-)" -> Color(0xFFE57373) // Red
                "Basic (+)" -> Color(0xFF64B5F6) // Blue
                else -> Color(0xFFBA68C8)
            }
        }
        MolecularColorScheme.ELEMENT_CPK -> {
            when (atom.element.uppercase()) {
                "C" -> ElementCarbon
                "N" -> ElementNitrogen
                "O" -> ElementOxygen
                "S" -> ElementSulfur
                "P" -> ElementPhosphorus
                "H" -> ElementHydrogen
                "FE" -> ElementIron
                "ZN" -> ElementZinc
                else -> Color(0xFFB0BEC5)
            }
        }
        MolecularColorScheme.B_FACTOR_THERMAL -> {
            val b = atom.tempFactor.coerceIn(0f, 80f)
            val ratio = b / 80f
            val hue = (1f - ratio) * 240f
            Color.hsl(hue, 0.9f, 0.5f)
        }
        MolecularColorScheme.CHAIN -> {
            val idx = chains.indexOf(residue.chainID)
            if (idx >= 0) CHAIN_COLORS[idx % CHAIN_COLORS.size] else Color(0xFF00E5FF)
        }
    }
}
