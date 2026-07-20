package com.streamflix.app.presentation.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.streamflix.app.data.repository.StreamFlixRepository
import com.streamflix.app.domain.model.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeState(
    val isLoading: Boolean = true,
    val isLoadingMore: Boolean = false,
    val trendingMovies: List<Movie> = emptyList(),
    val allMovies: List<Movie> = emptyList(),
    val popularMovies: List<Movie> = emptyList(),
    val topRatedMovies: List<Movie> = emptyList(),
    val imdbTopMovies: List<Movie> = emptyList(),
    val trendingSeries: List<Series> = emptyList(),
    val arabicMovies: List<Movie> = emptyList(),
    val arabicSeries: List<Series> = emptyList(),
    val animeMovies: List<Movie> = emptyList(),
    val animeSeries: List<Series> = emptyList(),
    val turkishMovies: List<Movie> = emptyList(),
    val turkishSeries: List<Series> = emptyList(),
    val koreanMovies: List<Movie> = emptyList(),
    val koreanSeries: List<Series> = emptyList(),
    val japaneseMovies: List<Movie> = emptyList(),
    val japaneseSeries: List<Series> = emptyList(),
    val indianMovies: List<Movie> = emptyList(),
    val indianSeries: List<Series> = emptyList(),
    val americanMovies: List<Movie> = emptyList(),
    val americanSeries: List<Series> = emptyList(),
    val todayMatches: List<Match> = emptyList(),
    val liveChannels: List<Channel> = emptyList(),
    val currentPage: Int = 1,
    val hasMore: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repo: StreamFlixRepository
) : ViewModel() {

    private val _state = MutableStateFlow(HomeState())
    val state: StateFlow<HomeState> = _state.asStateFlow()

    val history = repo.observeWatchHistory().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init { load() }

    fun load() {
        _state.value = _state.value.copy(isLoading = true, error = null)
        viewModelScope.launch {
            // تحميل كل الأقسام بالتوازي (parallel) - أسرع 5-10x من sequential
            // كل استدعاء في async منفصل، ثم awaitAll ينتظر الكل
            val results = listOf(
                async { repo.getTrendingMovies() },
                async { repo.getPopularMovies() },
                async { repo.getTopRatedMovies() },
                async { repo.getTrendingSeries() },
                async { repo.getTodayMatches() },
                async { repo.getLiveChannels() },
                async { repo.discoverMovies(page = 1) },
                async { repo.getImdbTopMovies() },
                // Arabic + Anime
                async { repo.getArabicMovies() },
                async { repo.getArabicSeries() },
                async { repo.getAnimeMovies() },
                async { repo.getAnimeSeries() },
                // International
                async { repo.getTurkishMovies() },
                async { repo.getTurkishSeries() },
                async { repo.getKoreanMovies() },
                async { repo.getKoreanSeries() },
                async { repo.getJapaneseMovies() },
                async { repo.getJapaneseSeries() },
                async { repo.getIndianMovies() },
                async { repo.getIndianSeries() },
                async { repo.getAmericanMovies() },
                async { repo.getAmericanSeries() }
            ).awaitAll()

            _state.value = HomeState(
                isLoading = false,
                trendingMovies = results[0] as List<Movie>,
                allMovies = results[6] as List<Movie>,
                popularMovies = results[1] as List<Movie>,
                topRatedMovies = results[2] as List<Movie>,
                imdbTopMovies = results[7] as List<Movie>,
                trendingSeries = results[3] as List<Series>,
                arabicMovies = results[8] as List<Movie>,
                arabicSeries = results[9] as List<Series>,
                animeMovies = results[10] as List<Movie>,
                animeSeries = results[11] as List<Series>,
                turkishMovies = results[12] as List<Movie>,
                turkishSeries = results[13] as List<Series>,
                koreanMovies = results[14] as List<Movie>,
                koreanSeries = results[15] as List<Series>,
                japaneseMovies = results[16] as List<Movie>,
                japaneseSeries = results[17] as List<Series>,
                indianMovies = results[18] as List<Movie>,
                indianSeries = results[19] as List<Series>,
                americanMovies = results[20] as List<Movie>,
                americanSeries = results[21] as List<Series>,
                todayMatches = results[4] as List<Match>,
                liveChannels = results[5] as List<Channel>,
                currentPage = 1,
                hasMore = (results[6] as List<Movie>).isNotEmpty()
            )
        }
    }

    /** Load more movies for infinite scroll */
    fun loadMoreMovies() {
        val current = _state.value
        if (current.isLoadingMore || !current.hasMore) return

        _state.value = current.copy(isLoadingMore = true)
        viewModelScope.launch {
            val nextPage = current.currentPage + 1
            val moreMovies = repo.discoverMovies(page = nextPage)
            _state.value = _state.value.copy(
                allMovies = _state.value.allMovies + moreMovies,
                currentPage = nextPage,
                isLoadingMore = false,
                hasMore = moreMovies.isNotEmpty() && nextPage < 500 // TMDB max 500 pages
            )
        }
    }
}
