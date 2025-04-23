package com.unifiedplayer

import com.facebook.react.ReactPackage
import com.facebook.react.bridge.NativeModule
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.uimanager.ViewManager
import android.util.Log

class UnifiedPlayerPackage : ReactPackage {
  private val TAG = "UnifiedPlayerPackage"
  
  override fun createNativeModules(reactContext: ReactApplicationContext): List<NativeModule> {
    Log.d(TAG, "Creating native modules")
    return listOf(
      UnifiedPlayerModule(reactContext)
    )
  }

  override fun createViewManagers(reactContext: ReactApplicationContext): List<ViewManager<*, *>> {
    Log.d(TAG, "Creating view managers")
    return listOf(UnifiedPlayerViewManager())
  }
}
