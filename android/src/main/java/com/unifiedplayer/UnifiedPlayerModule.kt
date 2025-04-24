package com.unifiedplayer

import android.graphics.Bitmap
import android.util.Base64
import android.util.Log
import com.facebook.react.bridge.*
import com.facebook.react.uimanager.UIManagerModule
import com.facebook.react.uimanager.UIBlock
import com.facebook.react.uimanager.NativeViewHierarchyManager
import java.io.ByteArrayOutputStream

class UnifiedPlayerModule(private val reactContext: ReactApplicationContext) : ReactContextBaseJavaModule(reactContext) {
    private var isModuleReady = false

    override fun initialize() {
        super.initialize()
        try {
            reactContext.getNativeModule(UIManagerModule::class.java)?.let {
                isModuleReady = true
                Log.d(TAG, "Module successfully initialized")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Initialization failed", e)
        }
    }
    companion object {
        private const val TAG = "UnifiedPlayerModule"
    }

    override fun getName(): String = "UnifiedPlayer"

    @ReactMethod
    fun play(viewTag: Int, promise: Promise) {
        try {
            val uiManager = reactApplicationContext.getNativeModule(UIManagerModule::class.java)
                ?: throw IllegalStateException("UIManagerModule not available")
            
            uiManager.addUIBlock(UIBlock { nativeViewHierarchyManager ->
                try {
                    val view = nativeViewHierarchyManager.resolveView(viewTag) as? UnifiedPlayerView
                    if (view != null) {
                        view.play()
                        promise.resolve(true)
                    } else {
                        promise.reject("INVALID_VIEW", "View with tag $viewTag not found")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error in play method", e)
                    promise.reject("PLAY_ERROR", "Error in play method", e)
                }
            })
        } catch (e: Exception) {
            Log.e(TAG, "Error in play method", e)
            promise.reject("UIMANAGER_ERROR", "UIManagerModule not available", e)
        }
    }

    @ReactMethod
    fun pause(viewTag: Int, promise: Promise) {
        try {
            val uiManager = reactApplicationContext.getNativeModule(UIManagerModule::class.java)
                ?: throw IllegalStateException("UIManagerModule not available")
            
            uiManager.addUIBlock(UIBlock { nativeViewHierarchyManager ->
                try {
                    val view = nativeViewHierarchyManager.resolveView(viewTag) as? UnifiedPlayerView
                    if (view != null) {
                        view.pause()
                        promise.resolve(true)
                    } else {
                        promise.reject("INVALID_VIEW", "View with tag $viewTag not found")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error in pause method", e)
                    promise.reject("PAUSE_ERROR", "Error in pause method", e)
                }
            })
        } catch (e: Exception) {
            Log.e(TAG, "Error in pause method", e)
            promise.reject("UIMANAGER_ERROR", "UIManagerModule not available", e)
        }
    }

    @ReactMethod
    fun seekTo(viewTag: Int, seconds: Float, promise: Promise) {
        try {
            val uiManager = reactApplicationContext.getNativeModule(UIManagerModule::class.java)
                ?: throw IllegalStateException("UIManagerModule not available")
            
            uiManager.addUIBlock(UIBlock { nativeViewHierarchyManager ->
                try {
                    val view = nativeViewHierarchyManager.resolveView(viewTag) as? UnifiedPlayerView
                    if (view != null) {
                        view.seekTo(seconds)
                        promise.resolve(true)
                    } else {
                        promise.reject("INVALID_VIEW", "View with tag $viewTag not found")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error in seekTo method", e)
                    promise.reject("SEEK_ERROR", "Error in seekTo method", e)
                }
            })
        } catch (e: Exception) {
            Log.e(TAG, "Error in seekTo method", e)
            promise.reject("UIMANAGER_ERROR", "UIManagerModule not available", e)
        }
    }

    @ReactMethod
    fun getCurrentTime(viewTag: Int, promise: Promise) {
        reactApplicationContext.getNativeModule(UIManagerModule::class.java)?.let { uiManager ->
            uiManager.addUIBlock(UIBlock { nativeViewHierarchyManager ->
                try {
                    val view = nativeViewHierarchyManager.resolveView(viewTag) as? UnifiedPlayerView
                    if (view != null) {
                        promise.resolve(view.getCurrentTime())
                    } else {
                        promise.reject("INVALID_VIEW", "View with tag $viewTag not found")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error in getCurrentTime method", e)
                    promise.reject("GET_TIME_ERROR", "Error in getCurrentTime method", e)
                }
            })
        } ?: run {
            promise.reject("ERROR", "UIManagerModule not available")
        }
    }

    @ReactMethod
    fun getDuration(viewTag: Int, promise: Promise) {
        reactApplicationContext.getNativeModule(UIManagerModule::class.java)?.let { uiManager ->
            uiManager.addUIBlock(UIBlock { nativeViewHierarchyManager ->
                try {
                    val view = nativeViewHierarchyManager.resolveView(viewTag) as? UnifiedPlayerView
                    if (view != null) {
                        promise.resolve(view.getDuration())
                    } else {
                        promise.reject("INVALID_VIEW", "View with tag $viewTag not found")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error in getDuration method", e)
                    promise.reject("GET_DURATION_ERROR", "Error in getDuration method", e)
                }
            })
        } ?: run {
            promise.reject("ERROR", "UIManagerModule not available")
        }
    }

    @ReactMethod
    fun capture(viewTag: Int, promise: Promise) {
        try {
            if (!isModuleReady) {
                throw IllegalStateException("Module not ready. Ensure React Native bridge is initialized.")
            }
            val uiManager = reactApplicationContext.getNativeModule(UIManagerModule::class.java)
                ?: throw IllegalStateException("UIManagerModule not available. Is the bridge active?")
            
            uiManager.addUIBlock(UIBlock { nativeViewHierarchyManager ->
                try {
                    val view = nativeViewHierarchyManager.resolveView(viewTag) as? UnifiedPlayerView
                    if (view != null) {
                        val bitmap = view.captureFrame()
                        if (bitmap != null) {
                            val outputStream = ByteArrayOutputStream()
                            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                            val base64 = Base64.encodeToString(outputStream.toByteArray(), Base64.DEFAULT)
                            promise.resolve(base64)
                        } else {
                            promise.reject("CAPTURE_ERROR", "Failed to capture frame")
                        }
                    } else {
                        promise.reject("INVALID_VIEW", "View with tag $viewTag not found")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error in capture method", e)
                    promise.reject("CAPTURE_ERROR", "Error in capture method", e)
                }
            })
        } catch (e: Exception) {
            Log.e(TAG, "Error in capture method", e)
            promise.reject("UIMANAGER_ERROR", "UIManagerModule not available", e)
        }
    }
}
