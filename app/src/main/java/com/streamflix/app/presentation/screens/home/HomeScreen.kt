package com.streamflix.app.presentation.screens.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.streamflix.app.domain.model.Anime
import com.streamflix.app.domain.model.WatchHistory
import com.streamflix.app.domain.model.Channel
import com.streamflix.app.domain.model.Match
import com.streamflix.app.domain.model.MatchStatus
import com.streamflix.app.domain.model.Movie
import com.streamflix.app.domain.model.Series
import com.streamflix.app.presentation.components.BannerCarousel
import com.streamflix.app.presentation.components.GlassCard
import com.streamflix.app.presentation.components.LiveBadge
import com.streamflix.app.presentation.components.QualityChip
import com.streamflix.app.presentation.components.RatingChip
import com.streamflix.app.presentation.components.ScrollToTopFab
import com.streamflix.app.presentation.components.SectionHeader
import com.streamflix.app.presentation.components.ShimmerBox
import com.streamflix.app.presentation.components.TopRankBadge
import com.streamflix.app.presentation.theme.BrandGold
import com.streamflix.app.presentation.theme.BrandGreen
import com.streamflix.app.presentation.theme.BrandPurple
import com.streamflix.app.presentation.theme.BrandRed
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HomeScreen(
    onNavigate: (String) -> Unit,
    onOpenMovie: (String) -> Unit,
    onOpenSeries: (String) -> Unit,
    onOpenAnime: (String) -> Unit,
    onOpenMatch: (String) -> Unit,
    onOpenChannel: (String) -> Unit,
    onOpenSearch: () -> Unit,
    onOpenNotifications: () -> Unit,
    onOpenImdbTop: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val history by viewModel.history.collectAsState()
    val listState = rememberLazyListState()
    val showScrollToTop by remember {
        derivedStateOf { listState.firstVisibleItemIndex > 0 }
    }

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 110.dp)
        ) {
        // Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "StreamFlix",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.weight(1f)
                )
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.8f))
                        .clickable { onOpenSearch() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.Search, contentDescription = "بحث", tint = MaterialTheme.colorScheme.onBackground)
                }
                Spacer(Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.8f))
                        .clickable { onOpenNotifications() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.Notifications, contentDescription = "إشعارات", tint = MaterialTheme.colorScheme.onBackground)
                }
            }
        }

        // Banner Carousel - rotating through 5 trending movies
        item {
            if (state.trendingMovies.isNotEmpty()) {
                BannerCarousel(
                    movies = state.trendingMovies.take(5),
                    onClick = { onOpenMovie(it.id) }
                )
            } else if (state.isLoading) {
                ShimmerBox(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    height = 260.dp
                )
            }
        }

        // Continue Watching
        if (history.isNotEmpty()) {
            item { SectionHeader("متابعة المشاهدة", emoji = "▶️") }
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(history.take(10)) { h ->
                        // احسب نسبة التقدم بشكل آمن
                        val progress = when {
                            h.durationSeconds > 0 -> (h.progressSeconds.toFloat() / h.durationSeconds).coerceIn(0f, 1f)
                            h.progressSeconds > 0 -> 0.3f // لو ماكو مدة بس فيه تقدم - أظهر شريط ثابت
                            else -> 0f
                        }
                        WatchHistoryCard(
                            title = h.title,
                            posterUrl = h.posterUrl,
                            progress = progress,
                            onClick = { onOpenMovie(h.contentId) }
                        )
                    }
                }
            }
        }

        // Trending
        item { SectionHeader("الأكثر رواجاً", emoji = "🔥") }
        item {
            if (state.isLoading) LoadingRow()
            else HorizontalMovieRow(state.trendingMovies, onClick = { onOpenMovie(it.id) })
        }

        // Popular Movies
        item { SectionHeader("أفلام رائجة", emoji = "🎬") }
        item {
            if (state.isLoading) LoadingRow()
            else HorizontalMovieRow(state.popularMovies, onClick = { onOpenMovie(it.id) })
        }

        // Series
        item { SectionHeader("مسلسلات", emoji = "📺", accentColor = BrandRed) }
        item {
            if (state.isLoading) LoadingRow()
            else HorizontalSeriesRow(state.trendingSeries, onClick = { onOpenSeries(it.id) })
        }

        // Arabic Movies - أفلام عربية
        if (state.arabicMovies.isNotEmpty()) {
            item { SectionHeader("أفلام عربية", emoji = "🎬", accentColor = BrandGreen) }
            item {
                HorizontalMovieRow(state.arabicMovies, onClick = { onOpenMovie(it.id) })
            }
        }

        // Arabic Series - مسلسلات عربية (رمضان، مصرية، سعودية)
        if (state.arabicSeries.isNotEmpty()) {
            item { SectionHeader("مسلسلات عربية", emoji = "📺", accentColor = BrandGold) }
            item {
                HorizontalSeriesRow(state.arabicSeries, onClick = { onOpenSeries(it.id) })
            }
        }

        // Anime Movies - أفلام أنمي
        if (state.animeMovies.isNotEmpty()) {
            item { SectionHeader("أفلام أنمي", emoji = "🎴", accentColor = BrandPurple) }
            item {
                HorizontalMovieRow(state.animeMovies, onClick = { onOpenMovie(it.id) })
            }
        }

        // Anime Series - مسلسلات أنمي
        if (state.animeSeries.isNotEmpty()) {
            item { SectionHeader("مسلسلات أنمي", emoji = "🎌", accentColor = BrandPurple) }
            item {
                HorizontalSeriesRow(state.animeSeries, onClick = { onOpenSeries(it.id) })
            }
        }

        // Turkish Movies - أفلام تركية
        if (state.turkishMovies.isNotEmpty()) {
            item { SectionHeader("أفلام تركية", emoji = "🇹🇷", accentColor = BrandRed) }
            item {
                HorizontalMovieRow(state.turkishMovies, onClick = { onOpenMovie(it.id) })
            }
        }

        // Turkish Series - مسلسلات تركية
        if (state.turkishSeries.isNotEmpty()) {
            item { SectionHeader("مسلسلات تركية", emoji = "📺", accentColor = BrandRed) }
            item {
                HorizontalSeriesRow(state.turkishSeries, onClick = { onOpenSeries(it.id) })
            }
        }

        // Korean Movies - أفلام كورية
        if (state.koreanMovies.isNotEmpty()) {
            item { SectionHeader("أفلام كورية", emoji = "🇰🇷", accentColor = BrandPurple) }
            item {
                HorizontalMovieRow(state.koreanMovies, onClick = { onOpenMovie(it.id) })
            }
        }

        // Korean Series - مسلسلات كورية (K-Drama)
        if (state.koreanSeries.isNotEmpty()) {
            item { SectionHeader("مسلسلات كورية", emoji = "💝", accentColor = BrandPurple) }
            item {
                HorizontalSeriesRow(state.koreanSeries, onClick = { onOpenSeries(it.id) })
            }
        }

        // Japanese Movies - أفلام يابانية
        if (state.japaneseMovies.isNotEmpty()) {
            item { SectionHeader("أفلام يابانية", emoji = "🇯🇵", accentColor = BrandRed) }
            item {
                HorizontalMovieRow(state.japaneseMovies, onClick = { onOpenMovie(it.id) })
            }
        }

        // Japanese Series - مسلسلات يابانية
        if (state.japaneseSeries.isNotEmpty()) {
            item { SectionHeader("مسلسلات يابانية", emoji = "🌸", accentColor = BrandRed) }
            item {
                HorizontalSeriesRow(state.japaneseSeries, onClick = { onOpenSeries(it.id) })
            }
        }

        // Indian Movies - أفلام هندية (Bollywood)
        if (state.indianMovies.isNotEmpty()) {
            item { SectionHeader("أفلام هندية", emoji = "🇮🇳", accentColor = BrandGold) }
            item {
                HorizontalMovieRow(state.indianMovies, onClick = { onOpenMovie(it.id) })
            }
        }

        // Indian Series - مسلسلات هندية
        if (state.indianSeries.isNotEmpty()) {
            item { SectionHeader("مسلسلات هندية", emoji = "🎭", accentColor = BrandGold) }
            item {
                HorizontalSeriesRow(state.indianSeries, onClick = { onOpenSeries(it.id) })
            }
        }

        // American Movies - أفلام أمريكية/أجنبية
        if (state.americanMovies.isNotEmpty()) {
            item { SectionHeader("أفلام أمريكية", emoji = "🇺🇸", accentColor = BrandPurple) }
            item {
                HorizontalMovieRow(state.americanMovies, onClick = { onOpenMovie(it.id) })
            }
        }

        // American Series - مسلسلات أمريكية
        if (state.americanSeries.isNotEmpty()) {
            item { SectionHeader("مسلسلات أمريكية", emoji = "🎬", accentColor = BrandPurple) }
            item {
                HorizontalSeriesRow(state.americanSeries, onClick = { onOpenSeries(it.id) })
            }
        }

        // Live Sports
        item { SectionHeader("مباريات اليوم", emoji = "⚽", accentColor = BrandGreen) }
        item {
            if (state.isLoading) Box(Modifier.padding(16.dp)) { ShimmerBox(height = 90.dp) }
            else HorizontalMatchRow(state.todayMatches, onClick = { onOpenMatch(it.id) })
        }

        // Live Channels
        item { SectionHeader("بث مباشر", emoji = "📡", accentColor = BrandGreen) }
        item {
            if (state.isLoading) LoadingRow()
            else HorizontalChannelRow(state.liveChannels, onClick = { onOpenChannel(it.id) })
        }

        // Top Rated
        item { SectionHeader("الأعلى تقييماً", emoji = "⭐", accentColor = BrandGold) }
        item {
            if (state.isLoading) LoadingRow()
            else HorizontalMovieRow(state.topRatedMovies, onClick = { onOpenMovie(it.id) })
        }

        // IMDB Top 250 - من Trakt (مدمج بشكل طبيعي بدون قسم منفصل)
        if (state.imdbTopMovies.isNotEmpty()) {
            item { SectionHeader("أفضل 250 فيلم - IMDB", emoji = "🏆", accentColor = BrandGold, onSeeAll = onOpenImdbTop) }
            item {
                HorizontalImdbTopRow(state.imdbTopMovies, onClick = { onOpenMovie(it.id) })
            }
        }

        // All Movies with infinite scroll
        item { SectionHeader("كل الأفلام", emoji = "🍿") }
        item {
            if (state.isLoading) {
                androidx.compose.foundation.layout.Row(modifier = Modifier.padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    repeat(3) { ShimmerBox(modifier = Modifier.width(130.dp), height = 220.dp) }
                }
            }
        }
        // Grid of all movies - loads more when scrolled to bottom
        items(state.allMovies.size) { index ->
            // Trigger load more when near the end
            if (index >= state.allMovies.size - 5 && !state.isLoadingMore && state.hasMore) {
                LaunchedEffect(Unit) { viewModel.loadMoreMovies() }
            }
            val movie = state.allMovies[index]
            // Display 2 per row using a Row
            if (index % 2 == 0) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 5.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MovieGridItem(movie = movie, onClick = { onOpenMovie(movie.id) }, modifier = Modifier.weight(1f))
                    val nextMovie = state.allMovies.getOrNull(index + 1)
                    if (nextMovie != null) {
                        MovieGridItem(movie = nextMovie, onClick = { onOpenMovie(nextMovie.id) }, modifier = Modifier.weight(1f))
                    } else {
                        Spacer(Modifier.weight(1f))
                    }
                }
            }
        }
        // Loading more indicator
        if (state.isLoadingMore) {
            item {
                Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                    androidx.compose.material3.CircularProgressIndicator(color = MaterialTheme.colorScheme.primary, strokeWidth = 2.dp, modifier = Modifier.size(32.dp))
                }
            }
        }
        } // end LazyColumn

        // Floating scroll-to-top button - بمستوى البار السفلي تماماً (شوي أعلى منه)
        val coroutineScope = rememberCoroutineScope()
        ScrollToTopFab(
            visible = showScrollToTop,
            onClick = {
                coroutineScope.launch {
                    listState.animateScrollToItem(0)
                }
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 20.dp, bottom = 16.dp)
        )
    }
}

