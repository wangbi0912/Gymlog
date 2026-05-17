package com.gymlog.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gymlog.app.domain.model.*
import com.gymlog.app.domain.repository.*
import com.gymlog.app.domain.usecase.ReviewUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class SessionUiState(
    val sessionId: String = "",
    val isActive: Boolean = false,
    val startTime: Long = 0,
    val elapsedSeconds: Long = 0,
    val bodyPart: BodyPart = BodyPart.CHEST,
    val exercises: List<SessionExerciseUi> = emptyList(),
    val isRestTimerActive: Boolean = false,
    val restSecondsRemaining: Int = 0,
    val restTotalSeconds: Int = 90,
    val showExercisePicker: Boolean = false,
    val showSetInput: Boolean = false,
    val editingExerciseId: String? = null,
    val setWeight: String = "",
    val setReps: String = "",
    val setType: SetType = SetType.WORKING,
    val setRpe: Int? = null,
    val totalVolumeKg: Float = 0f,
    val showCompleteDialog: Boolean = false,
    val showRpeGuide: Boolean = false,
    val note: String = "",
    val overallRpe: Int? = null,
    val submitForReview: Boolean = true,
    val savedSuccessfully: Boolean = false,
    val errorMessage: String? = null
)

data class SessionExerciseUi(
    val id: String,
    val exerciseId: String,
    val exerciseName: String,
    val sortOrder: Int,
    val supersetGroupId: String? = null,
    val note: String? = null,
    val sets: List<SetUi> = emptyList(),
    val isExpanded: Boolean = true,
    val lastWeight: Float? = null,
    val lastReps: Int? = null,
    val lastDate: Long? = null,
    val targetSets: Int? = null,
    val targetReps: Int? = null,
    val targetWeightKg: Float? = null,
    val targetSetType: SetType? = null
)

data class SetUi(
    val id: String,
    val setNumber: Int,
    val weightKg: Float? = null,
    val reps: Int? = null,
    val setType: SetType = SetType.WORKING,
    val rpe: Int? = null,
    val isCompleted: Boolean = true
)

