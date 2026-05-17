package com.gymlog.app.data.repository

import com.gymlog.app.data.local.dao.BodyMeasurementDao
import com.gymlog.app.data.local.entity.BodyMeasurementEntity
import com.gymlog.app.domain.model.BodyMeasurement
import com.gymlog.app.domain.repository.BodyMeasurementRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BodyMeasurementRepositoryImpl @Inject constructor(
    private val dao: BodyMeasurementDao
) : BodyMeasurementRepository {

    override fun getAll(): Flow<List<BodyMeasurement>> = dao.getAll().map { it.map { e -> e.toDomain() } }

    override suspend fun getInRange(from: Long, to: Long): List<BodyMeasurement> =
        dao.getInRange(from, to).map { it.toDomain() }

    override suspend fun getLatest(): BodyMeasurement? = dao.getLatest()?.toDomain()

    override suspend fun getByDate(date: Long): BodyMeasurement? = dao.getByDate(date)?.toDomain()

    override suspend fun save(measurement: BodyMeasurement) {
        val existing = dao.getByDate(measurement.date)
        if (existing != null) {
            dao.update(measurement.toEntity().copy(id = existing.id))
        } else {
            dao.insert(measurement.toEntity().copy(id = UUID.randomUUID().toString()))
        }
    }

    override suspend fun delete(measurement: BodyMeasurement) {
        dao.delete(measurement.toEntity())
    }
}

private fun BodyMeasurementEntity.toDomain() = BodyMeasurement(
    id = id, date = date, weightKg = weightKg, chestCm = chestCm,
    waistCm = waistCm, hipCm = hipCm, armCm = armCm, thighCm = thighCm
)

private fun BodyMeasurement.toEntity() = BodyMeasurementEntity(
    id = id, date = date, weightKg = weightKg, chestCm = chestCm,
    waistCm = waistCm, hipCm = hipCm, armCm = armCm, thighCm = thighCm
)
