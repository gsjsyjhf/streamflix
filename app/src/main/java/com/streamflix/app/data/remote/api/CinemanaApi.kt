package com.streamflix.app.data.remote.api

import com.streamflix.app.data.remote.dto.cinemana.CinemanaHomeGroups
import com.streamflix.app.data.remote.dto.cinemana.CinemanaTranscodeFile
import com.streamflix.app.data.remote.dto.cinemana.CinemanaVideoModel
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import java.util.concurrent.TimeUnit

/**
 * Cinemana API - شبكتي (Earthlink) العراق
 * Base URL: https://cnth2.shabakaty.com/
 * (بدون /api/android/ - المسارات مباشرة من جذر الموقع)
 */
interface CinemanaApi {

    @retrofit2.http.GET("allVideoInfo/id/{videoNb}")
    suspend fun getVideoInfo(
        @retrofit2.http.Path("videoNb") videoNb: String
    ): CinemanaVideoModel

    @retrofit2.http.GET("transcoddedFiles/id/{videoNb}")
    suspend fun getTranscodedFiles(
        @retrofit2.http.Path("videoNb") videoNb: String
    ): List<CinemanaTranscodeFile>

    @retrofit2.http.GET("videoSeason/id/{rootEpisodeId}")
    suspend fun getVideoSeason(
        @retrofit2.http.Path("rootEpisodeId") rootEpisodeId: String
    ): List<CinemanaVideoModel>

    @retrofit2.http.GET("videoGroups/lang/{language}/level/{parentalLevel}")
    suspend fun getHomeGroups(
        @retrofit2.http.Path("language") language: String = "ar",
        @retrofit2.http.Path("parentalLevel") parentalLevel: String = "1"
    ): CinemanaHomeGroups

    @retrofit2.http.GET("videoListPagination")
    suspend fun getVideoListPagination(
        @retrofit2.http.Query("groupID") groupId: String = "0",
        @retrofit2.http.Query("level") level: String = "1",
        @retrofit2.http.Query("itemsPerPage") itemsPerPage: String = "20",
        @retrofit2.http.Query("page") page: Int = 1
    ): List<CinemanaVideoModel>

    @retrofit2.http.GET("newlyVideosItems/level/{parentalLevel}/offset/12/")
    suspend fun getNewlyVideos(
        @retrofit2.http.Path("parentalLevel") parentalLevel: String = "1"
    ): List<CinemanaVideoModel>

    @retrofit2.http.GET("AdvancedSearch")
    suspend fun search(
        @retrofit2.http.Query("videoTitle") videoTitle: String? = null,
        @retrofit2.http.Query("page") page: Int = 1,
        @retrofit2.http.Query("level") level: String = "1"
    ): List<CinemanaVideoModel>

    companion object {
        // Try multiple base URLs - Cinemana uses different servers
        val BASE_URLS = listOf(
            "https://cnth2.shabakaty.com/",
            "https://cinemana.shabakaty.com/",
            "http://cnth2.shabakaty.com/"
        )

        fun create(): CinemanaApi {
            val client = OkHttpClient.Builder()
                .connectTimeout(20, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .addInterceptor(HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.HEADERS
                })
                .addInterceptor { chain ->
                    val request = chain.request().newBuilder()
                        .header("User-Agent", "Cinemana/3.1.2 (Linux; Android 13; com.cee.vod)")
                        .header("Accept", "application/json, text/plain, */*")
                        .header("Accept-Language", "ar,en;q=0.9")
                        .header("Accept-Encoding", "gzip, deflate, br")
                        .header("Connection", "keep-alive")
                        .build()
                    chain.proceed(request)
                }
                .retryOnConnectionFailure(true)
                .build()

            val json = Json {
                ignoreUnknownKeys = true
                coerceInputValues = true
                explicitNulls = false
                isLenient = true
            }

            return Retrofit.Builder()
                .baseUrl(BASE_URLS[0]) // Primary URL
                .client(client)
                .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
                .build()
                .create(CinemanaApi::class.java)
        }
    }
}