@HiltViewModel
class SessionViewModel @Inject constructor(
    private val sessionRepo: SessionRepository,
    private val exerciseRepo: ExerciseRepository,
    private val templateRepo: TemplateRepository,
    private val userPrefsRepo: UserPreferencesRepository,
    private val reviewUseCase: ReviewUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(SessionUiState())
    val state: StateFlow<SessionUiState> = _state

    private var timerJob: Job? = null
    private var restTimerJob: Job? = null
    private val restSeconds = MutableStateFlow(90)

    init {
        viewModelScope.launch {
            userPrefsRepo.restTimerSeconds.collect { sec ->
                restSeconds.value = sec
                _state.update { it.copy(restTotalSeconds = sec) }
            }
        }
    }

    fun startNewSession(bodyPart: BodyPart = BodyPart.CHEST) {
        val id = UUID.randomUUID().toString()
        val now = System.currentTimeMillis()
        _state.value = SessionUiState(
            sessionId = id, isActive = true, startTime = now, bodyPart = bodyPart,
            restTotalSeconds = restSeconds.value, exercises = emptyList()
        )
        startTimer()
        viewModelScope.launch {
            sessionRepo.save(TrainingSession(
                id = id, startTime = now, bodyPart = bodyPart, status = SessionStatus.IN_PROGRESS
            ))
        }
    }

    fun resumeSession(session: TrainingSession) {
        val now = System.currentTimeMillis()
        _state.value = SessionUiState(
            sessionId = session.id, isActive = true, startTime = session.startTime,
            elapsedSeconds = (now - session.startTime) / 1000,
            bodyPart = session.bodyPart,
            restTotalSeconds = restSeconds.value,
            exercises = session.exercises.map { ex ->
                SessionExerciseUi(
                    id = ex.id, exerciseId = ex.exerciseId, exerciseName = ex.exerciseName,
                    sortOrder = ex.sortOrder, supersetGroupId = ex.supersetGroupId,
                    note = ex.note,
                    sets = ex.sets.map { s -> SetUi(id = s.id, setNumber = s.setNumber, weightKg = s.weightKg, reps = s.reps, setType = s.setType, rpe = s.rpe) }
                )
            },
            totalVolumeKg = session.totalVolumeKg
        )
        startTimer()
    }

    fun startFromTemplate(template: TrainingTemplate) {
        val id = UUID.randomUUID().toString()
        val now = System.currentTimeMillis()
        val exercises = template.exercises.mapIndexed { i, te ->
            SessionExerciseUi(
                id = UUID.randomUUID().toString(),
                exerciseId = te.exerciseId,
                exerciseName = te.exerciseName,
                sortOrder = i,
                supersetGroupId = te.supersetGroupId,
                targetSets = te.targetSets,
                targetReps = te.targetReps,
                targetWeightKg = te.targetWeightKg,
                targetSetType = te.targetSetType
            )
        }
        _state.value = SessionUiState(
            sessionId = id, isActive = true, startTime = now,
            bodyPart = BodyPart.CHEST,
            restTotalSeconds = restSeconds.value,
            exercises = exercises
        )
        startTimer()
        viewModelScope.launch {
            sessionRepo.save(TrainingSession(
                id = id, startTime = now, bodyPart = BodyPart.CHEST,
                templateId = template.id, status = SessionStatus.IN_PROGRESS
            ))
        }
    }

    fun addExercise(exercise: Exercise) {
        val exUi = SessionExerciseUi(
            id = UUID.randomUUID().toString(),
            exerciseId = exercise.id,
            exerciseName = exercise.name,
            sortOrder = _state.value.exercises.size,
            lastWeight = exercise.lastWeightKg,
            lastReps = exercise.lastReps,
            lastDate = exercise.lastDate
        )
        _state.update { it.copy(exercises = it.exercises + exUi) }
        // Update body part if first exercise
        if (_state.value.exercises.size == 1) {
            _state.update { it.copy(bodyPart = exercise.bodyPart) }
        }
    }

    fun removeExercise(id: String) {
        _state.update { it.copy(exercises = it.exercises.filter { e -> e.id != id }) }
    }

    fun toggleExerciseExpanded(id: String) {
        _state.update { s ->
            s.copy(exercises = s.exercises.map {
                if (it.id == id) it.copy(isExpanded = !it.isExpanded) else it
            })
        }
    }

    fun showSetInput(exerciseId: String?, lastWeight: Float? = null, lastReps: Int? = null) {
        _state.update {
            it.copy(
                showSetInput = true,
                editingExerciseId = exerciseId,
                setWeight = lastWeight?.let { if (it > 0) "%.1f".format(it) else "" } ?: it.setWeight,
                setReps = lastReps?.toString() ?: it.setReps,
                setType = SetType.WORKING,
                setRpe = null
            )
        }
    }

    fun hideSetInput() {
        _state.update { it.copy(showSetInput = false, editingExerciseId = null) }
    }

    fun setSetWeight(w: String) { _state.update { it.copy(setWeight = w) } }
    fun setSetReps(r: String) { _state.update { it.copy(setReps = r) } }
    fun setSetType(t: SetType) { _state.update { it.copy(setType = t) } }
    fun setSetRpe(r: Int?) { _state.update { it.copy(setRpe = r) } }

    fun addSet() {
        val s = _state.value
        val exId = s.editingExerciseId ?: return
        _state.update { state ->
            val updated = state.exercises.map { ex ->
                if (ex.id == exId) {
                    val newSet = SetUi(
                        id = UUID.randomUUID().toString(),
                        setNumber = ex.sets.size + 1,
                        weightKg = state.setWeight.toFloatOrNull(),
                        reps = state.setReps.toIntOrNull(),
                        setType = state.setType,
                        rpe = state.setRpe
                    )
                    ex.copy(sets = ex.sets + newSet)
                } else ex
            }
            state.copy(
                exercises = updated,
                showSetInput = false,
                editingExerciseId = null,
                totalVolumeKg = updated.sumOf { ex ->
                    ex.sets.sumOf { set -> ((set.weightKg ?: 0f) * (set.reps ?: 0)).toDouble() }
                }.toFloat()
            )
        }
        // Start rest timer
        startRestTimer()
    }

    private fun startRestTimer() {
        restTimerJob?.cancel()
        val total = _state.value.restTotalSeconds
        _state.update { it.copy(isRestTimerActive = true, restSecondsRemaining = total) }
        restTimerJob = viewModelScope.launch {
            for (i in total downTo 0) {
                _state.update { it.copy(restSecondsRemaining = i) }
                delay(1000)
            }
            _state.update { it.copy(isRestTimerActive = false) }
        }
    }

    fun skipRest() {
        restTimerJob?.cancel()
        _state.update { it.copy(isRestTimerActive = false, restSecondsRemaining = 0) }
    }

    fun addRestTime(sec: Int) {
        val newRemaining = _state.value.restSecondsRemaining + sec
        _state.update { it.copy(restSecondsRemaining = newRemaining) }
    }

    fun showCompleteDialog() { _state.update { it.copy(showCompleteDialog = true) } }
    fun hideCompleteDialog() { _state.update { it.copy(showCompleteDialog = false) } }
    fun setNote(n: String) { _state.update { it.copy(note = n) } }
    fun setOverallRpe(r: Int?) { _state.update { it.copy(overallRpe = r) } }
    fun setSubmitForReview(s: Boolean) { _state.update { it.copy(submitForReview = s) } }
    fun setShowRpeGuide(s: Boolean) { _state.update { it.copy(showRpeGuide = s) } }
    fun clearError() { _state.update { it.copy(errorMessage = null) } }
    fun setShowExercisePicker(s: Boolean) { _state.update { it.copy(showExercisePicker = s) } }

    fun completeSession() {
        viewModelScope.launch {
            try {
                val s = _state.value
                timerJob?.cancel()
                restTimerJob?.cancel()

                val session = TrainingSession(
                    id = s.sessionId, startTime = s.startTime,
                    endTime = System.currentTimeMillis(), bodyPart = s.bodyPart,
                    overallRpe = s.overallRpe, note = s.note,
                    status = SessionStatus.COMPLETED,
                    exercises = s.exercises.map { ex ->
                        SessionExercise(
                            id = ex.id, sessionId = s.sessionId, exerciseId = ex.exerciseId,
                            exerciseName = ex.exerciseName, sortOrder = ex.sortOrder,
                            supersetGroupId = ex.supersetGroupId, note = ex.note,
                            sets = ex.sets.map { set ->
                                SessionSet(
                                    id = set.id, sessionExerciseId = ex.id,
                                    setNumber = set.setNumber, weightKg = set.weightKg,
                                    reps = set.reps, setType = set.setType, rpe = set.rpe
                                )
                            }
                        )
                    }
                )
                sessionRepo.save(session)

                if (s.submitForReview) {
                    try {
                        reviewUseCase.submitReview(s.sessionId)
                    } catch (_: Exception) { /* Review submission failed but session saved */ }
                }

                _state.update { it.copy(isActive = false, showCompleteDialog = false, savedSuccessfully = true) }
            } catch (e: Exception) {
                _state.update { it.copy(errorMessage = e.message) }
            }
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                _state.update { it.copy(elapsedSeconds = it.elapsedSeconds + 1) }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
        restTimerJob?.cancel()
    }
}
