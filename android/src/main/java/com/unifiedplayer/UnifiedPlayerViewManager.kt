package com.unifiedplayer

import com.facebook.react.uimanager.SimpleViewManager
import com.facebook.react.uimanager.ThemedReactContext
import com.facebook.react.uimanager.annotations.ReactProp

class UnifiedPlayerViewManager : SimpleViewManager<UnifiedPlayerView>() {
  override fun getName() = "UnifiedPlayerView"

  override fun createViewInstance(reactContext: ThemedReactContext): UnifiedPlayerView {
    return UnifiedPlayerView(reactContext)
  }

  @ReactProp(name = "videoUrl")
  fun setVideoUrl(view: UnifiedPlayerView, url: String?) {
    view.setVideoUrl(url)
  }

  @ReactProp(name = "authToken")
  fun setAuthToken(view: UnifiedPlayerView, token: String?) {
    view.setAuthToken(token)
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
}
