package com.gymlog.app.di

import android.content.Context
import androidx.room.Room
import com.gymlog.app.data.local.GymLogDatabase
import com.gymlog.app.data.local.dao.*
import com.gymlog.app.data.local.preset.PresetExercises
import com.gymlog.app.data.local.preset.PresetTemplates
import com.gymlog.app.data.remote.LLMApiClient
import com.gymlog.app.data.repository.*
import com.gymlog.app.domain.repository.*
import com.gymlog.app.domain.usecase.DashboardUseCase
import com.gymlog.app.domain.usecase.ReviewUseCase
import com.google.gson.Gson
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideGson(): Gson = Gson()

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): GymLogDatabase {
        return Room.databaseBuilder(
            context, GymLogDatabase::class.java, "gymlog.db"
        )
            .addCallback(object : androidx.room.RoomDatabase.Callback() {
                override fun onCreate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                    super.onCreate(db)
                    CoroutineScope(Dispatchers.IO).launch {
                        val database = db as? GymLogDatabase ?: return@launch
                        val exerciseCount = database.exerciseDao().count()
                        if (exerciseCount == 0) {
                            database.exerciseDao().insertAll(PresetExercises.getAll())
                            PresetTemplates.getAll().forEach { preset ->
                                database.templateDao().insert(preset.template)
                                database.templateDao().insertExercises(preset.exercises)
                            }
                        }
                    }
                }
            })
            .build()
    }

    @Provides fun provideExerciseDao(db: GymLogDatabase): ExerciseDao = db.exerciseDao()
    @Provides fun provideSessionDao(db: GymLogDatabase): SessionDao = db.sessionDao()
    @Provides fun provideTemplateDao(db: GymLogDatabase): TemplateDao = db.templateDao()
    @Provides fun provideReviewDao(db: GymLogDatabase): ReviewDao = db.reviewDao()
    @Provides fun provideBodyMeasurementDao(db: GymLogDatabase): BodyMeasurementDao = db.bodyMeasurementDao()

    @Provides @Singleton
    fun provideExerciseRepository(
        dao: ExerciseDao,
        sessionDao: SessionDao
    ): ExerciseRepository = ExerciseRepositoryImpl(dao, sessionDao)

    @Provides @Singleton
    fun provideSessionRepository(
        sessionDao: SessionDao,
        exerciseDao: ExerciseDao
    ): SessionRepository = SessionRepositoryImpl(sessionDao, exerciseDao)

    @Provides @Singleton
    fun provideTemplateRepository(
        dao: TemplateDao,
        exerciseDao: ExerciseDao
    ): TemplateRepository = TemplateRepositoryImpl(dao, exerciseDao)

    @Provides @Singleton
    fun provideReviewRepository(
        dao: ReviewDao,
        gson: Gson
    ): ReviewRepository = ReviewRepositoryImpl(dao, gson)

    @Provides @Singleton
    fun provideBodyMeasurementRepository(
        dao: BodyMeasurementDao
    ): BodyMeasurementRepository = BodyMeasurementRepositoryImpl(dao)

    @Provides @Singleton
    fun provideUserPreferencesRepository(
        @ApplicationContext context: Context
    ): UserPreferencesRepository = UserPreferencesRepositoryImpl(context)

    @Provides @Singleton
    fun provideLLMApiClient(gson: Gson): LLMApiClient = LLMApiClient(gson)

    @Provides @Singleton
    fun provideReviewUseCase(
        sessionRepo: SessionRepository,
        reviewRepo: ReviewRepository,
        userPrefsRepo: UserPreferencesRepository,
        bodyMeasurementRepo: BodyMeasurementRepository,
        llmClient: LLMApiClient,
        gson: Gson
    ): ReviewUseCase = ReviewUseCase(sessionRepo, reviewRepo, userPrefsRepo, bodyMeasurementRepo, llmClient, gson)

    @Provides @Singleton
    fun provideDashboardUseCase(
        sessionRepo: SessionRepository,
        reviewRepo: ReviewRepository,
        bodyMeasurementRepo: BodyMeasurementRepository
    ): DashboardUseCase = DashboardUseCase(sessionRepo, reviewRepo, bodyMeasurementRepo)
}
