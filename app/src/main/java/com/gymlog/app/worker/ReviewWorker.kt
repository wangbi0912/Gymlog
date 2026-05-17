package com.gymlog.app.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.gymlog.app.domain.usecase.ReviewUseCase
import com.gymlog.app.domain.repository.ReviewRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.concurrent.TimeUnit

@HiltWorker
class ReviewWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val reviewRepo: ReviewRepository,
    private val reviewUseCase: ReviewUseCase
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val reviewId = inputData.getString("review_id") ?: return Result.failure()

        return try {
            reviewUseCase.executeReview(reviewId)
            Result.success()
        } catch (e: Exception) {
            if (runAttemptCount < 3) Result.retry() else Result.failure()
        }
    }

    companion object {
        fun enqueue(reviewId: String, workManager: WorkManager) {
            val request = OneTimeWorkRequestBuilder<ReviewWorker>()
                .setInputData(workDataOf("review_id" to reviewId))
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .setBackoffCriteria(
                    BackoffPolicy.EXPONENTIAL,
                    30, TimeUnit.SECONDS
                )
                .build()
            workManager.enqueue(request)
        }

        fun enqueuePending(workManager: WorkManager) {
            val request = OneTimeWorkRequestBuilder<ReviewWorker>()
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .build()
            workManager.enqueue(request)
        }
    }
}

@HiltWorker
class ReviewQueueWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val reviewRepo: ReviewRepository,
    private val reviewUseCase: ReviewUseCase
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val pending = reviewRepo.getPendingReviews()
        pending.forEach { review ->
            try {
                reviewUseCase.executeReview(review.id)
            } catch (_: Exception) { /* continue with next */ }
        }
        return Result.success()
    }
}
