package com.gymlog.app.domain.usecase

import com.gymlog.app.domain.model.*
import com.gymlog.app.domain.repository.*
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.toList
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

data class DashboardData(
    val monthlySessionCount: Int = 0,
    val streakDays: Int = 0,
    val weeklySessions: Int = 0,
    val weeklyVolumeKg: Float = 0f,
    val weeklySets: Int = 0,
    val latestReview: ReviewRequest? = null,
    val latestWeight: BodyMeasurement? = null,
    val weightChange7d: Float? = null
)

@Singleton
class DashboardUseCase @Inject constructor(
    private val sessionRepo: SessionRepository,
    private val reviewRepo: ReviewRepository,
    private val bodyMeasurementRepo: BodyMeasurementRepository
) {
    suspend fun getDashboardData(): DashboardData {
        val now = System.currentTimeMillis()
        val cal = Calendar.getInstance()

        // Monthly count
        cal.timeInMillis = now
        val monthStart = cal.apply { set(Calendar.DAY_OF_MONTH, 1); set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0) }.timeInMillis
        val monthEnd = cal.apply { set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH)); set(Calendar.HOUR_OF_DAY, 23); set(Calendar.MINUTE, 59); set(Calendar.SECOND, 59) }.timeInMillis
        val monthlyCount = sessionRepo.getSessionCount(monthStart, monthEnd)

        // Week
        cal.timeInMillis = now
        cal.set(Calendar.DAY_OF_WEEK, cal.firstDayOfWeek)
        val weekStart = cal.apply { set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0) }.timeInMillis
        cal.timeInMillis = now
        val weekSessions = sessionRepo.getSessionsInRange(weekStart, now)
        val weekVolume = weekSessions.sumOf { it.totalVolumeKg.toDouble() }.toFloat()
        val weekSetCount = weekSessions.sumOf { it.workingSetCount }
        val weekCount = weekSessions.size

        // Streak
        val streak = calculateStreak()

        // Latest review
        val allReviews = reviewRepo.getAll().firstOrNull() ?: emptyList()
        val latestReview = allReviews.firstOrNull { it.status == ReviewStatus.COMPLETED }

        // Body
        val latestWeight = bodyMeasurementRepo.getLatest()
        val dayMs = 86_400_000L
        val weight7dAgo = bodyMeasurementRepo.getByDate(now - 7 * dayMs)
        val weightChange = if (latestWeight != null && weight7dAgo != null) {
            latestWeight.weightKg - weight7dAgo.weightKg
        } else null

        return DashboardData(
            monthlySessionCount = monthlyCount,
            streakDays = streak,
            weeklySessions = weekCount,
            weeklyVolumeKg = weekVolume,
            weeklySets = weekSetCount,
            latestReview = latestReview,
            latestWeight = latestWeight,
            weightChange7d = weightChange
        )
    }

    private suspend fun calculateStreak(): Int {
        val sessions = sessionRepo.getCompletedSessions()
        if (sessions.isEmpty()) return 0

        val dayMs = 86_400_000L
        val cal = Calendar.getInstance()
        val today = cal.apply { set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0) }.timeInMillis

        val trainingDays = sessions.map { session ->
            cal.timeInMillis = session.startTime
            cal.apply { set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0) }.timeInMillis
        }.toSet().sortedDescending()

        var streak = 0
        var checkDay = today
        for (day in trainingDays) {
            when {
                day == checkDay -> { streak++; checkDay -= dayMs }
                day == checkDay - dayMs -> { streak++; checkDay = day - dayMs }
                else -> break
            }
        }
        return streak
    }
}
