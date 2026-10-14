package com.example.model

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.*
import kotlin.math.sqrt

data class Vector3D(
    val x: Float,
    val y: Float,
    val z: Float
) {
    operator fun plus(other: Vector3D) = Vector3D(x + other.x, y + other.y, z + other.z)
    operator fun minus(other: Vector3D) = Vector3D(x - other.x, y - other.y, z - other.z)
    operator fun times(scalar: Float) = Vector3D(x * scalar, y * scalar, z * scalar)
    operator fun div(scalar: Float) = Vector3D(x / scalar, y / scalar, z / scalar)

    fun length(): Float = sqrt(x * x + y * y + z * z)

    fun normalize(): Vector3D {
        val len = length()
        return if (len > 0.00001f) this / len else Vector3D(0f, 0f, 0f)
    }

    fun dot(other: Vector3D): Float = x * other.x + y * other.y + z * other.z

    fun cross(other: Vector3D): Vector3D = Vector3D(
        x = y * other.z - z * other.y,
        y = z * other.x - x * other.z,
        z = x * other.y - y * other.x
    )

    fun distanceTo(other: Vector3D): Float {
        val dx = x - other.x
        val dy = y - other.y
        val dz = z - other.z
        return sqrt(dx * dx + dy * dy + dz * dz)
    }
}

enum class SecondaryStructure(val displayName: String) {
    HELIX("Alpha Helix (α)"),
    SHEET("Beta Sheet (β)"),
    LOOP("Loop / Coil")
}

data class Atom(
    val serial: Int,
    val name: String,
    val altLoc: Char = ' ',
    val resName: String,
    val chainID: String,
    val resSeq: Int,
    val position: Vector3D,
    val occupancy: Float = 1.0f,
    val tempFactor: Float = 0.0f, // B-factor or AlphaFold pLDDT (0-100)
    val element: String = "C",
    val isHetero: Boolean = false // HETATM vs ATOM
) {
    val isAlphaCarbon: Boolean get() = name == "CA"
    val isBackbone: Boolean get() = name in setOf("N", "CA", "C", "O")

    val vdwRadius: Float
        get() = when (element.uppercase()) {
            "H" -> 1.20f
            "C" -> 1.70f
            "N" -> 1.55f
            "O" -> 1.52f
            "S" -> 1.80f
            "P" -> 1.80f
            "FE" -> 1.80f
            "ZN" -> 1.39f
            else -> 1.60f
        }
}

data class Residue(
    val resSeq: Int,
    val resName: String,
    val chainID: String,
    val atoms: List<Atom>,
    var secondaryStructure: SecondaryStructure = SecondaryStructure.LOOP
) {
    val caAtom: Atom? get() = atoms.firstOrNull { it.isAlphaCarbon } ?: atoms.firstOrNull()

    val meanPLDDT: Float
        get() = if (atoms.isNotEmpty()) {
            atoms.map { it.tempFactor }.average().toFloat()
        } else 0f

    val oneLetterCode: Char
        get() = when (resName.uppercase()) {
            "ALA" -> 'A'
            "ARG" -> 'R'
            "ASN" -> 'N'
            "ASP" -> 'D'
            "CYS" -> 'C'
            "GLN" -> 'Q'
            "GLU" -> 'E'
            "GLY" -> 'G'
            "HIS" -> 'H'
            "ILE" -> 'I'
            "LEU" -> 'L'
            "LYS" -> 'K'
            "MET" -> 'M'
            "PHE" -> 'F'
            "PRO" -> 'P'
            "SER" -> 'S'
            "THR" -> 'T'
            "TRP" -> 'W'
            "TYR" -> 'Y'
            "VAL" -> 'V'
            else -> 'X'
        }

    val fullName: String
        get() = when (resName.uppercase()) {
            "ALA" -> "Alanine"
            "ARG" -> "Arginine"
            "ASN" -> "Asparagine"
            "ASP" -> "Aspartate"
            "CYS" -> "Cysteine"
            "GLN" -> "Glutamine"
            "GLU" -> "Glutamate"
            "GLY" -> "Glycine"
            "HIS" -> "Histidine"
            "ILE" -> "Isoleucine"
            "LEU" -> "Leucine"
            "LYS" -> "Lysine"
            "MET" -> "Methionine"
            "PHE" -> "Phenylalanine"
            "PRO" -> "Proline"
            "SER" -> "Serine"
            "THR" -> "Threonine"
            "TRP" -> "Tryptophan"
            "TYR" -> "Tyrosine"
            "VAL" -> "Valine"
            else -> resName
        }

    val polarityType: String
        get() = when (resName.uppercase()) {
            "ALA", "VAL", "LEU", "ILE", "MET", "PHE", "TRP", "PRO" -> "Hydrophobic"
            "SER", "THR", "CYS", "ASN", "GLN", "TYR", "GLY" -> "Polar Uncharged"
            "ASP", "GLU" -> "Acidic (-)"
            "LYS", "ARG", "HIS" -> "Basic (+)"
            else -> "Special"
        }
}

