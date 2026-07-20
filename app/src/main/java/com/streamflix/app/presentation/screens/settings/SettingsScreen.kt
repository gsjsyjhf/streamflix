package com.streamflix.app.presentation.screens.settings

import android.content.Context
import android.content.Intent
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Brightness6
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Surface
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.streamflix.app.presentation.components.GlassCard
import com.streamflix.app.presentation.theme.BrandPurple
import com.streamflix.app.presentation.theme.SettingsViewModel
import com.streamflix.app.util.AppLanguage
import com.streamflix.app.util.FontSizeScale
import com.streamflix.app.util.ThemeMode
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(viewModel: SettingsViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var showAbout by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 110.dp)
        ) {
            item {
                Text(
                    text = "الإعدادات",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(16.dp)
                )
            }

            // Theme
            item { SectionTitle("المظهر") }
            item {
                GlassCard(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("الوضع", fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.height(8.dp))
                        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                            ThemeMode.entries.forEachIndexed { i, mode ->
                                SegmentedButton(
                                    selected = state.themeMode == mode,
                                    onClick = { viewModel.setTheme(mode) },
                                    shape = SegmentedButtonDefaults.itemShape(i, ThemeMode.entries.size),
                                    label = { Text(when (mode) {
                                        ThemeMode.LIGHT -> "فاتح"
                                        ThemeMode.DARK -> "ليلي"
                                        ThemeMode.SYSTEM -> "تلقائي"
                                    }) },
                                    icon = { Icon(when (mode) {
                                        ThemeMode.LIGHT -> Icons.Filled.LightMode
                                        ThemeMode.DARK -> Icons.Filled.DarkMode
                                        ThemeMode.SYSTEM -> Icons.Filled.Brightness6
                                    }, contentDescription = null) }
                                )
                            }
                        }
                    }
                }
            }

            // Font
            item { SectionTitle("حجم الخط") }
            item {
                GlassCard(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                            FontSizeScale.entries.forEachIndexed { i, scale ->
                                SegmentedButton(
                                    selected = state.fontScale == scale,
                                    onClick = { viewModel.setFontScale(scale) },
                                    shape = SegmentedButtonDefaults.itemShape(i, FontSizeScale.entries.size),
                                    label = { Text(when (scale) {
                                        FontSizeScale.SMALL -> "صغير"
                                        FontSizeScale.MEDIUM -> "متوسط"
                                        FontSizeScale.LARGE -> "كبير"
                                    }) }
                                )
                            }
                        }
                    }
                }
            }

            // Language
            item { SectionTitle("اللغة") }
            item {
                GlassCard(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                            AppLanguage.entries.forEachIndexed { i, lang ->
                                SegmentedButton(
                                    selected = state.language == lang,
                                    onClick = { viewModel.setLanguage(lang) },
                                    shape = SegmentedButtonDefaults.itemShape(i, AppLanguage.entries.size),
                                    label = { Text(when (lang) {
                                        AppLanguage.AR -> "العربية"
                                        AppLanguage.EN -> "English"
                                    }) }
                                )
                            }
                        }
                    }
                }
            }

            // General
            item { SectionTitle("عام") }
            item {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    SettingsRow(Icons.Filled.CleaningServices, "حذف الكاش", "اضغط للمسح") {
                        scope.launch {
                            clearAppCache(context)
                            snackbarHostState.showSnackbar("تم حذف الكاش")
                        }
                    }
                    SettingsRow(Icons.Filled.Share, "مشاركة التطبيق", null) {
                        val sendIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, "جرّب تطبيق StreamFlix - منصة الترفيه المتكاملة")
                            type = "text/plain"
                        }
                        context.startActivity(Intent.createChooser(sendIntent, "مشاركة عبر"))
                    }
                    SettingsRow(Icons.Filled.RateReview, "تقييم التطبيق", null) {
                        scope.launch { snackbarHostState.showSnackbar("شكراً للتقييم! 🌟") }
                    }
                    SettingsRow(Icons.Filled.Info, "حول التطبيق", "الإصدار 2.7.14 • @i55544") {
                        showAbout = true
                    }
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 100.dp)
        ) { data -> Snackbar(snackbarData = data) }
    }

    if (showAbout) {
        AboutDialog(onDismiss = { showAbout = false })
    }
}

private fun clearAppCache(context: Context) {
    try { context.cacheDir?.deleteRecursively() } catch (_: Exception) { }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
    )
}

@Composable
private fun SettingsRow(icon: ImageVector, title: String, subtitle: String?, onClick: () -> Unit) {
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        pressEffect = true,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(40.dp).clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Medium)
                if (subtitle != null) {
                    Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
private fun AboutDialog(onDismiss: () -> Unit) {
    val context = LocalContext.current
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                androidx.compose.foundation.layout.Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(BrandPurple)
                ) {}
                Spacer(Modifier.width(12.dp))
                Column {
                    Text("StreamFlix", fontWeight = FontWeight.Bold, color = BrandPurple)
                    Text("v2.7.14", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        },
        text = {
            Column {
                Text(
                    "منصة ترفيهية متكاملة تجمع الأفلام والمسلسلات والأنمي والرياضة والبث المباشر في مكان واحد. يدعم محتوى من جميع الدول العربية والعالمية.",
                    fontSize = 12.sp
                )
                Spacer(Modifier.height(16.dp))

                // المطور
                Text("المطوّر", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "@i55544",
                    color = BrandPurple,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    modifier = Modifier.clickable {
                        openUrl(context, "https://t.me/nadoremalf")
                    }
                )

                Spacer(Modifier.height(16.dp))

                // القنوات
                Text("قنوات تلغرام", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(8.dp))

                // قناة 1
                Surface(
                    color = Color(0xFF0088cc).copy(alpha = 0.15f),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth().clickable {
                        openUrl(context, "https://t.me/nadoremalf")
                    }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.Send, contentDescription = null, tint = Color(0xFF0088cc), modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("nadoremalf", color = Color(0xFF0088cc), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            Text("t.me/nadoremalf", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.sp)
                        }
                    }
                }

                Spacer(Modifier.height(6.dp))

                // قناة 2
                Surface(
                    color = Color(0xFF0088cc).copy(alpha = 0.15f),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth().clickable {
                        openUrl(context, "https://t.me/TOOPENK")
                    }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.Send, contentDescription = null, tint = Color(0xFF0088cc), modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("TOOPENK", color = Color(0xFF0088cc), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            Text("t.me/TOOPENK", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.sp)
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))
                Text(
                    "© 2025 جميع الحقوق محفوظة للمطوّر @i55544",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 10.sp
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("حسناً") }
        }
    )
}

private fun openUrl(context: android.content.Context, url: String) {
    val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(url))
        .addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
    runCatching { context.startActivity(intent) }
}
