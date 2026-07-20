package com.streamflix.app.presentation.screens.sports

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil.compose.AsyncImage
import com.streamflix.app.data.remote.api.BeinApi
import com.streamflix.app.data.remote.api.BeinChannelDef
import com.streamflix.app.data.remote.dto.bein.BeinMatchDto
import com.streamflix.app.data.repository.StreamFlixRepository
import com.streamflix.app.presentation.components.GlassCard
import com.streamflix.app.presentation.components.LiveBadge
import com.streamflix.app.presentation.components.ShimmerBox
import com.streamflix.app.presentation.theme.BrandPurple
import com.streamflix.app.presentation.theme.BrandRed
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone
import javax.inject.Inject

// ===================== ViewModel =====================

@HiltViewModel
class SportsViewModel @Inject constructor(
    private val repo: StreamFlixRepository
) : ViewModel() {

    private val _matches = MutableStateFlow<List<BeinMatchDto>>(emptyList())
    val matches: StateFlow<List<BeinMatchDto>> = _matches.asStateFlow()

    private val _channels = MutableStateFlow<List<BeinChannelDef>>(emptyList())
    val channels: StateFlow<List<BeinChannelDef>> = _channels.asStateFlow()

    private val _loading = MutableStateFlow(true)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    init {
        _channels.value = repo.getBeinChannels()
        loadMatches()
    }

    fun loadMatches() {
        _loading.value = true
        viewModelScope.launch {
            _matches.value = repo.getBeinMatches()
            _loading.value = false
        }
    }

    fun findChannel(channelId: String): BeinChannelDef? = repo.findBeinChannel(channelId)
}

// ===================== Main Screen =====================

@Composable
fun SportsScreen(
    onOpenMatch: (String) -> Unit,
    onOpenChannels: () -> Unit,
    viewModel: SportsViewModel = hiltViewModel()
) {
    val matches by viewModel.matches.collectAsState()
    val channels by viewModel.channels.collectAsState()
    val loading by viewModel.loading.collectAsState()

    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("مباريات قادمة", "قنوات BEIN")

    val onPlayBeinChannel: (BeinChannelDef) -> Unit = { channel ->
        onOpenMatch("bein_channel:${channel.id}")
    }

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "الرياضة ⚽",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = { viewModel.loadMatches() }) {
                Icon(
                    imageVector = Icons.Filled.Refresh,
                    contentDescription = "تحديث",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        // Tabs
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(tabs.size) { i ->
                TabChip(
                    text = tabs[i],
                    selected = selectedTab == i,
                    onClick = { selectedTab = i }
                )
            }
        }

        Spacer(Modifier.height(12.dp))
        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

        when (selectedTab) {
            0 -> UpcomingMatchesTab(matches, loading, onPlayChannel = { channelId ->
                viewModel.findChannel(channelId)?.let { onPlayBeinChannel(it) }
            })
            1 -> BeinChannelsTab(channels, onPlayChannel = onPlayBeinChannel)
        }
    }
}

// ===================== Tab 1: Upcoming Matches =====================

@Composable
private fun UpcomingMatchesTab(
    matches: List<BeinMatchDto>,
    loading: Boolean,
    onPlayChannel: (String) -> Unit
) {
    if (loading) {
        LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            repeat(6) { item { ShimmerBox(height = 130.dp) } }
        }
        return
    }
    if (matches.isEmpty()) {
        EmptyStateView("لا توجد مباريات قادمة حالياً\nتابعنا لاحقاً!")
        return
    }

    val grouped = matches.groupBy { formatMatchDate(it.date) }
    val nextMatch = matches.firstOrNull()

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Surface(
                color = BrandRed.copy(alpha = 0.12f),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Filled.SportsSoccer, contentDescription = null, tint = BrandRed)
                    Spacer(Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "مباريات قادمة على BEIN Sports",
                            color = MaterialTheme.colorScheme.onBackground,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "${matches.size} مباراة في الانتظار • اضغط للمشاهدة",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        // بطاقة المباراة القادمة المميزة مع عداد تنازلي
        if (nextMatch != null) {
            item {
                FeaturedMatchCard(nextMatch, onPlay = {
                    nextMatch.channel_id?.let { onPlayChannel(it) }
                })
            }
        }

        grouped.forEach { (dateLabel, dayMatches) ->
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Filled.Schedule, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = dateLabel,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.width(8.dp))
                    Surface(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), shape = CircleShape) {
                        Text(
                            text = "${dayMatches.size}",
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
            items(dayMatches) { match ->
                UpcomingMatchCard(match, onPlay = {
                    match.channel_id?.let { onPlayChannel(it) }
                })
            }
        }
    }
}

