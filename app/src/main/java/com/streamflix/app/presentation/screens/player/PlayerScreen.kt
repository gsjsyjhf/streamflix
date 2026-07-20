package com.streamflix.app.presentation.screens.player

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ClosedCaption
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.streamflix.app.data.repository.StreamFlixRepository
import com.streamflix.app.domain.model.ContentType
import com.streamflix.app.domain.model.SubtitleTrack
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.io.ByteArrayInputStream
import javax.inject.Inject

@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val repo: StreamFlixRepository
) : ViewModel() {
    var title by mutableStateOf("")
        private set
    var streamUrl by mutableStateOf("")
        private set
    var contentId by mutableStateOf("")
        private set
    var currentImdbId by mutableStateOf<String?>(null)
        private set
    var subtitles by mutableStateOf<List<SubtitleTrack>>(emptyList())
        private set
    var selectedSubtitle by mutableStateOf<SubtitleTrack?>(null)
        private set
    var isLoadingSubs by mutableStateOf(false)
        private set
    /** موضع التشغيل المحفوظ من جلسة سابقة (بالملي ثانية) - 0 لو ماكو */
    var resumePositionMs by mutableStateOf(0L)
        private set
    /** مدة الفيديو الكاملة (بالملي ثانية) */
    var durationMs by mutableStateOf(0L)
        private set
    /** الموضع الحالي للتشغيل (بالملي ثانية) - يُحدّث باستمرار */
    var currentPositionMs by mutableStateOf(0L)
        private set

    fun updatePlaybackPosition(positionMs: Long, durationMs: Long) {
        currentPositionMs = positionMs
        this.durationMs = durationMs
    }

    /** يحفظ التقدم الحالي في قاعدة البيانات */
    fun saveCurrentProgress() {
        val pos = currentPositionMs
        val dur = durationMs
        if (pos > 0) {
            viewModelScope.launch {
                repo.saveProgress(
                    contentId = contentId,
                    title = title,
                    posterUrl = null,
                    contentType = ContentType.MOVIE,
                    progressSeconds = pos / 1000,
                    durationSeconds = dur / 1000
                )
            }
        }
    }

    fun init(type: String, id: String, seasonNumber: Int = 0, episodeNumber: Int = 0) {
        contentId = id
        when (type) {
            "movie" -> {
                viewModelScope.launch {
                    val m = repo.getMovieWithStreams(id)
                    title = m?.title ?: "فيلم"
                    streamUrl = m?.streamUrl ?: ""
                    currentImdbId = m?.imdbId
                    // استرجاع موضع التشغيل المحفوظ
                    val history = repo.observeWatchHistory().first()
                    val saved = history.firstOrNull { it.contentId == id }
                    resumePositionMs = (saved?.progressSeconds ?: 0L) * 1000
                    durationMs = (saved?.durationSeconds ?: 0L) * 1000
                    currentPositionMs = resumePositionMs
                    // Load subtitles in background
                    if (!currentImdbId.isNullOrBlank()) {
                        isLoadingSubs = true
                        subtitles = repo.getSubtitlesForMovie(currentImdbId)
                        // Auto-select first Arabic subtitle if available
                        selectedSubtitle = subtitles.firstOrNull { it.isArabic } ?: subtitles.firstOrNull()
                        isLoadingSubs = false
                    }
                }
            }
            "series" -> {
                viewModelScope.launch {
                    val tmdbId = id.removePrefix("tmdb_series_").toIntOrNull()
                    if (tmdbId != null) {
                        val series = repo.getSeriesWithStreams(id)
                        title = series?.title ?: "مسلسل"
                        streamUrl = "https://vidsrc.in/embed/tv/$tmdbId/1/1"
                    } else {
                        title = "مسلسل"
                        streamUrl = ""
                    }
                    // استرجاع موضع التشغيل المحفوظ
                    val history = repo.observeWatchHistory().first()
                    val saved = history.firstOrNull { it.contentId == id }
                    resumePositionMs = (saved?.progressSeconds ?: 0L) * 1000
                    durationMs = (saved?.durationSeconds ?: 0L) * 1000
                    currentPositionMs = resumePositionMs
                }
            }
            "episode" -> {
                viewModelScope.launch {
                    // يجلب تفاصيل المسلسل لمعرفة الاسم + imdb_id
                    val series = repo.getSeriesWithStreams(id)
                    val tmdbId = id.removePrefix("tmdb_series_").toIntOrNull()
                    title = if (series != null) {
                        "${series.title} - S${seasonNumber}E${episodeNumber}"
                    } else {
                        "مسلسل - S${seasonNumber}E${episodeNumber}"
                    }
                    streamUrl = if (tmdbId != null) {
                        "https://vidsrc.in/embed/tv/$tmdbId/$seasonNumber/$episodeNumber"
                    } else ""
                    currentImdbId = series?.imdbId
                    // استرجاع موضع التشغيل المحفوظ (باستخدام id مميز للحلقة)
                    val episodeContentId = "${id}_S${seasonNumber}E${episodeNumber}"
                    contentId = episodeContentId
                    val history = repo.observeWatchHistory().first()
                    val saved = history.firstOrNull { it.contentId == episodeContentId }
                    resumePositionMs = (saved?.progressSeconds ?: 0L) * 1000
                    durationMs = (saved?.durationSeconds ?: 0L) * 1000
                    currentPositionMs = resumePositionMs
                    // Load subtitles for this episode
                    if (!currentImdbId.isNullOrBlank()) {
                        isLoadingSubs = true
                        subtitles = repo.getSubtitlesForEpisode(currentImdbId, seasonNumber, episodeNumber)
                        selectedSubtitle = subtitles.firstOrNull { it.isArabic } ?: subtitles.firstOrNull()
                        isLoadingSubs = false
                    }
                }
            }
            "anime" -> {
                title = "أنمي"
                streamUrl = "https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8"
            }
            "match" -> {
                viewModelScope.launch {
                    val mt = repo.getTodayMatches().firstOrNull { it.id == id }
                    title = "${mt?.homeTeam ?: ""} vs ${mt?.awayTeam ?: ""}"
                    streamUrl = mt?.streamUrl ?: ""
                }
            }
            "channel" -> {
                val ch = repo.getLiveChannels().firstOrNull { it.id == id }
                title = ch?.name ?: "قناة مباشرة"
                streamUrl = ch?.streamUrl ?: ""
            }
            "bein_channel" -> {
                title = "جارٍ تحميل البث..."
                viewModelScope.launch {
                    val channel = repo.findBeinChannel(id)
                    title = channel?.name ?: "BEIN Sports"
                    val response = repo.getBeinChannelStream(id)
                    streamUrl = response?.stream_url ?: ""
                }
            }
        }
    }

    fun selectSubtitle(track: SubtitleTrack?) {
        selectedSubtitle = track
    }

    fun saveProgress(progressSeconds: Long, durationSeconds: Long) {
        viewModelScope.launch {
            repo.saveProgress(contentId, title, null, ContentType.MOVIE, progressSeconds, durationSeconds)
        }
    }
}

