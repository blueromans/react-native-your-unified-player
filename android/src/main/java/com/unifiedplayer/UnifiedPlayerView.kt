package com.unifiedplayer

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.util.Log
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.TextureView
import android.view.View
import android.widget.FrameLayout
import com.facebook.react.bridge.Arguments
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.PlaybackException
import com.google.android.exoplayer2.Tracks
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.video.VideoSize
import com.facebook.react.bridge.WritableMap
import com.facebook.react.bridge.ReactContext
import com.facebook.react.uimanager.events.RCTEventEmitter
import com.unifiedplayer.UnifiedPlayerEventEmitter.Companion.EVENT_COMPLETE
import com.unifiedplayer.UnifiedPlayerEventEmitter.Companion.EVENT_ERROR
import com.unifiedplayer.UnifiedPlayerEventEmitter.Companion.EVENT_LOAD_START
import com.unifiedplayer.UnifiedPlayerEventEmitter.Companion.EVENT_PAUSED
import com.unifiedplayer.UnifiedPlayerEventEmitter.Companion.EVENT_PLAYING
import com.unifiedplayer.UnifiedPlayerEventEmitter.Companion.EVENT_PROGRESS
import com.unifiedplayer.UnifiedPlayerEventEmitter.Companion.EVENT_READY
import com.unifiedplayer.UnifiedPlayerEventEmitter.Companion.EVENT_RESUMED
import com.unifiedplayer.UnifiedPlayerEventEmitter.Companion.EVENT_STALLED

class UnifiedPlayerView(context: Context) : FrameLayout(context) {
    companion object {
        private const val TAG = "UnifiedPlayerView"
    }

    private var videoUrl: String? = null
    private var autoplay: Boolean = true
    private var loop: Boolean = false
    private var playerView: PlayerView
    private var player: ExoPlayer? = null
    private var currentProgress = 0
    private var isPaused = false

