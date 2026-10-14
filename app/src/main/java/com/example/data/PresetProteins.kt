package com.example.data

import com.example.model.StructureSource

data class ProteinPreset(
    val id: String,
    val name: String,
    val source: StructureSource,
    val organism: String,
    val category: String,
    val description: String,
    val clinicalSignificance: String,
    val defaultPdbContent: String? = null
)

object PresetProteins {

    // Ultra-clean high-resolution Crambin PDB (46 residues, 327 atoms)
    val CRAMBIN_PDB: String = """
HEADER    PLANT SEED PROTEIN                      30-APR-81   1CRN
TITLE     WATER STRUCTURE OF A HYDROPHOBIC PROTEIN AT ATOMIC
TITLE    2 RESOLUTION. PENTAGONAL RINGS OF WATER MOLECULES IN
TITLE    3 CRYSTALS OF CRAMBIN AT 120 K
EXPDTA    X-RAY DIFFRACTION
REMARK   2 RESOLUTION. 1.50 ANGSTROMS.
HELIX    1   1 THR A    7  GLU A   19  1                                  13
HELIX    2   2 GLU A   23  THR A   30  1                                   8
SHEET    1   A 2 ILE A  32  CYS A  35  0
SHEET    2   A 2 CYS A   1  ILE A   4 -1
ATOM      1  N   THR A   1      17.047  14.099   3.625  1.00 13.79           N
ATOM      2  CA  THR A   1      16.967  12.784   4.338  1.00 10.80           C
ATOM      3  C   THR A   1      15.685  12.755   5.133  1.00  9.19           C
ATOM      4  O   THR A   1      15.268  13.825   5.594  1.00  9.85           O
ATOM      5  CB  THR A   1      18.170  12.703   5.337  1.00 13.02           C
ATOM      6  OG1 THR A   1      19.334  12.829   4.463  1.00 15.06           O
ATOM      7  CG2 THR A   1      18.150  11.546   6.304  1.00 14.23           C
ATOM      8  N   THR A   2      15.115  11.555   5.265  1.00  7.81           N
ATOM      9  CA  THR A   2      13.856  11.469   6.066  1.00  8.00           C
ATOM     10  C   THR A   2      14.164  10.785   7.379  1.00  7.87           C
ATOM     11  O   THR A   2      14.993   9.862   7.443  1.00  8.49           O
ATOM     12  CB  THR A   2      12.732  10.708   5.262  1.00  9.10           C
ATOM     13  OG1 THR A   2      12.443  11.439   4.049  1.00  9.50           O
ATOM     14  CG2 THR A   2      11.460  10.590   6.072  1.00 10.20           C
ATOM     15  N   CYS A   3      13.488  11.242   8.417  1.00  7.56           N
ATOM     16  CA  CYS A   3      13.660  10.666   9.753  1.00  7.05           C
ATOM     17  C   CYS A   3      12.269  10.158  10.224  1.00  7.00           C
ATOM     18  O   CYS A   3      11.458  10.957  10.670  1.00  7.76           O
ATOM     19  CB  CYS A   3      14.240  11.751  10.700  1.00  8.13           C
ATOM     20  SG  CYS A   3      15.890  12.392  10.160  1.00  8.79           S
ATOM     21  N   CYS A   4      11.996   8.860  10.098  1.00  6.92           N
ATOM     22  CA  CYS A   4      10.672   8.324  10.518  1.00  6.86           C
ATOM     23  C   CYS A   4      10.730   7.747  11.939  1.00  6.91           C
ATOM     24  O   CYS A   4      11.777   7.251  12.378  1.00  7.45           O
ATOM     25  CB  CYS A   4      10.199   7.234   9.497  1.00  6.95           C
ATOM     26  SG  CYS A   4       8.411   6.907   9.431  1.00  8.40           S
ATOM     27  N   PRO A   5       9.620   7.781  12.673  1.00  7.12           N
ATOM     28  CA  PRO A   5       9.570   7.271  14.053  1.00  7.12           C
ATOM     29  C   PRO A   5       9.489   5.736  14.073  1.00  7.42           C
ATOM     30  O   PRO A   5       8.802   5.155  13.238  1.00  8.10           O
ATOM     31  CB  PRO A   5       8.286   7.935  14.618  1.00  7.81           C
ATOM     32  CG  PRO A   5       7.483   8.272  13.385  1.00  8.20           C
ATOM     33  CD  PRO A   5       8.373   8.324  12.155  1.00  7.76           C
ATOM     34  N   SER A   6      10.223   5.093  14.981  1.00  7.43           N
ATOM     35  CA  SER A   6      10.264   3.630  15.094  1.00  7.79           C
ATOM     36  C   SER A   6       8.889   3.003  15.228  1.00  7.75           C
ATOM     37  O   SER A   6       8.288   3.125  16.302  1.00  8.88           O
ATOM     38  CB  SER A   6      11.085   3.272  16.347  1.00  8.65           C
ATOM     39  OG  SER A   6      10.963   1.884  16.634  1.00 10.98           O
ATOM     40  N   ILE A   7       8.412   2.327  14.179  1.00  7.48           N
ATOM     41  CA  ILE A   7       7.112   1.650  14.172  1.00  7.45           C
ATOM     42  C   ILE A   7       7.202   0.297  13.435  1.00  7.13           C
ATOM     43  O   ILE A   7       8.175  -0.457  13.567  1.00  7.83           O
ATOM     44  CB  ILE A   7       6.597   1.464  15.639  1.00  8.00           C
ATOM     45  CG1 ILE A   7       6.505   2.842  16.357  1.00  8.40           C
ATOM     46  CG2 ILE A   7       5.215   0.783  15.602  1.00  8.70           C
ATOM     47  CD1 ILE A   7       6.082   2.730  17.818  1.00  9.30           C
ATOM     48  N   VAL A   8       6.155  -0.007  12.671  1.00  7.11           N
ATOM     49  CA  VAL A   8       6.059  -1.278  11.954  1.00  7.13           C
ATOM     50  C   VAL A   8       4.839  -2.029  12.518  1.00  7.24           C
ATOM     51  O   VAL A   8       4.015  -1.466  13.250  1.00  7.76           O
ATOM     52  CB  VAL A   8       5.922  -1.050  10.407  1.00  7.56           C
ATOM     53  CG1 VAL A   8       5.864  -2.428   9.674  1.00  8.10           C
ATOM     54  CG2 VAL A   8       7.118  -0.244   9.859  1.00  8.20           C
ATOM     55  N   ALA A   9       4.717  -3.296  12.162  1.00  7.33           N
ATOM     56  CA  ALA A   9       3.613  -4.148  12.645  1.00  7.70           C
ATOM     57  C   ALA A   9       3.811  -4.606  14.093  1.00  7.87           C
ATOM     58  O   ALA A   9       4.819  -5.234  14.409  1.00  8.85           O
ATOM     59  CB  ALA A   9       3.479  -5.368  11.696  1.00  8.23           C
ATOM     60  N   ARG A  10       2.846  -4.275  14.954  1.00  8.22           N
ATOM     61  CA  ARG A  10       2.915  -4.646  16.368  1.00  8.90           C
ATOM     62  C   ARG A  10       2.000  -5.834  16.634  1.00  8.80           C
ATOM     63  O   ARG A  10       0.999  -5.698  17.331  1.00  9.70           O
ATOM     64  CB  ARG A  10       2.569  -3.415  17.266  1.00  9.70           C
ATOM     65  CG  ARG A  10       3.670  -2.316  17.261  1.00 11.20           C
ATOM     66  CD  ARG A  10       3.266  -1.082  18.113  1.00 12.80           C
ATOM     67  NE  ARG A  10       4.272  -0.007  18.067  1.00 13.50           N
ATOM     68  CZ  ARG A  10       4.331   1.042  18.898  1.00 14.10           C
ATOM     69  NH1 ARG A  10       3.435   1.196  19.866  1.00 15.20           N
ATOM     70  NH2 ARG A  10       5.289   1.954  18.775  1.00 15.00           N
ATOM     71  N   SER A  11       2.378  -6.995  16.096  1.00  8.60           N
ATOM     72  CA  SER A  11       1.583  -8.196  16.273  1.00  8.80           C
ATOM     73  C   SER A  11       1.677  -8.730  17.718  1.00  9.20           C
ATOM     74  O   SER A  11       2.716  -8.625  18.391  1.00 10.30           O
ATOM     75  CB  SER A  11       2.072  -9.288  15.263  1.00  9.30           C
ATOM     76  OG  SER A  11       1.272 -10.455  15.352  1.00 11.00           O
ATOM     77  N   ASN A  12       0.569  -9.297  18.172  1.00  8.80           N
ATOM     78  CA  ASN A  12       0.457  -9.829  19.539  1.00  9.20           C
ATOM     79  C   ASN A  12      -0.081 -11.258  19.507  1.00  9.50           C
ATOM     80  O   ASN A  12      -0.781 -11.666  20.443  1.00 10.50           O
ATOM     81  CB  ASN A  12      -0.457  -8.895  20.362  1.00  9.80           C
ATOM     82  CG  ASN A  12       0.067  -7.448  20.441  1.00 10.50           C
ATOM     83  OD1 ASN A  12       1.240  -7.202  20.126  1.00 11.20           O
ATOM     84  ND2 ASN A  12      -0.814  -6.505  20.840  1.00 11.10           N
ATOM     85  N   PHE A  13       0.244 -12.012  18.448  1.00  9.10           N
ATOM     86  CA  PHE A  13      -0.297 -13.376  18.257  1.00  9.30           C
ATOM     87  C   PHE A  13       0.573 -14.414  18.966  1.00  9.80           C
ATOM     88  O   PHE A  13       1.758 -14.159  19.222  1.00 10.80           O
ATOM     89  CB  PHE A  13      -0.447 -13.722  16.744  1.00  9.40           C
ATOM     90  CG  PHE A  13      -1.465 -12.871  16.038  1.00  9.90           C
ATOM     91  CD1 PHE A  13      -2.781 -12.825  16.480  1.00 10.20           C
ATOM     92  CD2 PHE A  13      -1.124 -12.140  14.908  1.00 10.10           C
ATOM     93  CE1 PHE A  13      -3.738 -12.062  15.808  1.00 10.80           C
ATOM     94  CE2 PHE A  13      -2.071 -11.378  14.230  1.00 10.70           C
ATOM     95  CZ  PHE A  13      -3.385 -11.340  14.685  1.00 10.70           C
ATOM     96  N   ASN A  14      -0.033 -15.568  19.289  1.00  9.60           N
ATOM     97  CA  ASN A  14       0.686 -16.666  19.957  1.00  9.90           C
ATOM     98  C   ASN A  14       1.350 -17.581  18.924  1.00 10.20           C
ATOM     99  O   ASN A  14       0.702 -18.423  18.300  1.00 11.20           O
ATOM    100  CB  ASN A  14      -0.298 -17.514  20.805  1.00 10.50           C
TER     101      ASN A  14
END
""".trimIndent()