private fun isWebViewUrl(url: String): Boolean {
    return url.contains("vidsrc") || url.contains("2embed") || url.contains("multiembed") ||
           url.contains("youtube.com") || url.contains("youtu.be") ||
           url.contains("/embed/") || url.contains("databasegdriveplayer") ||
           url.contains("player.php") || url.contains("smashystream") ||
           url.contains("gomo.to") || url.contains("streamingnow") ||
           url.contains("vsembed")
}

private val adDomains = listOf(
    "googlesyndication", "doubleclick", "googleads", "adserver",
    "popads", "popcash", "propellerads", "adsterra", "admacha",
    "histats", "tagivi", "cloudfront.net/?afjpd", "dpjf9a2rbjbvp",
    "kingads", "adskeeper", "mgid", "taboola", "outbrain",
    "exoclick", "juicyads", "trafficstars", "clickad",
    "play.google.com", "facebook.com/tr", "analytics"
)

private val allowedDomains = listOf(
    "vidsrc.in", "vidsrc", "vsembed", "streamingnow",
    "googlevideo", "ytimg", "jwplayer", "blob:",
    "m3u8", ".mp4", ".mkv", ".webm", "2embed"
)

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun PlayerScreen(
    type: String,
    contentId: String,
    onBack: () -> Unit,
    seasonNumber: Int = 0,
    episodeNumber: Int = 0,
    viewModel: PlayerViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    LaunchedEffect(contentId) { viewModel.init(type, contentId, seasonNumber, episodeNumber) }

    val isEmbed = isWebViewUrl(viewModel.streamUrl)
    val directUrl = if (isEmbed) "" else viewModel.streamUrl

    var showControls by remember { mutableStateOf(true) }
    var isFullscreen by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }
    var webView by remember { mutableStateOf<WebView?>(null) }
    var showSubtitlesDialog by remember { mutableStateOf(false) }
    var hasResumed by remember { mutableStateOf(false) }

    // ExoPlayer for direct streams (HLS/MP4 - channels, matches)
    val exoPlayer = remember(directUrl) {
        if (directUrl.isBlank()) null
        else ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(directUrl))
            prepare()
            playWhenReady = true
            // استئناف من الموضع المحفوظ لو موجود
            if (viewModel.resumePositionMs > 5000) {
                seekTo(viewModel.resumePositionMs)
            }
        }
    }

    // تتبع موضع التشغيل كل 2 ثانية وحفظه في ViewModel (لـ ExoPlayer)
    LaunchedEffect(exoPlayer) {
        exoPlayer?.let { player ->
            player.addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(state: Int) {
                    when (state) {
                        Player.STATE_BUFFERING -> isLoading = true
                        Player.STATE_READY -> isLoading = false
                        Player.STATE_ENDED -> isLoading = false
                        Player.STATE_IDLE -> isLoading = false
                    }
                }
            })
            // كل 2 ثانية حدّث الموضع الحالي واحفظه في الـ ViewModel
            while (true) {
                val pos = player.currentPosition
                val dur = player.duration.coerceAtLeast(0L)
                if (pos > 0) {
                    viewModel.updatePlaybackPosition(pos, dur)
                }
                delay(2000)
            }
        }
    }

    // تتبع موضع تشغيل WebView كل 2 ثانية
    // يستخدم بحث متكرر داخل iframes + fallback زمني
    LaunchedEffect(webView, viewModel.streamUrl) {
        webView?.let { wv ->
            val watchStartTime = System.currentTimeMillis()
            val estimatedDurationMs = 5400000L // 90 دقيقة افتراضياً للأفلام
            var lastVideoPosition = 0L
            var lastVideoDuration = 0L
            while (true) {
                // JavaScript يبحث عن عنصر video في كل الصفحة + inside iframes
                wv.evaluateJavascript("""
                    (function() {
                        // دالة تبحث عن video في الـ document وكل iframes
                        var findVideo = function(doc) {
                            try {
                                var v = doc.querySelector('video');
                                if (v && !isNaN(v.duration) && v.duration > 0) return v;
                                var iframes = doc.querySelectorAll('iframe');
                                for (var i = 0; i < iframes.length; i++) {
                                    try {
                                        var innerDoc = iframes[i].contentDocument || iframes[i].contentWindow.document;
                                        v = findVideo(innerDoc);
                                        if (v) return v;
                                    } catch(e) {} // cross-origin iframe - تخطى
                                }
                            } catch(e) {}
                            return null;
                        };
                        var v = findVideo(document);
                        if (v) {
                            return JSON.stringify({
                                pos: Math.floor(v.currentTime * 1000),
                                dur: Math.floor(v.duration * 1000),
                                paused: v.paused
                            });
                        }
                        return null;
                    })();
                """) { result ->
                    if (!result.isNullOrBlank() && result != "null") {
                        try {
                            val cleanResult = result.trim().removeSurrounding("\"").replace("\\\"", "\"")
                            val json = org.json.JSONObject(cleanResult)
                            val pos = json.optLong("pos", 0L)
                            val dur = json.optLong("dur", 0L)
                            val paused = json.optBoolean("paused", false)
                            if (pos > 0 && dur > 0) {
                                lastVideoPosition = pos
                                lastVideoDuration = dur
                                viewModel.updatePlaybackPosition(pos, dur)
                            }
                        } catch (_: Exception) {}
                    }
                }
                delay(2000)
            }
        }
    }

    // Time-based fallback: لو ما قدرنا نقرأ video.currentTime (cross-origin iframe)
    // نستخدم الوقت المنقضي منذ بدء المشاهدة
    LaunchedEffect(viewModel.streamUrl) {
        val watchStartTime = System.currentTimeMillis()
        val estimatedDurationMs = 5400000L // 90 دقيقة افتراضياً
        while (true) {
            delay(5000) // كل 5 ثوان
            // لو ماكو تحديث من الـ video tracking (currentPositionMs ما تغير من آخر 5 ثوان)
            val elapsed = System.currentTimeMillis() - watchStartTime
            // استخدم time-based لو ماكو بيانات من video element
            if (viewModel.currentPositionMs == 0L || viewModel.currentPositionMs < (elapsed - 10000)) {
                // durationMs لو ما متوفر، استخدم التقدير الافتراضي
                val dur = if (viewModel.durationMs > 0) viewModel.durationMs else estimatedDurationMs
                viewModel.updatePlaybackPosition(elapsed, dur)
            }
        }
    }

    // استئناف WebView من الموضع المحفوظ بعد تحميل الصفحة
    // يحاول البحث في iframes بشكل متكرر
    LaunchedEffect(webView, viewModel.streamUrl, isLoading) {
        if (webView != null && !isLoading && !hasResumed && viewModel.resumePositionMs > 5000) {
            delay(1000) // انتظر تحميل الفيديو
            val targetSeconds = viewModel.resumePositionMs / 1000.0
            webView?.evaluateJavascript("""
                (function() {
                    var findVideo = function(doc) {
                        try {
                            var v = doc.querySelector('video');
                            if (v) return v;
                            var iframes = doc.querySelectorAll('iframe');
                            for (var i = 0; i < iframes.length; i++) {
                                try {
                                    var innerDoc = iframes[i].contentDocument || iframes[i].contentWindow.document;
                                    v = findVideo(innerDoc);
                                    if (v) return v;
                                } catch(e) {}
                            }
                        } catch(e) {}
                        return null;
                    };
                    var v = findVideo(document);
                    if (v) {
                        try { v.currentTime = $targetSeconds; } catch(e) {}
                    }
                })();
            """, null)
            hasResumed = true
        }
    }

    // حفظ التقدم عند الخروج من الشاشة
    DisposableEffect(contentId) {
        onDispose {
            viewModel.saveCurrentProgress()
        }
    }

    // أيضاً حفظ دوري كل 30 ثانية (للأمان لو انطفأ الجهاز)
    LaunchedEffect(contentId) {
        while (true) {
            delay(30000)
            viewModel.saveCurrentProgress()
        }
    }

    DisposableEffect(exoPlayer) {
        onDispose { exoPlayer?.release() }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .pointerInput(Unit) {
                detectTapGestures(onTap = { showControls = !showControls })
            }
    ) {
        // === WEBVIEW (movies & series) ===
        if (isEmbed && viewModel.streamUrl.isNotBlank()) {
            AndroidView(
                factory = { ctx ->
                    WebView(ctx).apply {
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                        webChromeClient = WebChromeClient()
                        webViewClient = object : WebViewClient() {
                            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                                val url = request?.url?.toString() ?: return false
                                return !allowedDomains.any { url.contains(it) }
                            }

                            override fun shouldInterceptRequest(view: WebView?, request: WebResourceRequest?): WebResourceResponse? {
                                val url = request?.url?.toString() ?: return null
                                if (adDomains.any { url.contains(it) }) {
                                    return WebResourceResponse("text/plain", "utf-8", ByteArrayInputStream("".toByteArray()))
                                }
                                return null
                            }

                            override fun onPageFinished(view: WebView?, url: String?) {
                                super.onPageFinished(view, url)
                                isLoading = false
                                view?.evaluateJavascript("""
                                    (function() {
                                        var style = document.createElement('style');
                                        style.textContent = `
                                            * { margin:0 !important; padding:0 !important; box-sizing:border-box !important; }
                                            html, body {
                                                width: 100vw !important; height: 100vh !important;
                                                overflow: hidden !important; background: #000 !important;
                                                position: fixed !important; top: 0 !important; left: 0 !important;
                                            }
                                            iframe {
                                                width: 100% !important; height: 100% !important;
                                                border: 0 !important; position: fixed !important;
                                                top: 0 !important; left: 0 !important; z-index: 1 !important;
                                            }
                                            video {
                                                width: 100% !important; height: 100% !important;
                                                object-fit: contain !important; background: #000 !important;
                                            }
                                            [id*="ad"], [class*="ad"], [id*="banner"], [class*="banner"],
                                            [id*="popup"], [class*="popup"], [id*="overlay"], [class*="overlay"] {
                                                display: none !important; visibility: hidden !important;
                                            }
                                            nav, header, footer { display: none !important; }
                                        `;
                                        document.head.appendChild(style);
                                        var iframes = document.querySelectorAll('iframe');
                                        iframes.forEach(function(f) {
                                            var src = f.src || '';
                                            if (src.indexOf('google') >= 0 || src.indexOf('ads') >= 0 ||
                                                src.indexOf('histats') >= 0 || src.indexOf('tagivi') >= 0 ||
                                                src.indexOf('cloudfront') >= 0 || src.indexOf('dpjf9a') >= 0) {
                                                f.remove();
                                            }
                                        });
                                        window.open = function() { return null; };
                                        setInterval(function() {
                                            var adIframes = document.querySelectorAll('iframe');
                                            adIframes.forEach(function(f) {
                                                var src = f.src || '';
                                                if (src.indexOf('google') >= 0 || src.indexOf('ads') >= 0 ||
                                                    src.indexOf('histats') >= 0 || src.indexOf('cloudfront') >= 0) {
                                                    f.remove();
                                                }
                                            });
                                        }, 2000);
                                    })();
                                """, null)
                            }
                        }
                        settings.javaScriptEnabled = true
                        settings.mediaPlaybackRequiresUserGesture = false
                        settings.domStorageEnabled = true
                        settings.setSupportZoom(false)
                        settings.useWideViewPort = true
                        settings.loadWithOverviewMode = true
                        settings.userAgentString = "Mozilla/5.0 (Linux; Android 13; Pixel 7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36"
                        loadUrl(viewModel.streamUrl)
                        webView = this
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
        }

        // === EXOPLAYER (channels, matches) ===
        if (!isEmbed && exoPlayer != null) {
            AndroidView(
                factory = { ctx ->
                    PlayerView(ctx).apply {
                        player = exoPlayer
                        useController = true
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
        }

        // === Loading ===
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = Color.White, strokeWidth = 3.dp, modifier = Modifier.size(48.dp))
                    Spacer(Modifier.height(12.dp))
                    Text("جاري تحميل الفيديو…", color = Color.White, fontSize = 14.sp)
                    // لو فيه موضع محفوظ، أظهر "استئناف من..."
                    if (viewModel.resumePositionMs > 5000) {
                        Spacer(Modifier.height(6.dp))
                        val mins = (viewModel.resumePositionMs / 60000).toInt()
                        val secs = ((viewModel.resumePositionMs / 1000) % 60).toInt()
                        Text(
                            text = "استئناف من %02d:%02d".format(mins, secs),
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        // === Simple top bar (back + subtitles + fullscreen only) ===
        if (showControls) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Black.copy(alpha = 0.7f))
                    .padding(8.dp)
                    .align(Alignment.TopCenter),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = {
                    val activity = context as? ComponentActivity
                    activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                    onBack()
                }) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = "رجوع", tint = Color.White)
                }
                Text(
                    text = viewModel.title,
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(start = 8.dp)
                )
                Spacer(Modifier.fillMaxWidth())
                // Subtitles button - show only when subtitles are available
                if (viewModel.subtitles.isNotEmpty()) {
                    IconButton(onClick = { showSubtitlesDialog = true }) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Filled.ClosedCaption,
                                contentDescription = "ترجمة",
                                tint = if (viewModel.selectedSubtitle != null) Color(0xFFFFD700) else Color.White
                            )
                            if (viewModel.selectedSubtitle?.isArabic == true) {
                                Text(
                                    text = "ع",
                                    color = Color.Black,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }
                    }
                } else if (viewModel.isLoadingSubs) {
                    CircularProgressIndicator(
                        color = Color.White,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(20.dp).padding(end = 12.dp)
                    )
                }
                IconButton(onClick = {
                    val activity = context as? ComponentActivity
                    if (activity != null) {
                        isFullscreen = !isFullscreen
                        activity.requestedOrientation = if (isFullscreen)
                            ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                        else
                            ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                    }
                }) {
                    Icon(
                        imageVector = if (isFullscreen) Icons.Filled.FullscreenExit else Icons.Filled.Fullscreen,
                        contentDescription = "ملء الشاشة",
                        tint = Color.White
                    )
                }
            }
        }

        // === Subtitles dialog ===
        if (showSubtitlesDialog) {
            SubtitlesDialog(
                subtitles = viewModel.subtitles,
                selected = viewModel.selectedSubtitle,
                onSelect = { track ->
                    viewModel.selectSubtitle(track)
                    showSubtitlesDialog = false
                },
                onDismiss = { showSubtitlesDialog = false }
            )
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            webView?.apply {
                loadUrl("about:blank")
                clearHistory()
                (parent as? ViewGroup)?.removeView(this)
                destroy()
            }
            webView = null
        }
    }
}

