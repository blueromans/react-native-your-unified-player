package com.unifiedplayer

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.view.PixelCopy
import android.util.Base64
import java.io.ByteArrayOutputStream
import android.util.Log
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
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
import java.io.File
import java.io.IOException
import java.util.Collections // Import for Collections.emptyList()
import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.media.MediaMuxer
import android.view.Surface
import java.nio.ByteBuffer
import android.os.Environment
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
    // Recording related variables
    private var mediaRecorder: MediaMuxer? = null
    private var videoEncoder: MediaCodec? = null
    private var recordingSurface: Surface? = null
    private var isRecording = false
    private var outputPath: String? = null
    private var recordingThread: Thread? = null
    private var videoTrackIndex = -1
    private val bufferInfo = MediaCodec.BufferInfo()
    companion object {
        private const val TAG = "UnifiedPlayerView"
    }

    // Player state
    private var videoUrl: String? = null // Single video URL
    private var videoUrls: List<String> = emptyList() // Playlist URLs
    private var currentVideoIndex: Int = 0
    private var isPlaylist: Boolean = false
    private var thumbnailUrl: String? = null
    private var autoplay: Boolean = true
    private var loop: Boolean = false
    private var textureView: android.view.TextureView
    private var thumbnailImageView: ImageView? = null
    internal var player: ExoPlayer? = null
    private var currentProgress = 0
    private var isPaused = false

    private val progressHandler = Handler(Looper.getMainLooper())
    private val progressRunnable: Runnable = object : Runnable {
        override fun run() {
            player?.let {
                val currentTime = it.currentPosition.toFloat() / 1000f
                val duration = it.duration.toFloat() / 1000f

                // Log the actual values for debugging
                // Log.d(TAG, "Progress values - currentTime: $currentTime, duration: $duration, raw duration: ${it.duration}")

                // Only send valid duration values
                if (it.duration > 0) {
                    val event = Arguments.createMap()
                    event.putDouble("currentTime", currentTime.toDouble())
                    event.putDouble("duration", duration.toDouble())
                    
                    // Log.d(TAG, "Sending progress event: currentTime=$currentTime, duration=$duration")
                    sendEvent(EVENT_PROGRESS, event)
                } else {
                    // Log.d(TAG, "Not sending progress event because duration is $duration (raw: ${it.duration})")
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

        // Create TextureView for video rendering
        textureView = android.view.TextureView(context).apply {
            layoutParams = LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT
            )
        }

        // Create ImageView for thumbnail
        thumbnailImageView = ImageView(context).apply {
            layoutParams = LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT
            )
            scaleType = ImageView.ScaleType.CENTER_CROP
            visibility = View.GONE
        }

        // Add views to the layout (thumbnail on top of TextureView)
        addView(textureView)
        addView(thumbnailImageView)

    // We'll set the video surface when the TextureView's surface is available
    // in the onSurfaceTextureAvailable callback
    Log.d(TAG, "TextureView added to view hierarchy")
        // Log dimensions of the view
        post {
            Log.d(TAG, "UnifiedPlayerView dimensions after init: width=${width}, height=${height}")
        }

        player?.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                Log.d(TAG, "onPlaybackStateChanged: $playbackState")
                when (playbackState) {
                    Player.STATE_READY -> {
                        Log.d(TAG, "ExoPlayer STATE_READY")
                        // Ensure thumbnail is hidden when ready (might be needed if autoplay=false)
                        if (player?.isPlaying == false) { // Check if not already playing
                             thumbnailImageView?.visibility = View.GONE
                        }
                        sendEvent(EVENT_READY, Arguments.createMap())
                        // Start progress updates when ready
                        startProgressUpdates()
                    }
                    Player.STATE_ENDED -> {
                        Log.d(TAG, "ExoPlayer STATE_ENDED")
                        if (isPlaylist) {
                            // Playlist logic
                            val nextIndex = currentVideoIndex + 1
                            if (nextIndex < videoUrls.size) {
                                // Play next video in the list
                                Log.d(TAG, "Playlist: Loading next video at index $nextIndex")
                                loadVideoAtIndex(nextIndex)
                                // Don't send EVENT_COMPLETE for individual items in playlist
                            } else {
                                // Reached the end of the playlist
                                if (loop) {
                                    // Loop playlist: Go back to the first video
                                    Log.d(TAG, "Playlist: Looping back to start")
                                    loadVideoAtIndex(0)
                                    // Don't send EVENT_COMPLETE when looping playlist
                                } else {
                                    // End of playlist, not looping
                                    Log.d(TAG, "Playlist: Reached end, not looping")
                                    currentVideoIndex = 0 // Reset index for potential future play
                                    sendEvent(EVENT_COMPLETE, Arguments.createMap()) // Send completion for the whole list
                                }
                            }
                        } else {
                            // Single video logic (ExoPlayer handles looping via repeatMode)
                            if (!loop) {
                                // Send completion event only if not looping a single video
                                sendEvent(EVENT_COMPLETE, Arguments.createMap())
                            } else {
                                Log.d(TAG, "Single video ended and loop is ON - ExoPlayer will repeat.")
                                // Optionally send an event here if needed for single loop cycle completion
                            }
                        }
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
                    // Hide thumbnail when video starts playing
                    thumbnailImageView?.visibility = View.GONE
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
                // Handle video size changes if needed
            }
        })
    }

    // Helper function to load and prepare a video URL
    private fun loadVideoSource(url: String) {
        Log.d(TAG, "Loading video source: $url")
        try {
            val mediaItem = MediaItem.fromUri(url)
            player?.stop() // Stop previous playback
            player?.clearMediaItems() // Clear previous items
            player?.setMediaItem(mediaItem)
            player?.prepare()
            player?.playWhenReady = autoplay && !isPaused // Apply autoplay and paused state
            
            // Explicitly set repeat mode here based on current state
            if (isPlaylist) {
                 player?.repeatMode = Player.REPEAT_MODE_OFF // Force OFF for playlists
            } else {
                 player?.repeatMode = if (loop) Player.REPEAT_MODE_ONE else Player.REPEAT_MODE_OFF // Use loop prop for single videos
             }

            Log.d(TAG, "ExoPlayer configured with URL: $url, autoplay: $autoplay, loop: $loop, isPaused: $isPaused, repeatMode: ${player?.repeatMode}")
            // Send load start event, include index if it's a playlist
            val loadStartEvent = Arguments.createMap()
            if (isPlaylist) {
                loadStartEvent.putInt("index", currentVideoIndex)
            }
            sendEvent(EVENT_LOAD_START, loadStartEvent) 

        } catch (e: Exception) {
            Log.e(TAG, "Error setting video source: ${e.message}", e)
            val event = Arguments.createMap()
            event.putString("code", "SOURCE_ERROR")
            event.putString("message", "Failed to load video source: $url")
            sendEvent(EVENT_ERROR, event)
        }
    }

    // Method to load a specific video from the playlist
    private fun loadVideoAtIndex(index: Int) {
        if (index >= 0 && index < videoUrls.size) {
            currentVideoIndex = index
            val url = videoUrls[index]
            Log.d(TAG, "Loading playlist item at index $index: $url")
            loadVideoSource(url)
        } else {
            Log.e(TAG, "Invalid index $index for playlist size ${videoUrls.size}")
        }
    }

    // Called by ViewManager for single URL
    fun setVideoUrl(url: String?) {
        Log.d(TAG, "Setting single video URL: $url")
        isPlaylist = false // Mark as not a playlist
        videoUrls = emptyList() // Clear any previous playlist
        currentVideoIndex = 0

        if (url != null && url.isNotEmpty()) { // Check for non-null and non-empty
            videoUrl = url // Store the non-null url
            loadVideoSource(url) // Call loadVideoSource only when url is guaranteed non-null
        } else {
            Log.w(TAG, "Received null or empty URL for single video.")
            player?.stop()
            player?.clearMediaItems()
            videoUrl = null // Ensure internal state is cleared
            // Optionally show thumbnail or placeholder if URL is cleared
        }
    }

    // Called by ViewManager for URL list (playlist)
    fun setVideoUrls(urls: List<String>) {
        Log.d(TAG, "Setting video URL list (playlist) with ${urls.size} items.")
        if (urls.isEmpty()) {
            Log.w(TAG, "Received empty URL list.")
            setVideoUrl(null) // Treat empty list as clearing the source
            return
        }
        isPlaylist = true // Mark as a playlist
        videoUrl = null // Clear single video URL
        videoUrls = urls
        currentVideoIndex = 0 // Start from the beginning

        // Load the first video in the playlist
        loadVideoAtIndex(currentVideoIndex)
    }

    fun setAutoplay(value: Boolean) {
        autoplay = value
        player?.playWhenReady = value
    }

    fun setLoop(value: Boolean) {
        Log.d(TAG, "Setting loop to: $value, isPlaylist: $isPlaylist")
        loop = value
        // Only set ExoPlayer's repeatMode if NOT in playlist mode.
        // Playlist looping is handled manually in onPlaybackStateChanged.
        if (!isPlaylist) {
             // Use REPEAT_MODE_ONE for single item looping
             player?.repeatMode = if (loop) Player.REPEAT_MODE_ONE else Player.REPEAT_MODE_OFF
        } else {
             // Ensure repeat mode is off when handling playlists manually
             player?.repeatMode = Player.REPEAT_MODE_OFF
        }
    }

    fun setThumbnailUrl(url: String?) {
        Log.d(TAG, "Setting thumbnail URL: $url")
        
        thumbnailUrl = url
        
        if (url != null && url.isNotEmpty()) {
            // Show the thumbnail ImageView
            thumbnailImageView?.visibility = View.VISIBLE
            
            // Load the thumbnail image using Glide
            try {
                Glide.with(context)
                    .load(url)
                    .apply(RequestOptions().centerCrop())
                    .into(thumbnailImageView!!)
                
                Log.d(TAG, "Thumbnail image loading started")
            } catch (e: Exception) {
                Log.e(TAG, "Error loading thumbnail image: ${e.message}", e)
                thumbnailImageView?.visibility = View.GONE
            }
        } else {
            // Hide the thumbnail if URL is null or empty
            thumbnailImageView?.visibility = View.GONE
        }
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

    // Add a getter for the ExoPlayer instance
    val exoPlayer: ExoPlayer?
        get() = this.player

    private fun sendEvent(eventName: String, params: WritableMap) {
        try {
            // Log the event for debugging
            // Log.d(TAG, "Sending direct event: $eventName with params: $params")
            
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
    
    // Method to explicitly start progress updates
    private fun startProgressUpdates() {
        // Only start if player is ready and has duration
        if (player?.playbackState == Player.STATE_READY && (player?.duration ?: 0) > 0) {
             Log.d(TAG, "Starting progress updates")
             progressHandler.removeCallbacks(progressRunnable) // Remove existing callbacks
             progressHandler.post(progressRunnable) // Post the runnable
        } else {
             Log.d(TAG, "Skipping progress updates start: Player not ready or duration is 0")
        }
    }

    // Method to stop progress updates
    private fun stopProgressUpdates() {
        Log.d(TAG, "Stopping progress updates")
        progressHandler.removeCallbacks(progressRunnable)
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        val width = right - left
        val height = bottom - top
        Log.d(TAG, "UnifiedPlayerView onLayout: width=$width, height=$height")
        // Ensure textureView gets laid out properly
        textureView.layout(0, 0, width, height)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        Log.d(TAG, "UnifiedPlayerView onAttachedToWindow")
        textureView.surfaceTextureListener = object : android.view.TextureView.SurfaceTextureListener {
    override fun onSurfaceTextureAvailable(surface: android.graphics.SurfaceTexture, width: Int, height: Int) {
        Log.d(TAG, "TextureView onSurfaceTextureAvailable: width=$width, height=$height")
        // Create a Surface from the SurfaceTexture and set it on the player
        val videoSurface = android.view.Surface(surface)
        player?.setVideoSurface(videoSurface)
        Log.d(TAG, "Set video surface from TextureView's SurfaceTexture")
    }

    override fun onSurfaceTextureSizeChanged(surface: android.graphics.SurfaceTexture, width: Int, height: Int) {
        Log.d(TAG, "TextureView onSurfaceTextureSizeChanged: width=$width, height=$height")
    }

    override fun onSurfaceTextureDestroyed(surface: android.graphics.SurfaceTexture): Boolean {
        Log.d(TAG, "TextureView onSurfaceTextureDestroyed")
        // Set the player's surface to null to release it
        player?.setVideoSurface(null)
        Log.d(TAG, "Cleared video surface from player")
        return true
    }

    override fun onSurfaceTextureUpdated(surface: android.graphics.SurfaceTexture) {
        // This is called very frequently, so we'll comment out this log
        // Log.d(TAG, "TextureView onSurfaceTextureUpdated")
    }
}
        // Don't start progress updates here automatically, wait for STATE_READY
        // startProgressUpdates()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        Log.d(TAG, "UnifiedPlayerView onDetachedFromWindow")
        stopProgressUpdates() // Stop progress updates
        player?.release()
        player = null // Ensure player is nullified
        cleanupRecording() // Clean up recording resources if any
    }

    fun capture(): String {
        Log.d(TAG, "Capture method called")
        return try {
            player?.let { exoPlayer ->
                // Get the video size from the player
                val videoSize = exoPlayer.videoSize
                if (videoSize.width <= 0 || videoSize.height <= 0) {
                    Log.e(TAG, "Invalid video dimensions: ${videoSize.width}x${videoSize.height}")
                    return ""
                }

                // Get bitmap directly from TextureView
val bitmap = textureView.bitmap ?: run {
    Log.e(TAG, "Failed to get bitmap from TextureView")
    return ""
}

// Debugging: Log the dimensions of the bitmap
bitmap.let {
    Log.d(TAG, "Bitmap dimensions: width=${it.width}, height=${it.height}")
    
    // Check if bitmap is empty (all black)
    var hasNonBlackPixel = false
    for (x in 0 until it.width) {
        for (y in 0 until it.height) {
            if (it.getPixel(x, y) != Color.BLACK) {
                hasNonBlackPixel = true
                break
            }
        }
        if (hasNonBlackPixel) break
    }
    
    if (!hasNonBlackPixel) {
        Log.w(TAG, "Bitmap appears to be all black")
    }
}
                // Compress and encode the bitmap
                val byteArrayOutputStream = ByteArrayOutputStream()
                if (bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)) {
                    val byteArray = byteArrayOutputStream.toByteArray()
                    val base64EncodedString = Base64.encodeToString(byteArray, Base64.DEFAULT)
                    Log.d(TAG, "Capture successful, base64 length: ${base64EncodedString.length}")
                    base64EncodedString
                } else {
                    Log.e(TAG, "Failed to compress bitmap")
                    ""
                }
            } ?: run {
                Log.e(TAG, "Cannot capture: player is null")
                ""
                }
            } catch (e: Exception) {
            Log.e(TAG, "Error during capture: ${e.message}", e)
            ""
        }
    }
    
    /**
     * Start recording the video to the specified output path
     * @param outputPath Path where to save the recording
     * @return true if recording started successfully
     */
    fun startRecording(outputPath: String): Boolean {
        Log.d(TAG, "startRecording called with outputPath: $outputPath")
        
        if (isRecording) {
            Log.w(TAG, "Recording is already in progress")
            return false
        }
        
        try {
            player?.let { exoPlayer ->
                // Get the current media item's URI
                val currentUri = exoPlayer.currentMediaItem?.localConfiguration?.uri?.toString()
                if (currentUri == null) {
                    Log.e(TAG, "Current media URI is null")
                    return false
                }
                
                Log.d(TAG, "Current media URI: $currentUri")
                
                // Store the output path
                this.outputPath = if (outputPath.isNullOrEmpty()) {
                    // Use app-specific storage for Android 10+ (API level 29+)
                    val appContext = context.applicationContext
                    val moviesDir = File(appContext.getExternalFilesDir(Environment.DIRECTORY_MOVIES), "recordings")
                    if (!moviesDir.exists()) {
                        moviesDir.mkdirs()
                    }
                    File(moviesDir, "recording_${System.currentTimeMillis()}.mp4").absolutePath
                } else {
                    outputPath
                }
                
                // Create parent directories if they don't exist
                val outputFile = File(this.outputPath)
                outputFile.parentFile?.mkdirs()
                
                // Log the final output path
                Log.d(TAG, "Recording will be saved to: ${this.outputPath}")
                
                // Start a background thread to download the file
                Thread {
                    try {
                        // Create a URL from the URI
                        val url = java.net.URL(currentUri)
                        
                        // Open connection
                        val connection = url.openConnection() as java.net.HttpURLConnection
                        connection.requestMethod = "GET"
                        connection.connectTimeout = 15000
                        connection.readTimeout = 15000
                        connection.doInput = true
                        connection.connect()
                        
                        // Check if the connection was successful
                        if (connection.responseCode != java.net.HttpURLConnection.HTTP_OK) {
                            Log.e(TAG, "HTTP error code: ${connection.responseCode}")
                            return@Thread
                        }
                        
                        // Get the input stream
                        val inputStream = connection.inputStream
                        
                        // Create the output file
                        val outputFile = File(this.outputPath!!)
                        
                        // Create the output stream
                        val outputStream = outputFile.outputStream()
                        
                        // Create a buffer
                        val buffer = ByteArray(1024)
                        var bytesRead: Int
                        var totalBytesRead: Long = 0
                        val fileSize = connection.contentLength.toLong()
                        
                        // Read from the input stream and write to the output stream
                        while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                            outputStream.write(buffer, 0, bytesRead)
                            totalBytesRead += bytesRead
                            
                            // Log progress
                            if (fileSize > 0) {
                                val progress = (totalBytesRead * 100 / fileSize).toInt()
                                Log.d(TAG, "Download progress: $progress%")
                            }
                        }
                        
                        // Close the streams
                        outputStream.flush()
                        outputStream.close()
                        inputStream.close()
                        
                        Log.d(TAG, "File downloaded successfully to ${this.outputPath}")
                    } catch (e: Exception) {
                        Log.e(TAG, "Error downloading file: ${e.message}", e)
                    }
                }.start()
                
                isRecording = true
                Log.d(TAG, "Recording started successfully")
                return true
            } ?: run {
                Log.e(TAG, "Cannot start recording: player is null")
                return false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error starting recording: ${e.message}", e)
            return false
        }
    }
    
    /**
     * Stop recording and save the video
     * @return Path to the saved recording
     */
    fun stopRecording(): String {
        Log.d(TAG, "stopRecording called")
        
        if (!isRecording) {
            Log.w(TAG, "No recording in progress")
            return ""
        }
        
        // Simply mark recording as stopped
        isRecording = false
        
        // Wait a moment to ensure any background operations complete
        try {
            Thread.sleep(500)
        } catch (e: InterruptedException) {
            Log.e(TAG, "Sleep interrupted: ${e.message}")
        }
        
        // Return the path where the recording was saved
        val savedPath = outputPath ?: ""
        Log.d(TAG, "Recording stopped successfully, saved to: $savedPath")
        
        return savedPath
    }
    
    private fun cleanupRecording() {
        try {
            videoEncoder?.stop()
            videoEncoder?.release()
            videoEncoder = null
            
            recordingSurface?.release()
            recordingSurface = null
            
            mediaRecorder?.stop()
            mediaRecorder?.release()
            mediaRecorder = null
            
            videoTrackIndex = -1
            isRecording = false
        } catch (e: Exception) {
            Log.e(TAG, "Error cleaning up recording resources: ${e.message}", e)
        }
    }
    
    private inner class RecordingRunnable : Runnable {
        override fun run() {
            try {
                // Add video track to muxer
                val videoFormat = videoEncoder?.outputFormat
                if (videoFormat != null && mediaRecorder != null) {
                    videoTrackIndex = mediaRecorder!!.addTrack(videoFormat)
                } else {
                    Log.e(TAG, "Cannot add track: videoFormat or mediaRecorder is null")
                    videoTrackIndex = -1
                }
                
                // Start the muxer
                mediaRecorder?.start()
                
                // Process encoding
                while (isRecording) {
                    val encoderStatus = videoEncoder?.dequeueOutputBuffer(bufferInfo, 10000) ?: -1
                    
                    if (encoderStatus >= 0) {
                        val encodedData = videoEncoder?.getOutputBuffer(encoderStatus)
                        
                        if ((bufferInfo.flags and MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
                            // Ignore codec config data
                            bufferInfo.size = 0
                        }
                        
                        if (bufferInfo.size > 0 && encodedData != null && mediaRecorder != null && videoTrackIndex >= 0) {
                            encodedData.position(bufferInfo.offset)
                            encodedData.limit(bufferInfo.offset + bufferInfo.size)
                            
                            mediaRecorder!!.writeSampleData(videoTrackIndex, encodedData, bufferInfo)
                        }
                        
                        videoEncoder?.releaseOutputBuffer(encoderStatus, false)
                        
                        if ((bufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                            break
                        }
                    } else if (encoderStatus == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                        // Handle format change if needed
                    }
                }
                
                // Signal end of stream to encoder
                videoEncoder?.signalEndOfInputStream()
                
            } catch (e: Exception) {
                Log.e(TAG, "Error in recording thread: ${e.message}", e)
            }
        }
    }
}
