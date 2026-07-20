package com.streamflix.app.data.remote.api

import com.streamflix.app.data.remote.dto.SportsEventDto
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * TheSportsDB API - مجاني 100% (API Key = 3 للتجربة)
 * يعطي مباريات ونتائج حقيقية بتاريخ اليوم
 */
interface SportsApi {
    /** مباريات اليوم حسب الرياضة */
    @GET("eventsday.php")
    suspend fun getEventsForDay(
        @Query("d") date: String, // YYYY-MM-DD
        @Query("s") sport: String = "Soccer"
    ): SportsResponse

    /** آخر نتائج دوري معين (مثلاً الدوري الإنجليزي = 4328) */
    @GET("eventspastleague.php")
    suspend fun getPastLeagueEvents(
        @Query("id") leagueId: Long = 4328
    ): SportsResponse

    /** مباريات قادمة في دوري معين */
    @GET("eventsnextleague.php")
    suspend fun getNextLeagueEvents(
        @Query("id") leagueId: Long = 4328
    ): SportsResponse

    companion object {
        const val BASE_URL = "https://www.thesportsdb.com/api/v1/json/3/"
        const val API_KEY = "3" // free public test key
    }
}

data class SportsResponse(
    val events: List<SportsEventDto>? = null
)
