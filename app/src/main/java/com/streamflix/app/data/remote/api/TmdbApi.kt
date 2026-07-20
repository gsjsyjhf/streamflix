package com.streamflix.app.data.remote.api

import com.streamflix.app.data.remote.dto.TmdbMovieDto
import com.streamflix.app.data.remote.dto.TmdbMoviesResponse
import com.streamflix.app.data.remote.dto.TmdbSeasonDetails
import com.streamflix.app.data.remote.dto.TmdbSeriesDto
import com.streamflix.app.data.remote.dto.TmdbSeriesResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * TMDB API - مجاني 100% مع API key مجاني
 * يعطي أفلام ومسلسلات حقيقية بصور حقيقية شغّالة
 * سجل API key مجاني من: https://www.themoviedb.org/settings/api
 */
interface TmdbApi {
    /** الأفلام الرائجة حالياً */
    @GET("movie/popular")
    suspend fun getPopularMovies(
        @Query("api_key") apiKey: String = API_KEY,
        @Query("language") language: String = "ar",
        @Query("page") page: Int = 1
    ): TmdbMoviesResponse

    /** أفلام حسب النوع (لـ infinite scroll) */
    @GET("discover/movie")
    suspend fun discoverMovies(
        @Query("api_key") apiKey: String = API_KEY,
        @Query("language") language: String = "ar",
        @Query("page") page: Int = 1,
        @Query("sort_by") sortBy: String = "popularity.desc",
        @Query("include_adult") includeAdult: Boolean = false
    ): TmdbMoviesResponse

    /** أفلام عربية - مع لغة عربية للأسماء والأوصاف */
    @GET("discover/movie")
    suspend fun discoverArabicMovies(
        @Query("api_key") apiKey: String = API_KEY,
        @Query("language") language: String = "ar",
        @Query("page") page: Int = 1,
        @Query("with_original_language") originalLanguage: String = "ar",
        @Query("sort_by") sortBy: String = "popularity.desc",
        @Query("include_adult") includeAdult: Boolean = false
    ): TmdbMoviesResponse

    /** أنمي - أفلام الرسوم المتحركة (genre 16) */
    @GET("discover/movie")
    suspend fun discoverAnime(
        @Query("api_key") apiKey: String = API_KEY,
        @Query("language") language: String = "ar",
        @Query("page") page: Int = 1,
        @Query("with_genres") genres: String = "16",
        @Query("sort_by") sortBy: String = "popularity.desc",
        @Query("include_adult") includeAdult: Boolean = false
    ): TmdbMoviesResponse

    /** مسلسلات عربية */
    @GET("discover/tv")
    suspend fun discoverArabicSeries(
        @Query("api_key") apiKey: String = API_KEY,
        @Query("language") language: String = "ar",
        @Query("page") page: Int = 1,
        @Query("with_original_language") originalLanguage: String = "ar",
        @Query("sort_by") sortBy: String = "popularity.desc",
        @Query("include_adult") includeAdult: Boolean = false
    ): TmdbSeriesResponse

    /** مسلسلات أنمي */
    @GET("discover/tv")
    suspend fun discoverAnimeSeries(
        @Query("api_key") apiKey: String = API_KEY,
        @Query("language") language: String = "ar",
        @Query("page") page: Int = 1,
        @Query("with_genres") genres: String = "16",
        @Query("sort_by") sortBy: String = "popularity.desc",
        @Query("include_adult") includeAdult: Boolean = false
    ): TmdbSeriesResponse

    /** أفلام حسب لغة المنشأ (عام - يستخدم لأي دولة) */
    @GET("discover/movie")
    suspend fun discoverMoviesByLanguage(
        @Query("api_key") apiKey: String = API_KEY,
        @Query("language") language: String = "ar",
        @Query("page") page: Int = 1,
        @Query("with_original_language") originalLanguage: String,
        @Query("sort_by") sortBy: String = "popularity.desc",
        @Query("include_adult") includeAdult: Boolean = false
    ): TmdbMoviesResponse

    /** مسلسلات حسب لغة المنشأ (عام) */
    @GET("discover/tv")
    suspend fun discoverSeriesByLanguage(
        @Query("api_key") apiKey: String = API_KEY,
        @Query("language") language: String = "ar",
        @Query("page") page: Int = 1,
        @Query("with_original_language") originalLanguage: String,
        @Query("sort_by") sortBy: String = "popularity.desc",
        @Query("include_adult") includeAdult: Boolean = false
    ): TmdbSeriesResponse

