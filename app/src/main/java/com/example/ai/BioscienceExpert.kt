package com.example.ai

import com.example.data.PresetProteins
import com.example.model.ProteinStructure
import com.example.model.SecondaryStructure
import com.example.model.StructureSource

data class BioscienceInsight(
    val proteinId: String,
    val title: String,
    val executiveSummary: String,
    val foldingArchitecture: String,
    val alphaFoldConfidenceAnalysis: String,
    val biologicalMechanism: String,
    val medicalSignificance: String,
    val narrationScript: String
)

object BioscienceExpert {

    fun generateInsight(structure: ProteinStructure): BioscienceInsight {
        val preset = PresetProteins.CATALOG.firstOrNull { it.id.equals(structure.id, ignoreCase = true) }

        val totalResidues = maxOf(structure.totalResidues, 1)
        val helixPercent = (structure.helixCount * 100f / totalResidues).toInt()
        val sheetPercent = (structure.sheetCount * 100f / totalResidues).toInt()
        val loopPercent = (structure.loopCount * 100f / totalResidues).toInt()

        val avgPlddt = structure.meanPLDDT

        val foldingArch = buildString {
            append("Secondary Structure Composition: ")
            append("$helixPercent% Alpha-Helix, $sheetPercent% Beta-Sheet, $loopPercent% Loops & Turns across ")
            append("${structure.chains.size} polypeptide chain(s) containing $totalResidues residues and ${structure.totalAtoms} atoms. ")
            if (helixPercent > 40) {
                append("Dominated by right-handed alpha-helical bundles stabilized by i to i+4 backbone hydrogen bonds. ")
            } else if (sheetPercent > 35) {
                append("Rich in extended beta-strands forming pleated sheets stabilized by inter-strand backbone hydrogen bonds. ")
            } else {
                append("Possesses a mixed alpha/beta fold with functional loop transitions. ")
            }
        }

        val plddtAnalysis = buildString {
            if (structure.source == StructureSource.ALPHAFOLD || avgPlddt > 0f) {
                append("AlphaFold Confidence Score (mean pLDDT: ${"%.1f".format(avgPlddt)}/100): ")
                when {
                    avgPlddt >= 90f -> append("Very High confidence across the structural core. Backbone coordinates are reliable at atomistic crystallographic accuracy (~0.5-1.0 Å RMSD).")
                    avgPlddt >= 70f -> append("Confident backbone prediction. Well-defined secondary structure topology with minor side-chain conformational flexibility.")
                    avgPlddt >= 50f -> append("Low confidence overall. May contain large intrinsically disordered regions (IDRs) or mobile loops that only adopt structured conformations upon ligand binding.")
                    else -> append("Very Low confidence (< 50). Strong biophysical signature of an intrinsically disordered protein (IDP) lacking fixed tertiary equilibrium in isolation.")
                }
            } else {
                append("Experimental B-factor thermal mobility values reflect crystal packing rigidity and localized thermal vibration (mean: ${"%.1f".format(avgPlddt)} Å²).")
            }
        }

        val bioMech = if (preset != null) {
            preset.description
        } else {
            "Macromolecular coordinate model for ${structure.id}. Participates in cellular homeostasis, enzymatic catalysis, or structural scaffolding within ${structure.organism}."
        }

        val medSig = if (preset != null) {
            preset.clinicalSignificance
        } else {
            "Investigated as a molecular target for rational drug design, functional mutagenesis, and structural biology analysis."
        }

        val executiveSummary = buildString {
            append("${structure.title} (${structure.id}) from ${structure.organism}. ")
            append("Solvated molecular radius is approximately ${"%.1f".format(structure.radius)} Ångströms with ${structure.chains.size} distinct chain(s).")
        }

        val narration = buildString {
            append("Welcome to the structural analysis of ${structure.title}, cataloged as ${structure.id}. ")
            append("This macromolecule spans ${structure.chains.size} chain${if (structure.chains.size > 1) "s" else ""}, ")
            append("encompassing $totalResidues amino acid residues and ${structure.totalAtoms} atomic centers. ")
            append("The secondary structure comprises $helixPercent percent alpha helices, ")
            append("$sheetPercent percent beta pleated sheets, and $loopPercent percent functional loop coils. ")
            if (structure.source == StructureSource.ALPHAFOLD) {
                append("According to AlphaFold deep learning predictions, the average pLDDT confidence is ${avgPlddt.toInt()} out of one hundred, ")
                if (avgPlddt >= 80) {
                    append("confirming a highly ordered and structurally rigid catalytic architecture. ")
                } else {
                    append("highlighting regions of conformational flexibility characteristic of intrinsically disordered segments. ")
                }
            }
            if (preset != null) {
                append("Biochemically, ${preset.description} ")
                append("From a clinical perspective, ${preset.clinicalSignificance}")
            } else {
                append("This molecular structure provides key insights into spatial arrangement, ligand docking, and biochemical stability.")
            }
        }

        return BioscienceInsight(
            proteinId = structure.id,
            title = structure.title,
            executiveSummary = executiveSummary,
            foldingArchitecture = foldingArch,
            alphaFoldConfidenceAnalysis = plddtAnalysis,
            biologicalMechanism = bioMech,
            medicalSignificance = medSig,
            narrationScript = narration
        )
    }
}
