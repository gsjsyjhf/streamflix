package com.streamflix.app.data.repository

import com.streamflix.app.data.local.dao.FavoriteDao
import com.streamflix.app.data.local.dao.LastClickedSearchDao
import com.streamflix.app.data.local.dao.SearchHistoryDao
import com.streamflix.app.data.local.dao.WatchHistoryDao
import com.streamflix.app.data.local.entity.FavoriteEntity
import com.streamflix.app.data.local.entity.LastClickedSearchEntity
import com.streamflix.app.data.local.entity.SearchHistoryEntity
import com.streamflix.app.data.local.entity.WatchHistoryEntity
import com.streamflix.app.data.remote.api.BeinApi
import com.streamflix.app.data.remote.api.BeinChannelDef
import com.streamflix.app.data.remote.api.CinemanaApi
import com.streamflix.app.data.remote.api.OpenSubtitlesApi
import com.streamflix.app.data.remote.api.SportsApi
import com.streamflix.app.data.remote.api.TmdbApi
import com.streamflix.app.data.remote.api.TraktApi
import com.streamflix.app.data.remote.api.TraktApi.Companion.FEATURED_LISTS
import com.streamflix.app.data.remote.dto.OpenSubEntry
import com.streamflix.app.data.remote.dto.SportsEventDto
import com.streamflix.app.data.remote.dto.bein.BeinChannelStreamResponse
import com.streamflix.app.data.remote.dto.bein.BeinMatchDto
import com.streamflix.app.data.remote.dto.cinemana.CinemanaVideoModel
import com.streamflix.app.domain.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository موحد - كل شيء عبر الإنترنت فقط
 * TMDB + Trakt (قوائم مميزة) + OpenSubtitles (ترجمات) + SportsDB
 */
