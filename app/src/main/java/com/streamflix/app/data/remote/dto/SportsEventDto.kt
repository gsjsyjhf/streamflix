package com.streamflix.app.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class SportsEventDto(
    val idEvent: String,
    val strEvent: String? = null,        // "Barcelona vs Real Madrid"
    val strEventAlternate: String? = null,
    val strSport: String? = null,
    val idLeague: String? = null,
    val strLeague: String? = null,
    val strLeagueBadge: String? = null,
    val strSeason: String? = null,
    val strDescriptionEN: String? = null,
    val strHomeTeam: String? = null,
    val strAwayTeam: String? = null,
    val intHomeScore: String? = null,
    val intAwayScore: String? = null,
    val intRound: String? = null,
    val intSpectators: String? = null,
    val strOfficial: String? = null,
    val strTimestamp: String? = null,    // ISO 8601
    val dateEvent: String? = null,        // YYYY-MM-DD
    val dateEventLocal: String? = null,
    val strTime: String? = null,
    val strTimeLocal: String? = null,
    val strTVStation: String? = null,
    val idHomeTeam: String? = null,
    val idAwayTeam: String? = null,
    val strResult: String? = null,
    val strVenue: String? = null,
    val strCountry: String? = null,
    val strCity: String? = null,
    val strPoster: String? = null,
    val strThumb: String? = null,
    val strBanner: String? = null,
    val strMap: String? = null,
    val strTweet1: String? = null,
    val strVideo: String? = null,
    val strStatus: String? = null,        // "Match Finished", "1H", "2H", "Not Started"
    val strPostponed: String? = null,
    val strLocked: String? = null
)
