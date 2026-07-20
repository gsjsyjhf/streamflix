package com.streamflix.app.data.remote.dto.bein

import kotlinx.serialization.Serializable

@Serializable
data class BeinMatchesResponse(
    val matches: List<BeinMatchDto>? = null,
    val updated: String? = null
)

@Serializable
data class BeinMatchDto(
    val team1: String? = null,
    val team2: String? = null,
    val time: String? = null,
    val date: String? = null,
    val live: Boolean? = null,
    val league: String? = null,
    val league_slug: String? = null,
    val channel_id: String? = null,
    val channel: String? = null,
    val priority: String? = null
)

@Serializable
data class BeinChannelStreamResponse(
    val ok: Boolean? = null,
    val channel: String? = null,
    val expires_in_seconds: Int? = null,
    val expires_at: Long? = null,
    val stream_url: String? = null
)
