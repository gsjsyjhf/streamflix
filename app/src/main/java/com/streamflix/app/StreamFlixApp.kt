package com.streamflix.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import android.util.Log
import androidx.work.Configuration
import coil.Coil
import coil.ImageLoader
import coil.disk.DiskCache
import coil.memory.MemoryCache
import coil.request.CachePolicy
import com.streamflix.app.notifications.NotificationHelper
import com.streamflix.app.notifications.NewMoviesWorker
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class StreamFlixApp : Application(), Configuration.Provider {

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setMinimumLoggingLevel(Log.INFO)
            .build()

    override fun onCreate() {
        super.onCreate()
        val previousHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            Log.e("StreamFlixCrash", "Uncaught on ${thread.name}", throwable)
            previousHandler?.uncaughtException(thread, throwable)
        }
        // إعداد ImageLoader مخصص - يحل مشكلة بطء تحميل الصور في الصفحة الرئيسية
        // disk cache: 200MB + memory cache: 30% من الذاكرة + تجاهل cache headers من server
        setupImageLoader()
        createNotificationChannels()
        NewMoviesWorker.schedulePeriodicCheck(this)
    }

    private fun setupImageLoader() {
        val imageLoader = ImageLoader.Builder(this)
            .memoryCache {
                MemoryCache.Builder(this)
                    .maxSizePercent(0.3)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(cacheDir.resolve("image_cache"))
                    .maxSizeBytes(200L * 1024 * 1024) // 200 MB
                    .build()
            }
            .crossfade(true)
            .respectCacheHeaders(false) // تجاهل cache-control من server - نخزن محلياً دائماً
            .memoryCachePolicy(CachePolicy.ENABLED)
            .diskCachePolicy(CachePolicy.ENABLED)
            .build()
        Coil.setImageLoader(imageLoader)
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = getSystemService(NotificationManager::class.java)
            val contentChannel = NotificationChannel(
                CHANNEL_CONTENT, "محتوى جديد",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply { description = "إشعارات الأفلام والحلقات الجديدة" }
            val sportsChannel = NotificationChannel(
                CHANNEL_SPORTS, "مباريات مباشرة",
                NotificationManager.IMPORTANCE_HIGH
            ).apply { description = "تنبيهات بداية المباريات" }
            manager.createNotificationChannels(listOf(contentChannel, sportsChannel))
        }
        NotificationHelper.createChannel(this)
    }

    companion object {
        const val CHANNEL_CONTENT = "channel_content"
        const val CHANNEL_SPORTS = "channel_sports"
    }
}
