package com.gymlog.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gymlog.app.domain.model.BodyMeasurement
import com.gymlog.app.domain.repository.BodyMeasurementRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class BodyDataViewModel @Inject constructor(
    private val repo: BodyMeasurementRepository
) : ViewModel() {

    val measurements: StateFlow<List<BodyMeasurement>> = repo.getAll()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    fun saveWeight(weightKg: Float) {
        viewModelScope.launch {
            repo.save(BodyMeasurement(
                id = UUID.randomUUID().toString(),
                date = System.currentTimeMillis(),
                weightKg = weightKg
            ))
        }
    }
}
