/**
 * android/src/.../UnifiedPlayerView.kt (Refactored for Unified Player - Conceptual)
 * Custom Android View using ExoPlayer for MP4 and a WebRTC SDK for streams.
 * !!! REQUIRES FULL IMPLEMENTATION of player/SDK logic and state management !!!
 */
package com.yourunifiedplayer // Use your unified package name

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
// --- Import classes from your chosen WebRTC SDK ---
// import com.github.webrtc_sdk.android.? // Example
// import com.github.webrtc_sdk.android.SomeWebRTCView // Example

import com.facebook.react.bridge.ReactContext
import com.facebook.react.uimanager.events.RCTEventEmitter
import com.facebook.react.bridge.WritableMap
import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.ReadableMap

class UnifiedPlayerView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs /* Add relevant listeners */) {

    private enum class PlayerMode { NONE, MP4, WEBRTC }
    private var currentMode = PlayerMode.NONE
    private var currentSourceData: ReadableMap? = null // Store the source object
    private var currentSourceType: String? = null // Store the type ('mp4', 'webrtc')

    // --- Player Instances ---
    private var exoPlayer: ExoPlayer? = null
    private var playerView: PlayerView? = null

    // --- WebRTC related instances using chosen SDK ---
    // private var webrtcSdkInstance: SomeWebRTCClient? = null // Example
    // private var webrtcSdkVideoView: SomeWebRTCView? = null // Example

    // --- State ---
    private var isPaused: Boolean = true // Start paused by default
    private var isMuted: Boolean = false
    private var currentVolume: Float = 1.0f
    private var currentResizeMode: String = "contain"

    init {
        println("UnifiedPlayerView initialized")
        // Initialize WebRTC SDK globals if necessary (check SDK docs)
    }

    // --- Prop Setters (Called by Manager) ---

    fun setSourceType(type: String?) {
        println("UnifiedPlayerView: Setting source type: $type")
        // Basic validation
        val newType = if (type == "mp4" || type == "webrtc") type else null
        if (this.currentSourceType != newType) {
            this.currentSourceType = newType
            // Re-configure if source data already exists
            if (this.currentSourceData != null || newType == null) { // Also configure if type is cleared
                 configurePlayerBasedOnProps()
            }
        }
    }

    fun setSourceData(sourceMap: ReadableMap?) {
         println("UnifiedPlayerView: Setting source data")
         // Basic check, might need deep compare depending on sourceMap complexity
         if (this.currentSourceData != sourceMap) {
            this.currentSourceData = sourceMap
            // Re-configure based on new data and existing type
            configurePlayerBasedOnProps()
         }
    }

     fun setPaused(pause: Boolean) {
         println("UnifiedPlayerView: Setting paused: $pause")
         if (this.isPaused != pause) {
             this.isPaused = pause
             applyPausedState()
         }
     }

     fun setMuted(mute: Boolean) {
         println("UnifiedPlayerView: Setting muted: $mute")
         if (this.isMuted != mute) {
             this.isMuted = mute
             applyMutedState()
         }
     }

    fun setVolume(volume: Float) {
        val clampedVolume = volume.coerceIn(0f, 1f)
        println("UnifiedPlayerView: Setting volume: $clampedVolume")
        if (this.currentVolume != clampedVolume) {
            this.currentVolume = clampedVolume
            applyVolumeState() // Re-applies mute logic too
        }
    }

     fun setResizeMode(mode: String?) {
        val newMode = mode ?: "contain"
        println("UnifiedPlayerView: Setting resizeMode: $newMode")
        if (currentResizeMode != newMode) {
            currentResizeMode = newMode
            applyResizeMode()
        }
    }

    // --- Core Logic ---

