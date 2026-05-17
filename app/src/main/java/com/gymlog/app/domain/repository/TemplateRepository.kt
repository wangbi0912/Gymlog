package com.gymlog.app.domain.repository

import com.gymlog.app.domain.model.TrainingTemplate
import kotlinx.coroutines.flow.Flow

interface TemplateRepository {
    fun getAll(): Flow<List<TrainingTemplate>>
    suspend fun getById(id: String): TrainingTemplate?
    suspend fun save(template: TrainingTemplate)
    suspend fun delete(id: String)
}
