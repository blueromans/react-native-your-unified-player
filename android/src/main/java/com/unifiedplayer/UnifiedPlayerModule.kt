package com.unifiedplayer

import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import com.facebook.react.bridge.Promise
import com.facebook.react.uimanager.UIManagerModule
import android.util.Log
import com.facebook.react.bridge.UiThreadUtil
import android.view.View

class UnifiedPlayerModule(private val reactContext: ReactApplicationContext) : ReactContextBaseJavaModule(reactContext) {
    companion object {
        private const val TAG = "UnifiedPlayerModule"
    }
    
    init {
        Log.d(TAG, "UnifiedPlayerModule initialized")
    }
    
    override fun getName(): String {
        Log.d(TAG, "getName() called, returning 'UnifiedPlayer'")
        return "UnifiedPlayer"
    }
    
    private fun getPlayerViewByTag(viewTag: Int): UnifiedPlayerView? {
        try {
            val view = reactApplicationContext.currentActivity?.findViewById<View>(viewTag)
            Log.d(TAG, "Looking for view with tag: $viewTag, found: ${view != null}")
            
            if (view is UnifiedPlayerView) {
                return view
            } else if (view != null) {
                Log.e(TAG, "View with tag $viewTag is not a UnifiedPlayerView, it's a ${view.javaClass.simpleName}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error finding view with tag $viewTag: ${e.message}", e)
        }
        return null
    }
    
    @ReactMethod
    fun play(viewTag: Int, promise: Promise) {
        Log.d(TAG, "Native play method called with viewTag: $viewTag")
        try {
            val playerView = getPlayerViewByTag(viewTag)
            if (playerView != null) {
                UiThreadUtil.runOnUiThread {
                    try {
                        playerView.play()
                        Log.d(TAG, "Play command executed successfully")
                        promise.resolve(true)
                    } catch (e: Exception) {
                        Log.e(TAG, "Error during play: ${e.message}", e)
                        promise.reject("PLAY_ERROR", "Error during play: ${e.message}", e)
                    }
                }
            } else {
                Log.e(TAG, "Player view not found for tag: $viewTag")
                promise.reject("VIEW_NOT_FOUND", "Player view not found for tag: $viewTag")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in play method: ${e.message}", e)
            promise.reject("PLAY_ERROR", "Error in play method: ${e.message}", e)
        }
    }

    @ReactMethod
    fun pause(viewTag: Int, promise: Promise) {
        Log.d(TAG, "Native pause method called with viewTag: $viewTag")
        try {
            val playerView = getPlayerViewByTag(viewTag)
            if (playerView != null) {
                UiThreadUtil.runOnUiThread {
                    try {
                        playerView.pause()
                        Log.d(TAG, "Pause command executed successfully")
                        promise.resolve(true)
                    } catch (e: Exception) {
                        Log.e(TAG, "Error during pause: ${e.message}", e)
                        promise.reject("PAUSE_ERROR", "Error during pause: ${e.message}", e)
                    }
                }
            } else {
                Log.e(TAG, "Player view not found for tag: $viewTag")
                promise.reject("VIEW_NOT_FOUND", "Player view not found for tag: $viewTag")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in pause method: ${e.message}", e)
            promise.reject("PAUSE_ERROR", "Error in pause method: ${e.message}", e)
        }
    }

    @ReactMethod
    fun seekTo(viewTag: Int, seconds: Float, promise: Promise) {
        Log.d(TAG, "Native seekTo method called with viewTag: $viewTag, seconds: $seconds")
        try {
            val playerView = getPlayerViewByTag(viewTag)
            if (playerView != null) {
                UiThreadUtil.runOnUiThread {
                    try {
                        playerView.seekTo(seconds)
                        Log.d(TAG, "SeekTo command executed successfully to $seconds seconds")
                        promise.resolve(true)
                    } catch (e: Exception) {
                        Log.e(TAG, "Error during seekTo: ${e.message}", e)
                        promise.reject("SEEK_ERROR", "Error during seekTo: ${e.message}", e)
                    }
                }
            } else {
                Log.e(TAG, "Player view not found for tag: $viewTag")
                promise.reject("VIEW_NOT_FOUND", "Player view not found for tag: $viewTag")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in seekTo method: ${e.message}", e)
            promise.reject("SEEK_ERROR", "Error in seekTo method: ${e.message}", e)
        }
    }

    @ReactMethod
    fun getCurrentTime(viewTag: Int, promise: Promise) {
        Log.d(TAG, "Native getCurrentTime method called with viewTag: $viewTag")
        try {
            val playerView = getPlayerViewByTag(viewTag)
            if (playerView != null) {
                UiThreadUtil.runOnUiThread {
                    try {
                        val currentTime = playerView.getCurrentTime()
                        Log.d(TAG, "getCurrentTime executed successfully, current time: $currentTime")
                        promise.resolve(currentTime)
                    } catch (e: Exception) {
                        Log.e(TAG, "Error getting current time: ${e.message}", e)
                        promise.reject("GET_TIME_ERROR", "Error getting current time: ${e.message}", e)
                    }
                }
            } else {
                Log.e(TAG, "Player view not found for tag: $viewTag")
                promise.reject("VIEW_NOT_FOUND", "Player view not found for tag: $viewTag")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in getCurrentTime method: ${e.message}", e)
            promise.reject("GET_TIME_ERROR", "Error in getCurrentTime method: ${e.message}", e)
        }
    }

    @ReactMethod
    fun getDuration(viewTag: Int, promise: Promise) {
        Log.d(TAG, "Native getDuration method called with viewTag: $viewTag")
        try {
            val playerView = getPlayerViewByTag(viewTag)
            if (playerView != null) {
                UiThreadUtil.runOnUiThread {
                    try {
                        val duration = playerView.getDuration()
                        Log.d(TAG, "getDuration executed successfully, duration: $duration")
                        promise.resolve(duration)
                    } catch (e: Exception) {
                        Log.e(TAG, "Error getting duration: ${e.message}", e)
                        promise.reject("GET_DURATION_ERROR", "Error getting duration: ${e.message}", e)
                    }
                }
            } else {
                Log.e(TAG, "Player view not found for tag: $viewTag")
                promise.reject("VIEW_NOT_FOUND", "Player view not found for tag: $viewTag")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in getDuration method: ${e.message}", e)
            promise.reject("GET_DURATION_ERROR", "Error in getDuration method: ${e.message}", e)
        }
    }

}