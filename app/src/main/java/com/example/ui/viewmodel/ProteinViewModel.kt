package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.BioscienceExpert
import com.example.ai.BioscienceInsight
import com.example.ai.GeminiTtsService
import com.example.ai.TtsPlaybackState
import com.example.data.PresetProteins
import com.example.data.ProteinPreset
import com.example.model.*
import com.example.network.ProteinRepository
import com.example.parser.PdbParser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ProteinUiState(
    val structure: ProteinStructure? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val renderStyle: RenderStyle = RenderStyle.CARTOON,
    val colorScheme: MolecularColorScheme = MolecularColorScheme.SECONDARY_STRUCTURE,
    val selectedResidue: Residue? = null,
    val selectedAtom: Atom? = null,
    val measurementMode: Boolean = false,
    val measuredAtom1: Atom? = null,
    val measuredAtom2: Atom? = null,
    val measuredDistanceAngstroms: Float? = null,
    val autoSpin: Boolean = false,
    val useWebGLViewer: Boolean = false,
    val showCatalogSheet: Boolean = false,
    val showInsightSheet: Boolean = false,
    val bioscienceInsight: BioscienceInsight? = null,
    val resetCameraTrigger: Boolean = false
)

class ProteinViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ProteinRepository()
    private val ttsService = GeminiTtsService(application.applicationContext)

    private val _uiState = MutableStateFlow(ProteinUiState())
    val uiState: StateFlow<ProteinUiState> = _uiState.asStateFlow()

    val ttsState: StateFlow<TtsPlaybackState> = ttsService.playbackState

    init {
        // Load default preset Crambin immediately so user has high-fidelity 3D structure on launch
        loadDefaultPreset()
    }

    private fun loadDefaultPreset() {
        val defaultPreset = PresetProteins.CATALOG.first()
        val defaultPdb = defaultPreset.defaultPdbContent ?: PresetProteins.CRAMBIN_PDB
        val structure = PdbParser.parse(defaultPdb, defaultPreset.id, defaultPreset.source)
        val insight = BioscienceExpert.generateInsight(structure)

        _uiState.value = _uiState.value.copy(
            structure = structure,
            bioscienceInsight = insight,
            selectedResidue = structure.chains.values.firstOrNull()?.firstOrNull()
        )
    }

    fun loadPreset(preset: ProteinPreset) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null, showCatalogSheet = false)

            if (preset.defaultPdbContent != null) {
                val structure = PdbParser.parse(preset.defaultPdbContent, preset.id, preset.source)
                onStructureLoaded(structure)
            } else if (preset.source == StructureSource.ALPHAFOLD) {
                fetchAlphaFold(preset.id)
            } else {
                fetchPdb(preset.id)
            }
        }
    }

    fun fetchPdb(pdbId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null, showCatalogSheet = false)
            val result = repository.fetchPdb(pdbId)
            result.onSuccess { structure ->
                onStructureLoaded(structure)
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = error.message ?: "Failed to download PDB $pdbId"
                )
            }
        }
    }

    fun fetchAlphaFold(uniprotId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null, showCatalogSheet = false)
            val result = repository.fetchAlphaFold(uniprotId)
            result.onSuccess { structure ->
                onStructureLoaded(structure, defaultColorScheme = MolecularColorScheme.PLDDT_CONFIDENCE)
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = error.message ?: "Failed to retrieve AlphaFold prediction for $uniprotId"
                )
            }
        }
    }

    fun loadCustomPdbText(text: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null, showCatalogSheet = false)
            val result = repository.loadCustomPdbText(text)
            result.onSuccess { structure ->
                onStructureLoaded(structure)
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = error.message ?: "Failed to parse custom PDB"
                )
            }
        }
    }

    private fun onStructureLoaded(
        structure: ProteinStructure,
        defaultColorScheme: MolecularColorScheme? = null
    ) {
        val insight = BioscienceExpert.generateInsight(structure)
        val scheme = defaultColorScheme ?: if (structure.source == StructureSource.ALPHAFOLD) {
            MolecularColorScheme.PLDDT_CONFIDENCE
        } else {
            MolecularColorScheme.SECONDARY_STRUCTURE
        }

        _uiState.value = _uiState.value.copy(
            structure = structure,
            bioscienceInsight = insight,
            isLoading = false,
            errorMessage = null,
            colorScheme = scheme,
            selectedResidue = structure.chains.values.firstOrNull()?.firstOrNull(),
            selectedAtom = null,
            measuredAtom1 = null,
            measuredAtom2 = null,
            measuredDistanceAngstroms = null,
            resetCameraTrigger = true
        )
    }

    fun setRenderStyle(style: RenderStyle) {
        _uiState.value = _uiState.value.copy(renderStyle = style)
    }

    fun setColorScheme(scheme: MolecularColorScheme) {
        _uiState.value = _uiState.value.copy(colorScheme = scheme)
    }

    fun selectResidue(residue: Residue) {
        _uiState.value = _uiState.value.copy(
            selectedResidue = residue,
            selectedAtom = residue.caAtom
        )
    }

    fun selectAtom(atom: Atom, residue: Residue) {
        val current = _uiState.value
        if (current.measurementMode) {
            if (current.measuredAtom1 == null) {
                _uiState.value = current.copy(
                    measuredAtom1 = atom,
                    measuredAtom2 = null,
                    measuredDistanceAngstroms = null
                )
            } else if (current.measuredAtom2 == null && current.measuredAtom1.serial != atom.serial) {
                val dist = current.measuredAtom1.position.distanceTo(atom.position)
                _uiState.value = current.copy(
                    measuredAtom2 = atom,
                    measuredDistanceAngstroms = dist
                )
            } else {
                // Reset first measurement atom
                _uiState.value = current.copy(
                    measuredAtom1 = atom,
                    measuredAtom2 = null,
                    measuredDistanceAngstroms = null
                )
            }
        } else {
            _uiState.value = current.copy(
                selectedAtom = atom,
                selectedResidue = residue
            )
        }
    }

    fun toggleMeasurementMode() {
        val newMode = !_uiState.value.measurementMode
        _uiState.value = _uiState.value.copy(
            measurementMode = newMode,
            measuredAtom1 = null,
            measuredAtom2 = null,
            measuredDistanceAngstroms = null
        )
    }

    fun clearMeasurement() {
        _uiState.value = _uiState.value.copy(
            measuredAtom1 = null,
            measuredAtom2 = null,
            measuredDistanceAngstroms = null
        )
    }

    fun toggleAutoSpin() {
        _uiState.value = _uiState.value.copy(autoSpin = !_uiState.value.autoSpin)
    }

    fun toggleWebGLViewer() {
        _uiState.value = _uiState.value.copy(useWebGLViewer = !_uiState.value.useWebGLViewer)
    }

    fun resetCamera() {
        _uiState.value = _uiState.value.copy(resetCameraTrigger = true)
    }

    fun onCameraResetDone() {
        _uiState.value = _uiState.value.copy(resetCameraTrigger = false)
    }

    fun openCatalog() {
        _uiState.value = _uiState.value.copy(showCatalogSheet = true)
    }

    fun closeCatalog() {
        _uiState.value = _uiState.value.copy(showCatalogSheet = false)
    }

    fun openInsight() {
        _uiState.value = _uiState.value.copy(showInsightSheet = true)
    }

    fun closeInsight() {
        _uiState.value = _uiState.value.copy(showInsightSheet = false)
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    fun speakNarration(text: String, voiceName: String) {
        ttsService.speak(text, voiceName, viewModelScope)
    }

    fun stopNarration() {
        ttsService.stop()
    }

    override fun onCleared() {
        super.onCleared()
        ttsService.release()
    }
}
