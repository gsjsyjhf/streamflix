package com.streamflix.app.presentation.screens.channels

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.streamflix.app.domain.model.Channel
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
import javax.inject.Inject

@HiltViewModel
class ChannelsViewModel @Inject constructor(repo: StreamFlixRepository) : ViewModel() {
    private val _arabic = MutableStateFlow<List<Channel>>(emptyList())
    val arabic: StateFlow<List<Channel>> = _arabic.asStateFlow()
    private val _news = MutableStateFlow<List<Channel>>(emptyList())
    val news: StateFlow<List<Channel>> = _news.asStateFlow()
    private val _all = MutableStateFlow<List<Channel>>(emptyList())
    val all: StateFlow<List<Channel>> = _all.asStateFlow()
    private val _loading = MutableStateFlow(true)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    init {
        viewModelScope.launch {
            _arabic.value = repo.getArabicChannels()
            _news.value = repo.getNewsChannels()
            _all.value = repo.getLiveChannels()
            _loading.value = false
        }
    }
}

@Composable
fun LiveChannelsScreen(
    onBack: () -> Unit,
    onOpenChannel: (String) -> Unit,
    viewModel: ChannelsViewModel = hiltViewModel()
) {
    val arabic by viewModel.arabic.collectAsState()
    val news by viewModel.news.collectAsState()
    val all by viewModel.all.collectAsState()
    val loading by viewModel.loading.collectAsState()
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("الكل", "عربية", "أخبار دولية")

    val channels = when (selectedTab) {
        0 -> all
        1 -> arabic
        2 -> news
        else -> all
    }

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Box(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
            IconButton(onClick = onBack) {
                Icon(Icons.Filled.ArrowBack, contentDescription = "رجوع", tint = MaterialTheme.colorScheme.onBackground)
            }
            Text(
                text = "القنوات المباشرة",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.background,
            contentColor = MaterialTheme.colorScheme.primary
        ) {
            tabs.forEachIndexed { i, t ->
                Tab(
                    selected = selectedTab == i,
                    onClick = { selectedTab = i },
                    text = { Text(t, fontWeight = if (selectedTab == i) FontWeight.Bold else FontWeight.Normal) }
                )
            }
        }

        if (loading) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                repeat(6) { item { ShimmerBox(height = 140.dp) } }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(channels) { channel ->
                    ChannelCardItem(channel, onClick = { onOpenChannel(channel.id) })
                }
            }
        }
    }
}

@Composable
private fun ChannelCardItem(channel: Channel, onClick: () -> Unit) {
    GlassCard(
        modifier = Modifier.fillMaxWidth().height(150.dp),
        pressEffect = true,
        onClick = onClick
    ) {
        Box(
            modifier = Modifier.fillMaxSize().background(Brush.linearGradient(listOf(BrandPurple, BrandRed))),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(8.dp)) {
                if (channel.logoUrl != null) {
                    AsyncImage(
                        model = channel.logoUrl,
                        contentDescription = channel.name,
                        modifier = Modifier.fillMaxWidth().height(60.dp),
                        contentScale = ContentScale.Fit
                    )
                } else {
                    Box(
                        modifier = Modifier.fillMaxWidth().height(60.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("📺", fontSize = 32.sp)
                    }
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    text = channel.name,
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2
                )
                Spacer(Modifier.height(4.dp))
                LiveBadge()
            }
        }
    }
}
