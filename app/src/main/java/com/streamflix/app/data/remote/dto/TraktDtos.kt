package com.streamflix.app.data.remote.dto

import kotlinx.serialization.Serializable

/**
 * Trakt Movie DTO - يحتوي على معلومات الفيلم + ids لخدمات أخرى (TMDB, IMDB)
 */
@Serializable
data class TraktMovieDto(
    val watcher_count: Int? = null,
    val play_count: Int? = null,
    val collected_count: Int? = null,
    val movie: TraktMovieInfo? = null,
    val show: TraktMovieInfo? = null
)

@Serializable
data class TraktMovieInfo(
    val title: String,
    val year: Int? = null,
    val ids: TraktIds? = null,
    val overview: String? = null,
    val runtime: Int? = null,
    val rating: Double? = null,
    val genres: List<String>? = null,
    val tagline: String? = null
)

@Serializable
data class TraktIds(
    val trakt: Int? = null,
    val slug: String? = null,
    val imdb: String? = null,
    val tmdb: Int? = null
)

@Serializable
data class TraktListResponse(
    val rank: Int? = null,
    val listed_at: String? = null,
    val movie: TraktMovieInfo? = null,
    val show: TraktMovieInfo? = null
)
