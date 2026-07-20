package com.streamflix.app.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class TmdbMoviesResponse(
    val page: Int = 1,
    val results: List<TmdbMovieDto> = emptyList(),
    val total_pages: Int = 0,
    val total_results: Int = 0
)

@Serializable
data class TmdbSeriesResponse(
    val page: Int = 1,
    val results: List<TmdbSeriesDto> = emptyList(),
    val total_pages: Int = 0,
    val total_results: Int = 0
)

@Serializable
data class TmdbMovieDto(
    val id: Int,
    val title: String? = null,
    val name: String? = null,
    val original_title: String? = null,
    val overview: String? = null,
    val poster_path: String? = null,
    val backdrop_path: String? = null,
    val release_date: String? = null,
    val first_air_date: String? = null,
    val vote_average: Double = 0.0,
    val vote_count: Int = 0,
    val genre_ids: List<Int> = emptyList(),
    val popularity: Double = 0.0,
    val original_language: String? = null,
    val origin_country: List<String>? = null,
    val media_type: String? = null,
    val video: Boolean = false,
    val adult: Boolean = false,
    val runtime: Int? = null,
    val imdb_id: String? = null,
    val genres: List<TmdbGenre>? = null,
    val credits: TmdbCredits? = null,
    val videos: TmdbVideos? = null,
    val similar: TmdbMoviesResponse? = null,
    val release_dates: TmdbReleaseDates? = null
) {
    fun resolveTitle(): String = title ?: name ?: original_title ?: "Unknown"
    fun resolveYear(): Int = (release_date ?: first_air_date)?.take(4)?.toIntOrNull() ?: 0
    fun resolveMediaType(): String = media_type ?: if (title != null) "movie" else if (name != null) "tv" else "movie"
    fun resolveGenres(): List<String> = genres?.mapNotNull { it.name } ?: emptyList()
    fun resolveCountry(): String = origin_country?.firstOrNull() ?: ""
    fun resolveCertification(): String {
        // يحاول استخراج التصنيف العمري من release_dates (US أولاً)
        return release_dates?.results?.firstOrNull { it.iso_3166_1 == "US" }
            ?.release_dates?.firstOrNull()?.certification?.takeIf { it.isNotBlank() }
            ?: release_dates?.results?.firstOrNull()?.release_dates?.firstOrNull()?.certification?.takeIf { it.isNotBlank() }
            ?: ""
    }
}

@Serializable
data class TmdbReleaseDates(
    val results: List<TmdbReleaseDateResult> = emptyList()
)

@Serializable
data class TmdbReleaseDateResult(
    val iso_3166_1: String = "",
    val release_dates: List<TmdbReleaseDateItem> = emptyList()
)

@Serializable
data class TmdbReleaseDateItem(
    val certification: String? = null,
    val release_date: String? = null,
    val type: Int = 0
)

@Serializable
data class TmdbGenre(
    val id: Int,
    val name: String
)

@Serializable
data class TmdbSeriesDto(
    val id: Int,
    val name: String,
    val original_name: String? = null,
    val overview: String? = null,
    val poster_path: String? = null,
    val backdrop_path: String? = null,
    val first_air_date: String? = null,
    val last_air_date: String? = null,
    val vote_average: Double = 0.0,
    val vote_count: Int = 0,
    val genre_ids: List<Int> = emptyList(),
    val popularity: Double = 0.0,
    val origin_country: List<String> = emptyList(),
    val original_language: String? = null,
    val number_of_seasons: Int? = null,
    val number_of_episodes: Int? = null,
    val episode_run_time: List<Int> = emptyList(),
    val genres: List<TmdbGenre> = emptyList(),
    val seasons: List<TmdbSeason> = emptyList(),
    val credits: TmdbCredits? = null,
    val videos: TmdbVideos? = null,
    val similar: TmdbSeriesResponse? = null,
    val external_ids: TmdbExternalIds? = null,
    val media_type: String? = null
)

@Serializable
data class TmdbExternalIds(
    val imdb_id: String? = null,
    val tvdb_id: Int? = null,
    val facebook_id: String? = null,
    val instagram_id: String? = null,
    val twitter_id: String? = null
)

@Serializable
data class TmdbSeasonDetails(
    val id: Int? = null,
    val name: String? = null,
    val overview: String? = null,
    val poster_path: String? = null,
    val season_number: Int = 1,
    val air_date: String? = null,
    val episodes: List<TmdbEpisode> = emptyList()
)

@Serializable
data class TmdbEpisode(
    val id: Int,
    val name: String,
    val overview: String? = null,
    val episode_number: Int,
    val season_number: Int = 1,
    val air_date: String? = null,
    val runtime: Int? = null,
    val still_path: String? = null,
    val vote_average: Double = 0.0
) {
    fun resolveYear(): Int = air_date?.take(4)?.toIntOrNull() ?: 0
}

@Serializable
data class TmdbSeason(
    val id: Int,
    val name: String,
    val season_number: Int,
    val episode_count: Int = 0,
    val overview: String? = null,
    val poster_path: String? = null,
    val air_date: String? = null
)

@Serializable
data class TmdbCredits(
    val cast: List<TmdbCast> = emptyList()
)

@Serializable
data class TmdbCast(
    val id: Int,
    val name: String,
    val character: String? = null,
    val profile_path: String? = null,
    val order: Int = 0
)

@Serializable
data class TmdbVideos(
    val results: List<TmdbVideo> = emptyList()
)

@Serializable
data class TmdbVideo(
    val id: String,
    val key: String, // YouTube video ID
    val name: String,
    val site: String = "YouTube",
    val type: String? = null,
    val official: Boolean = false
)
