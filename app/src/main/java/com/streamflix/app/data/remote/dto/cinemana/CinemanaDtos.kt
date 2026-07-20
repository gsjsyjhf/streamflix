package com.streamflix.app.data.remote.dto.cinemana

import kotlinx.serialization.Serializable

@Serializable
data class CinemanaCategory(
    val id: String? = null,
    val categoryName: String? = null,
    val categoryArabicName: String? = null,
    val categoryEnglishName: String? = null,
    val categoryFrenchName: String? = null,
    val level: String? = null
)

@Serializable
data class CinemanaVideoModel(
    val id: String? = null,
    val videoNb: String? = null,
    val enTitle: String? = null,
    val arTitle: String? = null,
    val customEnTitle: String? = null,
    val customArTitle: String? = null,
    val enContent: String? = null,
    val arContent: String? = null,
    val rating: String? = null,
    val ratingLevel: String? = null,
    val duration: String? = null,
    val year: String? = null,
    val videoImg: String? = null,
    val videoImgThumb: String? = null,
    val backdrop: String? = null,
    val episodeFlag: String? = null,
    val episodeNummer: String? = null,
    val seasonNumber: String? = null,
    val rootEpisode: String? = null,
    val categories: List<CinemanaCategory>? = null,
    val actorsInfo: List<StaffsInfo>? = null,
    val directorsInfo: List<StaffsInfo>? = null,
    val castable: String? = null,
    val arTranslationFilePath: String? = null,
    val enTranslationFilePath: String? = null
) {
    fun resolveTitle(): String = customArTitle?.takeIf { it.isNotBlank() }
        ?: arTitle?.takeIf { it.isNotBlank() }
        ?: customEnTitle?.takeIf { it.isNotBlank() }
        ?: enTitle ?: "Unknown"

    fun resolvePosterUrl(): String? = when {
        !videoImg.isNullOrBlank() -> videoImg
        !backdrop.isNullOrBlank() -> backdrop
        !videoImgThumb.isNullOrBlank() -> videoImgThumb
        else -> null
    }

    fun resolveOverview(): String = arContent?.takeIf { it.isNotBlank() }
        ?: enContent ?: ""
}

@Serializable
data class StaffsInfo(
    val id: String? = null,
    val name: String? = null,
    val role: String? = null,
    val imgObjUrl: String? = null,
    val imgThumbObjUrl: String? = null,
    val imgMediumThumbObjUrl: String? = null,
    val title: String? = null
)

@Serializable
data class CinemanaTranscodeFile(
    val id: String? = null,
    val name: String? = null,
    val resolution: String? = null,
    val container: String? = null,
    val transcoddedFileName: String? = null,
    val videoUrl: String? = null,
    val size: String? = null,
    val audioIdx: String? = null,
    val duration: String? = null
) {
    fun resolveStreamUrl(): String? = videoUrl?.takeIf { it.isNotBlank() }
}

@Serializable
data class CinemanaHomeGroups(
    val groups: List<CinemanaGroup>? = null
)

@Serializable
data class CinemanaGroup(
    val id: String? = null,
    val groupName: String? = null,
    val groupArabicName: String? = null,
    val groupEnglishName: String? = null,
    val videos: List<CinemanaVideoModel>? = null
)