    private val progressHandler = Handler(Looper.getMainLooper())
    private val progressRunnable: Runnable = object : Runnable {
        override fun run() {
            player?.let {
                val currentTime = it.currentPosition.toFloat() / 1000f
                val duration = it.duration.toFloat() / 1000f

                // Log the actual values for debugging
                Log.d(TAG, "Progress values - currentTime: $currentTime, duration: $duration, raw duration: ${it.duration}")

                // Only send valid duration values
                if (it.duration > 0) {
                    val event = Arguments.createMap()
                    event.putDouble("currentTime", currentTime.toDouble())
                    event.putDouble("duration", duration.toDouble())
                    
                    Log.d(TAG, "Sending progress event: currentTime=$currentTime, duration=$duration")
                    sendEvent(EVENT_PROGRESS, event)
                } else {
                    Log.d(TAG, "Not sending progress event because duration is $duration (raw: ${it.duration})")
                }
            } ?: Log.e(TAG, "Cannot send progress event: player is null")
            
            // Schedule the next update
            progressHandler.postDelayed(this, 250) // Update every 250ms
        }
    }

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
            // playerView.surfaceView?.surfaceType = android.view.SurfaceView.SURFACE_TYPE_SOFTWARE // Reverted: Let's remove surfaceType setting
        }

        addView(playerView)
        // Add logging for playerView dimensions and post play call
        playerView.post {
            Log.d(TAG, "PlayerView dimensions after addView: width=${playerView.width}, height=${playerView.height}")
        }

        player?.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                Log.d(TAG, "onPlaybackStateChanged: $playbackState") // Added log
                when (playbackState) {
                    Player.STATE_READY -> {
                        Log.d(TAG, "ExoPlayer STATE_READY")
                        sendEvent(EVENT_READY, Arguments.createMap())
                    }
                    Player.STATE_ENDED -> {
                        Log.d(TAG, "ExoPlayer STATE_ENDED")
                        sendEvent(EVENT_COMPLETE, Arguments.createMap())
                    }
                    Player.STATE_BUFFERING -> {
                        Log.d(TAG, "ExoPlayer STATE_BUFFERING")
                        sendEvent(EVENT_STALLED, Arguments.createMap())
                    }
                    Player.STATE_IDLE -> {
                        Log.d(TAG, "ExoPlayer STATE_IDLE")
                    }
                }
            }

            override fun onIsPlayingChanged(isPlaying: Boolean) {
                Log.d(TAG, "onIsPlayingChanged: $isPlaying") // Added log
                if (isPlaying) {
                    Log.d(TAG, "ExoPlayer is now playing")
                    sendEvent(EVENT_RESUMED, Arguments.createMap())
                    sendEvent(EVENT_PLAYING, Arguments.createMap())
                } else {
                    Log.d(TAG, "ExoPlayer is now paused")
                    sendEvent(EVENT_PAUSED, Arguments.createMap())
                }
            }

            override fun onPlayerError(error: PlaybackException) {
                Log.e(TAG, "ExoPlayer error: $error")
                val event = Arguments.createMap()
                event.putString("code", "PLAYBACK_ERROR")
                event.putString("message", error.message ?: "Unknown playback error")
                sendEvent(EVENT_ERROR, event)
            }

            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                Log.d(TAG, "onMediaItemTransition with reason: $reason")
                sendEvent(EVENT_LOAD_START, Arguments.createMap())
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
                Log.d(TAG, "Video size changed: width=${videoSize.width}, height=${videoSize.height}")
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
        })
    }

    fun setVideoUrl(url: String?) {
        Log.d(TAG, "Setting video URL: $url")
        
        if (url == null || url.isEmpty()) {
            Log.e(TAG, "Empty or null URL provided")
            return
        }
        
        videoUrl = url
        
        try {
            // Create a MediaItem
            val mediaItem = MediaItem.fromUri(url)
            
            // Reset the player to ensure clean state
            player?.stop()
            player?.clearMediaItems()
            
            // Set the media item
            player?.setMediaItem(mediaItem)
            
            // Prepare the player (this will start loading the media)
            player?.prepare()
            
            // Set playWhenReady based on autoplay setting
            player?.playWhenReady = autoplay && !isPaused
            
            // Set repeat mode based on loop setting
            player?.repeatMode = if (loop) Player.REPEAT_MODE_ALL else Player.REPEAT_MODE_OFF
            
            // Log that we've set up the player
            Log.d(TAG, "ExoPlayer configured with URL: $url, autoplay: $autoplay, loop: $loop")
            
            // Add a listener to check when the player is ready and has duration
            player?.addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(state: Int) {
                    if (state == Player.STATE_READY) {
                        val duration = player?.duration ?: 0
                        Log.d(TAG, "Player ready with duration: ${duration / 1000f} seconds")
                        
                        // Force a progress update immediately
                        progressRunnable.run()
                    }
                }
            })
        } catch (e: Exception) {
            Log.e(TAG, "Error setting video URL: ${e.message}", e)
            
            // Send error event
            val event = Arguments.createMap()
            event.putString("code", "SOURCE_ERROR")
            event.putString("message", "Failed to load video source: $url")
            sendEvent(EVENT_ERROR, event)
        }
    }

    fun setAuthToken(token: String?) {
        // Removed as per request
        Log.d(TAG, "Auth token handling removed")
    }

    fun setAutoplay(value: Boolean) {
        autoplay = value
        player?.playWhenReady = value // Set ExoPlayer's playWhenReady property
    }

    fun setLoop(value: Boolean) {
        loop = value
        player?.repeatMode = if (loop) Player.REPEAT_MODE_ALL else Player.REPEAT_MODE_OFF
    }

    fun setIsPaused(isPaused: Boolean) {
        Log.d(TAG, "setIsPaused called with value: $isPaused")
        this.isPaused = isPaused
        if (isPaused) {
            player?.pause()
        } else {
            player?.play()
        }
    }

    fun play() {
        Log.d(TAG, "Play method called")
        player?.playWhenReady = true
    }

    fun pause() {
        Log.d(TAG, "Pause method called")
        player?.playWhenReady = false
    }

    fun seekTo(seconds: Float) {
        Log.d(TAG, "SeekTo method called with seconds: $seconds")
        player?.let {
            val milliseconds = (seconds * 1000).toLong()
            Log.d(TAG, "Seeking to $milliseconds ms")
            it.seekTo(milliseconds)
            
            // Force a progress update after seeking
            progressRunnable.run()
        } ?: Log.e(TAG, "Cannot seek: player is null")
    }

    fun getCurrentTime(): Float {
        Log.d(TAG, "GetCurrentTime method called")
        return player?.let {
            val currentTime = it.currentPosition.toFloat() / 1000f
            Log.d(TAG, "Current time: $currentTime seconds")
            currentTime
        } ?: run {
            Log.e(TAG, "Cannot get current time: player is null")
            0f
        }
    }

