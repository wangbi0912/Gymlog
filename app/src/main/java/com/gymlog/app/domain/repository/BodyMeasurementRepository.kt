package com.gymlog.app.domain.repository

import com.gymlog.app.domain.model.BodyMeasurement
import kotlinx.coroutines.flow.Flow

interface BodyMeasurementRepository {
    fun getAll(): Flow<List<BodyMeasurement>>
    suspend fun getInRange(from: Long, to: Long): List<BodyMeasurement>
    suspend fun getLatest(): BodyMeasurement?
    suspend fun getByDate(date: Long): BodyMeasurement?
    suspend fun save(measurement: BodyMeasurement)
    suspend fun delete(measurement: BodyMeasurement)
}
