package com.example.parser

import com.example.model.*
import kotlin.math.max
import kotlin.math.sqrt

object PdbParser {

    private data class HelixRange(val chain: String, val startSeq: Int, val endSeq: Int)
    private data class SheetRange(val chain: String, val startSeq: Int, val endSeq: Int)

    fun parse(pdbContent: String, defaultId: String = "PROTEIN", source: StructureSource = StructureSource.PDB): ProteinStructure {
        var id = defaultId
        var title = ""
        var organism = "Homo sapiens"
        var resolution = ""
        var depositionDate = ""

        val helixRanges = mutableListOf<HelixRange>()
        val sheetRanges = mutableListOf<SheetRange>()
        val atoms = mutableListOf<Atom>()

        val lines = pdbContent.lineSequence()

        for (line in lines) {
            val length = line.length
            if (length < 6) continue
            val recordName = line.substring(0, minOf(6, length)).trim()

            when (recordName) {
                "HEADER" -> {
                    if (length >= 66) {
                        val possibleId = line.substring(62, minOf(66, length)).trim()
                        if (possibleId.isNotEmpty() && id == defaultId) id = possibleId
                    }
                    if (length >= 50) {
                        val possibleDate = line.substring(50, minOf(59, length)).trim()
                        if (possibleDate.isNotEmpty()) depositionDate = possibleDate
                    }
                }
                "TITLE" -> {
                    val titlePart = if (length > 10) line.substring(10).trim() else ""
                    title = if (title.isEmpty()) titlePart else "$title $titlePart"
                }
                "EXPDTA" -> {
                    if (length > 10 && resolution.isEmpty()) {
                        resolution = line.substring(10).trim()
                    }
                }
                "REMARK" -> {
                    if (line.contains("RESOLUTION.", ignoreCase = true) && resolution.isEmpty()) {
                        val match = Regex("RESOLUTION\\.\\s*([0-9.]+)\\s*ANGSTROMS", RegexOption.IGNORE_CASE).find(line)
                        if (match != null) {
                            resolution = "${match.groupValues[1]} Å"
                        }
                    }
                }
                "HELIX" -> {
                    try {
                        if (length >= 38) {
                            val chain = line.substring(19, 20).trim()
                            val startSeq = line.substring(21, 25).trim().toIntOrNull() ?: 0
                            val endSeq = line.substring(33, 38).trim().toIntOrNull() ?: 0
                            if (chain.isNotEmpty() && endSeq >= startSeq) {
                                helixRanges.add(HelixRange(chain, startSeq, endSeq))
                            }
                        }
                    } catch (_: Exception) {}
                }
                "SHEET" -> {
                    try {
                        if (length >= 38) {
                            val chain = line.substring(21, 22).trim()
                            val startSeq = line.substring(22, 26).trim().toIntOrNull() ?: 0
                            val endSeq = line.substring(33, 37).trim().toIntOrNull() ?: 0
                            if (chain.isNotEmpty() && endSeq >= startSeq) {
                                sheetRanges.add(SheetRange(chain, startSeq, endSeq))
                            }
                        }
                    } catch (_: Exception) {}
                }
                "ATOM", "HETATM" -> {
                    val isHet = recordName == "HETATM"
                    if (length >= 54) {
                        try {
                            val serial = line.substring(6, minOf(11, length)).trim().toIntOrNull() ?: (atoms.size + 1)
                            val name = line.substring(12, minOf(16, length)).trim()
                            val altLoc = if (length > 16) line[16] else ' '
                            val resName = if (length >= 20) line.substring(17, 20).trim() else "UNK"
                            val chainID = if (length >= 22) line.substring(21, 22).trim().ifEmpty { "A" } else "A"
                            val resSeq = if (length >= 26) line.substring(22, 26).trim().toIntOrNull() ?: 1 else 1

                            val x = line.substring(30, minOf(38, length)).trim().toFloatOrNull() ?: 0f
                            val y = line.substring(38, minOf(46, length)).trim().toFloatOrNull() ?: 0f
                            val z = line.substring(46, minOf(54, length)).trim().toFloatOrNull() ?: 0f

                            val occupancy = if (length >= 60) line.substring(54, minOf(60, length)).trim().toFloatOrNull() ?: 1.0f else 1.0f
                            val tempFactor = if (length >= 66) line.substring(60, minOf(66, length)).trim().toFloatOrNull() ?: 0.0f else 0.0f
                            val element = if (length >= 78) {
                                line.substring(76, minOf(78, length)).trim().ifEmpty {
                                    inferElement(name)
                                }
                            } else {
                                inferElement(name)
                            }

                            // Keep standard altLoc or first alternate
                            if (altLoc == ' ' || altLoc == 'A' || altLoc == '1') {
                                atoms.add(
                                    Atom(
                                        serial = serial,
                                        name = name,
                                        altLoc = altLoc,
                                        resName = resName,
                                        chainID = chainID,
                                        resSeq = resSeq,
                                        position = Vector3D(x, y, z),
                                        occupancy = occupancy,
                                        tempFactor = tempFactor,
                                        element = element,
                                        isHetero = isHet
                                    )
                                )
                            }
                        } catch (_: Exception) {}
                    }
                }
            }
        }

        if (title.isEmpty()) {
            title = if (source == StructureSource.ALPHAFOLD) "AlphaFold Prediction: $id" else "Structure of $id"
        }

        // Group into residues by chainID and resSeq
        val groupedByChain = atoms.groupBy { it.chainID }
        val chainsMap = mutableMapOf<String, List<Residue>>()

        var totalHelix = 0
        var totalSheet = 0
        var totalLoop = 0

        for ((chain, chainAtoms) in groupedByChain) {
            val residueList = mutableListOf<Residue>()
            val resGroups = chainAtoms.groupBy { it.resSeq }
            val sortedResSeqs = resGroups.keys.sorted()

            for (resSeq in sortedResSeqs) {
                val resAtoms = resGroups[resSeq] ?: emptyList()
                val resName = resAtoms.firstOrNull()?.resName ?: "UNK"

                var sec = SecondaryStructure.LOOP
                // Check explicit HELIX records
                if (helixRanges.any { it.chain == chain && resSeq >= it.startSeq && resSeq <= it.endSeq }) {
                    sec = SecondaryStructure.HELIX
                } else if (sheetRanges.any { it.chain == chain && resSeq >= it.startSeq && resSeq <= it.endSeq }) {
                    sec = SecondaryStructure.SHEET
                }

                residueList.add(Residue(resSeq, resName, chain, resAtoms, sec))
            }

            // Fallback secondary structure prediction if no HELIX or SHEET defined in header
            if (helixRanges.isEmpty() && sheetRanges.isEmpty() && residueList.size > 4) {
                computeSecondaryStructureHeuristic(residueList)
            }

            for (res in residueList) {
                when (res.secondaryStructure) {
                    SecondaryStructure.HELIX -> totalHelix++
                    SecondaryStructure.SHEET -> totalSheet++
                    SecondaryStructure.LOOP -> totalLoop++
                }
            }

            chainsMap[chain] = residueList
        }

        // Calculate center of mass and radius
        val center: Vector3D
        var maxRadius = 10f

        if (atoms.isNotEmpty()) {
            var sumX = 0.0
            var sumY = 0.0
            var sumZ = 0.0
            for (a in atoms) {
                sumX += a.position.x
                sumY += a.position.y
                sumZ += a.position.z
            }
            val count = atoms.size.toDouble()
            center = Vector3D((sumX / count).toFloat(), (sumY / count).toFloat(), (sumZ / count).toFloat())

            var maxDistSq = 0f
            for (a in atoms) {
                val dx = a.position.x - center.x
                val dy = a.position.y - center.y
                val dz = a.position.z - center.z
                val distSq = dx * dx + dy * dy + dz * dz
                if (distSq > maxDistSq) maxDistSq = distSq
            }
            maxRadius = max(sqrt(maxDistSq), 5f)
        } else {
            center = Vector3D(0f, 0f, 0f)
        }

        return ProteinStructure(
            id = id,
            title = title,
            source = source,
            organism = organism,
            resolution = resolution.ifEmpty { if (source == StructureSource.ALPHAFOLD) "Predicted (AlphaFold v4)" else "1.8 Å" },
            depositionDate = depositionDate,
            chains = chainsMap,
            allAtoms = atoms,
            center = center,
            radius = maxRadius,
            helixCount = totalHelix,
            sheetCount = totalSheet,
            loopCount = totalLoop,
            rawPdbContent = pdbContent
        )
    }