// ===================== Featured Match Card (Next Match with Countdown) =====================

@Composable
private fun FeaturedMatchCard(match: BeinMatchDto, onPlay: () -> Unit) {
    val team1Ar = teamNameAr(match.team1)
    val team2Ar = teamNameAr(match.team2)
    val leagueAr = leagueNameAr(match.league)
    val flag1Url = teamFlagUrl(match.team1)
    val flag2Url = teamFlagUrl(match.team2)

    // حساب وقت المباراة (ميلي ثانية)
    val matchTimeMs = remember(match.date, match.time) { calculateMatchTimeMs(match.date, match.time) }
    var currentTime by remember { mutableStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            currentTime = System.currentTimeMillis()
            kotlinx.coroutines.delay(1000)
        }
    }
    val remainingMs = (matchTimeMs - currentTime).coerceAtLeast(0)
    val days = (remainingMs / (1000 * 60 * 60 * 24)).toInt()
    val hours = ((remainingMs / (1000 * 60 * 60)) % 24).toInt()
    val minutes = ((remainingMs / (1000 * 60)) % 60).toInt()
    val seconds = ((remainingMs / 1000) % 60).toInt()
    val isLive = remainingMs <= 0

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onPlay() },
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Top: League + "المباراة القادمة"
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = leagueAr,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
                Spacer(Modifier.weight(1f))
                Text(
                    text = "المباراة القادمة",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(Modifier.height(12.dp))
            // Teams with flags
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    TeamFlagOrInitials(
                        flagUrl = flag1Url,
                        name = team1Ar,
                        color = BrandPurple,
                        size = 60.dp
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = team1Ar,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        maxLines = 2,
                        textAlign = TextAlign.Center
                    )
                }
                // VS + countdown
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "VS",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black
                    )
                    Spacer(Modifier.height(6.dp))
                    if (isLive) {
                        LiveBadge()
                    } else {
                        Surface(
                            color = BrandRed.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = if (days > 0) "${days}ي ${hours}س ${minutes}د"
                                       else if (hours > 0) "${hours}:${String.format("%02d", minutes)}:${String.format("%02d", seconds)}"
                                       else "${minutes}:${String.format("%02d", seconds)}",
                                color = BrandRed,
                                fontWeight = FontWeight.Black,
                                fontSize = 14.sp,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    TeamFlagOrInitials(
                        flagUrl = flag2Url,
                        name = team2Ar,
                        color = BrandRed,
                        size = 60.dp
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = team2Ar,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        maxLines = 2,
                        textAlign = TextAlign.Center
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
            // Channel + Time
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(BrandRed.copy(alpha = 0.08f))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                BeinLogo(size = 22.dp)
                Spacer(Modifier.width(8.dp))
                Text(
                    text = match.channel ?: "BEIN Sports",
                    color = BrandRed,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "${match.time ?: "--:--"} UTC",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.width(8.dp))
                Icon(Icons.Filled.ArrowForward, contentDescription = null, tint = BrandRed, modifier = Modifier.size(16.dp))
            }
        }
    }
}

// ===================== Team Flag or Initials =====================