    private fun configurePlayerBasedOnProps() {
        val targetMode = when (currentSourceType) {
            "mp4" -> if (currentSourceData?.hasKey("uri") == true) PlayerMode.MP4 else PlayerMode.NONE
            "webrtc" -> if (currentSourceData?.hasKey("signalingUrl") == true) PlayerMode.WEBRTC else PlayerMode.NONE
            else -> PlayerMode.NONE
        }
        println("UnifiedPlayerView: Configuring for target mode: $targetMode")

        // Cleanup if mode changes or source becomes invalid/null
        if (targetMode != currentMode) {
            cleanupCurrentPlayer()
            currentMode = targetMode // Set new mode *after* cleanup
        } else if (targetMode == PlayerMode.NONE && currentMode != PlayerMode.NONE) {
            // If type/source becomes invalid, cleanup existing player
            cleanupCurrentPlayer()
            currentMode = PlayerMode.NONE
        }


        // Setup new player/SDK based on mode and source data
        // Only setup if mode is valid and not already setup (or if source data changed implicitly handled by cleanup)
        if (currentMode != PlayerMode.NONE) {
             try {
                when (currentMode) {
                    PlayerMode.MP4 -> {
                        val uri = currentSourceData?.getString("uri")!! // Assume not null due to check above
                        setupExoPlayer(uri)
                    }
                    PlayerMode.WEBRTC -> {
                        val signalingUrl = currentSourceData?.getString("signalingUrl")!! // Assume not null
                        val config = currentSourceData?.getMap("streamInfo") // Example config key
                        setupWebRTC(signalingUrl, config)
                    }
                    PlayerMode.NONE -> {} // Should not happen here
                }
                // Apply current state to the new player/sdk
                applyAllStates()
            } catch (e: Exception) {
                 handleError("Error configuring player: ${e.message}")
                 cleanupCurrentPlayer() // Cleanup on error during setup
            }
        }
    }

    private fun cleanupCurrentPlayer() {
        println("UnifiedPlayerView: Cleaning up player for mode: $currentMode")
        try {
            when (currentMode) {
                PlayerMode.MP4 -> cleanupExoPlayer()
                PlayerMode.WEBRTC -> cleanupWebRTC()
                PlayerMode.NONE -> {}
            }
        } catch (e: Exception) {
            println("Error during cleanup: ${e.message}")
        } finally {
            // Ensure views are removed and references nulled
            playerView?.let { removeView(it) }
            // webrtcSdkVideoView?.let { removeView(it) }
            playerView = null
            exoPlayer = null
            // webrtcSdkVideoView = null
            // webrtcSdkInstance = null
            // currentMode = PlayerMode.NONE // Reset mode after cleanup is done
        }
    }

    private fun applyAllStates() {
        applyPausedState()
        applyMutedState()
        // applyVolumeState() // Called by applyMutedState
        applyResizeMode()
    }

    private fun applyPausedState() {
         when (currentMode) {
             PlayerMode.MP4 -> exoPlayer?.playWhenReady = !isPaused
             PlayerMode.WEBRTC -> {
                 // Call SDK's pause/resume method
                 println("!!! applyPausedState for WebRTC SDK needed !!!")
             }
             PlayerMode.NONE -> {}
         }
     }
     private fun applyMutedState() {
          val vol = if (isMuted) 0f else currentVolume
          when (currentMode) {
             PlayerMode.MP4 -> exoPlayer?.volume = vol
             PlayerMode.WEBRTC -> {
                 // Call SDK's mute/unmute or setVolume method
                  println("!!! applyMutedState for WebRTC SDK needed !!!")
             }
             PlayerMode.NONE -> {}
         }
     }
    private fun applyVolumeState() { applyMutedState() /* Re-apply mute logic as it depends on volume */ }
    private fun applyResizeMode() {
         when (currentMode) {
             PlayerMode.MP4 -> {
                 playerView?.resizeMode = when (currentResizeMode) {
                    "cover" -> PlayerView.RESIZE_MODE_ZOOM
                    "stretch" -> PlayerView.RESIZE_MODE_FILL
                    else -> PlayerView.RESIZE_MODE_FIT // contain
                 }
             }
             PlayerMode.WEBRTC -> {
                  // Apply resize mode to WebRTC SDK's view if possible
                   println("!!! applyResizeMode for WebRTC SDK needed !!!")
             }
             PlayerMode.NONE -> {}
         }
    }


