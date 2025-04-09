/**
 * android/src/.../YourUnifiedPlayerViewManager.kt (Fabric Conceptual - Refactored)
 * Manages the View instance for Fabric. Interacts with C++ layer via Delegate.
 * !!! REQUIRES FULL IMPLEMENTATION !!!
 */
package com.yourunifiedplayer // Refactored package name

import android.view.View
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReadableMap
import com.facebook.react.module.annotations.ReactModule
import com.facebook.react.uimanager.SimpleViewManager // Or BaseViewManager? Check Fabric examples
import com.facebook.react.uimanager.ThemedReactContext
import com.facebook.react.uimanager.ViewManagerDelegate
import com.facebook.react.uimanager.annotations.ReactProp
// Import generated delegate and interface (names based on JS component name "UnifiedNativeVideoPlayer")
import com.facebook.react.viewmanagers.UnifiedNativeVideoPlayerManagerDelegate
import com.facebook.react.viewmanagers.UnifiedNativeVideoPlayerManagerInterface

// Refactored Kotlin class name to YourUnifiedPlayerViewManager
@ReactModule(name = YourUnifiedPlayerViewManager.REACT_CLASS) // Use companion object constant
class YourUnifiedPlayerViewManager( // <<< CLASS NAME REFACTORED HERE
    // val reactContext: ReactApplicationContext
) : SimpleViewManager<YourUnifiedPlayerView>(), // Use refactored View class
    UnifiedNativeVideoPlayerManagerInterface<YourUnifiedPlayerView> // Implement generated interface
{

    companion object {
        // Registered JS name (keep consistent with TS Spec and C++)
        // This does NOT change just because the Kotlin class name changed.
        const val REACT_CLASS = "UnifiedNativeVideoPlayer"
    }

    // Delegate provided by Fabric Codegen
    private val mDelegate: ViewManagerDelegate<YourUnifiedPlayerView>

    init {
        // Use generated delegate (name based on JS component name)
        // The delegate name doesn't change with the Kotlin manager class name.
        mDelegate = UnifiedNativeVideoPlayerManagerDelegate(this)
    }

    override fun getDelegate(): ViewManagerDelegate<YourUnifiedPlayerView>? {
        return mDelegate
    }

    override fun getName(): String {
        // Return the registered JS component name
        return REACT_CLASS
    }

    // Create the actual View instance (use refactored View class)
    override fun createViewInstance(reactContext: ThemedReactContext): YourUnifiedPlayerView {
        return YourUnifiedPlayerView(reactContext)
    }

    // --- Prop Setters (Called via Delegate from C++) ---
    // Implement methods defined in UnifiedNativeVideoPlayerManagerInterface

    override fun setPaused(view: YourUnifiedPlayerView, value: Boolean) { // Use non-nullable from generated interface? Check spec.
        view.setPaused(value)
    }

    override fun setMuted(view: YourUnifiedPlayerView, value: Boolean) {
         // view.setMuted(value) // Add method to View
    }

     override fun setVolume(view: YourUnifiedPlayerView, value: Float) {
         // view.setVolume(value) // Add method to View
     }

     override fun setResizeMode(view: YourUnifiedPlayerView, value: String?) { // Nullable? Check spec.
         // view.setResizeMode(value ?: "contain") // Add method to View
     }

    override fun setSource(view: YourUnifiedPlayerView, value: ReadableMap?) { // Nullable? Check spec.
         view.setSource(value) // Pass to view for handling
    }

    // --- Event Exports ---
    // Same as before, ensure keys match C++ event emitter names / registrationName matches TS spec
    override fun getExportedCustomDirectEventTypeConstants(): Map<String, Any>? {
        return com.facebook.react.common.MapBuilder.builder<String, Any>()
            .put("onUrlLoad", com.facebook.react.common.MapBuilder.of("registrationName", "onUrlLoad"))
            .put("onWebRTCConnected", com.facebook.react.common.MapBuilder.of("registrationName", "onWebRTCConnected"))
            .put("onError", com.facebook.react.common.MapBuilder.of("registrationName", "onError"))
            // ... add all other events ...
            .build()
    }

    // --- Command Handling ---
    // Implement methods from the generated interface called by the delegate

    override fun seekUrl(view: YourUnifiedPlayerView, timeSeconds: Double) {
        println("[Fabric Manager] seekUrl command received: $timeSeconds")
        view.seekUrl(timeSeconds) // Call method on the View instance
    }

    override fun sendWebRTCMessage(view: YourUnifiedPlayerView, message: String) {
       println("[Fabric Manager] sendWebRTCMessage command received: $message")
        // view.sendWebRTCMessage(message) // Add corresponding method to View
    }

    // --- Cleanup ---
    override fun onDropViewInstance(view: YourUnifiedPlayerView) { // Use refactored View type
        super.onDropViewInstance(view)
        println("[Fabric Manager] Dropping view instance, calling cleanup.")
        view.cleanUp() // Ensure view cleans up its resources
    }
}
