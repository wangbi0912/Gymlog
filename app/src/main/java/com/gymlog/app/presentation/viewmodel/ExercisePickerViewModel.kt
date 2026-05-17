package com.gymlog.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gymlog.app.domain.model.BodyPart
import com.gymlog.app.domain.model.Exercise
import com.gymlog.app.domain.repository.ExerciseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
class ExercisePickerViewModel @Inject constructor(
    private val repo: ExerciseRepository
) : ViewModel() {

    val allExercises: StateFlow<List<Exercise>> = repo.getAll()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    suspend fun search(query: String): List<Exercise> = repo.search(query)
}
