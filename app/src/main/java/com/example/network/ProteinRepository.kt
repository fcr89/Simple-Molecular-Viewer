package com.example.network

import com.example.model.ProteinStructure
import com.example.model.StructureSource
import com.example.parser.PdbParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class ProteinRepository {

    private val httpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    suspend fun fetchPdb(pdbId: String): Result<ProteinStructure> = withContext(Dispatchers.IO) {
        val cleanId = pdbId.trim().uppercase()
        val url = "https://files.rcsb.org/download/$cleanId.pdb"

        try {
            val request = Request.Builder().url(url).build()
            val response = httpClient.newCall(request).execute()

            if (!response.isSuccessful) {
                return@withContext Result.failure(
                    Exception("PDB ID $cleanId not found in RCSB (HTTP ${response.code})")
                )
            }

            val body = response.body?.string() ?: ""
            if (body.isEmpty() || !body.contains("ATOM")) {
                return@withContext Result.failure(Exception("Empty or invalid PDB content returned for $cleanId"))
            }

            // Fetch extra metadata if available
            val titleAndOrganism = fetchRcsbMetadata(cleanId)

            val structure = PdbParser.parse(body, cleanId, StructureSource.PDB)
            val enrichedStructure = structure.copy(
                title = titleAndOrganism.first.ifEmpty { structure.title },
                organism = titleAndOrganism.second.ifEmpty { structure.organism }
            )

            Result.success(enrichedStructure)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun fetchAlphaFold(uniprotId: String): Result<ProteinStructure> = withContext(Dispatchers.IO) {
        val cleanId = uniprotId.trim().uppercase()
        val apiUrl = "https://alphafold.ebi.ac.uk/api/prediction/$cleanId"

        try {
            val apiRequest = Request.Builder().url(apiUrl).build()
            val apiResponse = httpClient.newCall(apiRequest).execute()

            if (!apiResponse.isSuccessful) {
                return@withContext Result.failure(
                    Exception("UniProt ID $cleanId not found in AlphaFold DB (HTTP ${apiResponse.code})")
                )
            }

            val apiBody = apiResponse.body?.string() ?: ""
            val jsonArray = JSONArray(apiBody)
            if (jsonArray.length() == 0) {
                return@withContext Result.failure(Exception("No AlphaFold prediction models available for $cleanId"))
            }

            val firstModel = jsonArray.getJSONObject(0)
            val pdbUrl = firstModel.optString("pdbUrl")
            val gene = firstModel.optString("gene", cleanId)
            val description = firstModel.optString("uniprotDescription", "")
            val organism = firstModel.optString("organismScientificName", "Homo sapiens")

            if (pdbUrl.isEmpty()) {
                return@withContext Result.failure(Exception("PDB file URL missing in AlphaFold metadata for $cleanId"))
            }

            // Download the actual predicted PDB structure
            val pdbRequest = Request.Builder().url(pdbUrl).build()
            val pdbResponse = httpClient.newCall(pdbRequest).execute()

            if (!pdbResponse.isSuccessful) {
                return@withContext Result.failure(
                    Exception("Failed to download AlphaFold PDB model from $pdbUrl")
                )
            }

            val pdbContent = pdbResponse.body?.string() ?: ""
            val structure = PdbParser.parse(pdbContent, cleanId, StructureSource.ALPHAFOLD)

            val enriched = structure.copy(
                title = if (description.isNotEmpty()) "$gene - $description" else "AlphaFold Model: $gene",
                organism = organism,
                resolution = "pLDDT confidence (AlphaFold v4)"
            )

            Result.success(enriched)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun loadCustomPdbText(text: String, id: String = "CUSTOM"): Result<ProteinStructure> = withContext(Dispatchers.Default) {
        try {
            if (!text.contains("ATOM")) {
                return@withContext Result.failure(Exception("No ATOM records found in the provided PDB text"))
            }
            val structure = PdbParser.parse(text, id, StructureSource.CUSTOM)
            Result.success(structure)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun fetchRcsbMetadata(pdbId: String): Pair<String, String> {
        return try {
            val url = "https://data.rcsb.org/rest/v1/core/entry/$pdbId"
            val request = Request.Builder().url(url).build()
            val response = httpClient.newCall(request).execute()
            if (response.isSuccessful) {
                val jsonStr = response.body?.string() ?: ""
                val json = JSONObject(jsonStr)
                val struct = json.optJSONObject("struct")
                val title = struct?.optString("title") ?: ""
                val rcsbEntry = json.optJSONObject("rcsb_entry_info")
                val organism = rcsbEntry?.optString("polymer_entity_count_protein") ?: "Homo sapiens"
                Pair(title, organism)
            } else {
                Pair("", "")
            }
        } catch (_: Exception) {
            Pair("", "")
        }
    }
}
