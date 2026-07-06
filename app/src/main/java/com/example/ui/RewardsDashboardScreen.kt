package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.OfflineBolt
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SignalCellularAlt
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.RewardHistory
import com.example.data.UserStats
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RewardsDashboardScreen(
    viewModel: RewardsViewModel,
    modifier: Modifier = Modifier
) {
    val stats by viewModel.userStats.collectAsState()
    val history by viewModel.rewardHistory.collectAsState()
    val activeTab by viewModel.currentTab.collectAsState()

    val selectedVideo by viewModel.selectedVideo.collectAsState()
    val playbackState by viewModel.playbackState.collectAsState()

    val phoneInput by viewModel.phoneNumberInput.collectAsState()
    val selectedCarrier by viewModel.selectedCarrier.collectAsState()
    val activationState by viewModel.activationState.collectAsState()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = DeepSpaceDark,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.OfflineBolt,
                            contentDescription = null,
                            tint = CyberCyan,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = "DATA REWARDS",
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 2.sp,
                            color = TextWhite
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = DeepSpaceDark.copy(alpha = 0.9f),
                    titleContentColor = TextWhite
                ),
                actions = {
                    IconButton(
                        onClick = { viewModel.resetStats() },
                        modifier = Modifier.testTag("reset_stats_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteSweep,
                            contentDescription = "Reset Stats",
                            tint = TextGray
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            // HERO BANNERS & TITLE
            item {
                HeroBannerSection()
            }

            // NAVIGATION TAB SWITCHERS
            item {
                TabSelectorSection(
                    activeTab = activeTab,
                    onTabSelected = { viewModel.switchTab(it) }
                )
            }

            // RENDER ACTIVE SUB-SECTION
            when (activeTab) {
                "dashboard" -> {
                    // REWARDS DASHBOARD
                    item {
                        DashboardStatsSection(stats = stats)
                    }
                    item {
                        ActionCardsRow(
                            onWatchClick = { viewModel.switchTab("watch") },
                            onActivateClick = { viewModel.switchTab("activate") }
                        )
                    }
                    item {
                        RewardHistorySection(history = history)
                    }
                }
                "watch" -> {
                    // WATCH PORTAL
                    item {
                        VideoPlayerCard(
                            selectedVideo = selectedVideo,
                            playbackState = playbackState,
                            stats = stats,
                            onPlay = { viewModel.startVideo() },
                            onPause = { viewModel.pauseVideo() },
                            onStop = { viewModel.stopVideo() }
                        )
                    }
                    item {
                        VideoQueueList(
                            videos = viewModel.videoList,
                            selectedVideo = selectedVideo,
                            onVideoSelected = { viewModel.selectVideo(it) }
                        )
                    }
                }
                "activate" -> {
                    // DATA PROVISIONING
                    item {
                        DataProvisioningCard(
                            balanceMb = stats.currentBalanceMb,
                            phoneInput = phoneInput,
                            onPhoneChanged = { viewModel.setPhoneNumber(it) },
                            selectedCarrier = selectedCarrier,
                            onCarrierChanged = { viewModel.setCarrier(it) },
                            activationState = activationState,
                            onActivate = { viewModel.initiateDataActivation(it) },
                            onReset = { viewModel.resetActivation() }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun HeroBannerSection() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, CyberCyan.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
    ) {
        // Embed custom generated image asset
        Image(
            painter = painterResource(id = R.drawable.img_rewards_banner),
            contentDescription = "Rewards Banner Illustration",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        // Tint Gradient overlay to match dark cyber vibe
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            DeepSpaceDark.copy(alpha = 0.85f)
                        )
                    )
                )
        )
        // Text Overlays
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .background(CyberCyan, RoundedCornerShape(4.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
                    .padding(bottom = 2.dp)
            ) {
                Text(
                    text = "CAMPAIGN ACTIVE",
                    color = DeepSpaceDark,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp,
                    letterSpacing = 1.sp
                )
            }
            Text(
                text = "Watch 10 Videos = Claim 500 MB",
                color = TextWhite,
                fontWeight = FontWeight.Black,
                fontSize = 18.sp,
                fontFamily = FontFamily.SansSerif
            )
            Text(
                text = "Track your megabytes and activate direct SIM bundles.",
                color = TextGray,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
fun TabSelectorSection(
    activeTab: String,
    onTabSelected: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(CyberSurface, RoundedCornerShape(12.dp))
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        val tabs = listOf(
            Triple("dashboard", "Dashboard", Icons.Default.SignalCellularAlt),
            Triple("watch", "Watch (Earn)", Icons.Default.Videocam),
            Triple("activate", "Activate SIM", Icons.Default.CloudDownload)
        )

        tabs.forEach { (tabId, tabName, icon) ->
            val isSelected = activeTab == tabId
            val bgTint by animateColorAsState(
                targetValue = if (isSelected) CyberSurfaceVariant else Color.Transparent,
                animationSpec = tween(durationMillis = 200)
            )
            val contentColor by animateColorAsState(
                targetValue = if (isSelected) CyberCyan else TextGray,
                animationSpec = tween(durationMillis = 200)
            )

            Row(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(bgTint)
                    .clickable { onTabSelected(tabId) }
                    .padding(vertical = 10.dp, horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = tabName,
                    color = contentColor,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun DashboardStatsSection(stats: UserStats) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Balance Gauge Card
        Card(
            modifier = Modifier
                .weight(1.1f)
                .height(180.dp),
            colors = CardDefaults.cardColors(containerColor = SleekLightBlue),
            shape = RoundedCornerShape(28.dp),
            border = BorderStroke(1.dp, CyberCyan.copy(alpha = 0.15f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(90.dp)
                ) {
                    // Custom Glowing Gauge Drawing
                    CircularGauge(
                        progress = 1f, // static full circle
                        color = SleekDeepBlue,
                        strokeWidth = 6.dp
                    )
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = String.format("%,d", stats.currentBalanceMb),
                            color = SleekDeepBlue,
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "MB",
                            color = SleekDeepBlue,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            letterSpacing = 1.sp
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "AVAILABLE BALANCE",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = SleekDeepBlue.copy(alpha = 0.8f),
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Carrier Ready",
                    fontSize = 10.sp,
                    color = SleekDeepBlue,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // Daily Video Streak Tracker Card
        Card(
            modifier = Modifier
                .weight(1f)
                .height(180.dp),
            colors = CardDefaults.cardColors(containerColor = CyberSurface),
            shape = RoundedCornerShape(28.dp),
            border = BorderStroke(
                width = 1.dp,
                color = if (stats.videosWatchedInCurrentCycle >= 10) CyberCyan.copy(alpha = 0.6f) else CyberSurfaceVariant
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(90.dp)
                ) {
                    val cycleProgress = stats.videosWatchedInCurrentCycle / 10f
                    val sweepColor = if (stats.videosWatchedInCurrentCycle >= 10) SleekDeepBlue else CyberCyan

                    CircularGauge(
                        progress = cycleProgress,
                        color = sweepColor,
                        strokeWidth = 8.dp
                    )
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${stats.videosWatchedInCurrentCycle}/10",
                            color = TextWhite,
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "STREAK",
                            color = sweepColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            letterSpacing = 0.5.sp
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "VIDEOS TO BONUS",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextGray,
                    letterSpacing = 1.sp
                )
                val remaining = 10 - stats.videosWatchedInCurrentCycle
                Text(
                    text = if (remaining > 0) "$remaining left for +500 MB!" else "Claim Grand Bonus!",
                    fontSize = 10.sp,
                    color = if (remaining == 0) CyberCyan else TextGray,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun CircularGauge(
    progress: Float,
    color: Color,
    strokeWidth: androidx.compose.ui.unit.Dp
) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        // Draw track
        drawCircle(
            color = Color(0xFFE2E8F0),
            radius = size.minDimension / 2,
            style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
        )
        // Draw sweep
        drawArc(
            color = color,
            startAngle = -90f,
            sweepAngle = 360f * progress,
            useCenter = false,
            style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
        )
    }
}

@Composable
fun ActionCardsRow(
    onWatchClick: () -> Unit,
    onActivateClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Quick Watch Card
        Button(
            onClick = onWatchClick,
            modifier = Modifier
                .weight(1f)
                .height(48.dp),
            colors = ButtonDefaults.buttonColors(containerColor = CyberSurfaceVariant),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, CyberCyan.copy(alpha = 0.15f))
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    tint = CyberCyan,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Earn Megabytes",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = TextWhite
                )
            }
        }

        // Quick Activate Card
        Button(
            onClick = onActivateClick,
            modifier = Modifier
                .weight(1f)
                .height(48.dp),
            colors = ButtonDefaults.buttonColors(containerColor = CyberCyan),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CloudDownload,
                    contentDescription = null,
                    tint = DeepSpaceDark,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Activate to SIM",
                    fontWeight = FontWeight.Black,
                    fontSize = 13.sp,
                    color = DeepSpaceDark
                )
            }
        }
    }
}

@Composable
fun VideoPlayerCard(
    selectedVideo: VideoItem,
    playbackState: VideoPlaybackState,
    stats: UserStats,
    onPlay: () -> Unit,
    onPause: () -> Unit,
    onStop: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CyberSurface),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, CyberCyan.copy(alpha = 0.25f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Player Display Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF030712))
                    .border(1.dp, CyberSurfaceVariant, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                // Background visual bars representation
                if (playbackState is VideoPlaybackState.Playing) {
                    LiveAudioVisualizer()
                } else {
                    Icon(
                        imageVector = Icons.Default.Tv,
                        contentDescription = null,
                        tint = CyberSurfaceVariant,
                        modifier = Modifier.size(64.dp)
                    )
                }

                // Playback simulation Overlay Banner
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp)
                ) {
                    // Category Badge Top-Left
                    Box(
                        modifier = Modifier
                            .background(CyberCyan.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                            .border(1.dp, CyberCyan, RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                            .align(Alignment.TopStart)
                    ) {
                        Text(
                            text = selectedVideo.category.uppercase(),
                            color = CyberCyan,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.5.sp
                        )
                    }

                    // Reward badge Top-Right
                    Box(
                        modifier = Modifier
                            .background(CyberGold.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                            .border(1.dp, CyberGold, RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                            .align(Alignment.TopEnd)
                    ) {
                        Text(
                            text = "+${selectedVideo.rewardAmountMb} MB",
                            color = CyberGold,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black
                        )
                    }

                    // Bottom info: Channels & Titles
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomStart)
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f))
                                )
                            )
                            .padding(4.dp)
                    ) {
                        Text(
                            text = selectedVideo.title,
                            color = TextWhite,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "@${selectedVideo.channelName} • Premium Channel Partner",
                            color = TextGray,
                            fontSize = 10.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Playback Progress Slider
            var currentSecs = 0
            var totalSecs = selectedVideo.durationSeconds
            when (playbackState) {
                is VideoPlaybackState.Playing -> {
                    currentSecs = playbackState.elapsedSeconds
                    totalSecs = playbackState.totalSeconds
                }
                is VideoPlaybackState.Finished -> {
                    currentSecs = totalSecs
                }
                else -> {
                    currentSecs = 0
                }
            }

            val progressFraction = if (totalSecs > 0) currentSecs.toFloat() / totalSecs else 0f

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = String.format("0:%02d", currentSecs),
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    color = if (playbackState is VideoPlaybackState.Finished) NeonMint else TextGray
                )
                LinearProgressIndicator(
                    progress = { progressFraction },
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 10.dp)
                        .height(6.dp)
                        .clip(CircleShape),
                    color = if (playbackState is VideoPlaybackState.Finished) NeonMint else CyberCyan,
                    trackColor = CyberSurfaceVariant
                )
                Text(
                    text = String.format("0:%02d", totalSecs),
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    color = TextGray
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Player control action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // STOP
                IconButton(
                    onClick = onStop,
                    enabled = playbackState !is VideoPlaybackState.Idle
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(
                                if (playbackState !is VideoPlaybackState.Idle) CyberSurfaceVariant else Color.Transparent,
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.size(12.dp)) {
                            drawRect(color = if (playbackState !is VideoPlaybackState.Idle) TextWhite else TextMuted)
                        }
                    }
                }

                // PLAY/PAUSE CORE BUTTON
                Button(
                    onClick = {
                        if (playbackState is VideoPlaybackState.Playing) {
                            onPause()
                        } else {
                            onPlay()
                        }
                    },
                    modifier = Modifier
                        .height(46.dp)
                        .widthIn(min = 140.dp)
                        .testTag("watch_video_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (playbackState is VideoPlaybackState.Playing) CyberSurfaceVariant else CyberCyan
                    ),
                    shape = RoundedCornerShape(24.dp),
                    border = BorderStroke(
                        width = 1.dp,
                        color = if (playbackState is VideoPlaybackState.Playing) CyberCyan else Color.Transparent
                    )
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (playbackState is VideoPlaybackState.Playing) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = if (playbackState is VideoPlaybackState.Playing) CyberCyan else DeepSpaceDark,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (playbackState is VideoPlaybackState.Playing) "Pause Video" else "Watch Stream",
                            fontWeight = FontWeight.Black,
                            fontSize = 13.sp,
                            color = if (playbackState is VideoPlaybackState.Playing) CyberCyan else DeepSpaceDark
                        )
                    }
                }

                // DUMMY REFRESH
                IconButton(onClick = onStop) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Restart Video",
                        tint = TextGray
                    )
                }
            }

            // Toast/Alert prompt upon completion
            AnimatedVisibility(visible = playbackState is VideoPlaybackState.Finished) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                        .background(NeonMint.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                        .border(1.dp, NeonMint, RoundedCornerShape(8.dp))
                        .padding(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Success",
                            tint = NeonMint,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Stream Completed! +15 MB reward credited to your dashboard.",
                            color = TextWhite,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun LiveAudioVisualizer() {
    val infiniteTransition = rememberInfiniteTransition()
    val heights = List(12) {
        infiniteTransition.animateFloat(
            initialValue = 0.1f,
            targetValue = 0.9f,
            animationSpec = infiniteRepeatable(
                animation = tween(
                    durationMillis = (400 + (it * 80)),
                    easing = LinearEasing
                ),
                repeatMode = RepeatMode.Reverse
            ),
            label = "visualizer_height_$it"
        )
    }

    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        heights.forEach { hVal ->
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(hVal.value)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(CyberCyan, NeonMint)
                        ),
                        RoundedCornerShape(2.dp)
                    )
            )
        }
    }
}