fun getDuration(): Float {
    Log.d(TAG, "GetDuration method called")
    return player?.let {
        val duration = it.duration.toFloat() / 1000f
        Log.d(TAG, "Duration: $duration seconds (raw: ${it.duration})")
        if (it.duration > 0) duration else 0f
    } ?: run {
        Log.e(TAG, "Cannot get duration: player is null")
        0f
    }
}

fun captureFrame(): Bitmap? {
    Log.d(TAG, "CaptureFrame method called")
    try {
        // Create a bitmap with the same dimensions as the player view
        val bitmap = Bitmap.createBitmap(
            playerView.width, 
            playerView.height,
            Bitmap.Config.ARGB_8888
        )
        
        // Create a canvas with the bitmap
        val canvas = Canvas(bitmap)
        
        // Draw the player view onto the canvas
        playerView.draw(canvas)
        
        Log.d(TAG, "Successfully captured frame from PlayerView")
        return bitmap
    } catch (e: Exception) {
        Log.e(TAG, "Error capturing frame: ${e.message}", e)
        return null
    }
}

    // Add a getter for the ExoPlayer instance
    val exoPlayer: ExoPlayer?
        get() = this.player

    private fun sendEvent(eventName: String, params: WritableMap) {
        try {
            // Log the event for debugging
            Log.d(TAG, "Sending direct event: $eventName with params: $params")
            
            // Map event names to their corresponding top event names
            val topEventName = when (eventName) {
                EVENT_READY -> "topReadyToPlay"
                EVENT_ERROR -> "topError"
                EVENT_PROGRESS -> "topProgress"
                EVENT_COMPLETE -> "topPlaybackComplete"
                EVENT_STALLED -> "topPlaybackStalled"
                EVENT_RESUMED -> "topPlaybackResumed"
                EVENT_PLAYING -> "topPlaying"
                EVENT_PAUSED -> "topPlaybackPaused"
                EVENT_LOAD_START -> "topLoadStart"
                else -> "top${eventName.substring(2)}" // Fallback for any other events
            }
            
            // Use the ReactContext to dispatch the event directly to the view
            val reactContext = context as ReactContext
            reactContext.getJSModule(RCTEventEmitter::class.java)
                .receiveEvent(id, topEventName, params)
        } catch (e: Exception) {
            Log.e(TAG, "Error sending event $eventName: ${e.message}", e)
        }
    }
    
    // Add a method to explicitly start progress updates
    private fun startProgressUpdates() {
        Log.d(TAG, "Starting progress updates")
        // Remove any existing callbacks to avoid duplicates
        progressHandler.removeCallbacks(progressRunnable)
        // Post the runnable to start updates
        progressHandler.post(progressRunnable)
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        val width = right - left
        val height = bottom - top
        Log.d(TAG, "UnifiedPlayerView onLayout: width=$width, height=$height")
        // Ensure playerView also gets laid out
        playerView.layout(0, 0, width, height)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        Log.d(TAG, "UnifiedPlayerView onAttachedToWindow")
        playerView.setPlayer(player)
        startProgressUpdates() // Use the new method to start progress updates
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        Log.d(TAG, "UnifiedPlayerView onDetachedFromWindow")
        progressHandler.removeCallbacks(progressRunnable) // Stop progress updates
        player?.release()
    }
}
