package com.streamflix.app.presentation.screens.splash

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.streamflix.app.presentation.theme.BrandPurple
import com.streamflix.app.presentation.theme.BrandRed
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onDone: () -> Unit) {
    var showLogo by remember { mutableStateOf(false) }
    var showTitle by remember { mutableStateOf(false) }
    var showSubtitle by remember { mutableStateOf(false) }
    var loadingTextIndex by remember { mutableIntStateOf(0) }

    val loadingPhrases = listOf(
        "جاري تشغيل محرك الترفيه...",
        "تجهيز المكتبة السينمائية...",
        "تحميل القنوات والبث المباشر...",
        "أهلاً بك في StreamFlix..."
    )

    // Text rotation timer
    LaunchedEffect(Unit) {
        while (true) {
            delay(500)
            loadingTextIndex = (loadingTextIndex + 1) % loadingPhrases.size
        }
    }

    // Sequence timing
    LaunchedEffect(Unit) {
        delay(250)
        showLogo = true
        delay(400)
        showTitle = true
        delay(300)
        showSubtitle = true
        delay(2200) // Total splash duration
        onDone()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF1B1833),
                        Color(0xFF090813),
                        Color(0xFF050409)
                    ),
                    radius = 1200f
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        // === Glowing Background Ambient Orbs ===
        val infiniteTransition = rememberInfiniteTransition(label = "splashGlow")
        val orbScale by infiniteTransition.animateFloat(
            initialValue = 0.85f,
            targetValue = 1.15f,
            animationSpec = infiniteRepeatable(tween(1800, easing = LinearEasing), RepeatMode.Reverse),
            label = "orbScale"
        )

        Box(
            modifier = Modifier
                .size(340.dp)
                .scale(orbScale)
                .blur(80.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            BrandPurple.copy(alpha = 0.4f),
                            BrandRed.copy(alpha = 0.2f),
                            Color.Transparent
                        )
                    )
                )
        )

        // === Central Content Column ===
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(24.dp)
        ) {
            // Animated Logo Icon
            AnimatedVisibility(
                visible = showLogo,
                enter = scaleIn(tween(600), initialScale = 0.2f) + fadeIn(tween(400))
            ) {
                val pulseAnim by infiniteTransition.animateFloat(
                    initialValue = 1f,
                    targetValue = 1.08f,
                    animationSpec = infiniteRepeatable(tween(1000, easing = LinearEasing), RepeatMode.Reverse),
                    label = "logoPulse"
                )

                Box(
                    modifier = Modifier
                        .size(130.dp)
                        .scale(pulseAnim)
                        .clip(RoundedCornerShape(36.dp))
                        .background(
                            Brush.linearGradient(
                                colors = listOf(BrandPurple, BrandRed)
                            )
                        )
                        .border(
                            width = 2.dp,
                            brush = Brush.linearGradient(
                                colors = listOf(Color.White.copy(alpha = 0.7f), Color.Transparent)
                            ),
                            shape = RoundedCornerShape(36.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(96.dp)
                            .clip(RoundedCornerShape(26.dp))
                            .background(Color.Black.copy(alpha = 0.25f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "▶",
                            color = Color.White,
                            fontSize = 58.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }

            Spacer(Modifier.height(32.dp))

            // App Title with Gradient
            AnimatedVisibility(
                visible = showTitle,
                enter = fadeIn(tween(500)) + slideInVertically(tween(500), initialOffsetY = { it / 2 })
            ) {
                Text(
                    text = "StreamFlix",
                    fontSize = 46.sp,
                    fontWeight = FontWeight.Black,
                    style = androidx.compose.ui.text.TextStyle(
                        brush = Brush.horizontalGradient(
                            colors = listOf(Color.White, Color(0xFFE0E0E0), BrandPurple)
                        )
                    ),
                    textAlign = TextAlign.Center
                )
            }

            Spacer(Modifier.height(12.dp))

            // Subtitle / Version Badge
            AnimatedVisibility(
                visible = showSubtitle,
                enter = fadeIn(tween(500))
            ) {
                Surface(
                    color = Color.White.copy(alpha = 0.08f),
                    shape = RoundedCornerShape(20.dp),
                    border = androidx.compose.foundation.BorderStroke(0.5.dp, Color.White.copy(alpha = 0.15f))
                ) {
                    Text(
                        text = "✨ الإصدار الاحترافي v2.7.21 (مع التحسينات السينمائية)",
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
            }

            Spacer(Modifier.height(48.dp))

            // Loading Indicator & Dynamic Status Text
            AnimatedVisibility(
                visible = showSubtitle,
                enter = fadeIn(tween(400))
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(36.dp),
                        color = BrandPurple,
                        strokeWidth = 3.dp,
                        trackColor = Color.White.copy(alpha = 0.1f)
                    )

                    Text(
                        text = loadingPhrases[loadingTextIndex],
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
