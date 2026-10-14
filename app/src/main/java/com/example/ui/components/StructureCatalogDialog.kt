package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Dataset
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import com.example.data.PresetProteins
import com.example.data.ProteinPreset
import com.example.model.StructureSource
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StructureCatalogBottomSheet(
    isLoading: Boolean,
    onSelectPreset: (ProteinPreset) -> Unit,
    onSearchPdb: (String) -> Unit,
    onSearchAlphaFold: (String) -> Unit,
    onLoadCustomText: (String) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var queryText by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("ALL") } // ALL, PDB, ALPHAFOLD

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = BioSurface,
        scrimColor = Color.Black.copy(alpha = 0.65f),
        modifier = modifier.testTag("structure_catalog_bottom_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
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
                            imageVector = Icons.Default.Dataset,
                            contentDescription = null,
                            tint = CyanPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "Protein Structure Catalog",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "RCSB PDB & AlphaFold DB",
                            fontSize = 12.sp,
                            color = CyanPrimary
                        )
                    }
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("close_catalog_button")
                ) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = Color.Gray)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Search Bar
            OutlinedTextField(
                value = queryText,
                onValueChange = { queryText = it },
                label = { Text("Enter 4-char PDB ID or UniProt ID (e.g. 1A3N, P00533)") },
                singleLine = true,
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null, tint = CyanPrimary)
                },
                trailingIcon = {
                    if (queryText.isNotEmpty()) {
                        IconButton(onClick = { queryText = "" }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear", tint = Color.Gray)
                        }
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = CyanPrimary,
                    unfocusedBorderColor = BioSurfaceVariant,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedLabelColor = CyanPrimary,
                    unfocusedLabelColor = Color.Gray
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("search_pdb_input")
            )

            // Direct Action Fetch Buttons
            if (queryText.isNotBlank()) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) {
                    Button(
                        onClick = { onSearchPdb(queryText.trim()) },
                        colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary, contentColor = Color.Black),
                        shape = RoundedCornerShape(8.dp),
                        enabled = !isLoading,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("fetch_pdb_btn")
                    ) {
                        Icon(Icons.Default.CloudDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Fetch PDB", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = { onSearchAlphaFold(queryText.trim()) },
                        colors = ButtonDefaults.buttonColors(containerColor = MagentaSecondary, contentColor = Color.White),
                        shape = RoundedCornerShape(8.dp),
                        enabled = !isLoading,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("fetch_alphafold_btn")
                    ) {
                        Icon(Icons.Default.Hub, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Fetch AlphaFold", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Filter Chips
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                listOf(
                    "ALL" to "All Structures",
                    "PDB" to "RCSB PDB",
                    "ALPHAFOLD" to "AlphaFold Models"
                ).forEach { (key, label) ->
                    FilterChip(
                        selected = selectedFilter == key,
                        onClick = { selectedFilter = key },
                        label = { Text(label, fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = CyanPrimary.copy(alpha = 0.2f),
                            selectedLabelColor = CyanPrimary,
                            containerColor = BioSurfaceVariant,
                            labelColor = Color.Gray
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = CyanPrimary)
                }
            } else {
                // Preset Catalog List
                val filteredList = PresetProteins.CATALOG.filter { preset ->
                    val matchesType = when (selectedFilter) {
                        "PDB" -> preset.source == StructureSource.PDB
                        "ALPHAFOLD" -> preset.source == StructureSource.ALPHAFOLD
                        else -> true
                    }
                    val matchesQuery = queryText.isEmpty() ||
                            preset.name.contains(queryText, ignoreCase = true) ||
                            preset.id.contains(queryText, ignoreCase = true) ||
                            preset.category.contains(queryText, ignoreCase = true)

                    matchesType && matchesQuery
                }

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(420.dp)
                ) {
                    items(filteredList, key = { it.id }) { preset ->
                        PresetCard(
                            preset = preset,
                            onClick = { onSelectPreset(preset) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PresetCard(
    preset: ProteinPreset,
    onClick: () -> Unit
) {
    val isAlphaFold = preset.source == StructureSource.ALPHAFOLD
    val badgeColor = if (isAlphaFold) MagentaSecondary else CyanPrimary

    Card(
        colors = CardDefaults.cardColors(containerColor = BioSurfaceElevated),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, BioSurfaceVariant, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .testTag("preset_card_${preset.id}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
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
                            .clip(RoundedCornerShape(6.dp))
                            .background(badgeColor.copy(alpha = 0.2f))
                            .border(1.dp, badgeColor.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = preset.id,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = badgeColor
                        )
                    }
                    Text(
                        text = preset.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color.White
                    )
                }

                Text(
                    text = preset.source.label,
                    fontSize = 10.sp,
                    color = Color.LightGray
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "${preset.category} • ${preset.organism}",
                fontSize = 11.sp,
                color = CyanPrimary
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = preset.description,
                fontSize = 12.sp,
                maxLines = 2,
                color = Color(0xFFCBD5E1)
            )
        }
    }
}
