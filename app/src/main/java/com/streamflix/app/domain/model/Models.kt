package com.streamflix.app.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Movie(
    val id: String,
    val title: String,
    val overview: String = "",
    val posterUrl: String? = null,
    val backdropUrl: String? = null,
    val releaseYear: Int = 0,
    val durationMinutes: Int = 0,
    val rating: Float = 0f,
    val genres: List<String> = emptyList(),
    val quality: String = "HD",
    val cast: List<CastMember> = emptyList(),
    val streamUrl: String? = null,
    val trailerUrl: String? = null,
    val imdbId: String? = null,
    val similarMovies: List<Movie> = emptyList(),
    val originalLanguage: String = "",
    val originCountry: String = "",
    val certification: String = ""
) {
    /** هل الفيلم عربي؟ (لإظهار شارة "مترجم" للغير عربي) */
    val isArabic: Boolean get() = originalLanguage == "ar" || originCountry in arabicCountries
    /** اسم بلد المنشأ بالعربي */
    val countryNameAr: String get() = countryNameFromCode(originCountry)
}

/** قائمة كود الدول العربية */
val arabicCountries = listOf("EG", "SA", "IQ", "AE", "SY", "LB", "JO", "MA", "DZ", "TN", "KW", "QA", "BH", "OM", "YE", "LY", "SD", "PS", "MR")

/** يحول كود البلد (ISO 2) إلى اسم بالعربي */
fun countryNameFromCode(code: String): String = when (code) {
    "EG" -> "مصري"
    "SA" -> "سعودي"
    "IQ" -> "عراقي"
    "AE" -> "إماراتي"
    "SY" -> "سوري"
    "LB" -> "لبناني"
    "JO" -> "أردني"
    "MA" -> "مغربي"
    "DZ" -> "جزائري"
    "TN" -> "تونسي"
    "KW" -> "كويتي"
    "QA" -> "قطري"
    "BH" -> "بحريني"
    "OM" -> "عماني"
    "YE" -> "يمني"
    "LY" -> "ليبي"
    "SD" -> "سوداني"
    "PS" -> "فلسطيني"
    "MR" -> "موريتاني"
    "TR" -> "تركي"
    "IR" -> "إيراني"
    "IN" -> "هندي"
    "KR" -> "كوري"
    "JP" -> "ياباني"
    "CN" -> "صيني"
    "US" -> "أمريكي"
    "GB" -> "بريطاني"
    "FR" -> "فرنسي"
    "ES" -> "إسباني"
    "DE" -> "ألماني"
    "IT" -> "إيطالي"
    "MX" -> "مكسيكي"
    "BR" -> "برازيلي"
    else -> ""
}

@Serializable
data class CastMember(
    val id: String,
    val name: String,
    val character: String = "",
    val photoUrl: String? = null
)

@Serializable
data class Series(
    val id: String,
    val title: String,
    val overview: String = "",
    val posterUrl: String? = null,
    val backdropUrl: String? = null,
    val startYear: Int = 0,
    val rating: Float = 0f,
    val genres: List<String> = emptyList(),
    val seasons: List<Season> = emptyList(),
    val quality: String = "HD",
    val imdbId: String? = null,
    val numberOfSeasons: Int = 0,
    val numberOfEpisodes: Int = 0,
    val cast: List<CastMember> = emptyList(),
    val tmdbId: Int = 0,
    val originalLanguage: String = "",
    val originCountry: String = "",
    val certification: String = ""
) {
    val isArabic: Boolean get() = originalLanguage == "ar" || originCountry in arabicCountries
    /** اسم بلد المنشأ بالعربي */
    val countryNameAr: String get() = countryNameFromCode(originCountry)
}

@Serializable
data class Season(
    val id: String,
    val number: Int,
    val title: String,
    val episodes: List<Episode> = emptyList(),
    val episodeCount: Int = 0,
    val posterUrl: String? = null,
    val airDate: String? = null
)

@Serializable
data class Episode(
    val id: String,
    val number: Int,
    val title: String,
    val overview: String = "",
    val durationMinutes: Int = 0,
    val thumbnailUrl: String? = null,
    val streamUrl: String? = null,
    val airDate: String? = null,
    val seasonNumber: Int = 1
)

@Serializable
data class Anime(
    val id: String,
    val title: String,
    val overview: String = "",
    val posterUrl: String? = null,
    val bannerUrl: String? = null,
    val releaseYear: Int = 0,
    val rating: Float = 0f,
    val genres: List<String> = emptyList(),
    val episodesCount: Int = 0,
    val status: String = "",
    val streamUrl: String? = null
)

@Serializable
data class Match(
    val id: String,
    val homeTeam: String,
    val awayTeam: String,
    val homeScore: Int = 0,
    val awayScore: Int = 0,
    val startTime: Long,
    val status: MatchStatus,
    val league: String = "",
    val homeTeamLogo: String? = null,
    val awayTeamLogo: String? = null,
    val streamUrl: String? = null,
    val minute: Int = 0
)

enum class MatchStatus { LIVE, UPCOMING, FINISHED }

@Serializable
data class Channel(
    val id: String,
    val name: String,
    val logoUrl: String? = null,
    val streamUrl: String,
    val category: String = "Sports",
    val isLive: Boolean = true
)

@Serializable
data class WatchHistory(
    val id: String,
    val contentId: String,
    val title: String,
    val posterUrl: String? = null,
    val contentType: ContentType,
    val progressSeconds: Long = 0,
    val durationSeconds: Long = 0,
    val updatedAt: Long = System.currentTimeMillis()
)

@Serializable
data class FavoriteItem(
    val id: String,
    val contentId: String,
    val title: String,
    val posterUrl: String? = null,
    val contentType: ContentType,
    val addedAt: Long = System.currentTimeMillis()
)

enum class ContentType { MOVIE, SERIES, ANIME, MATCH, CHANNEL }

/**
 * ترجمة (Subtitle) من OpenSubtitles
 * @param language كود اللغة (ara, eng)
 * @param url رابط تحميل الترجمة (gzip)
 * @param fileName اسم ملف الترجمة
 * @param rating تقييم الترجمة (من 0 إلى 10)
 */
@Serializable
data class SubtitleTrack(
    val id: String,
    val language: String,
    val languageName: String,
    val url: String,
    val fileName: String,
    val rating: Float = 0f,
    val isArabic: Boolean = false
)
