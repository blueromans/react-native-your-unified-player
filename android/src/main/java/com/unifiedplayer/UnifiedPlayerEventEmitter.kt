package com.unifiedplayer

import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import com.facebook.react.bridge.WritableMap
import com.facebook.react.modules.core.DeviceEventManagerModule
import android.util.Log

class UnifiedPlayerEventEmitter(private val reactContext: ReactApplicationContext) : ReactContextBaseJavaModule(reactContext) {
    companion object {
        private const val TAG = "UnifiedPlayerEventEmitter"
        
        // Define all possible event types
        const val EVENT_LOAD_START = "onLoadStart"
        const val EVENT_READY = "onReadyToPlay"
        const val EVENT_ERROR = "onError"
        const val EVENT_PROGRESS = "onProgress"
        const val EVENT_COMPLETE = "onPlaybackComplete"
        const val EVENT_STALLED = "onPlaybackStalled"
        const val EVENT_RESUMED = "onPlaybackResumed"
        const val EVENT_PLAYING = "onPlaying"
        const val EVENT_PAUSED = "onPaused"
        const val EVENT_FULLSCREEN_CHANGED = "onFullscreenChanged"

        // Singleton instance for access from other classes
        private var instance: UnifiedPlayerEventEmitter? = null
        
        fun getInstance(): UnifiedPlayerEventEmitter? {
            return instance
        }
    }
    
    init {
        instance = this
    }
    
    override fun getName(): String {
        return "UnifiedPlayerEvents"
    }
    
    @ReactMethod
    fun addListener(eventName: String) {
        // Required for React Native event emitter
    }
    
    @ReactMethod
    fun removeListeners(count: Int) {
        // Required for React Native event emitter
    }
    
    fun sendEvent(eventName: String, params: WritableMap?) {
        try {
            reactContext
                .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java)
                .emit(eventName, params)
        } catch (e: Exception) {
            Log.e(TAG, "Error sending event: $eventName - ${e.message}")
        }
    }
    
    override fun onCatalystInstanceDestroy() {
        super.onCatalystInstanceDestroy()
        instance = null
    }
}