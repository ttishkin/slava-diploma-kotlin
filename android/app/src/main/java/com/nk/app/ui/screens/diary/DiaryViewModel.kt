package com.nk.app.ui.screens.diary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nk.app.data.repository.AuthRepository
import com.nk.app.data.repository.DiaryRepository
import com.nk.app.domain.model.DiaryEntry
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class DiaryState(
    val entries: List<DiaryEntry> = emptyList(),
    val day: String = LocalDate.now().toString(),
    val kcalNorm: Int = 2000,
    val isLoading: Boolean = true,
    val error: String? = null
) {
    val totalKcal: Int get() = entries.sumOf { it.kcal }
    val remaining: Int get() = kcalNorm - totalKcal
    val progress: Float get() = if (kcalNorm > 0) (totalKcal.toFloat() / kcalNorm).coerceIn(0f, 1.5f) else 0f
    val meals: Map<String, List<DiaryEntry>> get() = entries.groupBy { it.meal }
}

@HiltViewModel
class DiaryViewModel @Inject constructor(
    private val diaryRepo: DiaryRepository,
    private val authRepo: AuthRepository
) : ViewModel() {

    private val _state = MutableStateFlow(DiaryState())
    val state: StateFlow<DiaryState> = _state

    init { load() }

    fun setDay(day: String) {
        _state.value = _state.value.copy(day = day)
        load()
    }

    fun load() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            try {
                val profile = try { authRepo.getProfile() } catch (_: Exception) { null }
                val entries = diaryRepo.getEntries(_state.value.day)
                _state.value = _state.value.copy(
                    entries = entries,
                    kcalNorm = profile?.kcalNorm ?: 2000,
                    isLoading = false
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(error = e.message, isLoading = false)
            }
        }
    }

    fun deleteEntry(id: Int) {
        viewModelScope.launch {
            try {
                diaryRepo.deleteEntry(id)
                load()
            } catch (_: Exception) {}
        }
    }
}