@Composable
private fun MovieGridItem(movie: Movie, onClick: () -> Unit, modifier: Modifier = Modifier) {
    GlassCard(
        modifier = modifier.height(260.dp),
        pressEffect = true,
        onClick = onClick
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            AsyncImage(
                model = movie.posterUrl,
                contentDescription = movie.title,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            Box(modifier = Modifier.fillMaxSize().background(
                Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.85f)))
            ))
            Column(modifier = Modifier.align(Alignment.BottomStart).padding(10.dp)) {
                Text(text = movie.title, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                Spacer(Modifier.height(4.dp))
                Text(text = "${movie.releaseYear}", color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp)
                Spacer(Modifier.height(4.dp))
                if (movie.rating > 0) {
                    androidx.compose.material3.Surface(
                        color = Color(0xFFF59E0B).copy(alpha = 0.18f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text("★", color = Color(0xFFF59E0B), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Text(String.format("%.1f", movie.rating), color = Color(0xFFF59E0B), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LoadingRow() {
    LazyRow(contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        repeat(5) { item { ShimmerBox(modifier = Modifier.width(120.dp), height = 180.dp) } }
    }
}

@Composable
private fun MoviePosterCard(movie: Movie, onClick: () -> Unit) {
    GlassCard(
        modifier = Modifier.width(130.dp).height(220.dp),
        pressEffect = true,
        onClick = onClick
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            AsyncImage(
                model = movie.posterUrl,
                contentDescription = movie.title,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            Box(
                modifier = Modifier.fillMaxSize().background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Black.copy(alpha = 0.3f), Color.Transparent, Color.Black.copy(alpha = 0.85f))
                    )
                )
            )
            // شارات أعلى البطاقة (يسار + يمين)
            Row(
                modifier = Modifier.align(Alignment.TopStart).padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                // شارة "مترجم" للأفلام الأجنبية
                if (!movie.isArabic) {
                    MovieBadge(text = "مترجم", color = BrandPurple)
                }
                // شارة بلد المنشأ
                if (movie.countryNameAr.isNotEmpty()) {
                    MovieBadge(text = movie.countryNameAr, color = BrandGreen)
                }
            }
            // شارة التصنيف العمري (يمين)
            if (movie.certification.isNotBlank()) {
                MovieBadge(
                    text = movie.certification,
                    color = BrandRed,
                    modifier = Modifier.align(Alignment.TopEnd).padding(4.dp)
                )
            }
            // معلومات أسفل البطاقة
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(8.dp)
            ) {
                Text(text = movie.title, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                Spacer(Modifier.height(2.dp))
                // النوع الأول (إذا متوفر)
                if (movie.genres.isNotEmpty()) {
                    Text(
                        text = movie.genres.first(),
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 9.sp,
                        maxLines = 1
                    )
                    Spacer(Modifier.height(2.dp))
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    RatingChip(movie.rating)
                    QualityChip(movie.quality)
                }
            }
        }
    }
}

@Composable
private fun HorizontalMovieRow(movies: List<Movie>, onClick: (Movie) -> Unit) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(movies) { movie ->
            MoviePosterCard(movie, onClick = { onClick(movie) })
        }
    }
}

@Composable
private fun SeriesCard(series: Series, onClick: () -> Unit) {
    GlassCard(
        modifier = Modifier.width(130.dp).height(220.dp),
        pressEffect = true,
        onClick = onClick
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            AsyncImage(
                model = series.posterUrl,
                contentDescription = series.title,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            Box(modifier = Modifier.fillMaxSize().background(
                Brush.verticalGradient(listOf(Color.Black.copy(alpha = 0.3f), Color.Transparent, Color.Black.copy(alpha = 0.85f)))
            ))
            // شارات أعلى البطاقة
            Row(
                modifier = Modifier.align(Alignment.TopStart).padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                // شارة "مترجم" للمسلسلات الأجنبية
                if (!series.isArabic) {
                    MovieBadge(text = "مترجم", color = BrandPurple)
                }
                // شارة بلد المنشأ (مصري، عراقي، تركي، إلخ)
                if (series.countryNameAr.isNotEmpty()) {
                    MovieBadge(text = series.countryNameAr, color = BrandGold)
                }
            }
            Column(modifier = Modifier.align(Alignment.BottomStart).padding(8.dp)) {
                Text(text = series.title, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                Spacer(Modifier.height(2.dp))
                // النوع الأول (إذا متوفر)
                if (series.genres.isNotEmpty()) {
                    Text(
                        text = series.genres.first(),
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 9.sp,
                        maxLines = 1
                    )
                    Spacer(Modifier.height(2.dp))
                }
                RatingChip(series.rating)
            }
        }
    }
}

@Composable
private fun HorizontalSeriesRow(series: List<Series>, onClick: (Series) -> Unit) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(series) { s -> SeriesCard(s, onClick = { onClick(s) }) }
    }
}

@Composable
private fun AnimeCard(anime: Anime, onClick: () -> Unit) {
    GlassCard(
        modifier = Modifier.width(130.dp).height(220.dp),
        pressEffect = true,
        onClick = onClick
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            AsyncImage(
                model = anime.posterUrl,
                contentDescription = anime.title,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            Box(modifier = Modifier.fillMaxSize().background(
                Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.75f)))
            ))
            Column(modifier = Modifier.align(Alignment.BottomStart).padding(8.dp)) {
                Text(text = anime.title, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                Spacer(Modifier.height(2.dp))
                RatingChip(anime.rating)
            }
        }
    }
}

