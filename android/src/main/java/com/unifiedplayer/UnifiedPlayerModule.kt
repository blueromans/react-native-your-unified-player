package com.unifiedplayer

import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import com.facebook.react.bridge.Promise
import com.facebook.react.uimanager.UIManagerModule
import android.util.Log
import com.facebook.react.bridge.UiThreadUtil
import android.view.View
import android.os.Environment
import java.io.File

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

    @ReactMethod
    fun capture(viewTag: Int, promise: Promise) {
        Log.d(TAG, "Native capture method called with viewTag: $viewTag")
        try {
            val playerView = getPlayerViewByTag(viewTag)
            if (playerView != null) {
                UiThreadUtil.runOnUiThread {
                    try {
                        // Assuming playerView has a method called capture() that returns a String
                        val captureResult = playerView.capture()
                        Log.d(TAG, "Capture command executed successfully, result: $captureResult")
                        promise.resolve(captureResult)
                    } catch (e: Exception) {
                        Log.e(TAG, "Error during capture: ${e.message}", e)
                        promise.reject("CAPTURE_ERROR", "Error during capture: ${e.message}", e)
                    }
                }
            } else {
                Log.e(TAG, "Player view not found for tag: $viewTag")
                promise.reject("VIEW_NOT_FOUND", "Player view not found for tag: $viewTag")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in capture method: ${e.message}", e)
            promise.reject("CAPTURE_ERROR", "Error in capture method: ${e.message}", e)
        }
    }
    
    @ReactMethod
    fun startRecording(viewTag: Int, outputPath: String?, promise: Promise) {
        Log.d(TAG, "Native startRecording method called with viewTag: $viewTag, outputPath: $outputPath")
        try {
            val playerView = getPlayerViewByTag(viewTag)
            if (playerView != null) {
                UiThreadUtil.runOnUiThread {
                    try {
                        // Determine the output path
                        val finalOutputPath = if (outputPath.isNullOrEmpty()) {
                            // Use app-specific storage for Android 10+ (API level 29+)
                            val moviesDir = File(reactApplicationContext.getExternalFilesDir(Environment.DIRECTORY_MOVIES), "recordings")
                            if (!moviesDir.exists()) {
                                moviesDir.mkdirs()
                            }
                            val timestamp = System.currentTimeMillis()
                            File(moviesDir, "recording_$timestamp.mp4").absolutePath
                        } else {
                            outputPath
                        }
                        
                        // Start recording
                        val result = playerView.startRecording(finalOutputPath)
                        Log.d(TAG, "Start recording command executed successfully, result: $result")
                        promise.resolve(result)
                    } catch (e: Exception) {
                        Log.e(TAG, "Error during startRecording: ${e.message}", e)
                        promise.reject("RECORDING_ERROR", "Error during startRecording: ${e.message}", e)
                    }
                }
            } else {
                Log.e(TAG, "Player view not found for tag: $viewTag")
                promise.reject("VIEW_NOT_FOUND", "Player view not found for tag: $viewTag")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in startRecording method: ${e.message}", e)
            promise.reject("RECORDING_ERROR", "Error in startRecording method: ${e.message}", e)
        }
    }
    
    @ReactMethod
    fun toggleFullscreen(viewTag: Int, isFullscreen: Boolean, promise: Promise) {
        Log.d(TAG, "Native toggleFullscreen method called with viewTag: $viewTag, isFullscreen: $isFullscreen")
        try {
            val playerView = getPlayerViewByTag(viewTag)
            if (playerView != null) {
                UiThreadUtil.runOnUiThread {
                    try {
                        playerView.setIsFullscreen(isFullscreen)
                        Log.d(TAG, "toggleFullscreen executed successfully")
                        promise.resolve(true)
                    } catch (e: Exception) {
                        Log.e(TAG, "Error toggling fullscreen: ${e.message}", e)
                        promise.reject("FULLSCREEN_ERROR", "Error toggling fullscreen: ${e.message}", e)
                    }
                }
            } else {
                Log.e(TAG, "Player view not found for tag: $viewTag")
                promise.reject("VIEW_NOT_FOUND", "Player view not found for tag: $viewTag")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in toggleFullscreen method: ${e.message}", e)
            promise.reject("FULLSCREEN_ERROR", "Error in toggleFullscreen method: ${e.message}", e)
        }
    }

    @ReactMethod
    fun stopRecording(viewTag: Int, promise: Promise) {
        Log.d(TAG, "Native stopRecording method called with viewTag: $viewTag")
        try {
            val playerView = getPlayerViewByTag(viewTag)
            if (playerView != null) {
                UiThreadUtil.runOnUiThread {
                    try {
                        // Stop recording
                        val filePath = playerView.stopRecording()
                        Log.d(TAG, "Stop recording command executed successfully, filePath: $filePath")
                        promise.resolve(filePath)
                    } catch (e: Exception) {
                        Log.e(TAG, "Error during stopRecording: ${e.message}", e)
                        promise.reject("RECORDING_ERROR", "Error during stopRecording: ${e.message}", e)
                    }
                }
            } else {
                Log.e(TAG, "Player view not found for tag: $viewTag")
                promise.reject("VIEW_NOT_FOUND", "Player view not found for tag: $viewTag")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in stopRecording method: ${e.message}", e)
            promise.reject("RECORDING_ERROR", "Error in stopRecording method: ${e.message}", e)
        }
    }
}
