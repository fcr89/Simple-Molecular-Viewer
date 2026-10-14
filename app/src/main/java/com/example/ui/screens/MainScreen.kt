package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.CropFree
import androidx.compose.material.icons.filled.Dataset
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.ViewInAr
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.Atom
import com.example.model.MolecularColorScheme
import com.example.model.ProteinStructure
import com.example.model.RenderStyle
import com.example.model.Residue
import com.example.model.SecondaryStructure
import com.example.model.StructureSource
import com.example.ui.components.BioscienceInsightBottomSheet
import com.example.ui.components.MolecularViewer3D
import com.example.ui.components.MolstarWebViewer
import com.example.ui.components.ResidueSequenceBar
import com.example.ui.components.StructureCatalogBottomSheet
import com.example.ui.theme.*
import com.example.ui.viewmodel.ProteinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: ProteinViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val ttsState by viewModel.ttsState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    var showStyleMenu by remember { mutableStateOf(false) }
    var showColorMenu by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearError()
        }
    }

    Scaffold(
        modifier = modifier.testTag("fold3d_main_screen"),
        containerColor = BioDarkBg,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BioSurface,
                    titleContentColor = Color.White
                ),
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(CyanPrimary.copy(alpha = 0.2f))
                                .border(1.dp, CyanPrimary.copy(alpha = 0.4f), RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.ViewInAr,
                                contentDescription = null,
                                tint = CyanPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Fold3D",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(MagentaSecondary.copy(alpha = 0.2f))
                                        .padding(horizontal = 5.dp, vertical = 1.dp)
                                ) {
                                    Text(
                                        text = uiState.structure?.source?.label ?: "PDB",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MagentaSecondary
                                    )
                                }
                            }
                            Text(
                                text = uiState.structure?.title ?: "Protein Structure Viewer",
                                fontSize = 11.sp,
                                color = Color.LightGray,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.width(180.dp)
                            )
                        }
                    }
                },
                actions = {
                    // Renderer Engine Toggle (Native 3D vs WebGL)
                    IconButton(
                        onClick = { viewModel.toggleWebGLViewer() },
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = if (uiState.useWebGLViewer) CyanPrimary.copy(alpha = 0.2f) else Color.Transparent
                        ),
                        modifier = Modifier.testTag("toggle_engine_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.OpenInBrowser,
                            contentDescription = "Toggle WebGL",
                            tint = if (uiState.useWebGLViewer) CyanPrimary else Color.LightGray
                        )
                    }

                    // Bioscience Intelligence & TTS
                    IconButton(
                        onClick = { viewModel.openInsight() },
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = if (ttsState.isPlaying) MagentaSecondary.copy(alpha = 0.25f) else Color.Transparent
                        ),
                        modifier = Modifier.testTag("open_bioscience_btn")
                    ) {
                        Icon(
                            imageVector = if (ttsState.isPlaying) Icons.Default.GraphicEq else Icons.Default.Science,
                            contentDescription = "Bioscience & TTS",
                            tint = if (ttsState.isPlaying) MagentaSecondary else CyanPrimary
                        )
                    }

                    // Structure Catalog / Search
                    IconButton(
                        onClick = { viewModel.openCatalog() },
                        modifier = Modifier.testTag("open_catalog_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search Structures",
                            tint = Color.White
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            val structure = uiState.structure

            if (structure != null) {
                // 3D Viewport
                if (uiState.useWebGLViewer) {
                    MolstarWebViewer(
                        structure = structure,
                        renderStyle = uiState.renderStyle,
                        colorScheme = uiState.colorScheme,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    MolecularViewer3D(
                        structure = structure,
                        renderStyle = uiState.renderStyle,
                        colorScheme = uiState.colorScheme,
                        selectedResidue = uiState.selectedResidue,
                        measurementMode = uiState.measurementMode,
                        measuredAtom1 = uiState.measuredAtom1,
                        measuredAtom2 = uiState.measuredAtom2,
                        autoSpin = uiState.autoSpin,
                        onAtomSelected = { atom, res -> viewModel.selectAtom(atom, res) },
                        onResetCamera = uiState.resetCameraTrigger,
                        onCameraResetDone = { viewModel.onCameraResetDone() },
                        modifier = Modifier.fillMaxSize()
                    )
                }

                // Top Floating Toolbar (Style, Color, Reset, Spin, Ruler)
                Column(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Style & Color Row
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Style Chip
                        Box {
                            FilterChip(
                                selected = true,
                                onClick = { showStyleMenu = true },
                                label = {
                                    Text(
                                        uiState.renderStyle.label,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.ViewInAr,
                                        contentDescription = null,
                                        tint = CyanPrimary,
                                        modifier = Modifier.size(14.dp)
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = BioSurface.copy(alpha = 0.85f),
                                    selectedLabelColor = CyanPrimary
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = true,
                                    borderColor = CyanPrimary.copy(alpha = 0.4f)
                                ),
                                modifier = Modifier.testTag("style_chip")
                            )

                            DropdownMenu(
                                expanded = showStyleMenu,
                                onDismissRequest = { showStyleMenu = false },
                                modifier = Modifier.background(BioSurfaceElevated)
                            ) {
                                RenderStyle.values().forEach { style ->
                                    DropdownMenuItem(
                                        text = {
                                            Column {
                                                Text(style.label, color = if (uiState.renderStyle == style) CyanPrimary else Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                                Text(style.iconDescription, color = Color.Gray, fontSize = 10.sp)
                                            }
                                        },
                                        onClick = {
                                            viewModel.setRenderStyle(style)
                                            showStyleMenu = false
                                        }
                                    )
                                }
                            }
                        }

                        // Color Scheme Chip
                        Box {
                            FilterChip(
                                selected = true,
                                onClick = { showColorMenu = true },
                                label = {
                                    Text(
                                        uiState.colorScheme.label,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Palette,
                                        contentDescription = null,
                                        tint = MagentaSecondary,
                                        modifier = Modifier.size(14.dp)
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = BioSurface.copy(alpha = 0.85f),
                                    selectedLabelColor = MagentaSecondary
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = true,
                                    borderColor = MagentaSecondary.copy(alpha = 0.4f)
                                ),
                                modifier = Modifier.testTag("color_chip")
                            )

                            DropdownMenu(
                                expanded = showColorMenu,
                                onDismissRequest = { showColorMenu = false },
                                modifier = Modifier.background(BioSurfaceElevated)
                            ) {
                                MolecularColorScheme.values().forEach { scheme ->
                                    DropdownMenuItem(
                                        text = {
                                            Column {
                                                Text(scheme.label, color = if (uiState.colorScheme == scheme) MagentaSecondary else Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                                Text(scheme.description, color = Color.Gray, fontSize = 10.sp)
                                            }
                                        },
                                        onClick = {
                                            viewModel.setColorScheme(scheme)
                                            showColorMenu = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                // Right Floating Tool Controls (Reset, Auto-spin, Distance Measure)
                Column(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Reset View
                    ToolIconButton(
                        icon = Icons.Default.RestartAlt,
                        label = "Reset View",
                        active = false,
                        onClick = { viewModel.resetCamera() },
                        tag = "reset_view_btn"
                    )

                    // Auto Spin
                    ToolIconButton(
                        icon = Icons.Default.Sync,
                        label = "Auto Spin",
                        active = uiState.autoSpin,
                        activeColor = CyanPrimary,
                        onClick = { viewModel.toggleAutoSpin() },
                        tag = "auto_spin_btn"
                    )

                    // Distance Measure Tool
                    ToolIconButton(
                        icon = Icons.Default.Straighten,
                        label = "Measure Distance",
                        active = uiState.measurementMode,
                        activeColor = NeonGreen,
                        onClick = { viewModel.toggleMeasurementMode() },
                        tag = "measure_distance_btn"
                    )

                    // Quick Audio Narration Button
                    ToolIconButton(
                        icon = if (ttsState.isPlaying) Icons.Default.Stop else Icons.AutoMirrored.Filled.VolumeUp,
                        label = "TTS Audio",
                        active = ttsState.isPlaying,
                        activeColor = MagentaSecondary,
                        onClick = {
                            if (ttsState.isPlaying) {
                                viewModel.stopNarration()
                            } else {
                                uiState.bioscienceInsight?.let {
                                    viewModel.speakNarration(it.narrationScript, "Kore")
                                }
                            }
                        },
                        tag = "quick_tts_btn"
                    )
                }

                // Measurement HUD Card (If Active)
                if (uiState.measurementMode) {
                    MeasurementHud(
                        atom1 = uiState.measuredAtom1,
                        atom2 = uiState.measuredAtom2,
                        distance = uiState.measuredDistanceAngstroms,
                        onClear = { viewModel.clearMeasurement() },
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 64.dp)
                    )
                }

                // Bottom Section: Atom / Residue Inspector & Sequence Bar
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                ) {
                    // Selected Residue Floating Info Card
                    if (uiState.selectedResidue != null && !uiState.measurementMode) {
                        ResidueInspectorCard(
                            residue = uiState.selectedResidue!!,
                            selectedAtom = uiState.selectedAtom,
                            onClose = { viewModel.selectResidue(uiState.selectedResidue!!) }
                        )
                    }

                    // Audio Mini Player Bar (If Playing)
                    if (ttsState.isPlaying || ttsState.isGenerating) {
                        TtsMiniPlayerBar(
                            ttsState = ttsState,
                            onStop = { viewModel.stopNarration() }
                        )
                    }

                    // Amino Acid Sequence Scrubber Bar
                    ResidueSequenceBar(
                        chains = structure.chains,
                        selectedResidue = uiState.selectedResidue,
                        onResidueClicked = { res -> viewModel.selectResidue(res) }
                    )
                }
            }

            // Loading overlay
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(BioDarkBg.copy(alpha = 0.7f)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        CircularProgressIndicator(color = CyanPrimary)
                        Text(
                            text = "Parsing 3D Macromolecular Coordinates...",
                            color = CyanPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            // Catalog Bottom Sheet
            if (uiState.showCatalogSheet) {
                StructureCatalogBottomSheet(
                    isLoading = uiState.isLoading,
                    onSelectPreset = { preset -> viewModel.loadPreset(preset) },
                    onSearchPdb = { id -> viewModel.fetchPdb(id) },
                    onSearchAlphaFold = { id -> viewModel.fetchAlphaFold(id) },
                    onLoadCustomText = { text -> viewModel.loadCustomPdbText(text) },
                    onDismiss = { viewModel.closeCatalog() }
                )
            }

            // Bioscience Intelligence & TTS Bottom Sheet
            if (uiState.showInsightSheet && uiState.structure != null && uiState.bioscienceInsight != null) {
                BioscienceInsightBottomSheet(
                    structure = uiState.structure!!,
                    insight = uiState.bioscienceInsight!!,
                    ttsState = ttsState,
                    onSpeakClicked = { text, voice -> viewModel.speakNarration(text, voice) },
                    onStopClicked = { viewModel.stopNarration() },
                    onDismiss = { viewModel.closeInsight() }
                )
            }
        }
    }
}

// ----------------- UI Subcomponents -----------------

@Composable
private fun ToolIconButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    active: Boolean,
    activeColor: Color = CyanPrimary,
    onClick: () -> Unit,
    tag: String
) {
    Box(
        modifier = Modifier
            .size(42.dp)
            .clip(CircleShape)
            .background(if (active) activeColor.copy(alpha = 0.3f) else BioSurface.copy(alpha = 0.85f))
            .border(
                width = 1.dp,
                color = if (active) activeColor else BioSurfaceVariant,
                shape = CircleShape
            )
            .clickable(onClick = onClick)
            .testTag(tag),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (active) activeColor else Color.White,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun MeasurementHud(
    atom1: Atom?,
    atom2: Atom?,
    distance: Float?,
    onClear: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = BioSurfaceElevated.copy(alpha = 0.95f)),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
            .padding(horizontal = 16.dp)
            .border(1.dp, NeonGreen.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
            .testTag("measurement_hud")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
        ) {
            Icon(Icons.Default.Straighten, contentDescription = null, tint = NeonGreen, modifier = Modifier.size(18.dp))
            Column {
                if (atom1 == null) {
                    Text("Select 1st atom in 3D viewer", fontSize = 12.sp, color = Color.White)
                } else if (atom2 == null) {
                    Text("1: ${atom1.name} (${atom1.resName} ${atom1.resSeq}) → Select 2nd atom", fontSize = 12.sp, color = NeonGreen)
                } else if (distance != null) {
                    Text(
                        text = "Distance: ${"%.2f".format(distance)} Ångströms (Å)",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = NeonGreen
                    )
                    Text(
                        text = "${atom1.name} (${atom1.resName} ${atom1.resSeq}) ↔ ${atom2.name} (${atom2.resName} ${atom2.resSeq})",
                        fontSize = 11.sp,
                        color = Color.LightGray
                    )
                }
            }

            if (atom1 != null) {
                IconButton(onClick = onClear, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.Close, contentDescription = "Clear", tint = Color.Gray, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

@Composable
private fun ResidueInspectorCard(
    residue: Residue,
    selectedAtom: Atom?,
    onClose: () -> Unit
) {
    val secColor = when (residue.secondaryStructure) {
        SecondaryStructure.HELIX -> ColorHelix
        SecondaryStructure.SHEET -> ColorSheet
        SecondaryStructure.LOOP -> ColorLoop
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = BioSurface.copy(alpha = 0.92f)),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp)
            .border(1.dp, CyanPrimary.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
            .testTag("residue_inspector_card")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Large Residue Monogram
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(secColor.copy(alpha = 0.2f))
                        .border(1.dp, secColor, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${residue.oneLetterCode}",
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = secColor
                    )
                }

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "${residue.fullName} (${residue.resName} ${residue.resSeq})",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Chain ${residue.chainID}",
                            fontSize = 11.sp,
                            color = CyanPrimary
                        )
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = residue.secondaryStructure.displayName,
                            fontSize = 11.sp,
                            color = secColor
                        )
                        Text("•", color = Color.Gray, fontSize = 10.sp)
                        Text(
                            text = residue.polarityType,
                            fontSize = 11.sp,
                            color = Color.LightGray
                        )
                        if (residue.meanPLDDT > 0) {
                            Text("•", color = Color.Gray, fontSize = 10.sp)
                            Text(
                                text = "pLDDT: ${"%.1f".format(residue.meanPLDDT)}",
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                color = if (residue.meanPLDDT >= 70) CyanPrimary else AmberTertiary
                            )
                        }
                    }
                }
            }

            if (selectedAtom != null) {
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Atom: ${selectedAtom.name}",
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        color = CyanPrimary
                    )
                    Text(
                        text = "${selectedAtom.element} • (${"%.1f".format(selectedAtom.position.x)}, ${"%.1f".format(selectedAtom.position.y)})",
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}

@Composable
private fun TtsMiniPlayerBar(
    ttsState: com.example.ai.TtsPlaybackState,
    onStop: () -> Unit
) {
    Surface(
        color = BioSurfaceElevated,
        shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MagentaSecondary.copy(alpha = 0.4f), RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
            .testTag("tts_mini_player_bar")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.GraphicEq,
                    contentDescription = null,
                    tint = MagentaSecondary,
                    modifier = Modifier.size(20.dp)
                )

                Column {
                    Text(
                        text = "Speaking with gemini-3.8-flash-tts (${ttsState.voiceName})",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = Color.White
                    )
                    Text(
                        text = ttsState.statusMessage,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        color = MagentaSecondary
                    )
                }
            }

            IconButton(onClick = onStop, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.Stop, contentDescription = "Stop", tint = MagentaSecondary)
            }
        }
    }
}