@Composable
private fun HorizontalAnimeRow(anime: List<Anime>, onClick: (Anime) -> Unit) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(anime) { a -> AnimeCard(a, onClick = { onClick(a) }) }
    }
}

@Composable
private fun MatchCard(match: Match, onClick: () -> Unit) {
    val fmt = SimpleDateFormat("HH:mm", Locale.getDefault())
    GlassCard(
        modifier = Modifier.width(280.dp),
        pressEffect = true,
        onClick = onClick
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (match.status == MatchStatus.LIVE) LiveBadge()
                else Text(
                    text = fmt.format(Date(match.startTime)),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.weight(1f))
                Text(
                    text = match.league,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.labelSmall
                )
            }
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(modifier = Modifier.size(48.dp).clip(CircleShape).background(BrandPurple.copy(alpha = 0.2f)), contentAlignment = Alignment.Center) {
                        if (match.homeTeamLogo != null) {
                            AsyncImage(
                                model = match.homeTeamLogo,
                                contentDescription = match.homeTeam,
                                modifier = Modifier.fillMaxSize().padding(4.dp),
                                contentScale = ContentScale.Fit
                            )
                        } else {
                            Text(text = match.homeTeam.take(2), color = BrandPurple, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(text = match.homeTeam, color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold)
                }
                if (match.status == MatchStatus.LIVE || match.status == MatchStatus.FINISHED) {
                    Text(
                        text = "${match.homeScore} - ${match.awayScore}",
                        color = MaterialTheme.colorScheme.onBackground,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black
                    )
                } else {
                    Text(text = "VS", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(modifier = Modifier.size(48.dp).clip(CircleShape).background(BrandRed.copy(alpha = 0.2f)), contentAlignment = Alignment.Center) {
                        if (match.awayTeamLogo != null) {
                            AsyncImage(
                                model = match.awayTeamLogo,
                                contentDescription = match.awayTeam,
                                modifier = Modifier.fillMaxSize().padding(4.dp),
                                contentScale = ContentScale.Fit
                            )
                        } else {
                            Text(text = match.awayTeam.take(2), color = BrandRed, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(text = match.awayTeam, color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold)
                }
            }
            if (match.status == MatchStatus.LIVE) {
                Spacer(Modifier.height(6.dp))
                Surface(color = BrandRed.copy(alpha = 0.15f), shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "${match.minute}'",
                        color = BrandRed,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(4.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun HorizontalMatchRow(matches: List<Match>, onClick: (Match) -> Unit) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(matches) { m -> MatchCard(m, onClick = { onClick(m) }) }
    }
}

@Composable
private fun ChannelCard(channel: Channel, onClick: () -> Unit) {
    GlassCard(
        modifier = Modifier.width(140.dp).height(90.dp),
        pressEffect = true,
        onClick = onClick
    ) {
        Box(
            modifier = Modifier.fillMaxSize().background(Brush.linearGradient(listOf(BrandPurple, BrandRed))),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Filled.Search, contentDescription = null, tint = Color.White.copy(alpha = 0.7f), modifier = Modifier.size(24.dp))
                Spacer(Modifier.height(4.dp))
                Text(text = channel.name, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 2)
            }
        }
    }
}

@Composable
private fun HorizontalChannelRow(channels: List<Channel>, onClick: (Channel) -> Unit) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(channels) { c -> ChannelCard(c, onClick = { onClick(c) }) }
    }
}

@Composable
private fun WatchHistoryCard(title: String, posterUrl: String?, progress: Float, onClick: () -> Unit) {
    GlassCard(
        modifier = Modifier.width(180.dp).height(110.dp),
        pressEffect = true,
        onClick = onClick
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            AsyncImage(
                model = posterUrl,
                contentDescription = title,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            // تدرّج أسود من الأسفل لقابلية قراءة النص
            Box(
                modifier = Modifier.fillMaxSize().background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.5f),
                            Color.Black.copy(alpha = 0.9f)
                        )
                    )
                )
            )
            Column(
                modifier = Modifier.align(Alignment.BottomStart).padding(8.dp).fillMaxWidth()
            ) {
                Text(
                    text = title,
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1
                )
                Spacer(Modifier.height(4.dp))
                // شريط تقدم أحمر بارز مثل YouTube
                // الخلفية رمادية شفافة، والأحمر يملأ بنسبة التقدم
                if (progress > 0f) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(Color.White.copy(alpha = 0.25f))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(fraction = progress.coerceIn(0.05f, 1f))
                                .height(4.dp)
                                .background(BrandRed)
                        )
                    }
                }
            }
        }
    }
}

