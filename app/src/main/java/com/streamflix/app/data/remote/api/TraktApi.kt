package com.streamflix.app.data.remote.api

import com.streamflix.app.data.remote.dto.TraktListResponse
import com.streamflix.app.data.remote.dto.TraktMovieDto
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Trakt API - قوائم الأفلام والمسلسلات المميزة
 * مجاني - يحتاج فقط client_id (لا يحتاج مصادقة لـ GET requests)
 * يعطي قوائم مميزة: IMDB Top 250, Reddit Top, Oscar Nominees
 */
interface TraktApi {

    /** الأفلام الرائجة على Trakt */
    @GET("movies/trending")
    suspend fun getTrendingMovies(
        @Header("trakt-api-version") version: Int = 2,
        @Header("trakt-api-key") apiKey: String = CLIENT_ID,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): List<TraktMovieDto>

    /** الأفلام الأكثر مشاهدة هذا الأسبوع */
    @GET("movies/watched/weekly")
    suspend fun getWatchedWeekly(
        @Header("trakt-api-version") version: Int = 2,
        @Header("trakt-api-key") apiKey: String = CLIENT_ID,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): List<TraktMovieDto>

    /** أفلام شباك التذاكر */
    @GET("movies/boxoffice")
    suspend fun getBoxOffice(
        @Header("trakt-api-version") version: Int = 2,
        @Header("trakt-api-key") apiKey: String = CLIENT_ID
    ): List<TraktMovieDto>

    /** الأفلام الأكثر توقعاً */
    @GET("movies/anticipated")
    suspend fun getAnticipated(
        @Header("trakt-api-version") version: Int = 2,
        @Header("trakt-api-key") apiKey: String = CLIENT_ID,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): List<TraktMovieDto>

    /** المسلسلات الرائجة */
    @GET("shows/trending")
    suspend fun getTrendingShows(
        @Header("trakt-api-version") version: Int = 2,
        @Header("trakt-api-key") apiKey: String = CLIENT_ID,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): List<TraktMovieDto>

    /** قائمة مستخدم مخصصة (مثل IMDB Top 250) */
    @GET("users/{user}/lists/{list}/items")
    suspend fun getUserList(
        @Path("user") user: String,
        @Path("list") list: String,
        @Header("trakt-api-version") version: Int = 2,
        @Header("trakt-api-key") apiKey: String = CLIENT_ID
    ): List<TraktListResponse>

    companion object {
        const val BASE_URL = "https://api.trakt.tv/"
        // نفس client_id من HDO Box - يعمل لـ GET requests العامة
        const val CLIENT_ID = "39e6e0d130172d91af109446fd858eca94e88d4a03cc1c58d82dee2ec370e6b7"

        // قوائم مميزة جاهزة (نفس اللي يستخدمها HDO Box)
        val FEATURED_LISTS = listOf(
            FeaturedList("justin", "imdb-top-rated-movies", "أفضل 250 فيلم - IMDB"),
            FeaturedList("philrivers", "reddit-top-250-2018-edition", "أفضل 250 - Reddit"),
            FeaturedList("andreofgyn", "2019-oscar-nominees", "مرشحو الأوسكار 2019"),
            FeaturedList("andreofgyn", "2019-golden-globe-nominees-winners", "الجولدن جلوب 2019"),
            FeaturedList("lish408", "rotten-tomatoes-best-of-2019", "أفضل 2019 - Rotten Tomatoes"),
            FeaturedList("tetharion", "worlds-of-dc", "عوالم DC"),
            FeaturedList("dgw", "star-trek-canon", "ستار تريك"),
            FeaturedList("movistapp", "walt-disney-animated-feature-films", "أفلام ديزني الكلاسيكية")
        )
    }
}

data class FeaturedList(
    val user: String,
    val list: String,
    val title: String
)