    /** أفلام حسب بلد المنشأ (ISO 3166-1 code: TR, KR, JP, IN, US, GB, FR, ES, DE) */
    @GET("discover/movie")
    suspend fun discoverMoviesByCountry(
        @Query("api_key") apiKey: String = API_KEY,
        @Query("language") language: String = "ar",
        @Query("page") page: Int = 1,
        @Query("with_origin_country") originCountry: String,
        @Query("sort_by") sortBy: String = "popularity.desc",
        @Query("include_adult") includeAdult: Boolean = false
    ): TmdbMoviesResponse

    /** مسلسلات حسب بلد المنشأ */
    @GET("discover/tv")
    suspend fun discoverSeriesByCountry(
        @Query("api_key") apiKey: String = API_KEY,
        @Query("language") language: String = "ar",
        @Query("page") page: Int = 1,
        @Query("with_origin_country") originCountry: String,
        @Query("sort_by") sortBy: String = "popularity.desc",
        @Query("include_adult") includeAdult: Boolean = false
    ): TmdbSeriesResponse

    /** الأفلام الأعلى تقييماً */
    @GET("movie/top_rated")
    suspend fun getTopRatedMovies(
        @Query("api_key") apiKey: String = API_KEY,
        @Query("language") language: String = "ar",
        @Query("page") page: Int = 1
    ): TmdbMoviesResponse

    /** الأفلام التي تُعرض حالياً في السينما */
    @GET("movie/now_playing")
    suspend fun getNowPlayingMovies(
        @Query("api_key") apiKey: String = API_KEY,
        @Query("language") language: String = "ar",
        @Query("page") page: Int = 1
    ): TmdbMoviesResponse

    /** الأفلام القادمة */
    @GET("movie/upcoming")
    suspend fun getUpcomingMovies(
        @Query("api_key") apiKey: String = API_KEY,
        @Query("language") language: String = "ar",
        @Query("page") page: Int = 1
    ): TmdbMoviesResponse

    /** المسلسلات الرائجة */
    @GET("tv/popular")
    suspend fun getPopularSeries(
        @Query("api_key") apiKey: String = API_KEY,
        @Query("language") language: String = "ar",
        @Query("page") page: Int = 1
    ): TmdbSeriesResponse

    /** المسلسلات الأعلى تقييماً */
    @GET("tv/top_rated")
    suspend fun getTopRatedSeries(
        @Query("api_key") apiKey: String = API_KEY,
        @Query("language") language: String = "ar",
        @Query("page") page: Int = 1
    ): TmdbSeriesResponse

    /** تفاصيل فيلم + cast + تصنيف عمري */
    @GET("movie/{id}")
    suspend fun getMovieDetails(
        @Path("id") id: Int,
        @Query("api_key") apiKey: String = API_KEY,
        @Query("language") language: String = "ar",
        @Query("append_to_response") append: String = "credits,videos,similar,release_dates"
    ): TmdbMovieDto

    /** تفاصيل مسلسل كاملة + المواسم + cast + similar */
    @GET("tv/{id}")
    suspend fun getTvDetails(
        @Path("id") id: Int,
        @Query("api_key") apiKey: String = API_KEY,
        @Query("language") language: String = "ar",
        @Query("append_to_response") append: String = "credits,videos,similar,external_ids"
    ): TmdbSeriesDto

    /** تفاصيل موسم معين - يعطي قائمة الحلقات الكاملة */
    @GET("tv/{tv_id}/season/{season_number}")
    suspend fun getSeasonDetails(
        @Path("tv_id") tvId: Int,
        @Path("season_number") seasonNumber: Int,
        @Query("api_key") apiKey: String = API_KEY,
        @Query("language") language: String = "ar"
    ): TmdbSeasonDetails

    /** البحث عن أفلام ومسلسلات */
    @GET("search/multi")
    suspend fun searchMulti(
        @Query("api_key") apiKey: String = API_KEY,
        @Query("language") language: String = "ar",
        @Query("query") query: String,
        @Query("page") page: Int = 1
    ): TmdbMoviesResponse

    companion object {
        const val BASE_URL = "https://api.themoviedb.org/3/"
        const val API_KEY = "8265bd1679663a7ea12ac168da84d2e8" // demo public key
        const val IMAGE_BASE = "https://image.tmdb.org/t/p/"
        fun posterUrl(path: String?) = if (path.isNullOrBlank()) null else "${IMAGE_BASE}w500$path"
        fun backdropUrl(path: String?) = if (path.isNullOrBlank()) null else "${IMAGE_BASE}w780$path"
        fun profileUrl(path: String?) = if (path.isNullOrBlank()) null else "${IMAGE_BASE}w185$path"
    }
}
