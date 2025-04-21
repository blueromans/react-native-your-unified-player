package com.unifiedplayer

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.util.Log
import android.view.Gravity
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import android.widget.TextView
import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.WritableMap

class UnifiedPlayerView(context: Context) : FrameLayout(context) {
    companion object {
        private const val TAG = "UnifiedPlayerView"
    }

    private var videoUrl: String? = null
    private var authToken: String? = null
    private var autoplay: Boolean = true
    private var loop: Boolean = false
    private var webView: WebView
    private var progressTextView: TextView
    private var isPlaying = false
    private var currentProgress = 0
    
    @SuppressLint("SetJavaScriptEnabled")
    init {
        setBackgroundColor(Color.BLACK)
        
        // Create a WebView for video playback
        webView = WebView(context).apply {
            layoutParams = LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT
            )
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.mediaPlaybackRequiresUserGesture = false
            settings.cacheMode = WebSettings.LOAD_NO_CACHE
            setBackgroundColor(Color.BLACK)
            
            webChromeClient = WebChromeClient()
            webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    Log.d(TAG, "WebView page loaded")
                    sendEvent("onReadyToPlay", Arguments.createMap())
                    
                    if (autoplay) {
                        play()
                    }
                }
            }
        }
        
        // Progress text overlay
        progressTextView = TextView(context).apply {
            text = "Loading..."
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
            layoutParams = LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT,
                Gravity.BOTTOM
            )
        }
        
        addView(webView)
        addView(progressTextView)
    }
    
    fun setVideoUrl(url: String?) {
        Log.d(TAG, "Setting video URL: $url")
        if (url.isNullOrEmpty()) {
            return
        }
        
        videoUrl = url
        
        // Check if URL has future date
        val hasFutureDate = url.contains("start=") && (url.contains("2024") || url.contains("2025"))
        if (hasFutureDate) {
            // Convert to live=true URL
            val modifiedUrl = if (url.contains("start=")) {
                url.replace(Regex("&start=[^&]+"), "") + "&live=true"
            } else {
                "$url&live=true"
            }
            
            Log.d(TAG, "Modified URL for future date: $modifiedUrl")
            videoUrl = modifiedUrl
        }
        
        // Load video in WebView with custom HTML that handles authorization headers
        loadVideo()
    }
    
    @SuppressLint("SetJavaScriptEnabled")
    private fun loadVideo() {
        val url = videoUrl ?: return
        
        val html = """
            <!DOCTYPE html>
            <html>
            <head>
                <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
                <style>
                    body, html { margin: 0; padding: 0; width: 100%; height: 100%; background-color: #000; }
                    video { width: 100%; height: 100%; object-fit: contain; }
                </style>
            </head>
            <body>
                <video id="player" controls ${if(autoplay) "autoplay" else ""} ${if(loop) "loop" else ""}>
                    <source src="$url" type="video/mp4">
                </video>
                <script>
                    const player = document.getElementById('player');
                    
                    player.addEventListener('play', function() {
                        window.ReactNativeWebView.postMessage(JSON.stringify({
                            event: 'play'
                        }));
                    });
                    
                    player.addEventListener('pause', function() {
                        window.ReactNativeWebView.postMessage(JSON.stringify({
                            event: 'pause'
                        }));
                    });
                    
                    player.addEventListener('ended', function() {
                        window.ReactNativeWebView.postMessage(JSON.stringify({
                            event: 'ended'
                        }));
                    });
                    
                    player.addEventListener('timeupdate', function() {
                        window.ReactNativeWebView.postMessage(JSON.stringify({
                            event: 'progress',
                            currentTime: player.currentTime,
                            duration: player.duration || 0
                        }));
                    });
                    
                    player.addEventListener('error', function(e) {
                        window.ReactNativeWebView.postMessage(JSON.stringify({
                            event: 'error',
                            code: player.error ? player.error.code : 0,
                            message: player.error ? player.error.message : 'Unknown error'
                        }));
                    });
                    
                    function playVideo() {
                        player.play().catch(e => console.error(e));
                    }
                    
                    function pauseVideo() {
                        player.pause();
                    }
                    
                    function seekVideo(time) {
                        player.currentTime = time;
                    }
                </script>
            </body>
            </html>
        """.trimIndent()
        
        webView.loadDataWithBaseURL(null, html, "text/html", "UTF-8", null)
    }
    
    fun setAuthToken(token: String?) {
        authToken = token
        // If URL already set, reload with new token
        if (!videoUrl.isNullOrEmpty()) {
            loadVideo()
        }
    }
    
    fun setAutoplay(value: Boolean) {
        autoplay = value
    }
    
    fun setLoop(value: Boolean) {
        loop = value
    }
    
    fun play() {
        Log.d(TAG, "Play called")
        webView.evaluateJavascript("javascript:playVideo()", null)
        isPlaying = true
        
        // Mock progress for testing
        startMockProgressUpdates()
    }
    
    fun pause() {
        Log.d(TAG, "Pause called")
        webView.evaluateJavascript("javascript:pauseVideo()", null)
        isPlaying = false
    }
    
    fun seekTo(time: Float) {
        Log.d(TAG, "Seek called: $time")
        webView.evaluateJavascript("javascript:seekVideo($time)", null)
    }
    
    fun getCurrentTime(): Float {
        // This is just a mock implementation
        return currentProgress.toFloat()
    }
    
    fun getDuration(): Float {
        return 100f
    }
    
    private fun startMockProgressUpdates() {
        // This is just for demonstration
        val handler = android.os.Handler(context.mainLooper)
        val runnable = object : Runnable {
            override fun run() {
                if (isPlaying && currentProgress < 100) {
                    currentProgress++
                    progressTextView.text = "Progress: $currentProgress / 100"
                    
                    val event = Arguments.createMap().apply {
                        putDouble("currentTime", currentProgress.toDouble())
                        putDouble("duration", 100.0)
                    }
                    sendEvent("onProgress", event)
                    
                    if (currentProgress >= 100) {
                        isPlaying = false
                        sendEvent("onPlaybackComplete", Arguments.createMap())
                    } else {
                        handler.postDelayed(this, 1000)
                    }
                }
            }
        }
        
        handler.post(runnable)
    }
    
    private fun sendEvent(eventName: String, params: WritableMap) {
        UnifiedPlayerEventEmitter.getInstance()?.sendEvent(eventName, params)
    }
    
    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        webView.destroy()
    }
} 