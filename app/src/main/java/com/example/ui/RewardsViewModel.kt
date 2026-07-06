package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.RewardHistory
import com.example.data.RewardsRepository
import com.example.data.UserStats
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class VideoItem(
    val id: String,
    val title: String,
    val channelName: String,
    val durationSeconds: Int,
    val category: String,
    val rewardAmountMb: Int = 15,
    val videoTopic: String
)

sealed interface VideoPlaybackState {
    object Idle : VideoPlaybackState
    data class Playing(val elapsedSeconds: Int, val totalSeconds: Int) : VideoPlaybackState
    object Paused : VideoPlaybackState
    object Finished : VideoPlaybackState
}

sealed interface ActivationState {
    object Idle : ActivationState
    data class Processing(val stepName: String, val progress: Float) : ActivationState
    data class Success(val amountMb: Int, val carrier: String, val phoneNumber: String) : ActivationState
    data class Error(val errorMessage: String) : ActivationState
}

class RewardsViewModel(private val repository: RewardsRepository) : ViewModel() {

    // UI Navigation State: "dashboard", "watch", "activate"
    private val _currentTab = MutableStateFlow("dashboard")
    val currentTab: StateFlow<String> = _currentTab.asStateFlow()

    // Observe User Stats and History from database
    val userStats: StateFlow<UserStats> = repository.userStatsFlow
        .map { it ?: UserStats() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UserStats()
        )

    val rewardHistory: StateFlow<List<RewardHistory>> = repository.allHistoryFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Curated Video List
    val videoList = listOf(
        VideoItem("v1", "Demystifying 6G and Quantum Mobile Nets", "CyberTech Daily", 12, "Technology", 15, "Next gen cellular networks and high-frequency wave modulations."),
        VideoItem("v2", "Satisfying High-Speed Glass Sculpting", "SatisfyLab", 10, "Art & Crafts", 15, "Fluid glass heating and precise hand shaping loops."),
        VideoItem("v3", "Epic AI Robot Solves Rubik's Cube in 0.3s", "GadgetVerse", 14, "Robotics", 15, "High-torque precision robotic arms solving puzzle arrays."),
        VideoItem("v4", "Iceland Drone Sweep: Active Volcanic Rivers", "TerraGlobe", 12, "Nature", 15, "Magnificent aerial high-dynamic sweeps over glowing lava beds."),
        VideoItem("v5", "Lo-fi Aesthetic Study Beat & Ambient Loop", "ChillStation", 15, "Music", 15, "Relaxing lofi music track with a beautifully detailed bedroom sunset."),
        VideoItem("v6", "Mind-Bending Kinetic Illusion Sculptures", "ArtCurious", 11, "Design", 15, "Fascinating physics installations moving in perfect symmetry."),
        VideoItem("v7", "The Physics of Supercars Aerodynamics", "SpeedScience", 13, "Automotive", 15, "Wind-tunnel smoke analysis on next-generation active spoiler designs."),
        VideoItem("v8", "Making Perfect Crispy Neapolitan Pizza", "ChefBites", 12, "Cooking", 15, "Artisanal dough fermentation, high-temperature oak wood fire.")
    )

    private val _selectedVideo = MutableStateFlow(videoList[0])
    val selectedVideo: StateFlow<VideoItem> = _selectedVideo.asStateFlow()

    // Video Playback Simulation State
    private val _playbackState = MutableStateFlow<VideoPlaybackState>(VideoPlaybackState.Idle)
    val playbackState: StateFlow<VideoPlaybackState> = _playbackState.asStateFlow()

    private var timerJob: Job? = null

    // Carrier & Activation States
    private val _phoneNumberInput = MutableStateFlow("")
    val phoneNumberInput: StateFlow<String> = _phoneNumberInput.asStateFlow()

    private val _selectedCarrier = MutableStateFlow("Vodacom")
    val selectedCarrier: StateFlow<String> = _selectedCarrier.asStateFlow()

    private val _activationState = MutableStateFlow<ActivationState>(ActivationState.Idle)
    val activationState: StateFlow<ActivationState> = _activationState.asStateFlow()

    init {
        // Create initial stats in database if absent
        viewModelScope.launch {
            val stats = repository.getStatsDirect()
            // Just triggers initialization
        }
    }

    fun switchTab(tab: String) {
        _currentTab.value = tab
    }

    fun selectVideo(video: VideoItem) {
        stopVideo()
        _selectedVideo.value = video
        _playbackState.value = VideoPlaybackState.Idle
    }

    fun startVideo() {
        val currentVideo = _selectedVideo.value
        val currentPlayback = _playbackState.value

        var elapsed = 0
        if (currentPlayback is VideoPlaybackState.Playing) {
            elapsed = currentPlayback.elapsedSeconds
        } else if (currentPlayback is VideoPlaybackState.Paused) {
            // resume
            // find elapsed from previous state if stored elsewhere, or we can track it
        }

        timerJob?.cancel()
        _playbackState.value = VideoPlaybackState.Playing(elapsed, currentVideo.durationSeconds)

        timerJob = viewModelScope.launch {
            while (elapsed < currentVideo.durationSeconds) {
                delay(1000)
                elapsed++
                _playbackState.value = VideoPlaybackState.Playing(elapsed, currentVideo.durationSeconds)
            }
            _playbackState.value = VideoPlaybackState.Finished
            completeVideoAndReward()
        }
    }

