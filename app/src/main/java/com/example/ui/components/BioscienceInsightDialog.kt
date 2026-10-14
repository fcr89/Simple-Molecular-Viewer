package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ai.BioscienceInsight
import com.example.ai.TtsPlaybackState
import com.example.ai.TtsProvider
import com.example.model.ProteinStructure
import com.example.model.StructureSource
import com.example.ui.theme.*

private val GEMINI_VOICES = listOf("Kore", "Puck", "Aoede", "Charon", "Fenrir")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BioscienceInsightBottomSheet(
    structure: ProteinStructure,
    insight: BioscienceInsight,
    ttsState: TtsPlaybackState,
    onSpeakClicked: (String, String) -> Unit, // text, voice
    onStopClicked: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var selectedVoice by remember { mutableStateOf("Kore") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = BioSurface,
        scrimColor = Color.Black.copy(alpha = 0.65f),
        modifier = modifier.testTag("bioscience_insight_bottom_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header Row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(CyanPrimary.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Science,
                            contentDescription = "Bioscience",
                            tint = CyanPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "Bioscience Intelligence",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "${structure.id} • ${structure.source.label}",
                            fontSize = 12.sp,
                            color = CyanPrimary
                        )
                    }
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("close_sheet_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // TTS Audio Narration Card
            Card(
                colors = CardDefaults.cardColors(containerColor = BioSurfaceElevated),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, CyanPrimary.copy(alpha = 0.35f), RoundedCornerShape(16.dp))
                    .padding(vertical = 4.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.RecordVoiceOver,
                                contentDescription = null,
                                tint = MagentaSecondary
                            )
                            Text(
                                text = "Gemini-3.8-Flash-TTS Narration",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Color.White
                            )
                        }

                        // Model Tag Badge
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(MagentaSecondary.copy(alpha = 0.15f))
                                .border(1.dp, MagentaSecondary.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "gemini-3.8-flash-tts",
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                color = MagentaSecondary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Listen to an expert scientific audio breakdown detailing the 3D folding domains, quaternary assembly, and disease mechanisms.",
                        fontSize = 12.sp,
                        color = Color.LightGray
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Voice selector chips
                    Text(
                        text = "Select Narrator Voice:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = CyanPrimary
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                    ) {
                        GEMINI_VOICES.forEach { voice ->
                            FilterChip(
                                selected = selectedVoice == voice,
                                onClick = { selectedVoice = voice },
                                label = { Text(voice, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = CyanPrimary.copy(alpha = 0.25f),
                                    selectedLabelColor = CyanPrimary,
                                    containerColor = BioSurface,
                                    labelColor = Color.Gray
                                ),
                                modifier = Modifier.testTag("voice_chip_$voice")
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Audio Status & Visualizer
                    if (ttsState.isPlaying) {
                        AnimatedSoundVisualizer()
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    if (ttsState.statusMessage.isNotEmpty()) {
                        Text(
                            text = ttsState.statusMessage,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            color = if (ttsState.isPlaying) CyanPrimary else Color.LightGray
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    // Play/Stop Action Buttons
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Button(
                            onClick = {
                                if (ttsState.isPlaying) {
                                    onStopClicked()
                                } else {
                                    onSpeakClicked(insight.narrationScript, selectedVoice)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (ttsState.isPlaying) MagentaSecondary else CyanPrimary,
                                contentColor = Color.Black
                            ),
                            shape = RoundedCornerShape(10.dp),
                            enabled = !ttsState.isGenerating,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("tts_play_button")
                        ) {
                            if (ttsState.isGenerating) {
                                CircularProgressIndicator(
                                    color = Color.Black,
                                    modifier = Modifier.size(18.dp),
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Generating Speech...", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            } else if (ttsState.isPlaying) {
                                Icon(Icons.Default.Stop, contentDescription = null)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Stop Narration", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            } else {
                                Icon(Icons.Default.PlayArrow, contentDescription = null)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Play Audio Narration", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Science Content Cards
            SectionCard(
                title = "Macromolecular Summary",
                body = insight.executiveSummary,
                badge = "${structure.totalResidues} residues • ${structure.totalAtoms} atoms"
            )

            SectionCard(
                title = "Folding & Secondary Structure Topology",
                body = insight.foldingArchitecture,
                badge = "${structure.helixCount} Helices • ${structure.sheetCount} Sheets"
            )

            SectionCard(
                title = "AlphaFold pLDDT Confidence Evaluation",
                body = insight.alphaFoldConfidenceAnalysis,
                badge = if (structure.source == StructureSource.ALPHAFOLD) "Predicted Model" else "Experimental"
            )

            SectionCard(
                title = "Biochemical Function & Mechanism",
                body = insight.biologicalMechanism,
                badge = structure.organism
            )

            SectionCard(
                title = "Clinical & Pathological Significance",
                body = insight.medicalSignificance,
                badge = "Biomedical Impact"
            )

            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}

@Composable
private fun SectionCard(
    title: String,
    body: String,
    badge: String
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = BioSurfaceElevated),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .border(1.dp, BioSurfaceVariant, RoundedCornerShape(12.dp))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = CyanPrimary
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(BioSurfaceVariant)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = badge,
                        fontSize = 10.sp,
                        color = Color.LightGray
                    )
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = body,
                fontSize = 13.sp,
                lineHeight = 19.sp,
                color = Color(0xFFE2E8F0)
            )
        }
    }
}

@Composable
private fun AnimatedSoundVisualizer() {
    val infiniteTransition = rememberInfiniteTransition(label = "equalizer")
    val h1 by infiniteTransition.animateFloat(
        initialValue = 6f, targetValue = 24f,
        animationSpec = infiniteRepeatable(tween(400, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "h1"
    )
    val h2 by infiniteTransition.animateFloat(
        initialValue = 22f, targetValue = 8f,
        animationSpec = infiniteRepeatable(tween(550, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "h2"
    )
    val h3 by infiniteTransition.animateFloat(
        initialValue = 10f, targetValue = 28f,
        animationSpec = infiniteRepeatable(tween(350, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "h3"
    )
    val h4 by infiniteTransition.animateFloat(
        initialValue = 26f, targetValue = 10f,
        animationSpec = infiniteRepeatable(tween(450, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "h4"
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(30.dp)
            .background(BioDarkBg, RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp)
    ) {
        Icon(Icons.Default.GraphicEq, contentDescription = null, tint = CyanPrimary, modifier = Modifier.size(16.dp))
        Text("Audio Waveform:", fontSize = 11.sp, color = CyanPrimary)
        Spacer(modifier = Modifier.width(8.dp))

        listOf(h1, h2, h3, h4, h2, h1, h3).forEach { h ->
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(h.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(MagentaSecondary)
            )
        }
    }
}
