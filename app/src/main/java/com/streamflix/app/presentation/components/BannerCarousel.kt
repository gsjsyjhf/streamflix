package com.streamflix.app.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.streamflix.app.domain.model.Movie
import com.streamflix.app.presentation.theme.BrandPurple
import com.streamflix.app.presentation.theme.BrandRed
import kotlinx.coroutines.delay

/**
 * بانر سينمائي محسّن - يعرض 5 أفلام رائجة مع:
 * - تبديل تلقائي كل 6 ثوان
 * - تأثير slide + fade + scale بين البانرات
 * - شارة "مترجم" + شارة بلد المنشأ
 * - زر تشغيل + زر تفاصيل
 * - مؤشرات تفاعلية (dots) متحركة
 * - شارة "رائج" في الزاوية
 */
@Composable
fun BannerCarousel(
    movies: List<Movie>,
    onClick: (Movie) -> Unit
) {
    if (movies.isEmpty()) return
    val displayMovies = movies.take(5)
    var currentIndex by remember { mutableStateOf(0) }
    var autoRotate by remember { mutableStateOf(true) }

    // Auto-rotate every 6 seconds (slower for better reading)
    LaunchedEffect(displayMovies.size, autoRotate) {
        while (autoRotate) {
            delay(6000)
            currentIndex = (currentIndex + 1) % displayMovies.size
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(260.dp)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // Stack of banners with enhanced slide + fade + scale animation
        displayMovies.forEachIndexed { index, movie ->
            val visible = index == currentIndex
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(tween(600)) + scaleIn(tween(600), initialScale = 0.95f),
                exit = fadeOut(tween(600)) + scaleOut(tween(600), targetScale = 1.05f),
                modifier = Modifier.fillMaxSize()
            ) {
                BannerItem(
                    movie = movie,
                    rank = index + 1,
                    onClick = { onClick(movie) }
                )
            }
        }

        // Indicator dots - at bottom center, animated
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            displayMovies.forEachIndexed { index, _ ->
                val isSelected = index == currentIndex
                // Animated width change for selected dot
                val targetWidth = if (isSelected) 22.dp else 6.dp
                Box(
                    modifier = Modifier
                        .height(6.dp)
                        .width(targetWidth)
                        .clip(RoundedCornerShape(3.dp))
                        .background(
                            if (isSelected) BrandPurple else Color.White.copy(alpha = 0.4f)
                        )
                        .clickable {
                            currentIndex = index
                            autoRotate = false // لو المستخدم ضغط بنفسه، وقف الـ auto
                            // أعد التشغيل بعد 10 ثوان
                            autoRotate = true
                        }
                )
            }
        }
    }
}

@Composable
private fun BannerItem(movie: Movie, rank: Int, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(24.dp))
            .clickable(onClick = onClick)
    ) {
        AsyncImage(
            model = movie.backdropUrl,
            contentDescription = movie.title,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        // Gradient overlay - 3 colors for cinematic depth
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.5f),
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.4f),
                            Color.Black.copy(alpha = 0.9f)
                        )
                    )
                )
        )
        // Left side gradient for text legibility
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.7f),
                            Color.Black.copy(alpha = 0.2f),
                            Color.Transparent
                        )
                    )
                )
        )

        // شارة "رائج #1" - أعلى يمين
        Surface(
            color = BrandRed,
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.align(Alignment.TopEnd).padding(12.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("🔥", fontSize = 11.sp)
                Spacer(Modifier.width(2.dp))
                Text(
                    text = "رائج #$rank",
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // شارة "مترجم" - أعلى يسار
        if (!movie.isArabic) {
            Surface(
                color = BrandPurple,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.align(Alignment.TopStart).padding(12.dp)
            ) {
                Text(
                    text = "🌐 مترجم",
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }

        // Content - at bottom with more info
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(20.dp)
        ) {
            // Title - bigger and bolder
            Text(
                text = movie.title,
                color = Color.White,
                fontSize = 26.sp,
                fontWeight = FontWeight.Black,
                maxLines = 2
            )
            Spacer(Modifier.height(6.dp))
            // Meta info - year, country, genres
            val metaText = buildString {
                append(movie.releaseYear)
                if (movie.countryNameAr.isNotEmpty()) {
                    append(" • ")
                    append(movie.countryNameAr)
                }
                if (movie.genres.isNotEmpty()) {
                    append(" • ")
                    append(movie.genres.take(2).joinToString("، "))
                }
            }
            Text(
                text = metaText,
                color = Color.White.copy(alpha = 0.85f),
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
            // Overview - 1 line preview
            if (movie.overview.isNotBlank()) {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = movie.overview,
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 11.sp,
                    maxLines = 1
                )
            }
            Spacer(Modifier.height(10.dp))
            // Action buttons
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // Rating chip with star icon
                Surface(
                    color = Color(0xFFF59E0B).copy(alpha = 0.25f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.Star, contentDescription = null, tint = Color(0xFFF59E0B), modifier = Modifier.size(12.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(
                            String.format("%.1f", movie.rating),
                            color = Color(0xFFF59E0B),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                // Play button - gradient
                Surface(
                    color = BrandRed,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.clickable(onClick = onClick)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.PlayArrow, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("شاهد الآن", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
                // Details button - subtle outline
                Surface(
                    color = Color.White.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.clickable(onClick = onClick)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.Bookmark, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("تفاصيل", color = Color.White, fontWeight = FontWeight.Medium, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}