    private fun inferElement(atomName: String): String {
        val trimmed = atomName.trim().uppercase()
        return when {
            trimmed.startsWith("CL") -> "CL"
            trimmed.startsWith("FE") -> "FE"
            trimmed.startsWith("ZN") -> "ZN"
            trimmed.startsWith("MG") -> "MG"
            trimmed.startsWith("CA") && trimmed.length > 2 -> "CA"
            trimmed.startsWith("C") -> "C"
            trimmed.startsWith("N") -> "N"
            trimmed.startsWith("O") -> "O"
            trimmed.startsWith("S") -> "S"
            trimmed.startsWith("P") -> "P"
            trimmed.startsWith("H") -> "H"
            else -> "C"
        }
    }

    private fun computeSecondaryStructureHeuristic(residues: List<Residue>) {
        val n = residues.size
        for (i in 0 until n) {
            val caCurrent = residues[i].caAtom?.position ?: continue

            // Check helix: C_alpha(i) to C_alpha(i+3) and C_alpha(i+4)
            val hasHelixAhead = (i + 3 < n && (residues[i + 3].caAtom?.position?.distanceTo(caCurrent) ?: 99f) in 4.8f..5.8f) ||
                    (i + 4 < n && (residues[i + 4].caAtom?.position?.distanceTo(caCurrent) ?: 99f) in 5.8f..6.8f)

            // Check sheet: C_alpha(i) to C_alpha(i+2) extended conformation ~ 6.5 - 7.3 Å
            val hasSheetSpan = i + 2 < n && (residues[i + 2].caAtom?.position?.distanceTo(caCurrent) ?: 0f) in 6.4f..7.4f

            when {
                hasHelixAhead -> residues[i].secondaryStructure = SecondaryStructure.HELIX
                hasSheetSpan -> residues[i].secondaryStructure = SecondaryStructure.SHEET
                else -> residues[i].secondaryStructure = SecondaryStructure.LOOP
            }
        }
    }
}