    // --- ExoPlayer Methods ---
    private fun setupExoPlayer(url: String) {
        if (exoPlayer != null) return // Already setup or needs cleanup first? Add check.
        println("UnifiedPlayerView: Setting up ExoPlayer for $url")
        // Create PlayerView if needed, add to FrameLayout
        if (playerView == null) {
             playerView = PlayerView(context).apply { layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT) }
             addView(playerView, 0) // Add at index 0 (bottom)
        }
        // Initialize ExoPlayer, set media item, add listener, attach to playerView
        exoPlayer = ExoPlayer.Builder(context).build().apply {
             // TODO: Add listener to handle state changes (Player.Listener)
             //       - In listener, call sendEvent for 'onVideoLoad', 'onVideoEnd', 'onReadyForDisplay', 'onVideoError'
             //       - Start/Stop progress emitter based on isPlaying state
             // setMediaItem(MediaItem.fromUri(url))
             // prepare()
        }
        playerView?.player = exoPlayer
        playerView?.visibility = View.VISIBLE
        // Hide WebRTC view if it exists
        // webrtcSdkVideoView?.visibility = View.GONE
        println("!!! setupExoPlayer() IMPLEMENTATION NEEDED !!!")
    }
    private fun cleanupExoPlayer() {
        println("UnifiedPlayerView: Cleaning up ExoPlayer")
        // TODO: Remove ExoPlayer listener
        exoPlayer?.release() // Release player resources
        exoPlayer = null
        playerView?.player = null
        playerView?.visibility = View.GONE
    }

    // --- WebRTC SDK Methods ---
    private fun setupWebRTC(signalingUrl: String, config: ReadableMap?) {
        if (/* webrtcSdkInstance != null */ false) return // Already setup?
         println("UnifiedPlayerView: Setting up WebRTC SDK for $signalingUrl")
        // Create SDK's view if needed, add to FrameLayout
        // if (webrtcSdkVideoView == null) { ... addView(webrtcSdkVideoView, 0) ... }
        // Initialize SDK client, set listeners
        // TODO: Add SDK listeners
        //       - In listeners, call sendEvent for 'onWebRTCConnected', 'onWebRTCDisconnected', 'onWebRTCStreamAdded', 'onVideoError'
        // Connect using signalingUrl, config
        // Hide ExoPlayer view if it exists
        playerView?.visibility = View.GONE
        // webrtcSdkVideoView?.visibility = View.VISIBLE
        println("!!! setupWebRTC() IMPLEMENTATION NEEDED using chosen SDK !!!")
    }
    private fun cleanupWebRTC() {
        println("UnifiedPlayerView: Cleaning up WebRTC SDK")
        // TODO: Remove SDK listeners
        // Call SDK's disconnect/release methods
        // webrtcSdkInstance?.release()
        // webrtcSdkInstance = null
        // webrtcSdkVideoView?.release()
        // webrtcSdkVideoView?.visibility = View.GONE
         println("!!! cleanupWebRTC() IMPLEMENTATION NEEDED using chosen SDK !!!")
    }

    // --- Event Emission ---
    private fun sendEvent(eventName: String, params: WritableMap?) {
        try {
            val reactContext = context as ReactContext
            reactContext.getJSModule(RCTEventEmitter::class.java).receiveEvent(id, eventName, params)
        } catch (e: Exception) {
             println("Error sending event $eventName: ${e.message}")
        }
    }
    private fun handleError(message: String) {
        println("UnifiedPlayerView Error: $message")
        val errorMap = Arguments.createMap().apply { putString("error", message) }
        sendEvent("onVideoError", errorMap) // Use the common error event
    }

    // --- Listeners ---
    // TODO: Implement ExoPlayer's Player.Listener
    // TODO: Implement listeners provided by the chosen WebRTC SDK

    // --- Lifecycle / Cleanup ---
     override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        println("UnifiedPlayerView: Detaching from window.")
        cleanup() // Ensure cleanup on detach
    }
     fun cleanup() {
         println("UnifiedPlayerView: Explicit cleanup called.")
         cleanupCurrentPlayer()
     }

    // --- Imperative Commands (Called by Manager) ---
     fun seek(timeSeconds: Double) {
         if (currentMode == PlayerMode.MP4) {
             exoPlayer?.seekTo((timeSeconds * 1000).toLong())
         } else {
              println("Seek command ignored: not applicable for mode $currentMode")
         }
     }
     // fun switchCamera() { ... call SDK method ... }

}