@Singleton
class StreamFlixRepository @Inject constructor(
    private val sportsApi: SportsApi,
    private val beinApi: BeinApi,
    private val tmdbApi: TmdbApi,
    private val cinemanaApi: CinemanaApi,
    private val traktApi: TraktApi,
    private val openSubtitlesApi: OpenSubtitlesApi,
    private val favoriteDao: FavoriteDao,
    private val watchHistoryDao: WatchHistoryDao,
    private val searchHistoryDao: SearchHistoryDao,
    private val lastClickedSearchDao: LastClickedSearchDao
) {
    /** 12 قناة BEIN Sports */
    fun getBeinChannels(): List<BeinChannelDef> = BeinApi.CHANNELS

    /** إيجاد قناة BEIN بـ id */
    fun findBeinChannel(channelId: String): BeinChannelDef? =
        BeinApi.CHANNELS.firstOrNull { it.id == channelId }

    /** قائمة المباريات القادمة من BEIN API */
    suspend fun getBeinMatches(): List<BeinMatchDto> = runCatching {
        beinApi.getMatches().matches ?: emptyList()
    }.getOrDefault(emptyList())

    /** رابط بث القناة المباشر (صالح لمدة 10 دقائق) */
    suspend fun getBeinChannelStream(channelId: String, quality: String = "720"): BeinChannelStreamResponse? = runCatching {
        beinApi.getChannelStream(channelId, quality)
    }.getOrNull()

    /**
     * أفلام رائجة - TMDB (صور حقيقية + معلومات كاملة)
     * كل فيلم له YouTube trailer كرابط بث
     */
    suspend fun getTrendingMovies(): List<Movie> = runCatching {
        tmdbApi.getNowPlayingMovies().results.map { it.toMovie() }
    }.getOrElse { emptyList() }

    suspend fun getPopularMovies(): List<Movie> = runCatching {
        tmdbApi.getPopularMovies().results.map { it.toMovie() }
    }.getOrElse { emptyList() }

    suspend fun getTopRatedMovies(): List<Movie> = runCatching {
        tmdbApi.getTopRatedMovies().results.map { it.toMovie() }
    }.getOrElse { emptyList() }

    /**
     * قائمة مميزة من Trakt - تعرض كقسم طبيعي في الـ Home (مثل IMDB Top 250)
     * تجلب أسماء + tmdb_ids من Trakt، ثم تجلب التفاصيل الكاملة (بوستر/وصف) من TMDB
     */
    suspend fun getFeaturedList(user: String, listId: String, maxResults: Int = 20): List<Movie> = runCatching {
        val traktItems = traktApi.getUserList(user, listId)
        val movies = mutableListOf<Movie>()
        traktItems.take(maxResults).forEach { item ->
            val movie = item.movie
            val tmdbId = movie?.ids?.tmdb
            if (tmdbId != null) {
                val details = runCatching { tmdbApi.getMovieDetails(tmdbId, append = "credits,videos,similar") }.getOrNull()
                if (details != null) {
                    movies.add(details.toMovie())
                }
            }
        }
        movies
    }.getOrElse { emptyList() }

    /** قائمة IMDB Top 250 المميزة (للعرض كقسم في الـ Home) */
    suspend fun getImdbTopMovies(): List<Movie> = runCatching {
        // trakt.getUserList("justin", "imdb-top-rated-movies")
        val traktItems = traktApi.getUserList("justin", "imdb-top-rated-movies")
        val movies = mutableListOf<Movie>()
        traktItems.take(20).forEach { item ->
            val tmdbId = item.movie?.ids?.tmdb
            if (tmdbId != null) {
                val details = runCatching { tmdbApi.getMovieDetails(tmdbId, append = "credits,videos,similar") }.getOrNull()
                if (details != null) {
                    movies.add(details.toMovie())
                }
            }
        }
        movies
    }.getOrElse { emptyList() }

    /**
     * يجلب كل أفلام IMDB Top 250 - لشاشة عرض الكل
     * @param limit عدد الأفلام (افتراضياً 100)
     */
    suspend fun getAllImdbTopMovies(limit: Int = 100): List<Movie> = runCatching {
        val traktItems = traktApi.getUserList("justin", "imdb-top-rated-movies")
        val movies = mutableListOf<Movie>()
        traktItems.take(limit).forEach { item ->
            val tmdbId = item.movie?.ids?.tmdb
            if (tmdbId != null) {
                val details = runCatching { tmdbApi.getMovieDetails(tmdbId, append = "credits,videos,similar") }.getOrNull()
                if (details != null) {
                    movies.add(details.toMovie())
                }
            }
        }
        movies
    }.getOrElse { emptyList() }

    /**
     * ترجمات من OpenSubtitles لفيلم معين
     * يحتاج imdb_id - يرجع قائمة روابط الترجمات (عربي + إنجليزي)
     */
    suspend fun getSubtitlesForMovie(imdbId: String?): List<SubtitleTrack> = runCatching {
        if (imdbId.isNullOrBlank()) return@runCatching emptyList<SubtitleTrack>()
        val cleanId = imdbId.removePrefix("tt").trim()
        val response = openSubtitlesApi.searchByImdb(cleanId)
        response.data?.mapNotNull { it.toSubtitleTrack() } ?: emptyList()
    }.getOrElse { emptyList() }

    /**
     * ترجمات لحلقة مسلسل
     */
    suspend fun getSubtitlesForEpisode(imdbId: String?, season: Int, episode: Int): List<SubtitleTrack> = runCatching {
        if (imdbId.isNullOrBlank()) return@runCatching emptyList<SubtitleTrack>()
        val cleanId = imdbId.removePrefix("tt").trim()
        val response = openSubtitlesApi.searchEpisode(cleanId, season, episode)
        response.data?.mapNotNull { it.toSubtitleTrack() } ?: emptyList()
    }.getOrElse { emptyList() }

    /** أفلام بـ pagination (لـ infinite scroll) - كل صفحة 20 فيلم */
    suspend fun discoverMovies(page: Int = 1): List<Movie> = runCatching {
        tmdbApi.discoverMovies(page = page).results.map { it.toMovie() }
    }.getOrElse { emptyList() }

    /**
     * يجلب تفاصيل الفيلم الكاملة + رابط بث حقيقي
     * 1. لو من TMDB: يجلب التفاصيل + YouTube trailer كمصدر بث
     * 2. لو من Cinemana: يجلب التفاصيل + روابط البث الحقيقية
     */
    suspend fun getMovieWithStreams(movieId: String): Movie? {
        // TMDB movie (id starts with "tmdb_movie_")
        if (movieId.startsWith("tmdb_movie_")) {
            val tmdbId = movieId.removePrefix("tmdb_movie_").toIntOrNull()
            if (tmdbId != null) {
                val details = runCatching {
                    tmdbApi.getMovieDetails(tmdbId, append = "credits,videos,similar")
                }.getOrNull()
                if (details != null) {
                    return details.toMovie().copy(id = movieId)
                }
                // Fallback: create basic movie with stream URL
                return Movie(
                    id = movieId,
                    title = "فيلم",
                    overview = "",
                    streamUrl = "https://vidsrc.in/embed/movie/$tmdbId"
                )
            }
        }

        // TMDB series (id starts with "tmdb_series_") - treat as movie for detail screen
        if (movieId.startsWith("tmdb_series_")) {
            val tmdbId = movieId.removePrefix("tmdb_series_").toIntOrNull()
            if (tmdbId != null) {
                // Use vidsrc.in for TV series - works without frame-busting
                val streamUrl = "https://vidsrc.in/embed/tv/$tmdbId/1/1"
                return Movie(
                    id = movieId,
                    title = "مسلسل",
                    overview = "مسلسل من TMDB",
                    streamUrl = streamUrl
                )
            }
        }

        // Cinemana movie (id starts with "cin_" or is numeric)
        val cinemanaId = when {
            movieId.startsWith("cin_") -> movieId.removePrefix("cin_")
            movieId.all { it.isDigit() } && movieId.isNotEmpty() -> movieId
            else -> null
        }

        if (cinemanaId != null) {
            val info = runCatching { cinemanaApi.getVideoInfo(cinemanaId) }.getOrNull()
            val streams = runCatching { cinemanaApi.getTranscodedFiles(cinemanaId) }.getOrNull().orEmpty()
            if (info != null) {
                val streamUrl = streams.mapNotNull { it.videoUrl }.firstOrNull()
                return info.toMovie().copy(id = movieId, streamUrl = streamUrl)
            }
        }

        return null
    }

    fun getMovieById(id: String): Movie? = null // Deprecated - use getMovieWithStreams

    suspend fun getTrendingSeries(): List<Series> = runCatching {
        tmdbApi.getPopularSeries().results.map { it.toSeries() }
    }.getOrElse { emptyList() }

    suspend fun getPopularSeries(): List<Series> = getTrendingSeries()

    /** أفلام عربية - بأسماء وأوصاف عربية من TMDB */
    suspend fun getArabicMovies(): List<Movie> = runCatching {
        tmdbApi.discoverArabicMovies().results.map { it.toMovie() }
    }.getOrElse { emptyList() }

    /** مسلسلات عربية - رمضان، مصرية، سعودية، الخ */
    suspend fun getArabicSeries(): List<Series> = runCatching {
        tmdbApi.discoverArabicSeries().results.map { it.toSeries() }
    }.getOrElse { emptyList() }

    /** أفلام أنمي - بأسماء عربية */
    suspend fun getAnimeMovies(): List<Movie> = runCatching {
        tmdbApi.discoverAnime().results.map { it.toMovie() }
    }.getOrElse { emptyList() }

    /** مسلسلات أنمي */
    suspend fun getAnimeSeries(): List<Series> = runCatching {
        tmdbApi.discoverAnimeSeries().results.map { it.toSeries() }
    }.getOrElse { emptyList() }

    /** أفلام تركية */
    suspend fun getTurkishMovies(): List<Movie> = runCatching {
        tmdbApi.discoverMoviesByLanguage(originalLanguage = "tr").results.map { it.toMovie() }
    }.getOrElse { emptyList() }

    /** مسلسلات تركية */
    suspend fun getTurkishSeries(): List<Series> = runCatching {
        tmdbApi.discoverSeriesByLanguage(originalLanguage = "tr").results.map { it.toSeries() }
    }.getOrElse { emptyList() }

    /** أفلام كورية */
    suspend fun getKoreanMovies(): List<Movie> = runCatching {
        tmdbApi.discoverMoviesByLanguage(originalLanguage = "ko").results.map { it.toMovie() }
    }.getOrElse { emptyList() }

    /** مسلسلات كورية (K-Drama) */
    suspend fun getKoreanSeries(): List<Series> = runCatching {
        tmdbApi.discoverSeriesByLanguage(originalLanguage = "ko").results.map { it.toSeries() }
    }.getOrElse { emptyList() }

    /** أفلام يابانية */
    suspend fun getJapaneseMovies(): List<Movie> = runCatching {
        tmdbApi.discoverMoviesByLanguage(originalLanguage = "ja").results.map { it.toMovie() }
    }.getOrElse { emptyList() }

    /** مسلسلات يابانية */
    suspend fun getJapaneseSeries(): List<Series> = runCatching {
        tmdbApi.discoverSeriesByLanguage(originalLanguage = "ja").results.map { it.toSeries() }
    }.getOrElse { emptyList() }

    /** أفلام هندية (Bollywood) */
    suspend fun getIndianMovies(): List<Movie> = runCatching {
        tmdbApi.discoverMoviesByLanguage(originalLanguage = "hi").results.map { it.toMovie() }
    }.getOrElse { emptyList() }

    /** مسلسلات هندية */
    suspend fun getIndianSeries(): List<Series> = runCatching {
        tmdbApi.discoverSeriesByLanguage(originalLanguage = "hi").results.map { it.toSeries() }
    }.getOrElse { emptyList() }

    /** أفلام أمريكية/بريطانية */
    suspend fun getAmericanMovies(): List<Movie> = runCatching {
        tmdbApi.discoverMoviesByLanguage(originalLanguage = "en").results.map { it.toMovie() }
    }.getOrElse { emptyList() }

    /** مسلسلات أمريكية/بريطانية */
    suspend fun getAmericanSeries(): List<Series> = runCatching {
        tmdbApi.discoverSeriesByLanguage(originalLanguage = "en").results.map { it.toSeries() }
    }.getOrElse { emptyList() }

    /**
     * يجلب تفاصيل مسلسل كاملة - المواسم الحقيقية + معلومات كاملة من TMDB
     * يستخدم tv/{id} endpoint الذي يعطي seasons[] مع كل موسم
     */
    suspend fun getSeriesWithStreams(seriesId: String): Series? {
        if (!seriesId.startsWith("tmdb_series_")) return null
        val tmdbId = seriesId.removePrefix("tmdb_series_").toIntOrNull() ?: return null

        val details = runCatching {
            tmdbApi.getTvDetails(tmdbId, append = "credits,videos,similar,external_ids")
        }.getOrNull() ?: return null

        val cast = details.credits?.cast?.take(15)?.mapIndexed { i, c ->
            CastMember(
                id = "cast_${c.id}",
                name = c.name,
                character = c.character ?: "",
                photoUrl = TmdbApi.profileUrl(c.profile_path)
            )
        } ?: emptyList()

        val seasonsList = details.seasons
            .filter { it.season_number > 0 } // تجاهل "Specials" (season 0)
            .map { season ->
                Season(
                    id = "season_${tmdbId}_${season.season_number}",
                    number = season.season_number,
                    title = season.name ?: "الموسم ${season.season_number}",
                    episodeCount = season.episode_count,
                    posterUrl = TmdbApi.posterUrl(season.poster_path),
                    airDate = season.air_date,
                    episodes = emptyList() // تُجلب عند فتح الموسم (lazy load)
                )
            }

        return Series(
            id = seriesId,
            title = details.name,
            overview = details.overview ?: "",
            posterUrl = TmdbApi.posterUrl(details.poster_path),
            backdropUrl = TmdbApi.backdropUrl(details.backdrop_path),
            startYear = details.first_air_date?.take(4)?.toIntOrNull() ?: 0,
            rating = details.vote_average.toFloat(),
            genres = details.genres.mapNotNull { it.name },
            quality = "HD",
            seasons = seasonsList,
            imdbId = details.external_ids?.imdb_id,
            numberOfSeasons = details.number_of_seasons ?: seasonsList.size,
            numberOfEpisodes = details.number_of_episodes ?: 0,
            cast = cast,
            tmdbId = tmdbId
        )
    }

    /**
     * يجلب حلقات موسم معين - يستخدم tv/{id}/season/{n} endpoint
     * يعطي قائمة الحلقات الكاملة: اسم، وصف، صورة، تاريخ بث، مدة
     */
    suspend fun getSeasonEpisodes(seriesId: String, seasonNumber: Int): List<Episode> {
        val tmdbId = seriesId.removePrefix("tmdb_series_").toIntOrNull() ?: return emptyList()
        val seasonData = runCatching {
            tmdbApi.getSeasonDetails(tmdbId, seasonNumber)
        }.getOrNull() ?: return emptyList()

        return seasonData.episodes.map { ep ->
            Episode(
                id = "ep_${tmdbId}_${seasonNumber}_${ep.episode_number}",
                number = ep.episode_number,
                title = ep.name,
                overview = ep.overview ?: "",
                durationMinutes = ep.runtime ?: 0,
                thumbnailUrl = TmdbApi.backdropUrl(ep.still_path),
                streamUrl = "https://vidsrc.in/embed/tv/$tmdbId/$seasonNumber/${ep.episode_number}",
                airDate = ep.air_date,
                seasonNumber = seasonNumber
            )
        }
    }

    fun getSeriesById(id: String): Series? = null
    fun getPopularAnime(): List<Anime> = emptyList()
    fun getAnimeById(id: String): Anime? = null

    fun getLiveChannels(): List<Channel> = listOf(
        Channel("ch1", "Al Jazeera العربية", "https://upload.wikimedia.org/wikipedia/commons/thumb/f/f2/Aljazeera.svg/512px-Aljazeera.svg.png", "https://live-hls-web-aja.getaj.net/AJA/01.m3u8", "أخبار", true),
        Channel("ch2", "Al Arabiya", "https://upload.wikimedia.org/wikipedia/commons/thumb/c/c3/Al_Arabiya_logo.svg/512px-Al_Arabiya_logo.svg.png", "https://live.alarabiya.net/alarabiapublish/alarabiya.smil/playlist.m3u8", "أخبار", true),
        Channel("ch3", "France 24 عربية", "https://upload.wikimedia.org/wikipedia/commons/thumb/8/8a/France_24.svg/512px-France_24.svg.png", "https://static.france24.com/live/F24_AR_LO_HLS/live_web.m3u8", "أخبار", true),
        Channel("ch4", "France 24 English", "https://upload.wikimedia.org/wikipedia/commons/thumb/8/8a/France_24.svg/512px-France_24.svg.png", "https://static.france24.com/live/F24_EN_LO_HLS/live_web.m3u8", "أخبار", true),
        Channel("ch5", "Red Bull TV", "https://upload.wikimedia.org/wikipedia/commons/thumb/2/29/Red_Bull_TV_logo.svg/512px-Red_Bull_TV_logo.svg.png", "https://rbmn-live.akamaized.net/hls/live/590964/BoRB-AT/master.m3u8", "ترفيه", true),
        Channel("ch6", "NASA TV", "https://upload.wikimedia.org/wikipedia/commons/thumb/b/b4/NASA_TV.svg/512px-NASA_TV.svg.png", "https://ntv1.akamaized.net/hls/live/2014075/NASA-NTV1-HLS/master.m3u8", "علوم", true),
        Channel("ch7", "DW English", "https://upload.wikimedia.org/wikipedia/commons/thumb/6/69/Deutsche_Welle.svg/512px-Deutsche_Welle.svg.png", "https://dwamdstream102.akamaized.net/hls/live/2015525/dwstream102/index.m3u8", "أخبار", true)
    )

    fun getArabicChannels(): List<Channel> = getLiveChannels().filter { it.category.contains("أخبار") }
    fun getNewsChannels(): List<Channel> = getLiveChannels().filter { it.name.contains("English") || it.name.contains("DW") || it.name.contains("NASA") || it.name.contains("Red Bull") }

    /** بحث شامل عبر الإنترنت - TMDB + Cinemana */
    suspend fun searchAll(query: String): SearchResults {
        if (query.isBlank()) return SearchResults()

        val movies = mutableListOf<Movie>()

        // 1. Search TMDB - يجلب أفلام + مسلسلات مع media_type صحيح
        runCatching {
            val response = tmdbApi.searchMulti(query = query)
            response.results
                .filter { it.media_type != "person" }
                .map { it.toSearchResult() } // يحدد تلقائياً movie/tv
                .let { movies.addAll(it) }
        }

        // 2. Search Cinemana (if TMDB returned nothing)
        if (movies.isEmpty()) {
            runCatching {
                val results = cinemanaApi.search(videoTitle = query)
                movies.addAll(results.map { it.toMovie() })
            }
        }

        return SearchResults(movies = movies)
    }

    data class SearchResults(
        val movies: List<Movie> = emptyList(),
        val series: List<Series> = emptyList(),
        val anime: List<Anime> = emptyList(),
        val matches: List<Match> = emptyList()
    )

    /** مباريات حقيقية من كل الدوريات الكبرى */
    suspend fun getTodayMatches(): List<Match> {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
        val allMatches = mutableListOf<Match>()

        runCatching {
            val response = sportsApi.getEventsForDay(today, "Soccer")
            response.events?.map { it.toMatch() }?.let { allMatches.addAll(it) }
        }

        val majorLeagues = listOf(4328L, 4335L, 4332L, 4331L, 4334L, 4480L)
        majorLeagues.forEach { leagueId ->
            runCatching {
                val response = sportsApi.getNextLeagueEvents(leagueId)
                response.events?.firstOrNull()?.let { allMatches.add(it.toMatch()) }
            }
        }

        runCatching {
            val response = sportsApi.getPastLeagueEvents(4328)
            response.events?.take(3)?.map { it.toMatch() }?.let { allMatches.addAll(it) }
        }

        return allMatches.distinctBy { it.id }
    }

    // ========== Mappers ==========

    private fun CinemanaVideoModel.toMovie(): Movie {
        return Movie(
            id = "cin_${videoNb ?: id ?: "0"}",
            title = resolveTitle(),
            overview = resolveOverview(),
            posterUrl = resolvePosterUrl(),
            backdropUrl = backdrop ?: resolvePosterUrl(),
            releaseYear = year?.toIntOrNull() ?: 0,
            durationMinutes = duration?.toIntOrNull() ?: 0,
            rating = rating?.toFloatOrNull() ?: 0f,
            genres = categories?.mapNotNull { it.categoryArabicName ?: it.categoryEnglishName } ?: emptyList(),
            quality = "HD",
            cast = actorsInfo?.mapIndexed { i, s ->
                CastMember(
                    id = s.id ?: "cast_$i",
                    name = s.name ?: s.title ?: "",
                    character = s.role ?: "",
                    photoUrl = s.imgMediumThumbObjUrl ?: s.imgThumbObjUrl ?: s.imgObjUrl
                )
            } ?: emptyList(),
            streamUrl = null,
            trailerUrl = null
        )
    }

    private fun com.streamflix.app.data.remote.dto.TmdbMovieDto.toMovie(): Movie {
        // Use databasegdriveplayer.co - JW Player directly on page (no iframe!)
        // Our JavaScript CAN control the video element directly
        val streamUrl = "https://vidsrc.in/embed/movie/$id"

        return Movie(
            id = "tmdb_movie_$id",
            title = resolveTitle(),
            overview = overview ?: "",
            posterUrl = TmdbApi.posterUrl(poster_path),
            backdropUrl = TmdbApi.backdropUrl(backdrop_path),
            releaseYear = resolveYear(),
            durationMinutes = runtime ?: 0,
            rating = vote_average.toFloat(),
            genres = resolveGenres(),
            quality = "HD",
            cast = credits?.cast?.take(10)?.mapIndexed { i, c ->
                CastMember(
                    id = "cast_${c.id}",
                    name = c.name,
                    character = c.character ?: "",
                    photoUrl = TmdbApi.profileUrl(c.profile_path)
                )
            } ?: emptyList(),
            streamUrl = streamUrl,
            trailerUrl = null,
            imdbId = imdb_id,
            similarMovies = similar?.results?.take(15)?.mapNotNull { sim ->
                if (sim.id == id || sim.poster_path.isNullOrBlank()) null
                else Movie(
                    id = "tmdb_movie_${sim.id}",
                    title = sim.resolveTitle(),
                    overview = sim.overview ?: "",
                    posterUrl = TmdbApi.posterUrl(sim.poster_path),
                    backdropUrl = TmdbApi.backdropUrl(sim.backdrop_path),
                    releaseYear = sim.resolveYear(),
                    rating = sim.vote_average.toFloat(),
                    genres = sim.resolveGenres(),
                    quality = "HD",
                    streamUrl = "https://vidsrc.in/embed/movie/${sim.id}",
                    trailerUrl = null,
                    imdbId = sim.imdb_id,
                    originalLanguage = sim.original_language ?: "",
                    originCountry = sim.resolveCountry(),
                    certification = sim.resolveCertification()
                )
            } ?: emptyList(),
            originalLanguage = original_language ?: "",
            originCountry = resolveCountry(),
            certification = resolveCertification()
        )
    }

    /**
     * تحويل نتيجة بحث TMDB search/multi - يحدد تلقائياً إن كانت فيلم أو مسلسل
     * وينشئ الـ id بالبادئة الصحيحة (tmdb_movie_ أو tmdb_series_)
     * هذا يمنع مشكلة فتح محتوى مختلف عند الضغط على نتيجة البحث
     */
    private fun com.streamflix.app.data.remote.dto.TmdbMovieDto.toSearchResult(): Movie {
        val isTv = resolveMediaType() == "tv"
        val streamUrl = if (isTv) {
            "https://vidsrc.in/embed/tv/$id/1/1"
        } else {
            "https://vidsrc.in/embed/movie/$id"
        }
        return Movie(
            id = if (isTv) "tmdb_series_$id" else "tmdb_movie_$id",
            title = resolveTitle(),
            overview = overview ?: "",
            posterUrl = TmdbApi.posterUrl(poster_path),
            backdropUrl = TmdbApi.backdropUrl(backdrop_path),
            releaseYear = resolveYear(),
            rating = vote_average.toFloat(),
            genres = resolveGenres(),
            quality = "HD",
            streamUrl = streamUrl,
            trailerUrl = null,
            imdbId = imdb_id,
            originalLanguage = original_language ?: "",
            originCountry = resolveCountry(),
            certification = resolveCertification()
        )
    }

    /** تحويل ترجمة OpenSubtitles إلى نموذج التطبيق */
    private fun OpenSubEntry.toSubtitleTrack(): SubtitleTrack? {
        val url = SubDownloadLink ?: return null
        val lang = SubLanguageID ?: "eng"
        val langName = when (lang) {
            "ara" -> "العربية"
            "eng" -> "English"
            "fre" -> "Français"
            "spa" -> "Español"
            "tur" -> "Türkçe"
            "per" -> "فارسی"
            "heb" -> "עברית"
            "kor" -> "한국어"
            "jpn" -> "日本語"
            else -> lang
        }
        val ratingValue = SubRating?.toFloatOrNull() ?: 0f
        return SubtitleTrack(
            id = IDSubtitleFile ?: SubFileGUID ?: url.hashCode().toString(),
            language = lang,
            languageName = langName,
            url = url,
            fileName = SubFileName ?: "$lang.srt",
            rating = ratingValue,
            isArabic = lang == "ara"
        )
    }

    private fun com.streamflix.app.data.remote.dto.TmdbSeriesDto.toSeries(): Series {
        return Series(
            id = "tmdb_series_$id",
            title = name,
            overview = overview ?: "",
            posterUrl = TmdbApi.posterUrl(poster_path),
            backdropUrl = TmdbApi.backdropUrl(backdrop_path),
            startYear = first_air_date?.take(4)?.toIntOrNull() ?: 0,
            rating = vote_average.toFloat(),
            genres = genres.mapNotNull { it.name },
            quality = "HD",
            seasons = listOf(
                Season(
                    id = "season_$id",
                    number = 1,
                    title = "الموسم الأول",
                    episodes = listOf(
                        Episode(
                            id = "ep_${id}_1",
                            number = 1,
                            title = "الحلقة 1",
                            overview = "الحلقة الأولى من $name",
                            durationMinutes = 45,
                            thumbnailUrl = TmdbApi.backdropUrl(backdrop_path),
                            streamUrl = "https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8"
                        )
                    )
                )
            ),
            originalLanguage = original_language ?: "",
            originCountry = origin_country.firstOrNull() ?: ""
        )
    }

    private fun SportsEventDto.toMatch(): Match {
        val status = when {
            strStatus?.contains("Finished", ignoreCase = true) == true -> MatchStatus.FINISHED
            strStatus?.matches(Regex("\\d+[Hh]")) == true || strStatus?.contains("LIVE", ignoreCase = true) == true -> MatchStatus.LIVE
            else -> MatchStatus.UPCOMING
        }
        val timestamp = strTimestamp?.let {
            runCatching {
                SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).apply {
                    timeZone = TimeZone.getTimeZone("UTC")
                }.parse(it)?.time ?: 0L
            }.getOrDefault(0L)
        } ?: 0L
        return Match(
            id = idEvent,
            homeTeam = strHomeTeam ?: "Unknown",
            awayTeam = strAwayTeam ?: "Unknown",
            homeScore = intHomeScore?.toIntOrNull() ?: 0,
            awayScore = intAwayScore?.toIntOrNull() ?: 0,
            startTime = if (timestamp > 0) timestamp else System.currentTimeMillis(),
            status = status,
            league = strLeague ?: "",
            homeTeamLogo = strThumb,
            awayTeamLogo = null,
            streamUrl = getLiveChannels().firstOrNull()?.streamUrl,
            minute = strStatus?.filter { it.isDigit() }?.toIntOrNull() ?: 0
        )
    }

    // ========== Favorites & History ==========
    fun observeFavorites(): Flow<List<FavoriteItem>> = favoriteDao.observeAll().map { list ->
        list.map { e ->
            FavoriteItem(
                id = e.id, contentId = e.contentId, title = e.title,
                posterUrl = e.posterUrl,
                contentType = runCatching { ContentType.valueOf(e.contentType) }.getOrDefault(ContentType.MOVIE),
                addedAt = e.addedAt
            )
        }
    }

    fun isFavorite(contentId: String): Flow<Boolean> = favoriteDao.isFavorite(contentId)

    suspend fun toggleFavorite(item: FavoriteItem) {
        val isFav = favoriteDao.isFavorite(item.contentId).first()
        if (isFav) favoriteDao.deleteByContentId(item.contentId)
        else favoriteDao.insert(
            FavoriteEntity(
                id = item.contentId, contentId = item.contentId, title = item.title,
                posterUrl = item.posterUrl, contentType = item.contentType.name, addedAt = item.addedAt
            )
        )
    }

    fun observeWatchHistory(): Flow<List<WatchHistory>> =
        watchHistoryDao.observeRecent().map { list -> list.map { it.toModel() } }

    suspend fun saveProgress(contentId: String, title: String, posterUrl: String?, contentType: ContentType, progressSeconds: Long, durationSeconds: Long) {
        watchHistoryDao.upsert(
            WatchHistoryEntity(
                id = contentId, contentId = contentId, title = title, posterUrl = posterUrl,
                contentType = contentType.name, progressSeconds = progressSeconds,
                durationSeconds = durationSeconds, updatedAt = System.currentTimeMillis()
            )
        )
    }

    fun observeSearchHistory() = searchHistoryDao.observeRecent()
    suspend fun addSearchQuery(query: String) =
        searchHistoryDao.insert(SearchHistoryEntity(query = query, searchedAt = System.currentTimeMillis()))
    suspend fun clearSearchHistory() = searchHistoryDao.clearAll()

    /** آخر فيلم نقر عليه المستخدم من نتائج البحث - مثل Cinemana */
    fun observeLastClickedSearch() = lastClickedSearchDao.observe()
    suspend fun saveLastClickedSearch(
        movieId: String,
        title: String,
        posterUrl: String?,
        backdropUrl: String?,
        query: String
    ) {
        lastClickedSearchDao.upsert(
            LastClickedSearchEntity(
                id = "singleton",
                movieId = movieId,
                title = title,
                posterUrl = posterUrl,
                backdropUrl = backdropUrl,
                query = query,
                clickedAt = System.currentTimeMillis()
            )
        )
    }
    suspend fun clearLastClickedSearch() = lastClickedSearchDao.clear()
}

private fun WatchHistoryEntity.toModel() = WatchHistory(
    id = id, contentId = contentId, title = title, posterUrl = posterUrl,
    contentType = runCatching { ContentType.valueOf(contentType) }.getOrDefault(ContentType.MOVIE),
    progressSeconds = progressSeconds, durationSeconds = durationSeconds, updatedAt = updatedAt
)
