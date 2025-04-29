package com.unifiedplayer

import com.facebook.react.uimanager.SimpleViewManager
import com.facebook.react.uimanager.ThemedReactContext
import com.facebook.react.uimanager.annotations.ReactProp
import com.facebook.react.common.MapBuilder
import android.util.Log
import com.facebook.react.bridge.ReadableArray
import com.facebook.react.bridge.Dynamic // Import Dynamic
import com.facebook.react.bridge.ReadableType // Import ReadableType
import com.facebook.react.uimanager.events.RCTEventEmitter

class UnifiedPlayerViewManager : SimpleViewManager<UnifiedPlayerView>() {
  private val TAG = "UnifiedPlayerViewManager"
  
  override fun getName() = "UnifiedPlayerView"

  override fun createViewInstance(reactContext: ThemedReactContext): UnifiedPlayerView {
    Log.d(TAG, "Creating UnifiedPlayerView instance")
    return UnifiedPlayerView(reactContext)
  }

  @ReactProp(name = "videoUrl")
  fun setVideoUrl(view: UnifiedPlayerView, videoUrl: Dynamic?) {
    if (videoUrl == null) {
      view.setVideoUrl(null)
      return
    }

    when (videoUrl.type) {
      ReadableType.String -> {
        view.setVideoUrl(videoUrl.asString())
      }
      ReadableType.Array -> {
        val urlList = mutableListOf<String>()
        val array = videoUrl.asArray()
        for (i in 0 until array.size()) {
          if (array.getType(i) == ReadableType.String) {
            val urlString = array.getString(i) // Get nullable string
            if (urlString != null) { // Check if it's not null
                 urlList.add(urlString) // Add the non-null string
            } else {
                 Log.w(TAG, "Null string found in videoUrl array at index $i.")
            }
          } else {
            Log.w(TAG, "Invalid type in videoUrl array at index $i. Expected String.")
          }
        }
        view.setVideoUrls(urlList) // Call the new method for playlists
      }
      else -> {
        Log.w(TAG, "Invalid type for videoUrl prop. Expected String or Array.")
        view.setVideoUrl(null) // Or handle error appropriately
      }
    }
  }

  @ReactProp(name = "thumbnailUrl")
  fun setThumbnailUrl(view: UnifiedPlayerView, url: String?) {
    view.setThumbnailUrl(url)
  }

  @ReactProp(name = "autoplay")
  fun setAutoplay(view: UnifiedPlayerView, autoplay: Boolean) {
    view.setAutoplay(autoplay)
  }

  @ReactProp(name = "loop")
  fun setLoop(view: UnifiedPlayerView, loop: Boolean) {
    view.setLoop(loop)
  }

  @ReactProp(name = "isPaused")
  fun setIsPaused(view: UnifiedPlayerView, isPaused: Boolean) {
    view.setIsPaused(isPaused)
  }

  @ReactProp(name = "isFullscreen")
  fun setIsFullscreen(view: UnifiedPlayerView, isFullscreen: Boolean) {
    view.setIsFullscreen(isFullscreen)
  }


  
  // Register direct events
  override fun getExportedCustomDirectEventTypeConstants(): Map<String, Any> {
    Log.d(TAG, "Registering direct events")
    
    // Create a map of event names to their registration names
    return MapBuilder.builder<String, Any>()
      .put("topReadyToPlay", MapBuilder.of("registrationName", "onReadyToPlay"))
      .put("topError", MapBuilder.of("registrationName", "onError"))
      .put("topProgress", MapBuilder.of("registrationName", "onProgress"))
      .put("topPlaybackComplete", MapBuilder.of("registrationName", "onPlaybackComplete"))
      .put("topPlaybackResumed", MapBuilder.of("registrationName", "onPlaybackResumed"))
      .put("topPlaybackStalled", MapBuilder.of("registrationName", "onPlaybackStalled"))
      .put("topPlaybackPaused", MapBuilder.of("registrationName", "onPaused"))
      .put("topPlaying", MapBuilder.of("registrationName", "onPlaying"))
      .put("topLoadStart", MapBuilder.of("registrationName", "onLoadStart"))
      .put("topFullscreenChanged", MapBuilder.of("registrationName", "onFullscreenChanged"))
      .build()
  }
}
