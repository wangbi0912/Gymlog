package com.gymlog.app.data.local.dao

import androidx.room.*
import com.gymlog.app.data.local.entity.BodyMeasurementEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BodyMeasurementDao {
    @Query("SELECT * FROM body_measurements ORDER BY date DESC")
    fun getAll(): Flow<List<BodyMeasurementEntity>>

    @Query("SELECT * FROM body_measurements WHERE date BETWEEN :from AND :to ORDER BY date ASC")
    suspend fun getInRange(from: Long, to: Long): List<BodyMeasurementEntity>

    @Query("SELECT * FROM body_measurements ORDER BY date DESC LIMIT 1")
    suspend fun getLatest(): BodyMeasurementEntity?

    @Query("SELECT * FROM body_measurements WHERE date = :date LIMIT 1")
    suspend fun getByDate(date: Long): BodyMeasurementEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(measurement: BodyMeasurementEntity)

    @Update
    suspend fun update(measurement: BodyMeasurementEntity)

    @Delete
    suspend fun delete(measurement: BodyMeasurementEntity)
}
