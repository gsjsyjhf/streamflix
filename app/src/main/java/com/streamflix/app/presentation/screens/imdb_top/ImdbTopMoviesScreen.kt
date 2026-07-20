package com.streamflix.app.presentation.screens.imdb_top

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
import com.streamflix.app.domain.model.Movie
import com.streamflix.app.presentation.components.GlassCard
import com.streamflix.app.presentation.components.RatingChip
import com.streamflix.app.presentation.components.ShimmerBox
import com.streamflix.app.presentation.components.TopRankBadge
import com.streamflix.app.presentation.theme.BrandGold
import com.streamflix.app.presentation.theme.BrandRed
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ImdbTopUiState(
    val isLoading: Boolean = true,
    val movies: List<Movie> = emptyList()
)

@HiltViewModel
class ImdbTopViewModel @Inject constructor(
    private val repo: StreamFlixRepository
) : ViewModel() {
    private val _state = MutableStateFlow(ImdbTopUiState())
    val state: StateFlow<ImdbTopUiState> = _state.asStateFlow()

    init { load() }

    fun load() {
        _state.value = _state.value.copy(isLoading = true)
        viewModelScope.launch {
            val movies = repo.getAllImdbTopMovies(limit = 100)
            _state.value = ImdbTopUiState(isLoading = false, movies = movies)
        }
    }
}

@Composable
fun ImdbTopMoviesScreen(
    onBack: () -> Unit,
    onOpenMovie: (String) -> Unit,
    viewModel: ImdbTopViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        // Top bar
        Row(
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Filled.ArrowBack, contentDescription = "رجوع", tint = MaterialTheme.colorScheme.onBackground)
            }
            Column(modifier = Modifier.padding(start = 8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "🏆 ",
                        fontSize = 18.sp
                    )
                    Text(
                        text = "أفضل 250 فيلم - IMDB",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
                if (!state.isLoading) {
                    Text(
                        text = "${state.movies.size} فيلم",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp
                    )
                }
            }
        }

        if (state.isLoading) {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                repeat(8) {
                    item {
                        ShimmerBox(
                            modifier = Modifier.fillMaxWidth(),
                            height = 130.dp,
                            cornerRadius = 12.dp
                        )
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                itemsIndexed(state.movies) { index, movie ->
                    ImdbTopRankRow(
                        rank = index + 1,
                        movie = movie,
                        onClick = { onOpenMovie(movie.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ImdbTopRankRow(rank: Int, movie: Movie, onClick: () -> Unit) {
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        pressEffect = true,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Top rank badge - big for top 3
            Box(modifier = Modifier.padding(horizontal = 4.dp)) {
                TopRankBadge(rank = rank)
            }
            Spacer(Modifier.width(8.dp))
            // Poster
            Box(
                modifier = Modifier.size(70.dp, 100.dp).clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                if (movie.posterUrl != null) {
                    AsyncImage(
                        model = movie.posterUrl,
                        contentDescription = movie.title,
                        modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Text("🎬", fontSize = 24.sp)
                }
            }
            Spacer(Modifier.width(10.dp))
            // Info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = movie.title,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2
                )
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (movie.releaseYear > 0) {
                        Text(
                            text = "${movie.releaseYear}",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.sp
                        )
                    }
                    if (movie.rating > 0) {
                        RatingChip(movie.rating)
                    }
                }
                Spacer(Modifier.height(4.dp))
                if (movie.overview.isNotBlank()) {
                    Text(
                        text = movie.overview,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 10.sp,
                        maxLines = 2
                    )
                }
            }
        }
    }
}
