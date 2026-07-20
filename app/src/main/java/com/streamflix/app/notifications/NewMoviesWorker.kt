package com.streamflix.app.notifications

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.streamflix.app.data.remote.api.TmdbApi
import com.streamflix.app.data.remote.dto.TmdbMovieDto
import com.streamflix.app.domain.model.Movie
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit

/**
 * Worker دوري - يفحص كل 12 ساعة الأفلام الجديدة
 * لو في فيلم جديد ما شافه المستخدم قبل، يعرض إشعار
 */
class NewMoviesWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            val tmdbApi = createTmdbApi()
            val nowPlaying = runCatching {
                tmdbApi.getNowPlayingMovies().results.map { it.toSimpleMovie() }
            }.getOrDefault(emptyList())

            nowPlaying.firstOrNull()?.let { movie ->
                val prefs = applicationContext.getSharedPreferences("streamflix_notifs", Context.MODE_PRIVATE)
                val lastNotifiedId = prefs.getString("last_notified_movie_id", null)
                if (lastNotifiedId != movie.id) {
                    NotificationHelper.showNewMovieNotification(applicationContext, movie)
                    prefs.edit().putString("last_notified_movie_id", movie.id).apply()
                }
            }
            Result.success()
        } catch (_: Exception) {
            Result.retry()
        }
    }

    private fun createTmdbApi(): TmdbApi {
        val json = kotlinx.serialization.json.Json {
            ignoreUnknownKeys = true
            coerceInputValues = true
            explicitNulls = false
            isLenient = true
        }
        val client = OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .build()
        val retrofit = Retrofit.Builder()
            .baseUrl(TmdbApi.BASE_URL)
            .client(client)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
        return retrofit.create(TmdbApi::class.java)
    }

    private fun TmdbMovieDto.toSimpleMovie(): Movie {
        return Movie(
            id = "tmdb_movie_$id",
            title = resolveTitle(),
            overview = overview ?: "",
            posterUrl = TmdbApi.posterUrl(poster_path),
            backdropUrl = TmdbApi.backdropUrl(backdrop_path),
            releaseYear = resolveYear(),
            rating = vote_average.toFloat(),
            streamUrl = "https://vidsrc.in/embed/movie/$id"
        )
    }

    companion object {
        private const val WORK_NAME = "streamflix_new_movies_check"

        /** يبدأ الفحص الدوري كل 12 ساعة */
        fun schedulePeriodicCheck(context: Context) {
            val request = PeriodicWorkRequestBuilder<NewMoviesWorker>(
                12, TimeUnit.HOURS
            ).build()
            WorkManager.getInstance(context).enqueue(request)
        }
    }
}
