package com.streamflix.app.data.remote.api

import com.streamflix.app.data.remote.dto.bein.BeinChannelStreamResponse
import com.streamflix.app.data.remote.dto.bein.BeinMatchesResponse
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * BEIN Sports API - adam-bein.vercel.app
 * مجاني 100% بدون مفتاح - يوفر:
 *  - 12 قناة BEIN Sports بث مباشر (6 عادية + 6 MAX)
 *  - قائمة المباريات القادمة مع تخصيص القناة لكل مباراة
 *
 * ملاحظة: شعار BEIN Sports محمّل محلياً في res/drawable/bein_sports_logo.png
 */
interface BeinApi {

    @GET("api/matches")
    suspend fun getMatches(): BeinMatchesResponse

    @GET("api/channel")
    suspend fun getChannelStream(
        @Query("ch") channelId: String,
        @Query("q") quality: String = "720"
    ): BeinChannelStreamResponse

    companion object {
        const val BASE_URL = "https://adam-bein.vercel.app/"

        val CHANNELS = listOf(
            BeinChannelDef("beee1", "BEIN Sports 1", "1", "bein"),
            BeinChannelDef("beee2", "BEIN Sports 2", "2", "bein"),
            BeinChannelDef("beee3", "BEIN Sports 3", "3", "bein"),
            BeinChannelDef("beee4", "BEIN Sports 4", "4", "bein"),
            BeinChannelDef("beee5", "BEIN Sports 5", "5", "bein"),
            BeinChannelDef("beee6", "BEIN Sports 6", "6", "bein"),
            BeinChannelDef("beemax1", "BEIN MAX 1", "M1", "max"),
            BeinChannelDef("beemax2", "BEIN MAX 2", "M2", "max"),
            BeinChannelDef("beemax3", "BEIN MAX 3", "M3", "max"),
            BeinChannelDef("beemax4", "BEIN MAX 4", "M4", "max"),
            BeinChannelDef("beemax5", "BEIN MAX 5", "M5", "max"),
            BeinChannelDef("beemax6", "BEIN MAX 6", "M6", "max")
        )
    }
}

data class BeinChannelDef(
    val id: String,
    val name: String,
    val iconLabel: String,
    val group: String
)
