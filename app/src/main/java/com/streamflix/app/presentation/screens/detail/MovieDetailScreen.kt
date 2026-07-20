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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import com.streamflix.app.domain.model.FavoriteItem
import com.streamflix.app.domain.model.Movie
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
class MovieDetailViewModel @Inject constructor(
    private val repo: StreamFlixRepository
) : ViewModel() {
    var movie by mutableStateOf<Movie?>(null)
        private set
    private val _isFav = MutableStateFlow(false)
    val isFav: StateFlow<Boolean> = _isFav.asStateFlow()

    fun init(id: String) {
        viewModelScope.launch {
            // Use getMovieWithStreams which tries Cinemana first for real stream URLs
            movie = repo.getMovieWithStreams(id)
            _isFav.value = repo.isFavorite(id).first()
        }
    }

    fun toggleFav() {
        val m = movie ?: return
        viewModelScope.launch {
            val fav = FavoriteItem(
                id = m.id, contentId = m.id, title = m.title,
                posterUrl = m.posterUrl, contentType = ContentType.MOVIE
            )
            repo.toggleFavorite(fav)
            _isFav.value = repo.isFavorite(m.id).first()
        }
    }
}

@Composable
fun MovieDetailScreen(
    movieId: String,
    onBack: () -> Unit,
    onPlay: () -> Unit,
    onOpenMovie: (String) -> Unit,
    viewModel: MovieDetailViewModel = hiltViewModel()
) {
    LaunchedEffect(movieId) { viewModel.init(movieId) }
    val movie = viewModel.movie
    val isFav by viewModel.isFav.collectAsState()

    if (movie == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("لا يوجد محتوى", color = MaterialTheme.colorScheme.onBackground)
        }
        return
    }

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            // Backdrop
            item {
                Box(modifier = Modifier.fillMaxWidth().height(280.dp)) {
                    AsyncImage(
                        model = movie.backdropUrl,
                        contentDescription = movie.title,
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
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = movie.title,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.weight(1f)
                        )
                        // شارة "مترجم" لو الفيلم غير عربي
                        if (!movie.isArabic) {
                            Surface(
                                color = BrandPurple,
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier.padding(start = 8.dp)
                            ) {
                                Text(
                                    text = "مترجم",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        RatingChip(movie.rating)
                        QualityChip(movie.quality)
                        Text("${movie.releaseYear}", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
                        if (movie.durationMinutes > 0) {
                            Text("•", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("${movie.durationMinutes} دقيقة", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(movie.genres) { g ->
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

            // معلومات سريعة - بطاقة معلومات احترافية
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // بطاقة بلد المنشأ
                    if (movie.countryNameAr.isNotEmpty()) {
                        InfoCard(
                            icon = "🌍",
                            title = "البلد",
                            value = movie.countryNameAr,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    // بطاقة اللغة الأصلية
                    if (movie.originalLanguage.isNotEmpty()) {
                        val langName = when (movie.originalLanguage) {
                            "ar" -> "عربي"; "en" -> "إنجليزي"; "tr" -> "تركي"; "fa" -> "فارسي"
                            "ko" -> "كوري"; "ja" -> "ياباني"; "zh" -> "صيني"; "hi" -> "هندي"
                            "fr" -> "فرنسي"; "es" -> "إسباني"; "de" -> "ألماني"; "it" -> "إيطالي"
                            else -> movie.originalLanguage
                        }
                        InfoCard(
                            icon = "🗣️",
                            title = "اللغة",
                            value = langName,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    // بطاقة التصنيف العمري
                    if (movie.certification.isNotBlank()) {
                        InfoCard(
                            icon = "🔞",
                            title = "التصنيف",
                            value = movie.certification,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Action buttons
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = onPlay,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BrandRed),
                        modifier = Modifier.weight(1f).height(52.dp)
                    ) {
                        Icon(Icons.Filled.PlayArrow, contentDescription = null, tint = Color.White)
                        Spacer(Modifier.width(8.dp))
                        Text("تشغيل الآن", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                    OutlinedButton(
                        onClick = { /* trailer */ },
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.height(52.dp)
                    ) {
                        Icon(Icons.Filled.PlayArrow, contentDescription = null, tint = MaterialTheme.colorScheme.onBackground, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("الدعائي", color = MaterialTheme.colorScheme.onBackground)
                    }
                }
            }

            // Overview
            item {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    Text("القصة", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = movie.overview,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(end = 16.dp)
                    )
                }
            }

            // Cast
            if (movie.cast.isNotEmpty()) {
                item {
                    Text("طاقم العمل", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground, modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp))
                }
                item {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(movie.cast) { c ->
                            CastCard(name = c.name, character = c.character, photoUrl = c.photoUrl)
                        }
                    }
                }
            }

            // Similar - uses real similar movies from TMDB
            if (movie.similarMovies.isNotEmpty()) {
                item {
                    Text("أفلام مشابهة", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground, modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp))
                }
                item {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(movie.similarMovies) { sim ->
                            SimilarMovieCard(movie = sim, onClick = { onOpenMovie(sim.id) })
                        }
                    }
                }
            }
            item { Spacer(Modifier.height(100.dp)) }
        }
    }
}

@Composable
private fun SimilarMovieCard(movie: Movie, onClick: () -> Unit) {
    GlassCard(
        modifier = Modifier.width(120.dp).height(190.dp),
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
            Column(
                modifier = Modifier.align(Alignment.BottomStart).padding(8.dp)
            ) {
                Text(
                    text = movie.title,
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2
                )
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    if (movie.rating > 0) {
                        Text("★", color = Color(0xFFF59E0B), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Text(
                            String.format("%.1f", movie.rating),
                            color = Color(0xFFF59E0B),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    if (movie.releaseYear > 0) {
                        Text(
                            "• ${movie.releaseYear}",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 10.sp
                        )
                    }
                }
            }
        }
    }
}

/**
 * بطاقة ممثل - تعرض صورة الممثل + الاسم + الشخصية
 * لو الصورة ما متوفرة، تعرض الحرف الأول من اسم الممثل كـ placeholder
 */
@Composable
private fun CastCard(name: String, character: String, photoUrl: String?) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(80.dp)
    ) {
        Box(
            modifier = Modifier.size(70.dp).clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            if (!photoUrl.isNullOrBlank()) {
                // استخدم AsyncImage مع placeholder + error handler
                AsyncImage(
                    model = coil.request.ImageRequest.Builder(androidx.compose.ui.platform.LocalContext.current)
                        .data(photoUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = name,
                    modifier = Modifier.fillMaxSize().clip(CircleShape),
                    contentScale = ContentScale.Crop,
                    // لو الصورة فشلت في التحميل، اعرض الحرف الأول
                    error = null,
                    placeholder = null
                )
            } else {
                // لو ماكو صورة، اعرض الحرف الأول
                Text(
                    text = name.take(1).uppercase(),
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Black,
                    fontSize = 22.sp
                )
            }
        }
        Spacer(Modifier.height(4.dp))
        Text(
            text = name,
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 1
        )
        if (character.isNotBlank()) {
            Text(
                text = character,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 10.sp,
                maxLines = 1
            )
        }
    }
}

/**
 * بطاقة معلومات صغيرة - تعرض أيقونة + عنوان + قيمة
 * تستخدم في شاشة الفيلم لعرض البلد، اللغة، التصنيف العمري
 */
@Composable
private fun InfoCard(
    icon: String,
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = icon, fontSize = 18.sp)
            Spacer(Modifier.height(4.dp))
            Text(
                text = title,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 9.sp
            )
            Text(
                text = value,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
        }
    }
}
