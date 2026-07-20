package com.streamflix.app.presentation.screens.detail

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil.compose.AsyncImage
import com.streamflix.app.data.repository.StreamFlixRepository
import com.streamflix.app.domain.model.ContentType
import com.streamflix.app.domain.model.Episode
import com.streamflix.app.domain.model.FavoriteItem
import com.streamflix.app.domain.model.Season
import com.streamflix.app.domain.model.Series
import com.streamflix.app.presentation.components.GlassCard
import com.streamflix.app.presentation.components.QualityChip
import com.streamflix.app.presentation.components.RatingChip
import com.streamflix.app.presentation.theme.BrandPurple
import com.streamflix.app.presentation.theme.BrandRed
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SeriesDetailViewModel @Inject constructor(
    private val repo: StreamFlixRepository
) : ViewModel() {
    var series by mutableStateOf<Series?>(null)
        private set
    var isLoading by mutableStateOf(true)
        private set
    var selectedSeasonNumber by mutableStateOf(1)
        private set
    var episodes by mutableStateOf<List<Episode>>(emptyList())
        private set
    var isLoadingEpisodes by mutableStateOf(false)
        private set
    private val _isFav = MutableStateFlow(false)
    val isFav: StateFlow<Boolean> = _isFav.asStateFlow()

    fun init(id: String) {
        viewModelScope.launch {
            isLoading = true
            series = repo.getSeriesWithStreams(id)
            isLoading = false
            _isFav.value = repo.isFavorite(id).first()
            // Load episodes for first season automatically
            series?.seasons?.firstOrNull()?.let { season ->
                selectSeason(season.number)
            }
        }
    }

    fun selectSeason(seasonNumber: Int) {
        selectedSeasonNumber = seasonNumber
        episodes = emptyList()
        isLoadingEpisodes = true
        viewModelScope.launch {
            val seriesId = series?.id ?: return@launch
            episodes = repo.getSeasonEpisodes(seriesId, seasonNumber)
            isLoadingEpisodes = false
        }
    }

    fun toggleFav() {
        val s = series ?: return
        viewModelScope.launch {
            val fav = FavoriteItem(
                id = s.id, contentId = s.id, title = s.title,
                posterUrl = s.posterUrl, contentType = ContentType.SERIES
            )
            repo.toggleFavorite(fav)
            _isFav.value = repo.isFavorite(s.id).first()
        }
    }
}

@Composable
fun SeriesDetailScreen(
    seriesId: String,
    onBack: () -> Unit,
    onPlayEpisode: (seriesId: String, seasonNumber: Int, episodeNumber: Int) -> Unit,
    viewModel: SeriesDetailViewModel = hiltViewModel()
) {
    LaunchedEffect(seriesId) { viewModel.init(seriesId) }
    val series = viewModel.series
    val isFav by viewModel.isFav.collectAsState()

    if (viewModel.isLoading || series == null) {
        Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = BrandPurple)
        }
        return
    }

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            // Backdrop
            item {
                Box(modifier = Modifier.fillMaxWidth().height(280.dp)) {
                    AsyncImage(
                        model = series.backdropUrl,
                        contentDescription = series.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    Box(modifier = Modifier.fillMaxSize().background(
                        Brush.verticalGradient(listOf(Color.Transparent, MaterialTheme.colorScheme.background))
                    ))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                            .align(Alignment.TopStart)
                    ) {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Filled.ArrowBack, contentDescription = "رجوع", tint = Color.White)
                        }
                        Spacer(Modifier.weight(1f))
                        IconButton(onClick = { viewModel.toggleFav() }) {
                            Icon(
                                imageVector = if (isFav) Icons.Filled.Bookmark else Icons.Filled.BookmarkBorder,
                                contentDescription = "مفضلة",
                                tint = if (isFav) BrandRed else Color.White
                            )
                        }
                    }
                }
            }

            // Title + meta
            item {
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Text(
                        text = series.title,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        RatingChip(series.rating)
                        QualityChip(series.quality)
                        Text("${series.startYear}", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
                        Text("•", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("${series.numberOfSeasons} موسم", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
                        Text("•", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("${series.numberOfEpisodes} حلقة", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
                    }
                    Spacer(Modifier.height(8.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(series.genres) { g ->
                            Surface(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f), shape = RoundedCornerShape(12.dp)) {
                                Text(
                                    text = g,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Overview
            item {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    Text("القصة", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = series.overview,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(end = 16.dp)
                    )
                }
            }

            // Cast
            if (series.cast.isNotEmpty()) {
                item {
                    Text("طاقم العمل", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground, modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp))
                }
                item {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(series.cast) { c ->
                            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(80.dp)) {
                                Box(
                                    modifier = Modifier.size(70.dp).clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    AsyncImage(
                                        model = c.photoUrl,
                                        contentDescription = c.name,
                                        modifier = Modifier.fillMaxSize().clip(CircleShape),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                                Spacer(Modifier.height(4.dp))
                                Text(text = c.name, color = MaterialTheme.colorScheme.onSurface, fontSize = 11.sp, fontWeight = FontWeight.Medium, maxLines = 1)
                                Text(text = c.character, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.sp, maxLines = 1)
                            }
                        }
                    }
                }
            }

            // Seasons selector
            item {
                Text(
                    "المواسم",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp)
                )
            }
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(series.seasons) { season ->
                        val isSelected = season.number == viewModel.selectedSeasonNumber
                        Surface(
                            color = if (isSelected) BrandPurple else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.clickable { viewModel.selectSeason(season.number) }
                        ) {
                            Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "الموسم ${season.number}",
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${season.episodeCount} حلقة",
                                    color = if (isSelected) Color.White.copy(alpha = 0.85f) else MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }
                }
            }

            // Episodes list
            item {
                Text(
                    "حلقات الموسم ${viewModel.selectedSeasonNumber}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp)
                )
            }

            if (viewModel.isLoadingEpisodes) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = BrandPurple, strokeWidth = 2.dp, modifier = Modifier.size(28.dp))
                    }
                }
            } else {
                items(viewModel.episodes) { episode ->
                    EpisodeRow(
                        episode = episode,
                        onClick = { onPlayEpisode(seriesId, episode.seasonNumber, episode.number) }
                    )
                }
            }
            item { Spacer(Modifier.height(100.dp)) }
        }
    }
}

@Composable
private fun EpisodeRow(episode: Episode, onClick: () -> Unit) {
    GlassCard(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 5.dp),
        pressEffect = true,
        onClick = onClick
    ) {
        Row(modifier = Modifier.padding(8.dp)) {
            // Thumbnail with episode number badge
            Box(
                modifier = Modifier.size(120.dp, 70.dp).clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = episode.thumbnailUrl,
                    contentDescription = episode.title,
                    modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
                Box(
                    modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.4f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.PlayArrow, contentDescription = "تشغيل", tint = Color.White, modifier = Modifier.size(28.dp))
                }
                Surface(
                    color = BrandRed,
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier.align(Alignment.TopStart).padding(4.dp)
                ) {
                    Text(
                        text = "حلقة ${episode.number}",
                        color = Color.White,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                    )
                }
            }
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = episode.title,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2
                )
                if (episode.airDate != null) {
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = "بث: ${episode.airDate}",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp
                    )
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    text = episode.overview,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp,
                    maxLines = 2
                )
            }
        }
    }
}
