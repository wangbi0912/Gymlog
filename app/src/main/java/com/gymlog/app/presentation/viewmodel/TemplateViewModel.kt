package com.gymlog.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gymlog.app.domain.model.TrainingTemplate
import com.gymlog.app.domain.repository.TemplateRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
class TemplateViewModel @Inject constructor(
    private val repo: TemplateRepository
) : ViewModel() {

    val templates: StateFlow<List<TrainingTemplate>> = repo.getAll()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
}
