package com.streamflix.app.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.streamflix.app.domain.model.Movie

/**
 * مساعد الإشعارات - ينشئ قناة الإشعارات + يعرض إشعارات النظام
 * يدعم الإشعارات حتى لو التطبيق مغلق (عبر WorkManager)
 */
object NotificationHelper {
    const val CHANNEL_ID = "streamflix_new_movies"
    private const val CHANNEL_NAME = "أفلام جديدة"
    private const val CHANNEL_DESC = "إشعارات الأفلام والمسلسلات الجديدة"

    /** ينشئ قناة الإشعارات (مطلوب لـ Android 8+) */
    fun createChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = CHANNEL_DESC
                enableVibration(true)
                enableLights(true)
            }
            val manager = context.getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    /** يعرض إشعار فيلم جديد */
    fun showNewMovieNotification(context: Context, movie: Movie) {
        if (!hasNotificationPermission(context)) return

        // Intent لفتح التطبيق على تفاصيل الفيلم عبر deep link
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("streamflix://movie/${movie.id}")).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            movie.id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setContentTitle("🎬 فيلم جديد: ${movie.title}")
            .setContentText(movie.overview.take(80) + if (movie.overview.length > 80) "..." else "")
            .setStyle(NotificationCompat.BigTextStyle().bigText(movie.overview))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(
                movie.id.hashCode(),
                notification
            )
        } catch (_: SecurityException) {
            // لو ما عنده صلاحية إشعارات - ما نسوي شي
        }
    }

    fun hasNotificationPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }
}
