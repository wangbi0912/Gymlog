package com.gymlog.app.data.local.dao

import androidx.room.*
import com.gymlog.app.data.local.entity.ExerciseEntity
import com.gymlog.app.domain.model.BodyPart
import kotlinx.coroutines.flow.Flow

@Dao
interface ExerciseDao {
    @Query("SELECT * FROM exercises WHERE isHidden = 0 ORDER BY bodyPart, name")
    fun getAll(): Flow<List<ExerciseEntity>>

    @Query("SELECT * FROM exercises WHERE isHidden = 0 ORDER BY bodyPart, name")
    suspend fun getAllList(): List<ExerciseEntity>

    @Query("SELECT * FROM exercises WHERE id = :id")
    suspend fun getById(id: String): ExerciseEntity?

    @Query("SELECT * FROM exercises WHERE id IN (:ids)")
    suspend fun getByIds(ids: List<String>): List<ExerciseEntity>

    @Query("SELECT * FROM exercises WHERE name LIKE '%' || :query || '%' AND isHidden = 0 LIMIT :limit")
    suspend fun search(query: String, limit: Int = 20): List<ExerciseEntity>

    @Query("SELECT * FROM exercises WHERE bodyPart = :bodyPart AND isHidden = 0 ORDER BY name")
    fun getByBodyPart(bodyPart: BodyPart): Flow<List<ExerciseEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(exercises: List<ExerciseEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(exercise: ExerciseEntity)

    @Update
    suspend fun update(exercise: ExerciseEntity)

    @Query("UPDATE exercises SET isHidden = :hidden WHERE id = :id")
    suspend fun setHidden(id: String, hidden: Boolean)

    @Query("SELECT COUNT(*) FROM exercises")
    suspend fun count(): Int
}
