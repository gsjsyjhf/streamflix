package com.streamflix.app.data.remote.api

import com.streamflix.app.data.remote.dto.OpenSubsResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * OpenSubtitles REST API - ترجمات مجانية
 * مجاني بدون مفتاح API - يحتاج فقط User-Agent مخصص
 * Endpoints: https://trac.opensubtitles.org/projects/opensubtitles/wiki/DevReadXMLRPC
 */
interface OpenSubtitlesApi {

    /** بحث عن ترجمات بـ IMDB ID (للأفلام) */
    @GET("search/imdbid-{imdbId}")
    suspend fun searchByImdb(
        @Path("imdbId") imdbId: String,
        @Query("sublanguageid") language: String = "ara,eng"
    ): OpenSubsResponse

    /** بحث عن ترجمات لكل حلقة مسلسل */
    @GET("search/episode-{episode}/season-{season}/imdbid-{imdbId}")
    suspend fun searchEpisode(
        @Path("imdbId") imdbId: String,
        @Path("season") season: Int,
        @Path("episode") episode: Int,
        @Query("sublanguageid") language: String = "ara,eng"
    ): OpenSubsResponse

    /** بحث بالاسم (fallback) */
    @GET("search/query-{query}")
    suspend fun searchByQuery(
        @Path("query") query: String,
        @Query("sublanguageid") language: String = "ara,eng"
    ): OpenSubsResponse

    companion object {
        const val BASE_URL = "https://rest.opensubtitles.org/"
        // User-Agent مطلوب من OpenSubtitles (مسجل رسمياً)
        const val USER_AGENT = "StreamFlix/v2.7.6 (https://streamflix.app)"
    }
}
