package com.unifiedplayer

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.util.Log
import android.view.Gravity
import android.widget.FrameLayout
import com.facebook.react.bridge.Arguments
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.PlaybackException
import com.google.android.exoplayer2.Timeline
import com.google.android.exoplayer2.Tracks
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.video.VideoSize
import com.facebook.react.bridge.WritableMap

class UnifiedPlayerView(context: Context) : FrameLayout(context) {
    companion object {
        private const val TAG = "UnifiedPlayerView"
    }

    private var videoUrl: String? = null
    private var authToken: String? = null
    private var autoplay: Boolean = true
    private var loop: Boolean = false
    private var playerView: PlayerView
    private var player: ExoPlayer? = null
    private var isPlaying = false
    private var currentProgress = 0

    init {
        setBackgroundColor(Color.BLACK)

        // Create ExoPlayer
        player = ExoPlayer.Builder(context).build()

        // Create PlayerView
        playerView = PlayerView(context).apply {
            layoutParams = LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT
            )
            setPlayer(player)
            // playerView.surfaceView?.surfaceType = android.view.SurfaceView.SURFACE_TYPE_SOFTWARE
        }

        addView(playerView)

        player?.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                Log.d(TAG, "onPlaybackStateChanged: $playbackState") // Added log
                when (playbackState) {
                    Player.STATE_READY -> {
                        Log.d(TAG, "ExoPlayer STATE_READY")
                        sendEvent("onReadyToPlay", Arguments.createMap())
                        if (autoplay) {
                            play()
                        }
                    }
                    Player.STATE_ENDED -> {
                        Log.d(TAG, "ExoPlayer STATE_ENDED")
                        sendEvent("onPlaybackComplete", Arguments.createMap())
                    }
                    Player.STATE_BUFFERING -> {
                        Log.d(TAG, "ExoPlayer STATE_BUFFERING")
                    }
                    Player.STATE_IDLE -> {
                        Log.d(TAG, "ExoPlayer STATE_IDLE")
                    }
                    Player.STATE_BUFFERING -> {
                        Log.d(TAG, "ExoPlayer STATE_BUFFERING")
                    }
                }
            }

            override fun onIsPlayingChanged(isPlaying: Boolean) {
                if (isPlaying) {
                    Log.d(TAG, "ExoPlayer isPlaying")
                    sendEvent("onPlay", Arguments.createMap())
                } else {
                    Log.d(TAG, "ExoPlayer isPaused")
                    sendEvent("onPause", Arguments.createMap())
                }
            }

            override fun onPlayerError(error: PlaybackException) {
                Log.e(TAG, "ExoPlayer Error: ${error.message}, errorCode: ${error.errorCodeName}, errorCode বিস্তারিত: ${error.errorCode}")
                val event = Arguments.createMap().apply {
                    putInt("code", error.errorCode)
                    putString("message", error.message)
                }
                sendEvent("onError", event)
            }

            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                Log.d(TAG, "ExoPlayer onMediaItemTransition")
                sendEvent("onLoadStart", Arguments.createMap())
            }

            override fun onPlaybackSuppressionReasonChanged(playbackSuppressionReason: Int) {
                // Called when playback is suppressed temporarily due to content restrictions.
                Log.d(TAG, "ExoPlayer onPlaybackSuppressionReasonChanged: $playbackSuppressionReason")
            }

            override fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) {
                // Called when playWhenReady state changes.
                Log.d(TAG, "ExoPlayer onPlayWhenReadyChanged: playWhenReady=$playWhenReady, reason=$reason")
            }

            override fun onPositionDiscontinuity(reason: Int) {
                 // Called when there's a discontinuity in playback, such as seeking or ad insertion.
                 Log.d(TAG, "ExoPlayer onPositionDiscontinuity: reason=$reason")
            }

            override fun onRepeatModeChanged(repeatMode: Int) {
                // Called when the repeat mode changes.
                Log.d(TAG, "ExoPlayer onRepeatModeChanged: repeatMode=$repeatMode")
            }

            override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {
                // Called when shuffle mode is enabled or disabled.
                Log.d(TAG, "ExoPlayer onShuffleModeEnabledChanged: shuffleModeEnabled=$shuffleModeEnabled")
            }

            override fun onTracksChanged(tracks: Tracks) {
                // Called when available tracks change.
                Log.d(TAG, "ExoPlayer onTracksChanged: tracks=$tracks")
            }

            override fun onVolumeChanged(volume: Float) {
                // Called when volume changes.
                Log.d(TAG, "ExoPlayer onVolumeChanged: volume=$volume")
            }

            override fun onVideoSizeChanged(videoSize: VideoSize) {
                // Called when video size changes.
                Log.d(TAG, "ExoPlayer onVideoSizeChanged: videoSize=$videoSize")
            }

            override fun onSurfaceSizeChanged(width: Int, height: Int) {
                // Called when the size of the surface changes.
                Log.d(TAG, "ExoPlayer onSurfaceSizeChanged: width=$width, height=$height")
            }

            override fun onRenderedFirstFrame() {
                // Called when the first frame is rendered.
                Log.d(TAG, "ExoPlayer onRenderedFirstFrame")
            }

            override fun onSkipSilenceEnabledChanged(skipSilenceEnabled: Boolean) {
                // Called when skip silence is enabled or disabled.
                Log.d(TAG, "ExoPlayer onSkipSilenceEnabledChanged: skipSilenceEnabled=$skipSilenceEnabled")
            }

            override fun onTimelineChanged(timeline: Timeline, reason: Int) {
                // Called when the timeline changes, like when a new media source is loaded or an ad break starts or ends.
                Log.d(TAG, "ExoPlayer onTimelineChanged: timeline=$timeline, reason=$reason")
            }
        })
    }

    fun setVideoUrl(url: String?) {
        Log.d(TAG, "Setting video URL: $url")
        if (url.isNullOrEmpty()) {
            Log.d(TAG, "Video URL is null or empty, skipping load")
            return
        }

        videoUrl = url


        // Load video with ExoPlayer
        Log.d(TAG, "Creating MediaItem from URI: $videoUrl")
        val mediaItem = MediaItem.fromUri(videoUrl!!)
        player?.setMediaItem(mediaItem)
        Log.d(TAG, "Preparing ExoPlayer")
        player?.prepare()
        Log.d(TAG, "ExoPlayer prepared")
    }
    
    fun setAuthToken(token: String?) {
        authToken = token
        // If URL already set, reload with new token
        if (!videoUrl.isNullOrEmpty()) {
            setVideoUrl(videoUrl) // reload video
        }
    }
    
    fun setAutoplay(value: Boolean) {
        autoplay = value
    }
    
    fun setLoop(value: Boolean) {
        loop = value
        player?.repeatMode = if (loop) Player.REPEAT_MODE_ALL else Player.REPEAT_MODE_OFF
    }
    
    fun play() {
        Log.d(TAG, "Play called")
        player?.play()
        isPlaying = true
    }
    
    fun pause() {
        Log.d(TAG, "Pause called")
        player?.pause()
        isPlaying = false
    }
    
    fun seekTo(time: Float) {
        Log.d(TAG, "Seek called: $time")
        player?.seekTo((time * 1000).toLong()) // time in seconds, ExoPlayer in milliseconds
    }
    
    fun getCurrentTime(): Float {
        return (player?.currentPosition?.toFloat() ?: 0f) / 1000f // to seconds
    }
    
    fun getDuration(): Float {
        return (player?.duration?.toFloat() ?: 0f) / 1000f // to seconds
    }
    
    private fun sendEvent(eventName: String, params: WritableMap) {
        UnifiedPlayerEventEmitter.getInstance()?.sendEvent(eventName, params)
    }
    
    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        player?.release()
    }
}
