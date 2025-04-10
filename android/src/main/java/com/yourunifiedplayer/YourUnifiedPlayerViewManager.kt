/**
 * android/src/.../UnifiedPlayerViewManager.kt (Refactored for Unified Player)
 * Bridges UnifiedPlayerView (handling MP4 & WebRTC) to React Native.
 * !!! Conceptual - Requires corresponding View implementation !!!
 */
package com.yourunifiedplayer // Use your unified package name

import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReadableArray
import com.facebook.react.bridge.ReadableMap
import com.facebook.react.common.MapBuilder
import com.facebook.react.uimanager.SimpleViewManager
import com.facebook.react.uimanager.ThemedReactContext
import com.facebook.react.uimanager.annotations.ReactProp

// Assuming your refactored View class is named UnifiedPlayerView
// import com.yourcompany.yourunifiedplayer.UnifiedPlayerView

// Renamed Manager class
class UnifiedPlayerViewManager(
    // Context might be needed by the View or SDKs
    private val callerContext: ReactApplicationContext? = null
) : SimpleViewManager<UnifiedPlayerView>() { // Use refactored View class name

    companion object {
        // Match the JS requireNativeComponent name for the unified player
        const val REACT_CLASS = "YourUnifiedPlayerView"

        // --- Unified Command Names ---
        // Use distinct names or check sourceType in receiveCommand
        const val COMMAND_SEEK = "seek" // Primarily for MP4
        // Add WebRTC specific commands if needed
        // const val COMMAND_SWITCH_CAMERA = "switchCamera"
    }

    override fun getName(): String {
        return REACT_CLASS
    }

    // Create instance of the refactored View
    override fun createViewInstance(reactContext: ThemedReactContext): UnifiedPlayerView {
        // Pass context if needed by the View or SDKs
        return UnifiedPlayerView(reactContext)
    }

    // --- Unified Prop Exports ---

    @ReactProp(name = "sourceType")
    fun setSourceType(view: UnifiedPlayerView, type: String?) {
        // Pass type to the view to help determine mode
        view.setSourceType(type)
    }

    @ReactProp(name = "source")
    fun setSource(view: UnifiedPlayerView, source: ReadableMap?) { // Assuming source is always an object for WebRTC case
        // Pass the source object/map to the view for parsing
        view.setSourceData(source)
        // Note: The previous JS component used string for MP4, object for WebRTC.
        // Consider standardizing on a source object like:
        // { type: 'mp4', uri: '...' } or { type: 'webrtc', signalingUrl: '...' }
        // The View's setSourceData would handle parsing this structure.
    }

    @ReactProp(name = "paused", defaultBoolean = false)
    fun setPaused(view: UnifiedPlayerView, paused: Boolean) {
        view.setPaused(paused)
    }

    @ReactProp(name = "muted", defaultBoolean = false)
    fun setMuted(view: UnifiedPlayerView, muted: Boolean) {
        view.setMuted(muted)
    }

    @ReactProp(name = "volume", defaultFloat = 1.0f)
    fun setVolume(view: UnifiedPlayerView, volume: Float) {
         view.setVolume(volume)
    }

    // resizeMode primarily applies to MP4 rendering
    @ReactProp(name = "resizeMode")
    fun setResizeMode(view: UnifiedPlayerView, resizeMode: String?) {
        view.setResizeMode(resizeMode)
    }

    // --- Unified Event Exports ---
    // Ensure these match events emitted by UnifiedPlayerView and expected by JS component
    override fun getExportedCustomDirectEventTypeConstants(): Map<String, Any>? {
        return MapBuilder.builder<String, Any>()
            // MP4 / Common Events (Names might need adjustment based on View logic)
            .put("onVideoLoad", MapBuilder.of("registrationName", "onVideoLoad")) // MP4 loaded / WebRTC connected?
            .put("onVideoProgress", MapBuilder.of("registrationName", "onVideoProgress")) // MP4 progress
            .put("onVideoEnd", MapBuilder.of("registrationName", "onVideoEnd")) // MP4 finished / WebRTC disconnected?
            .put("onReadyForDisplay", MapBuilder.of("registrationName", "onReadyForDisplay")) // First frame ready
            // WebRTC Specific Events (Match JS component)
            .put("onWebRTCConnected", MapBuilder.of("registrationName", "onWebRTCConnected"))
            .put("onWebRTCDisconnected", MapBuilder.of("registrationName", "onWebRTCDisconnected"))
            .put("onWebRTCStreamAdded", MapBuilder.of("registrationName", "onWebRTCStreamAdded"))
            // Common Error Event
            .put("onVideoError", MapBuilder.of("registrationName", "onVideoError")) // Unified error reporting
            .build()
    }

    // --- Unified Command Exports ---
    override fun getCommandsMap(): Map<String, Int>? {
        return mapOf(
            COMMAND_SEEK to 1 // Example: Assign unique integer IDs
            // COMMAND_SWITCH_CAMERA to 2
        )
    }

    // Handle commands dispatched from JS
    override fun receiveCommand(
        view: UnifiedPlayerView,
        commandId: String?, // Command ID as String (more common now)
        args: ReadableArray?
    ) {
        println("Unified Player Manager: Received command: $commandId")
        when (commandId) {
             // Match based on String command name
             COMMAND_SEEK -> {
                 val timeSeconds = args?.getDouble(0) ?: 0.0
                 println("Unified Player Manager: Seeking to $timeSeconds")
                 view.seek(timeSeconds) // Call seek method on the View
             }
             // Handle other commands...
             // COMMAND_SWITCH_CAMERA -> view.switchCamera()
            else -> {
                 println("Unified Player Manager: Unknown command received: $commandId")
            }
        }
    }

     // --- Cleanup ---
     override fun onDropViewInstance(view: UnifiedPlayerView) {
        super.onDropViewInstance(view)
        println("Unified Player Manager: Dropping view instance, releasing player.")
        view.cleanup() // Call unified cleanup method on the View
    }
}