@Composable
private fun TeamFlagOrInitials(
    flagUrl: String?,
    name: String,
    color: androidx.compose.ui.graphics.Color,
    size: androidx.compose.ui.unit.Dp
) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(color.copy(alpha = 0.15f))
            .border(2.dp, color.copy(alpha = 0.4f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        if (flagUrl != null) {
            AsyncImage(
                model = flagUrl,
                contentDescription = name,
                modifier = Modifier
                    .size(size * 0.75f)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop,
                error = null,
                placeholder = null
            )
        } else {
            // Fallback: الأحرف الأولى (للأندية)
            Text(
                text = teamInitialsAr(name),
                color = color,
                fontWeight = FontWeight.Black,
                fontSize = (size.value / 3.5f).sp
            )
        }
    }
}

@Composable
private fun UpcomingMatchCard(match: BeinMatchDto, onPlay: () -> Unit) {
    val team1Ar = teamNameAr(match.team1)
    val team2Ar = teamNameAr(match.team2)
    val leagueAr = leagueNameAr(match.league)
    val flag1Url = teamFlagUrl(match.team1)
    val flag2Url = teamFlagUrl(match.team2)

    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        pressEffect = true,
        onClick = onPlay
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Top row: time + league
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    color = BrandPurple.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = "${match.time ?: "--:--"} UTC",
                        color = BrandPurple,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
                Spacer(Modifier.width(8.dp))
                Text(
                    text = leagueAr,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.labelSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                    fontSize = 11.sp
                )
                if (match.live == true) {
                    LiveBadge()
                }
            }
            Spacer(Modifier.height(12.dp))
            // Teams row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    TeamFlagOrInitials(
                        flagUrl = flag1Url,
                        name = team1Ar,
                        color = BrandPurple,
                        size = 48.dp
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = team1Ar,
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 2,
                        textAlign = TextAlign.Center,
                        fontSize = 11.sp
                    )
                }
                Text(
                    text = "VS",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black
                )
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    TeamFlagOrInitials(
                        flagUrl = flag2Url,
                        name = team2Ar,
                        color = BrandRed,
                        size = 48.dp
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = team2Ar,
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 2,
                        textAlign = TextAlign.Center,
                        fontSize = 11.sp
                    )
                }
            }
            Spacer(Modifier.height(10.dp))
            // Channel badge with BEIN logo
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(BrandRed.copy(alpha = 0.08f))
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                BeinLogo(size = 18.dp)
                Spacer(Modifier.width(6.dp))
                Text(
                    text = match.channel ?: "BEIN Sports",
                    color = BrandRed,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "اضغط للمشاهدة ←",
                    color = BrandRed,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// ===================== Tab 2: BEIN Channels Grid =====================

@Composable
private fun BeinChannelsTab(
    channels: List<BeinChannelDef>,
    onPlayChannel: (BeinChannelDef) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item(span = { GridItemSpan(2) }) {
            Surface(
                color = BrandRed.copy(alpha = 0.1f),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, BrandRed.copy(alpha = 0.3f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BeinLogo(size = 48.dp)
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "12 قناة BEIN Sports",
                            color = MaterialTheme.colorScheme.onBackground,
                            fontWeight = FontWeight.Black,
                            fontSize = 15.sp
                        )
                        Text(
                            text = "بث مباشر HD • اضغط للعرض",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }
        items(channels) { channel ->
            BeinChannelCard(channel, onClick = { onPlayChannel(channel) })
        }
    }
}

@Composable
private fun BeinChannelCard(channel: BeinChannelDef, onClick: () -> Unit) {
    val isMax = channel.group == "max"
    val accentColor = if (isMax) BrandRed else BrandPurple
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        pressEffect = true,
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // شعار BEIN المحلي داخل إطار ملوّن
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(accentColor.copy(alpha = 0.08f))
                    .border(1.dp, accentColor.copy(alpha = 0.25f), RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                BeinLogo(size = 50.dp)
            }
            Spacer(Modifier.height(8.dp))
            Text(
                text = channel.name,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(6.dp))
            // Channel number badge
            Surface(
                color = accentColor.copy(alpha = 0.15f),
                shape = RoundedCornerShape(6.dp)
            ) {
                Text(
                    text = channel.iconLabel,
                    color = accentColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                )
            }
            Spacer(Modifier.height(4.dp))
            Surface(
                color = BrandRed.copy(alpha = 0.15f),
                shape = RoundedCornerShape(4.dp)
            ) {
                Text(
                    text = "● مباشر",
                    color = BrandRed,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }
    }
}

// ===================== BEIN Logo (Local Drawable) =====================

@Composable
private fun BeinLogo(size: androidx.compose.ui.unit.Dp = 40.dp) {
    val context = LocalContext.current
    val resId = remember(context) {
        context.resources.getIdentifier("bein_sports_logo", "drawable", context.packageName)
    }
    if (resId != 0) {
        val painter = painterResource(id = resId)
        androidx.compose.foundation.Image(
            painter = painter,
            contentDescription = "BEIN Sports",
            modifier = Modifier.size(size),
            contentScale = ContentScale.Fit
        )
    } else {
        // Fallback: نص BEIN
        Box(
            modifier = Modifier.size(size).clip(RoundedCornerShape(4.dp)).background(BrandRed),
            contentAlignment = Alignment.Center
        ) {
            Text("beIN", color = androidx.compose.ui.graphics.Color.White, fontSize = (size.value / 3).sp, fontWeight = FontWeight.Black)
        }
    }
}

// ===================== Shared UI =====================

@Composable
private fun TabChip(text: String, selected: Boolean, onClick: () -> Unit) {
    Surface(
        color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier
            .clickable { onClick() }
            .border(
                width = 1.dp,
                color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                shape = RoundedCornerShape(20.dp)
            )
    ) {
        Text(
            text = text,
            color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            fontSize = 13.sp
        )
    }
}

@Composable
private fun EmptyStateView(message: String) {
    Box(modifier = Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Filled.SportsSoccer, contentDescription = null, tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(48.dp))
            Spacer(Modifier.height(8.dp))
            Text(
                message,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                fontSize = 13.sp
            )
        }
    }
}