    fun pauseVideo() {
        val currentPlayback = _playbackState.value
        if (currentPlayback is VideoPlaybackState.Playing) {
            timerJob?.cancel()
            _playbackState.value = VideoPlaybackState.Paused
        }
    }

    fun stopVideo() {
        timerJob?.cancel()
        _playbackState.value = VideoPlaybackState.Idle
    }

    private suspend fun completeVideoAndReward() {
        val video = _selectedVideo.value
        val stats = repository.getStatsDirect()

        val nextCycleCount = stats.videosWatchedInCurrentCycle + 1
        val totalWatched = stats.totalVideosWatched + 1
        var claimedBonusMb = video.rewardAmountMb
        var newBalance = stats.currentBalanceMb + claimedBonusMb
        var newTotalEarned = stats.totalEarnedMb + claimedBonusMb

        // Log the standard video watch bonus
        repository.addHistory(
            RewardHistory(
                title = "Video Reward (${video.channelName})",
                amountMb = claimedBonusMb,
                status = "Completed"
            )
        )

        // Check if 10 videos completed!
        var cycleReset = nextCycleCount
        if (nextCycleCount >= 10) {
            cycleReset = 0
            val grandRewardMb = 500 // Grand bonus of 500MB!
            newBalance += grandRewardMb
            newTotalEarned += grandRewardMb

            // Log the grand award
            repository.addHistory(
                RewardHistory(
                    title = "🚀 Watch 10 Videos Grand Reward!",
                    amountMb = grandRewardMb,
                    status = "Completed"
                )
            )
        }

        val updatedStats = stats.copy(
            currentBalanceMb = newBalance,
            totalEarnedMb = newTotalEarned,
            totalVideosWatched = totalWatched,
            videosWatchedInCurrentCycle = cycleReset
        )
        repository.updateStats(updatedStats)
    }

    fun setPhoneNumber(num: String) {
        _phoneNumberInput.value = num
    }

    fun setCarrier(carrier: String) {
        _selectedCarrier.value = carrier
    }

    fun initiateDataActivation(activationAmountMb: Int) {
        val currentStats = userStats.value
        val phone = _phoneNumberInput.value.trim()
        val carrier = _selectedCarrier.value

        if (currentStats.currentBalanceMb < activationAmountMb) {
            _activationState.value = ActivationState.Error("Insufficient data balance. Please watch more videos to earn megabytes.")
            return
        }

        if (phone.length < 8) {
            _activationState.value = ActivationState.Error("Please enter a valid phone number (at least 8 digits).")
            return
        }

        viewModelScope.launch {
            try {
                // Step-by-step data activation simulation
                _activationState.value = ActivationState.Processing("Securing network gateway...", 0.1f)
                delay(1200)

                _activationState.value = ActivationState.Processing("Verifying number with $carrier...", 0.4f)
                delay(1500)

                _activationState.value = ActivationState.Processing("Syncing and provisioning $activationAmountMb MB high-speed bundle...", 0.7f)
                delay(1500)

                _activationState.value = ActivationState.Processing("Injecting data payload to SIM IMSI...", 0.9f)
                delay(1000)

                // Update database
                val updatedBalance = currentStats.currentBalanceMb - activationAmountMb
                repository.updateStats(
                    currentStats.copy(
                        currentBalanceMb = updatedBalance,
                        phoneNumber = phone,
                        selectedCarrier = carrier
                    )
                )

                repository.addHistory(
                    RewardHistory(
                        title = "Bundle Activation ($carrier Sync)",
                        amountMb = -activationAmountMb,
                        status = "Synced"
                    )
                )

                _activationState.value = ActivationState.Success(
                    amountMb = activationAmountMb,
                    carrier = carrier,
                    phoneNumber = phone
                )
            } catch (e: Exception) {
                _activationState.value = ActivationState.Error("Carrier handshake timed out. Please try again.")
            }
        }
    }

    fun resetActivation() {
        _activationState.value = ActivationState.Idle
    }

    fun debugAddMockMegabytes(amountMb: Int) {
        viewModelScope.launch {
            val stats = repository.getStatsDirect()
            repository.updateStats(
                stats.copy(
                    currentBalanceMb = stats.currentBalanceMb + amountMb,
                    totalEarnedMb = stats.totalEarnedMb + amountMb
                )
            )
            repository.addHistory(
                RewardHistory(
                    title = "Developer Mock Grant",
                    amountMb = amountMb,
                    status = "Completed"
                )
            )
        }
    }

    fun resetStats() {
        viewModelScope.launch {
            repository.clearAllHistory()
        }
    }
}

class RewardsViewModelFactory(private val repository: RewardsRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RewardsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return RewardsViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
