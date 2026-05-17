package com.gymlog.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class GymLogApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        val channels = listOf(
            NotificationChannel(
                CHANNEL_REST_TIMER,
                "休息计时器",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply { description = "组间休息提醒" },
            NotificationChannel(
                CHANNEL_REVIEW,
                "AI审查",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply { description = "训练审查完成通知" },
            NotificationChannel(
                CHANNEL_REMINDER,
                "训练提醒",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply { description = "训练打卡提醒" },
            NotificationChannel(
                CHANNEL_TIMER,
                "训练计时器",
                NotificationManager.IMPORTANCE_LOW
            ).apply { description = "前台训练计时服务" }
        )

        val manager = getSystemService(NotificationManager::class.java)
        channels.forEach { manager.createNotificationChannel(it) }
    }

    companion object {
        const val CHANNEL_REST_TIMER = "rest_timer"
        const val CHANNEL_REVIEW = "review"
        const val CHANNEL_REMINDER = "reminder"
        const val CHANNEL_TIMER = "workout_timer"
    }
}