/**
 * صف أفلام قائمة IMDB Top - مع شارة ترتيب على كل بطاقة (1، 2، 3...)
 */
@Composable
private fun HorizontalImdbTopRow(movies: List<Movie>, onClick: (Movie) -> Unit) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        itemsIndexed(movies.take(20)) { index, movie ->
            ImdbTopCard(movie = movie, rank = index + 1, onClick = { onClick(movie) })
        }
    }
}

@Composable
private fun ImdbTopCard(movie: Movie, rank: Int, onClick: () -> Unit) {
    GlassCard(
        modifier = Modifier.width(130.dp).height(220.dp),
        pressEffect = true,
        onClick = onClick
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            AsyncImage(
                model = movie.posterUrl,
                contentDescription = movie.title,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            Box(
                modifier = Modifier.fillMaxSize().background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.85f))
                    )
                )
            )
            // Top rank badge - top left corner
            Box(modifier = Modifier.align(Alignment.TopStart).padding(6.dp)) {
                TopRankBadge(rank = rank)
            }
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(8.dp)
            ) {
                Text(text = movie.title, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                Spacer(Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    RatingChip(movie.rating)
                    Text(
                        text = "${movie.releaseYear}",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 10.sp
                    )
                }
            }
        }
    }
}

/**
 * شارة صغيرة على البطاقة - "مترجم"، بلد المنشأ، تصنيف عمري
 */
@Composable
private fun MovieBadge(
    text: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        color = color.copy(alpha = 0.9f),
        shape = RoundedCornerShape(4.dp),
        modifier = modifier
    ) {
        Text(
            text = text,
            color = Color.White,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
        )
    }
}
