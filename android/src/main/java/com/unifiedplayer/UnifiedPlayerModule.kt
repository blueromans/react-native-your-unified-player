package com.unifiedplayer

import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import com.facebook.react.bridge.Promise
import com.facebook.react.uimanager.UIManagerModule
import android.util.Log

class UnifiedPlayerModule(private val reactContext: ReactApplicationContext) : ReactContextBaseJavaModule(reactContext) {
    companion object {
        private const val TAG = "UnifiedPlayerModule"
    }
    
    override fun getName(): String {
        return "UnifiedPlayer"
    }
    
    private fun getPlayerView(viewId: Int): UnifiedPlayerView? {
        val uiManager = reactContext.getNativeModule(UIManagerModule::class.java)
        val view = uiManager?.resolveView(viewId) as? UnifiedPlayerView
        
        if (view == null) {
            Log.e(TAG, "Unable to find UnifiedPlayerView with id $viewId")
        }
        
        return view
    }
    
    @ReactMethod
    fun play(viewId: Int) {
        try {
            Log.d(TAG, "Play command received for view $viewId")
            reactContext.runOnUiQueueThread {
                getPlayerView(viewId)?.play()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error playing video: ${e.message}")
        }
    }
    
    @ReactMethod
    fun pause(viewId: Int) {
        try {
            Log.d(TAG, "Pause command received for view $viewId")
            reactContext.runOnUiQueueThread {
                getPlayerView(viewId)?.pause()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error pausing video: ${e.message}")
        }
    }
    
    @ReactMethod
    fun seekTo(viewId: Int, time: Float) {
        try {
            Log.d(TAG, "Seek command received for view $viewId to time $time")
            reactContext.runOnUiQueueThread {
                getPlayerView(viewId)?.seekTo(time)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error seeking: ${e.message}")
        }
    }
    
    @ReactMethod
    fun getCurrentTime(viewId: Int, promise: Promise) {
        try {
            Log.d(TAG, "Get current time for view $viewId")
            reactContext.runOnUiQueueThread {
                val time = getPlayerView(viewId)?.getCurrentTime() ?: 0f
                promise.resolve(time)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting current time: ${e.message}")
            promise.reject("ERROR", "Failed to get current time: ${e.message}")
        }
    }
    
    @ReactMethod
    fun getDuration(viewId: Int, promise: Promise) {
        try {
            Log.d(TAG, "Get duration for view $viewId")
            reactContext.runOnUiQueueThread {
                val duration = getPlayerView(viewId)?.getDuration() ?: 0f
                promise.resolve(duration)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting duration: ${e.message}")
            promise.reject("ERROR", "Failed to get duration: ${e.message}")
        }
    }
} 