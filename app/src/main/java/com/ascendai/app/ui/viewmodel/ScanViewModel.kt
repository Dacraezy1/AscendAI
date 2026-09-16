package com.ascendai.app.ui.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ascendai.app.data.AscensionData
import com.ascendai.app.ml.FaceAnalyzer
import com.ascendai.app.model.AnalysisResult
import com.ascendai.app.model.DailyHabit
import com.ascendai.app.model.GuideCategory
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ScanUiState(
    val frontImageUri: Uri? = null,
    val sideImageUri: Uri? = null,
    val isAnalyzing: Boolean = false,
    val analysisProgress: Float = 0f,
    val currentStepText: String = "",
    val currentResult: AnalysisResult? = null,
    val scanHistory: List<AnalysisResult> = emptyList(),
    val habits: List<DailyHabit> = AscensionData.defaultHabits,
    val selectedCategory: GuideCategory = GuideCategory.ALL
)

class ScanViewModel(application: Application) : AndroidViewModel(application) {

    private val faceAnalyzer = FaceAnalyzer(application.applicationContext)

    private val _uiState = MutableStateFlow(ScanUiState())
    val uiState: StateFlow<ScanUiState> = _uiState.asStateFlow()

    fun setFrontImage(uri: Uri) {
        _uiState.update { it.copy(frontImageUri = uri) }
    }

    fun setSideImage(uri: Uri) {
        _uiState.update { it.copy(sideImageUri = uri) }
    }

    fun clearImages() {
        _uiState.update { it.copy(frontImageUri = null, sideImageUri = null) }
    }

    fun setSelectedCategory(category: GuideCategory) {
        _uiState.update { it.copy(selectedCategory = category) }
    }

    fun toggleHabit(habitId: String) {
        _uiState.update { state ->
            val updated = state.habits.map { habit ->
                if (habit.id == habitId) {
                    val newCompleted = !habit.isCompleted
                    val newStreak = if (newCompleted) habit.streakDays + 1 else maxOf(0, habit.streakDays - 1)
                    habit.copy(isCompleted = newCompleted, streakDays = newStreak)
                } else habit
            }
            state.copy(habits = updated)
        }
    }

    fun startAnalysis(onComplete: () -> Unit) {
        val currentState = _uiState.value
        if (currentState.frontImageUri == null && currentState.sideImageUri == null) {
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isAnalyzing = true,
                    analysisProgress = 0.05f,
                    currentStepText = "Initializing Neural Biometrics..."
                )
            }

            val steps = listOf(
                Pair(0.20f, "Detecting 468 Facial Landmarks..."),
                Pair(0.40f, "Measuring Canthal Tilt & Periorbital Vector..."),
                Pair(0.60f, "Computing Gonial Jaw Angle & Mandible Flare..."),
                Pair(0.80f, "Evaluating Hemifacial Symmetry & Thirds..."),
                Pair(0.95f, "Synthesizing Looksmax Tier & Potential...")
            )

            for ((progress, text) in steps) {
                delay(600)
                _uiState.update { it.copy(analysisProgress = progress, currentStepText = text) }
            }

            // Perform real analysis via ML Kit
            val result = faceAnalyzer.analyzeImages(
                frontUri = currentState.frontImageUri,
                sideUri = currentState.sideImageUri
            )

            delay(400)
            _uiState.update {
                it.copy(
                    isAnalyzing = false,
                    analysisProgress = 1.0f,
                    currentResult = result,
                    scanHistory = listOf(result) + it.scanHistory
                )
            }

            onComplete()
        }
    }
}
