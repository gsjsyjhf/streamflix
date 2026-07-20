package com.streamflix.app.data.remote.dto

import kotlinx.serialization.Serializable

/**
 * OpenSubtitles XML/JSON response - نستخدم JSON لأنه أسرع
 * Sample: <http://rest.opensubtitles.org/search/imdbid-tt0111161>
 */
@Serializable
data class OpenSubsResponse(
    val data: List<OpenSubEntry>? = null
)

@Serializable
data class OpenSubEntry(
    val MovieName: String? = null,
    val MovieReleaseName: String? = null,
    val MovieYear: String? = null,
    val MovieImdbRating: String? = null,
    val SubFileName: String? = null,
    val SubFileGUID: String? = null,
    val SubLanguageID: String? = null,
    val SubDownloadLink: String? = null,
    val SubRating: String? = null,
    val SubBad: String? = null,
    val SeriesSeason: String? = null,
    val SeriesEpisode: String? = null,
    val IDSubtitleFile: String? = null
)
