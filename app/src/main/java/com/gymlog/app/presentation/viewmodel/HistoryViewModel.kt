package com.gymlog.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gymlog.app.domain.model.*
import com.gymlog.app.domain.repository.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

data class HistoryState(
    val sessions: List<TrainingSession> = emptyList(),
    val filteredSessions: List<TrainingSession> = emptyList(),
    val selectedYear: Int = Calendar.getInstance().get(Calendar.YEAR),
    val selectedMonth: Int = Calendar.getInstance().get(Calendar.MONTH),
    val trainingDays: Set<Int> = emptySet(),
    val dayVolumes: Map<Int, Float> = emptyMap(),
    val maxDayVolume: Float = 1f,
    val selectedDay: Int? = null,
    val searchQuery: String = "",
    val filterBodyPart: BodyPart? = null,
    val loading: Boolean = false
)

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val sessionRepo: SessionRepository
) : ViewModel() {

    private val _state = MutableStateFlow(HistoryState())
    val state: StateFlow<HistoryState> = _state

    init { loadMonth(Calendar.getInstance().get(Calendar.YEAR), Calendar.getInstance().get(Calendar.MONTH)) }

    fun loadMonth(year: Int, month: Int) {
        viewModelScope.launch {
            _state.update { it.copy(loading = true, selectedYear = year, selectedMonth = month) }

            val cal = Calendar.getInstance()
            cal.set(year, month, 1, 0, 0, 0)
            cal.set(Calendar.MILLISECOND, 0)
            val from = cal.timeInMillis

            cal.set(year, month, cal.getActualMaximum(Calendar.DAY_OF_MONTH), 23, 59, 59)
            val to = cal.timeInMillis

            val sessions = sessionRepo.getSessionsInRange(from, to)
            val trainingDays = mutableSetOf<Int>()
            val dayVolumes = mutableMapOf<Int, Float>()

            sessions.forEach { s ->
                cal.timeInMillis = s.startTime
                val day = cal.get(Calendar.DAY_OF_MONTH)
                trainingDays.add(day)
                dayVolumes[day] = (dayVolumes[day] ?: 0f) + s.totalVolumeKg
            }

            val maxVol = dayVolumes.values.maxOrNull() ?: 1f

            _state.update {
                it.copy(
                    sessions = sessions,
                    filteredSessions = applyFilter(sessions, it.searchQuery, it.filterBodyPart),
                    trainingDays = trainingDays,
                    dayVolumes = dayVolumes,
                    maxDayVolume = maxVol,
                    selectedDay = null,
                    loading = false
                )
            }
        }
    }

    fun selectDay(day: Int) {
        _state.update { it.copy(selectedDay = if (it.selectedDay == day) null else day) }
    }

    fun setSearchQuery(query: String) {
        _state.update {
            it.copy(
                searchQuery = query,
                filteredSessions = applyFilter(it.sessions, query, it.filterBodyPart)
            )
        }
    }

    fun setFilterBodyPart(bp: BodyPart?) {
        _state.update {
            it.copy(
                filterBodyPart = bp,
                filteredSessions = applyFilter(it.sessions, it.searchQuery, bp)
            )
        }
    }

    fun getSessionsForDay(day: Int): List<TrainingSession> {
        val cal = Calendar.getInstance()
        return _state.value.sessions.filter {
            cal.timeInMillis = it.startTime
            cal.get(Calendar.DAY_OF_MONTH) == day
        }
    }

    private fun applyFilter(
        sessions: List<TrainingSession>,
        query: String,
        bodyPart: BodyPart?
    ): List<TrainingSession> {
        var result = sessions
        if (query.isNotBlank()) {
            result = result.filter {
                it.exercises.any { ex -> ex.exerciseName.contains(query, ignoreCase = true) } ||
                it.note?.contains(query, ignoreCase = true) == true
            }
        }
        if (bodyPart != null) {
            result = result.filter { it.bodyPart == bodyPart }
        }
        return result
    }
}