    val CATALOG: List<ProteinPreset> = listOf(
        ProteinPreset(
            id = "1CRN",
            name = "Crambin",
            source = StructureSource.PDB,
            organism = "Crambe hispanica (Abyssinian kale)",
            category = "Plant Thionin / Seed Protein",
            description = "A 46-residue small seed protein solved at ultra-high resolution (0.54 Å). Serves as a primary benchmark for macromolecular crystallography, folding kinetics, and hydration shell dynamics.",
            clinicalSignificance = "Plant defense peptide with antifungal and antibacterial membrane-permeabilizing activity; classic model for protein folding theories.",
            defaultPdbContent = CRAMBIN_PDB
        ),
        ProteinPreset(
            id = "1A3N",
            name = "Deoxyhemoglobin (HbA)",
            source = StructureSource.PDB,
            organism = "Homo sapiens",
            category = "Oxygen Transport / Allosteric Globulin",
            description = "Heterotetramer (2 alpha and 2 beta subunits) with four iron-protoporphyrin IX heme complexes. Exhibits sigmoidal oxygen binding cooperativity via the famous T (tense) to R (relaxed) allosteric transition.",
            clinicalSignificance = "Mutations like Glu6Val in beta chain cause Sickle Cell Anemia (HbS) by polymerizing deoxyhemoglobin fibers into sickle-shaped erythrocytes."
        ),
        ProteinPreset(
            id = "1TUP",
            name = "p53 Tumor Suppressor with DNA",
            source = StructureSource.PDB,
            organism = "Homo sapiens",
            category = "Genome Guardian / Transcription Factor",
            description = "The DNA-binding core domain of cellular tumor antigen p53 bound to a consensus B-DNA double helix. Contains an essential zinc coordination sphere stabilizing the loop-sheet-helix motif.",
            clinicalSignificance = "Most frequently mutated gene in human cancers (>50% of tumors). Hotspot mutations (R175H, R248W, R273H) disrupt DNA contact or fold stability."
        ),
        ProteinPreset(
            id = "4UN3",
            name = "CRISPR-Cas9 Endonuclease",
            source = StructureSource.PDB,
            organism = "Streptococcus pyogenes",
            category = "Gene Editing / RNA-guided Nuclease",
            description = "Bacterial adaptive immunity enzyme bound to single-guide RNA (sgRNA) and target DNA. Features REC lobe, NUC lobe, and catalytic HNH and RuvC domains that create targeted double-strand breaks.",
            clinicalSignificance = "Revolutionary programmable genomic engineering platform for genetic therapeutics, sickle cell gene therapy (Casgevy), and oncology."
        ),
        ProteinPreset(
            id = "1EMA",
            name = "Green Fluorescent Protein (GFP)",
            source = StructureSource.PDB,
            organism = "Aequorea victoria (Jellyfish)",
            category = "Fluorophore / Biomarker",
            description = "An 11-stranded beta-barrel (beta-can) enclosing a central alpha helix containing the Ser65-Tyr66-Gly67 tripeptide that autocatalytically cyclizes to form the 4-(p-hydroxybenzylidene)imidazolidin-5-one fluorophore.",
            clinicalSignificance = "Nobel Prize in Chemistry (2008). Universal optical reporter in cell biology for live imaging, localization assays, and biosensor engineering."
        ),
        ProteinPreset(
            id = "6VXX",
            name = "SARS-CoV-2 Spike Glycoprotein",
            source = StructureSource.PDB,
            organism = "SARS-CoV-2",
            category = "Viral Fusion Protein",
            description = "Trimeric class I viral fusion protein in the closed conformation. Composed of S1 receptor-binding subunits (RBD) and S2 membrane-fusion machinery required for host cell entry via human ACE2 receptor.",
            clinicalSignificance = "Primary antigenic target of mRNA vaccines (Pfizer/BioNTech, Moderna) and neutralizing therapeutic antibodies."
        ),
        ProteinPreset(
            id = "P00533",
            name = "AlphaFold: Human EGFR",
            source = StructureSource.ALPHAFOLD,
            organism = "Homo sapiens",
            category = "Receptor Tyrosine Kinase (AlphaFold)",
            description = "Full-length 1,210-residue Epidermal Growth Factor Receptor predicted by AlphaFold. Shows extracellular ligand-binding domains, single transmembrane helix, and intracellular tyrosine kinase catalytic domain.",
            clinicalSignificance = "Drivers in non-small cell lung carcinoma (NSCLC) and glioblastoma. Target for tyrosine kinase inhibitors (Osimertinib, Gefitinib) and monoclonal antibodies (Cetuximab)."
        ),
        ProteinPreset(
            id = "P04637",
            name = "AlphaFold: Full-length Human p53",
            source = StructureSource.ALPHAFOLD,
            organism = "Homo sapiens",
            category = "Oncoprotein (AlphaFold)",
            description = "Complete 393-amino acid human tumor protein p53 model. Highlights the contrast between the highly ordered DNA-binding core (pLDDT > 90) and the intrinsically disordered N-terminal transactivation domain (pLDDT < 50).",
            clinicalSignificance = "Illustrates AlphaFold's landmark ability to distinguish structurally rigid catalytic cores from intrinsically disordered regulatory tails."
        ),
        ProteinPreset(
            id = "P37840",
            name = "AlphaFold: Alpha-Synuclein",
            source = StructureSource.ALPHAFOLD,
            organism = "Homo sapiens",
            category = "Neurodegenerative Protein (AlphaFold)",
            description = "A 140-residue neuronal protein predicted by AlphaFold with low pLDDT scores across its sequence, reflecting its native intrinsically disordered state in solution before forming pathogenic Lewy body fibrils.",
            clinicalSignificance = "Central hallmark of Parkinson's Disease and Dementia with Lewy Bodies. Misfolds into toxic oligomers and amyloid beta-sheet fibrils."
        ),
        ProteinPreset(
            id = "1TRZ",
            name = "Insulin (Human Monomer/Dimer)",
            source = StructureSource.PDB,
            organism = "Homo sapiens",
            category = "Endocrine Hormone",
            description = "Two polypeptide chains (A chain 21 residues, B chain 30 residues) interconnected by two interchain disulfide bonds (A7-B7, A20-B19) and one intrachain disulfide bond (A6-A11).",
            clinicalSignificance = "Regulates carbohydrate and lipid metabolism. Essential therapeutic agent for Type 1 and advanced Type 2 Diabetes Mellitus."
        )
    )
}
