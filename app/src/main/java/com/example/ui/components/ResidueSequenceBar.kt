package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.example.model.Residue
import com.example.model.SecondaryStructure
import com.example.ui.theme.*

@Composable
fun ResidueSequenceBar(
    chains: Map<String, List<Residue>>,
    selectedResidue: Residue?,
    onResidueClicked: (Residue) -> Unit,
    modifier: Modifier = Modifier
) {
    val chainList = chains.keys.toList()
    var selectedChainIndex by remember { mutableIntStateOf(0) }

    val activeChainId = chainList.getOrNull(selectedChainIndex) ?: "A"
    val activeResidues = chains[activeChainId] ?: emptyList()

    val listState = rememberLazyListState()

    // Scroll to selected residue when changed
    LaunchedEffect(selectedResidue) {
        if (selectedResidue != null) {
            val idx = activeResidues.indexOfFirst { it.resSeq == selectedResidue.resSeq }
            if (idx >= 0) {
                listState.animateScrollToItem(maxOf(0, idx - 4))
            }
        }
    }

    Column(
        modifier = modifier
            .testTag("residue_sequence_bar")
            .fillMaxWidth()
            .background(BioSurface.copy(alpha = 0.95f))
            .border(width = 1.dp, color = BioSurfaceVariant)
            .padding(vertical = 4.dp)
    ) {
        // Chain tabs if more than 1 chain
        if (chainList.size > 1) {
            ScrollableTabRow(
                selectedTabIndex = selectedChainIndex,
                edgePadding = 8.dp,
                containerColor = Color.Transparent,
                contentColor = CyanPrimary,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedChainIndex]),
                        color = CyanPrimary
                    )
                },
                modifier = Modifier.fillMaxWidth().height(32.dp)
            ) {
                chainList.forEachIndexed { index, chainId ->
                    Tab(
                        selected = selectedChainIndex == index,
                        onClick = { selectedChainIndex = index },
                        text = {
                            Text(
                                "Chain $chainId (${chains[chainId]?.size ?: 0} aa)",
                                fontSize = 11.sp,
                                fontWeight = if (selectedChainIndex == index) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedChainIndex == index) CyanPrimary else Color.LightGray
                            )
                        }
                    )
                }
            }
        }

        // Sequence badge list
        LazyRow(
            state = listState,
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(activeResidues, key = { "${it.chainID}_${it.resSeq}" }) { res ->
                val isSelected = selectedResidue?.chainID == res.chainID && selectedResidue.resSeq == res.resSeq
                ResidueItemBadge(
                    residue = res,
                    isSelected = isSelected,
                    onClick = { onResidueClicked(res) }
                )
            }
        }
    }
}

@Composable
private fun ResidueItemBadge(
    residue: Residue,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val secColor = when (residue.secondaryStructure) {
        SecondaryStructure.HELIX -> ColorHelix
        SecondaryStructure.SHEET -> ColorSheet
        SecondaryStructure.LOOP -> ColorLoop
    }

    val bgColor = if (isSelected) CyanPrimary.copy(alpha = 0.25f) else BioSurfaceElevated

    Box(
        modifier = Modifier
            .testTag("residue_badge_${residue.resSeq}")
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor)
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) CyanPrimary else secColor.copy(alpha = 0.4f),
                shape = RoundedCornerShape(6.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 7.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(secColor)
                )
                Text(
                    text = "${residue.oneLetterCode}",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = if (isSelected) CyanPrimary else Color.White
                )
            }
            Text(
                text = "${residue.resSeq}",
                fontSize = 9.sp,
                fontFamily = FontFamily.Monospace,
                color = Color.LightGray
            )
        }
    }
}