data class Bond(
    val atom1: Atom,
    val atom2: Atom
)

enum class StructureSource(val label: String) {
    PDB("RCSB PDB"),
    ALPHAFOLD("AlphaFold DB"),
    CUSTOM("Custom File")
}

data class ProteinStructure(
    val id: String,
    val title: String,
    val source: StructureSource,
    val organism: String = "Homo sapiens",
    val resolution: String = "",
    val depositionDate: String = "",
    val chains: Map<String, List<Residue>>,
    val allAtoms: List<Atom>,
    val center: Vector3D,
    val radius: Float,
    val helixCount: Int,
    val sheetCount: Int,
    val loopCount: Int,
    val rawPdbContent: String = ""
) {
    val totalResidues: Int get() = chains.values.sumOf { it.size }
    val totalAtoms: Int get() = allAtoms.size
    val chainIds: List<String> get() = chains.keys.toList()

    val meanPLDDT: Float
        get() = if (allAtoms.isNotEmpty()) {
            allAtoms.map { it.tempFactor }.average().toFloat()
        } else 0f

    val alphaCarbons: List<Atom>
        get() = allAtoms.filter { it.isAlphaCarbon }
}

enum class RenderStyle(val label: String, val iconDescription: String) {
    CARTOON("Ribbon / Cartoon", "Smooth secondary structure ribbon"),
    BACKBONE("Backbone Trace", "Continuous C-alpha peptide chain"),
    BALL_AND_STICK("Ball & Stick", "Covalent bonds and atom spheres"),
    SPACEFILL("Spacefill / VDW", "Full van der Waals spheres")
}

enum class MolecularColorScheme(val label: String, val description: String) {
    PLDDT_CONFIDENCE("AlphaFold pLDDT", "Color by AI prediction confidence (>90 Very High, <50 Low)"),
    SECONDARY_STRUCTURE("Secondary Structure", "Magenta = Alpha Helix, Cyan = Beta Sheet, Gray = Loop"),
    RAINBOW_N_TO_C("Rainbow N→C", "Gradient from Blue (N-terminus) to Red (C-terminus)"),
    RESIDUE_TYPE("Residue Chemistry", "Yellow = Hydrophobic, Green = Polar, Red = Acidic, Blue = Basic"),
    ELEMENT_CPK("Element CPK", "Gray = C, Blue = N, Red = O, Yellow = S"),
    B_FACTOR_THERMAL("B-Factor Thermal", "Cold Blue to Hot Red thermal flexibility"),
    CHAIN("Chain ID", "Distinct color for each polypeptide chain")
}

data class DistanceMeasurement(
    val atom1: Atom,
    val atom2: Atom,
    val distanceAngstroms: Float
)