// ===================== Helpers =====================

private fun formatMatchDate(dateStr: String?): String {
    if (dateStr.isNullOrBlank()) return "تاريخ غير محدد"
    return runCatching {
        val fmt = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        val date = fmt.parse(dateStr) ?: return@runCatching dateStr
        val today = Calendar.getInstance().get(Calendar.DAY_OF_YEAR)
        val todayYear = Calendar.getInstance().get(Calendar.YEAR)
        val matchCal = Calendar.getInstance().apply { time = date }
        val matchDay = matchCal.get(Calendar.DAY_OF_YEAR)
        val matchYear = matchCal.get(Calendar.YEAR)
        val diff = if (todayYear == matchYear) matchDay - today
                   else (matchYear - todayYear) * 365 + (matchDay - today)
        val outFmt = SimpleDateFormat("EEEE d MMMM yyyy", Locale("ar"))
        when (diff) {
            0 -> "اليوم"
            1 -> "غداً"
            else -> outFmt.format(date)
        }
    }.getOrDefault(dateStr)
}

/**
 * يحسب وقت المباراة بالمللي ثانية (UTC) من التاريخ والوقت
 * مثال: date="2026-07-19", time="00:00" → 1784400000000
 */
private fun calculateMatchTimeMs(dateStr: String?, timeStr: String?): Long {
    if (dateStr.isNullOrBlank()) return 0L
    return runCatching {
        val dateTimeStr = if (timeStr.isNullOrBlank()) "$dateStr 00:00" else "$dateStr $timeStr"
        val fmt = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        fmt.parse(dateTimeStr)?.time ?: 0L
    }.getOrDefault(0L)
}