@Composable
private fun SubtitlesDialog(
    subtitles: List<SubtitleTrack>,
    selected: SubtitleTrack?,
    onSelect: (SubtitleTrack?) -> Unit,
    onDismiss: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.6f))
            .clickable(onClick = onDismiss),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth()
                .clickable(enabled = false) {}, // prevent dismiss on inner click
            color = Color(0xFF1A1A2E),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "الترجمات",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                LazyColumn(
                    modifier = Modifier.height(360.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Off option
                    item {
                        SubtitleRow(
                            title = "بدون ترجمة",
                            subtitle = "",
                            isSelected = selected == null,
                            onClick = { onSelect(null) }
                        )
                    }
                    items(subtitles) { track ->
                        SubtitleRow(
                            title = track.languageName,
                            subtitle = track.fileName,
                            isSelected = selected?.id == track.id,
                            onClick = { onSelect(track) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SubtitleRow(
    title: String,
    subtitle: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        color = if (isSelected) Color(0xFF6366F1).copy(alpha = 0.25f) else Color.Transparent,
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
                if (subtitle.isNotBlank()) {
                    Text(
                        text = subtitle,
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 11.sp,
                        maxLines = 1
                    )
                }
            }
            if (isSelected) {
                Text("✓", color = Color(0xFF6366F1), fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
        }
    }
}