@Composable
fun VideoQueueList(
    videos: List<VideoItem>,
    selectedVideo: VideoItem,
    onVideoSelected: (VideoItem) -> Unit
) {
    Column {
        Text(
            text = "SELECT VIDEO STREAM CHANNEL",
            color = TextGray,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(bottom = 8.dp, top = 4.dp)
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(videos) { video ->
                val isSelected = video.id == selectedVideo.id
                Card(
                    modifier = Modifier
                        .width(180.dp)
                        .height(115.dp)
                        .clickable { onVideoSelected(video) },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) CyberSurfaceVariant else CyberSurface
                    ),
                    border = BorderStroke(
                        width = 1.dp,
                        color = if (isSelected) CyberCyan else Color.Transparent
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(10.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .background(CyberCyan.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = video.category,
                                    color = CyberCyan,
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Text(
                                text = "0:${video.durationSeconds}s",
                                color = TextGray,
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        Text(
                            text = video.title,
                            color = TextWhite,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "@${video.channelName}",
                                color = TextGray,
                                fontSize = 9.sp
                            )
                            Text(
                                text = "+${video.rewardAmountMb}MB",
                                color = CyberGold,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DataProvisioningCard(
    balanceMb: Int,
    phoneInput: String,
    onPhoneChanged: (String) -> Unit,
    selectedCarrier: String,
    onCarrierChanged: (String) -> Unit,
    activationState: ActivationState,
    onActivate: (Int) -> Unit,
    onReset: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CyberSurface),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, CyberCyan.copy(alpha = 0.2f))
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                text = "CARRIER CELLULAR HANDSHAKE",
                color = CyberCyan,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Provision Earned Megabytes to SIM",
                color = TextWhite,
                fontWeight = FontWeight.Black,
                fontSize = 16.sp
            )
            Text(
                text = "Activate high-speed cellular bandwidth. Ensure your SIM card number is entered correctly.",
                color = TextGray,
                fontSize = 11.sp,
                modifier = Modifier.padding(bottom = 14.dp)
            )

            when (activationState) {
                is ActivationState.Idle -> {
                    // Input Form
                    Text(
                        text = "SELECT NETWORK PROVIDER",
                        color = TextGray,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        letterSpacing = 0.5.sp,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )

                    val carriers = listOf("Vodacom", "MTN", "Airtel", "Orange", "Verizon", "T-Mobile")
                    var expanded by remember { mutableStateOf(false) }

                    // Carrier Dropdown selector
                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = !expanded },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = selectedCarrier,
                            onValueChange = {},
                            readOnly = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                                .testTag("carrier_dropdown"),
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = TextWhite,
                                unfocusedTextColor = TextWhite,
                                focusedContainerColor = CyberSurfaceVariant,
                                unfocusedContainerColor = CyberSurfaceVariant,
                                focusedBorderColor = CyberCyan,
                                unfocusedBorderColor = CyberSurfaceVariant,
                                focusedTrailingIconColor = CyberCyan,
                                unfocusedTrailingIconColor = TextGray
                            ),
                            shape = RoundedCornerShape(8.dp)
                        )
                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false },
                            modifier = Modifier.background(CyberSurface)
                        ) {
                            carriers.forEach { carrierOption ->
                                DropdownMenuItem(
                                    text = { Text(carrierOption, color = TextWhite) },
                                    onClick = {
                                        onCarrierChanged(carrierOption)
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "TARGET MOBILE NUMBER",
                        color = TextGray,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        letterSpacing = 0.5.sp,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )

                    OutlinedTextField(
                        value = phoneInput,
                        onValueChange = onPhoneChanged,
                        placeholder = { Text("e.g. +27 72 123 4567", color = TextMuted) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("phone_number_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextWhite,
                            unfocusedTextColor = TextWhite,
                            focusedContainerColor = CyberSurfaceVariant,
                            unfocusedContainerColor = CyberSurfaceVariant,
                            focusedBorderColor = CyberCyan,
                            unfocusedBorderColor = CyberSurfaceVariant
                        ),
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp)
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = "CHOOSE BUNDLE SIZE",
                        color = TextGray,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        letterSpacing = 0.5.sp,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )

                    val bundles = listOf(50, 250, 500, 1000)
                    var selectedBundle by remember { mutableStateOf(250) }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        bundles.forEach { size ->
                            val isSelected = size == selectedBundle
                            val isAffordable = balanceMb >= size

                            Card(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable(enabled = isAffordable) { selectedBundle = size },
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) CyberCyan else if (isAffordable) CyberSurfaceVariant else CyberSurfaceVariant.copy(alpha = 0.3f)
                                ),
                                border = BorderStroke(
                                    width = 1.dp,
                                    color = if (isSelected) CyberCyan else if (isAffordable) CyberCyan.copy(alpha = 0.15f) else Color.Transparent
                                )
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 12.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "$size",
                                        fontWeight = FontWeight.Black,
                                        fontSize = 14.sp,
                                        color = if (isSelected) DeepSpaceDark else TextWhite
                                    )
                                    Text(
                                        text = "MB",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp,
                                        color = if (isSelected) DeepSpaceDark else CyberCyan
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = { onActivate(selectedBundle) },
                        enabled = balanceMb >= selectedBundle && phoneInput.isNotBlank(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("activate_bundle_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = NeonMint,
                            disabledContainerColor = CyberSurfaceVariant
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = if (balanceMb >= selectedBundle) "Activate $selectedBundle MB Package" else "Insufficient Balance",
                            fontWeight = FontWeight.Black,
                            fontSize = 13.sp,
                            color = if (balanceMb >= selectedBundle && phoneInput.isNotBlank()) DeepSpaceDark else TextMuted
                        )
                    }
                }
                is ActivationState.Processing -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(
                            color = CyberCyan,
                            modifier = Modifier.size(44.dp)
                        )
                        Spacer(modifier = Modifier.height(18.dp))
                        Text(
                            text = activationState.stepName,
                            color = TextWhite,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = { activationState.progress },
                            modifier = Modifier
                                .width(180.dp)
                                .height(4.dp)
                                .clip(CircleShape),
                            color = CyberCyan,
                            trackColor = CyberSurfaceVariant
                        )
                        Text(
                            text = "ESTIMATING TRANSFERS • DO NOT CLOSE APP",
                            color = TextGray,
                            fontSize = 9.sp,
                            modifier = Modifier.padding(top = 10.dp)
                        )
                    }
                }
                is ActivationState.Success -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Success",
                            tint = NeonMint,
                            modifier = Modifier.size(56.dp)
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "SYNC PROVISIONING COMPLETED",
                            fontWeight = FontWeight.Black,
                            fontSize = 14.sp,
                            color = NeonMint,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "${activationState.amountMb} MB high-speed data activated!",
                            color = TextWhite,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Target SIM: ${activationState.phoneNumber} (${activationState.carrier})",
                            color = TextGray,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        Button(
                            onClick = onReset,
                            colors = ButtonDefaults.buttonColors(containerColor = CyberSurfaceVariant),
                            modifier = Modifier.fillMaxWidth(0.7f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Done", color = TextWhite)
                        }
                    }
                }
                is ActivationState.Error -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.ErrorOutline,
                            contentDescription = "Error",
                            tint = ErrorRed,
                            modifier = Modifier.size(56.dp)
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "HANDSHAKE FAILED",
                            fontWeight = FontWeight.Black,
                            fontSize = 14.sp,
                            color = ErrorRed,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = activationState.errorMessage,
                            color = TextWhite,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        Button(
                            onClick = onReset,
                            colors = ButtonDefaults.buttonColors(containerColor = CyberSurfaceVariant),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Retry Setup", color = TextWhite)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RewardHistorySection(history: List<RewardHistory>) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "LEDGER SYNCHRONIZATION HISTORY",
                color = TextGray,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Icon(
                imageVector = Icons.Default.NetworkCheck,
                contentDescription = "Synced Ledger",
                tint = NeonMint,
                modifier = Modifier.size(14.dp)
            )
        }

        if (history.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CyberSurface, RoundedCornerShape(12.dp))
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = CyberSurfaceVariant,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "No history recorded yet.",
                        color = TextGray,
                        fontSize = 12.sp
                    )
                    Text(
                        text = "Watch video streams to start generating rewards.",
                        color = TextMuted,
                        fontSize = 10.sp
                    )
                }
            }
        } else {
            Card(
                colors = CardDefaults.cardColors(containerColor = CyberSurface),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    history.take(6).forEach { log ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = log.title,
                                    color = TextWhite,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = formatTimestamp(log.timestamp),
                                    color = TextMuted,
                                    fontSize = 9.sp
                                )
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .background(
                                            color = when (log.status) {
                                                "Synced" -> NeonMint.copy(alpha = 0.15f)
                                                else -> CyberCyan.copy(alpha = 0.15f)
                                            },
                                            shape = RoundedCornerShape(4.dp)
                                        )
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = log.status.uppercase(),
                                        color = when (log.status) {
                                            "Synced" -> NeonMint
                                            else -> CyberCyan
                                        },
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                }

                                val isEarning = log.amountMb > 0
                                Text(
                                    text = if (isEarning) "+${log.amountMb} MB" else "${log.amountMb} MB",
                                    color = if (isEarning) NeonMint else ErrorRed,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                        if (log != history.take(6).last()) {
                            HorizontalDivider(color = CyberSurfaceVariant, thickness = 1.dp)
                        }
                    }
                }
            }
        }
    }
}

fun formatTimestamp(timestamp: Long): String {
    val date = Date(timestamp)
    val sdf = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())
    return sdf.format(date)
}
