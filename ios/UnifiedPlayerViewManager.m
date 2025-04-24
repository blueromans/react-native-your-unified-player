#import <React/RCTViewManager.h>
#import <React/RCTLog.h>
#import <React/RCTBridgeModule.h>
#import <React/RCTEventEmitter.h>
#import <React/RCTUIManager.h>
#import <React/RCTBridge.h>
#import <React/RCTUIManagerUtils.h>
#import <MobileVLCKit/MobileVLCKit.h>

// Forward declarations
@interface UnifiedPlayerUIView : UIView <VLCMediaPlayerDelegate>
@property (nonatomic, strong) VLCMediaPlayer *player;
@property (nonatomic, copy) NSString *videoUrlString;
@property (nonatomic, assign) BOOL autoplay;
@property (nonatomic, assign) BOOL loop;
@property (nonatomic, assign) BOOL isPaused; // Add isPaused property
@property (nonatomic, strong) NSArray *mediaOptions;
@property (nonatomic, weak) RCTBridge *bridge;
@property (nonatomic, assign) VLCMediaPlayerState previousState;
@property (nonatomic, assign) BOOL hasRenderedVideo;

- (void)setupWithVideoUrlString:(NSString *)videoUrlString;
- (void)play;
- (void)pause;
- (void)seekToTime:(float)time;
- (float)getCurrentTime;
- (float)getDuration;
@end

// UnifiedPlayerModule - Module for handling control methods
@interface UnifiedPlayerModule : RCTEventEmitter <RCTBridgeModule>
@end

@implementation UnifiedPlayerModule

RCT_EXPORT_MODULE();

- (NSArray<NSString *> *)supportedEvents {
    return @[
        @"onLoadStart",
        @"onReadyToPlay",
        @"onError",
        @"onProgress",
        @"onPlaybackComplete",
        @"onPlaybackStalled",
        @"onPlaybackResumed",
        @"onPlaying",
        @"onPaused"
    ];
}

- (dispatch_queue_t)methodQueue {
    return dispatch_get_main_queue();
}

// Play video
RCT_EXPORT_METHOD(play:(nonnull NSNumber *)reactTag) {
    [self.bridge.uiManager addUIBlock:^(RCTUIManager *uiManager, NSDictionary<NSNumber *,UIView *> *viewRegistry) {
        UnifiedPlayerUIView *view = (UnifiedPlayerUIView *)viewRegistry[reactTag];
        if (![view isKindOfClass:[UnifiedPlayerUIView class]]) {
            RCTLogError(@"Invalid view for tag %@", reactTag);
            return;
        }
        [view play];
    }];
}

// Pause video
RCT_EXPORT_METHOD(pause:(nonnull NSNumber *)reactTag) {
    [self.bridge.uiManager addUIBlock:^(RCTUIManager *uiManager, NSDictionary<NSNumber *,UIView *> *viewRegistry) {
        UnifiedPlayerUIView *view = (UnifiedPlayerUIView *)viewRegistry[reactTag];
        if (![view isKindOfClass:[UnifiedPlayerUIView class]]) {
            RCTLogError(@"Invalid view for tag %@", reactTag);
            return;
        }
        [view pause];
    }];
}

// Seek to specific time
RCT_EXPORT_METHOD(seekTo:(nonnull NSNumber *)reactTag time:(nonnull NSNumber *)time) {
    [self.bridge.uiManager addUIBlock:^(RCTUIManager *uiManager, NSDictionary<NSNumber *,UIView *> *viewRegistry) {
        UnifiedPlayerUIView *view = (UnifiedPlayerUIView *)viewRegistry[reactTag];
        if (![view isKindOfClass:[UnifiedPlayerUIView class]]) {
            RCTLogError(@"Invalid view for tag %@", reactTag);
            return;
        }
        [view seekToTime:[time floatValue]];
    }];
}

// Get current playback time
RCT_EXPORT_METHOD(getCurrentTime:(nonnull NSNumber *)reactTag
                  resolver:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject) {
    [self.bridge.uiManager addUIBlock:^(RCTUIManager *uiManager, NSDictionary<NSNumber *,UIView *> *viewRegistry) {
        UnifiedPlayerUIView *view = (UnifiedPlayerUIView *)viewRegistry[reactTag];
        if (![view isKindOfClass:[UnifiedPlayerUIView class]]) {
            reject(@"error", @"Invalid view for tag", nil);
            return;
        }
        resolve(@([view getCurrentTime]));
    }];
}

// Get video duration
RCT_EXPORT_METHOD(getDuration:(nonnull NSNumber *)reactTag
                  resolver:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject) {
    [self.bridge.uiManager addUIBlock:^(RCTUIManager *uiManager, NSDictionary<NSNumber *,UIView *> *viewRegistry) {
        UnifiedPlayerUIView *view = (UnifiedPlayerUIView *)viewRegistry[reactTag];
        if (![view isKindOfClass:[UnifiedPlayerUIView class]]) {
            reject(@"error", @"Invalid view for tag", nil);
            return;
        }
        resolve(@([view getDuration]));
    }];
}

@end

// Global event emitter instance
static UnifiedPlayerModule *eventEmitter = nil;

// Main player view implementation
@implementation UnifiedPlayerUIView

- (instancetype)init {
    if ((self = [super init])) {
        RCTLogInfo(@"[UnifiedPlayerViewManager] Initializing player view");
        
        // Initialize properties
        _hasRenderedVideo = NO;
        
        // Create the player
        _player = [[VLCMediaPlayer alloc] init];
        _player.delegate = self;
        
        // Make sure we're visible and properly laid out
        self.backgroundColor = [UIColor blackColor];
        self.opaque = YES;
        self.userInteractionEnabled = YES;
        
        // Important: Enable content mode to scale properly
        self.contentMode = UIViewContentModeScaleAspectFit;
        self.clipsToBounds = YES;
        
        // After the view is fully initialized, set it as the drawable
        _player.drawable = self;
        
        _autoplay = YES;
        _loop = NO;
        
        // Add notification for app entering background
        [[NSNotificationCenter defaultCenter] addObserver:self
                                                 selector:@selector(appDidEnterBackground:)
                                                     name:UIApplicationDidEnterBackgroundNotification
                                                   object:nil];
        
        // Add notification for app entering foreground
        [[NSNotificationCenter defaultCenter] addObserver:self
                                                 selector:@selector(appDidBecomeActive:)
                                                     name:UIApplicationDidBecomeActiveNotification
                                                   object:nil];
    }
    return self;
}

// Override drawRect to ensure our rendering context is set up without recursion
- (void)drawRect:(CGRect)rect {
    [super drawRect:rect];
    
    // This can sometimes help with VLC rendering issues
    // But check if we need to do this to avoid unnecessary work
    if (_player && _player.drawable != self) {
        _player.drawable = self;
        // Do not call any methods that would trigger layoutSubviews or drawRect again
    }
}

// Override layoutSubviews with safer implementation to avoid recursion
- (void)layoutSubviews {
    [super layoutSubviews];
    
    // Check bounds without using NSStringFromCGRect (which can cause recursion)
    CGRect bounds = self.bounds;
    
    // Only update if we have valid bounds and a player
    if (bounds.size.width > 0 && bounds.size.height > 0 && _player) {
        // Ensure drawable is set but don't call any methods that could cause recursion
        if (_player.drawable != self) {
            _player.drawable = self;
        }
        
        // Let VLC know the size has changed but don't force any redraws here
        // This may be VLC-specific and not required for all implementations
    }
}

- (void)didMoveToSuperview {
    [super didMoveToSuperview];
    RCTLogInfo(@"[UnifiedPlayerViewManager] View moved to superview: %@", self.superview);
    
    // Sometimes VLC needs a reset of the drawable when moving to a superview
    if (_player) {
        _player.drawable = nil;
        _player.drawable = self;
    }
}

- (void)didMoveToWindow {
    [super didMoveToWindow];
    RCTLogInfo(@"[UnifiedPlayerViewManager] View moved to window: %@", self.window);
    
    // Ensure proper rendering when window changes
    if (_player) {
        _player.drawable = nil;
        _player.drawable = self;
    }
}

- (void)appDidEnterBackground:(NSNotification *)notification {
    if (_player.isPlaying) {
        [_player pause];
    }
}

- (void)appDidBecomeActive:(NSNotification *)notification {
    // Optionally resume playback when app becomes active again
    // if (wasPlayingBeforeBackground) {
    //    [_player play];
    // }
}

- (void)sendProgressEvent:(float)currentTime duration:(float)duration {
    NSDictionary *event = @{
        @"currentTime": @(currentTime),
        @"duration": @(duration)
    };
    
    [self sendEvent:@"onProgress" body:event];
}

- (void)sendEvent:(NSString *)eventName body:(NSDictionary *)body {
    if (eventEmitter != nil) {
        [eventEmitter sendEventWithName:eventName body:body];
    }
}

- (void)setupWithVideoUrlString:(NSString *)videoUrlString {
    RCTLogInfo(@"[UnifiedPlayerViewManager] setupWithVideoUrlString: %@", videoUrlString);
    _videoUrlString = [videoUrlString copy];
    
    if (videoUrlString) {
        // Send onLoadStart event when we set a video URL, before loading starts
        [self sendEvent:@"onLoadStart" body:@{}];
        [self loadVideo];
    } else {
        [_player stop];
        _player.media = nil;
    }
}

- (void)loadVideo {
    // Log view state first
    RCTLogInfo(@"[UnifiedPlayerViewManager] View state before loading - Frame: %@, Bounds: %@, Visible: %@, Superview: %@", 
               NSStringFromCGRect(self.frame),
               NSStringFromCGRect(self.bounds),
               self.window ? @"YES" : @"NO",
               self.superview ? @"YES" : @"NO");
    
    // Reset the rendered flag
    _hasRenderedVideo = NO;
    
    // Make sure we're in the main thread
    dispatch_async(dispatch_get_main_queue(), ^{
    // Check if URL is valid
        NSURL *videoURL = [NSURL URLWithString:self->_videoUrlString];
    if (!videoURL) {
        // Try with encoding if the original URL doesn't work
            NSString *escapedString = [self->_videoUrlString stringByAddingPercentEncodingWithAllowedCharacters:[NSCharacterSet URLQueryAllowedCharacterSet]];
        videoURL = [NSURL URLWithString:escapedString];
        
        if (!videoURL) {
            NSDictionary *errorInfo = @{
                @"code": @"INVALID_URL",
                @"message": @"Invalid URL format",
                    @"details": self->_videoUrlString ?: @""
            };
            [self sendEvent:@"onError" body:errorInfo];
            return;
        }
        
        RCTLogInfo(@"[UnifiedPlayerViewManager] URL needed encoding: %@", videoURL.absoluteString);
    }
    
    RCTLogInfo(@"[UnifiedPlayerViewManager] Using URL: %@", videoURL.absoluteString);
    
        // Create VLC media options array
        NSMutableArray *mediaOptions = [NSMutableArray array];
        
        
        // Add default network caching options for streaming
        BOOL isStreaming = [videoURL.scheme hasPrefix:@"http"] || 
                           [videoURL.scheme hasPrefix:@"rtsp"] || 
                           [videoURL.scheme hasPrefix:@"rtmp"] || 
                           [videoURL.scheme hasPrefix:@"mms"];
        
        if (isStreaming) {
            if (![self->_mediaOptions containsObject:@"network-caching"]) {
                [mediaOptions addObject:@"--network-caching=1500"];
                [mediaOptions addObject:@"--live-caching=1500"];
                [mediaOptions addObject:@"--file-caching=1500"];
                [mediaOptions addObject:@"--disc-caching=300"];
                [mediaOptions addObject:@"--clock-jitter=0"];
                [mediaOptions addObject:@"--clock-synchro=0"];
            }
            
            // Improve TCP/UDP performance for streams
            [mediaOptions addObject:@"--rtsp-tcp"];
            [mediaOptions addObject:@"--ipv4-timeout=1000"];
            [mediaOptions addObject:@"--http-reconnect"];
        }
        
        // Add custom media options if provided
        if (self->_mediaOptions && self->_mediaOptions.count > 0) {
            for (NSString *option in self->_mediaOptions) {
                if ([option isKindOfClass:[NSString class]]) {
                    // Make sure option starts with --
                    if ([option hasPrefix:@"--"]) {
                        [mediaOptions addObject:option];
                    } else {
                        [mediaOptions addObject:[NSString stringWithFormat:@"--%@", option]];
                    }
                }
            }
        }
        
        // Stop any existing playback first
        [self->_player stop];
        
        // Create VLC media with options
        VLCMedia *media = [VLCMedia mediaWithURL:videoURL];
        
        // Apply media options
        for (NSString *option in mediaOptions) {
            [media addOption:option];
        }
        
        // Set new media to player
        self->_player.media = media;
        
        // Ensure drawable is set correctly
        self->_player.drawable = self;
        
        // Configure additional player settings
        self->_player.videoAspectRatio = NULL; // Use default aspect ratio - must be NULL not nil for VLCKit
        
        RCTLogInfo(@"[UnifiedPlayerViewManager] Media configured with options: %@", mediaOptions);
        RCTLogInfo(@"[UnifiedPlayerViewManager] Player drawable: %@, bounds: %@", 
                  self->_player.drawable, NSStringFromCGRect(self.bounds));
        
        // Instead of force redraw, use these methods directly:
        [self setNeedsLayout];
        [self layoutIfNeeded];
        [self setNeedsDisplay];
        
        // Start playback if autoplay is enabled
        if (self->_autoplay) {
            [self->_player play];
            
            // Don't call forceRedraw again, just update the view
            [self setNeedsDisplay];
        }
    });
}

- (void)play {
    if (_player.media) {
        // Make sure drawable is properly set before playing
        if (_player.drawable != self) {
            _player.drawable = self;
        }
        
        // Ensure video track is enabled if available
        if (_player.numberOfVideoTracks > 0 && _player.currentVideoTrackIndex == -1) {
            // Attempt to enable the first video track
            if (_player.videoTrackIndexes.count > 0) {
                NSNumber *videoTrackIndex = [_player.videoTrackIndexes firstObject];
                _player.currentVideoTrackIndex = [videoTrackIndex intValue];
                RCTLogInfo(@"[UnifiedPlayerViewManager] Enabling video track index: %@", videoTrackIndex);
            }
        }
        
        // Apply aspect ratio settings
        _player.videoAspectRatio = NULL; // Use default aspect ratio
        
        // Set scale mode for the VLC video output
        // Note: VLC has its own scaling which is separate from UIView contentMode
        _player.scaleFactor = 0.0; // Auto scale
        
        [_player play];
        
        // Use these methods instead of forceRedraw
        [self setNeedsLayout];
        [self layoutIfNeeded];
        [self setNeedsDisplay];
        
        RCTLogInfo(@"[UnifiedPlayerViewManager] play called, drawable: %@, frame: %@", 
                 _player.drawable, NSStringFromCGRect(self.frame));
    }
}

- (void)pause {
    [_player pause];
    RCTLogInfo(@"[UnifiedPlayerViewManager] pause called");
}

- (void)seekToTime:(float)time {
    // VLC uses a 0-1 position value for seeking
    float position = time / [self getDuration];
    position = MAX(0, MIN(1, position)); // Ensure position is between 0 and 1
    
    [_player setPosition:position];
    RCTLogInfo(@"[UnifiedPlayerViewManager] Seek to %f (position: %f)", time, position);
}

- (float)getCurrentTime {
    return _player.time.intValue / 1000.0f; // Convert from milliseconds to seconds
}

- (float)getDuration {
    return _player.media.length.intValue / 1000.0f; // Convert from milliseconds to seconds
}

- (void)setAutoplay:(BOOL)autoplay {
    _autoplay = autoplay;
}

- (void)setLoop:(BOOL)loop {
    _loop = loop;
}

- (void)setIsPaused:(BOOL)isPaused {
    if (_isPaused != isPaused) {
        _isPaused = isPaused;
        if (_player) {
            if (_isPaused && _player.isPlaying) {
                [_player pause];
                RCTLogInfo(@"[UnifiedPlayerViewManager] Paused via isPaused prop");
            } else if (!_isPaused && !_player.isPlaying) {
                [_player play];
                RCTLogInfo(@"[UnifiedPlayerViewManager] Played via isPaused prop");
            }
        }
    }
}


#pragma mark - VLCMediaPlayerDelegate

- (void)mediaPlayerTimeChanged:(NSNotification *)notification {
    float currentTime = [self getCurrentTime];
    float duration = [self getDuration];
    
    // Avoid sending progress events for invalid durations
    if (duration > 0 && !isnan(duration)) {
        [self sendProgressEvent:currentTime duration:duration];
    }
}

- (void)mediaPlayerStateChanged:(NSNotification *)notification {
    VLCMediaPlayerState state = _player.state;
    
    // Debug information for video output
    if (state == VLCMediaPlayerStatePlaying) {
        RCTLogInfo(@"[UnifiedPlayerViewManager] Video size: %@", 
                   NSStringFromCGSize(_player.videoSize));
        RCTLogInfo(@"[UnifiedPlayerViewManager] Has video out: %@", 
                   _player.hasVideoOut ? @"YES" : @"NO");
        
        // Check video tracks
        NSArray *videoTracks = [_player.media tracksInformation];
        if (videoTracks.count > 0) {
            RCTLogInfo(@"[UnifiedPlayerViewManager] Video tracks found: %lu", (unsigned long)videoTracks.count);
            
            // Send ready event the first time we start playing
            if (!_hasRenderedVideo) {
                [self sendEvent:@"onReadyToPlay" body:@{}];
            }
            
            // Send playing event when we actually start playing
            [self sendEvent:@"onPlaying" body:@{}];
            
            // Trigger a delayed drawable update to help with rendering
            if (!_hasRenderedVideo && _player.videoSize.width > 0) {
                _hasRenderedVideo = YES;
                [self updatePlayerDrawableWithDelay];
            }
        } else {
            RCTLogInfo(@"[UnifiedPlayerViewManager] No video tracks found!");
        }
    }
    
    // Check for buffer state transitions
    if (_previousState == VLCMediaPlayerStateBuffering && state == VLCMediaPlayerStatePlaying) {
        // We've recovered from buffering
        [self sendEvent:@"onPlaybackResumed" body:@{}];
    }
    
    // Store the current state for future comparisons
    _previousState = state;
    
    // React to state changes
    switch (state) {
        case VLCMediaPlayerStateOpening:
            RCTLogInfo(@"[UnifiedPlayerViewManager] VLCMediaPlayerStateOpening");
            break;
            
        case VLCMediaPlayerStateBuffering:
            RCTLogInfo(@"[UnifiedPlayerViewManager] VLCMediaPlayerStateBuffering");
            [self sendEvent:@"onPlaybackStalled" body:@{}];
            break;
            
        case VLCMediaPlayerStatePlaying:
            RCTLogInfo(@"[UnifiedPlayerViewManager] VLCMediaPlayerStatePlaying");
            break;
            
        case VLCMediaPlayerStatePaused:
            RCTLogInfo(@"[UnifiedPlayerViewManager] VLCMediaPlayerStatePaused");
            [self sendEvent:@"onPaused" body:@{}];
            break;
            
        case VLCMediaPlayerStateStopped:
            RCTLogInfo(@"[UnifiedPlayerViewManager] VLCMediaPlayerStateStopped");
            // We don't emit onStopped event as it's not in our unified event list
            break;
            
        case VLCMediaPlayerStateEnded:
            RCTLogInfo(@"[UnifiedPlayerViewManager] VLCMediaPlayerStateEnded");
            [self sendEvent:@"onPlaybackComplete" body:@{}];
            
            // Handle looping
            if (_loop) {
                [_player stop];
                [_player play];
            }
            break;
            
        case VLCMediaPlayerStateError:
            RCTLogInfo(@"[UnifiedPlayerViewManager] VLCMediaPlayerStateError");
            [self sendEvent:@"onError" body:@{
                @"code": @"PLAYBACK_ERROR",
                @"message": @"VLC player encountered an error",
                @"details": @{@"url": _videoUrlString ?: @""}
            }];
            break;
            
        default:
            break;
    }
}

- (void)mediaPlayerSnapshot:(NSNotification *)notification {
    // Handle snapshot completion if needed
}

- (void)mediaPlayerTitleChanged:(NSNotification *)notification {
    // Handle title changes if needed
}

// Define a method that maps VLC buffer state changes to appropriate RN events
- (void)handleBufferingStatusChange {
    if (_player.state == VLCMediaPlayerStateBuffering) {
        // When buffering, send stalled event
        [self sendEvent:@"onPlaybackStalled" body:@{}];
    } else if (_player.state == VLCMediaPlayerStatePlaying && _player.isPlaying) {
        // When buffer is full and playing again, send resumed event
        [self sendEvent:@"onPlaybackResumed" body:@{}];
    }
}

- (void)dealloc {
    RCTLogInfo(@"[UnifiedPlayerViewManager] Deallocating player view");
    
    // Remove all observers
    [[NSNotificationCenter defaultCenter] removeObserver:self];
    
    // Stop playback and release player
    [_player stop];
    _player.delegate = nil;
    _player = nil;
}

// Update updatePlayerDrawableWithDelay to use safer redraw approach
- (void)updatePlayerDrawableWithDelay {
    // Sometimes a slight delay can help with rendering issues
    dispatch_after(dispatch_time(DISPATCH_TIME_NOW, (int64_t)(0.1 * NSEC_PER_SEC)), dispatch_get_main_queue(), ^{
        if (self->_player) {
            // Reset drawable
            self->_player.drawable = nil;
            self->_player.drawable = self;
            
            // Call UIKit methods directly instead of forceRedraw
            [self setNeedsLayout];
            [self layoutIfNeeded];
            [self setNeedsDisplay];
            
            RCTLogInfo(@"[UnifiedPlayerViewManager] Drawable updated with delay");
        }
    });
}

@end

@interface UnifiedPlayerViewManager : RCTViewManager
@end

@implementation UnifiedPlayerViewManager

RCT_EXPORT_MODULE(UnifiedPlayerView)

@synthesize bridge = _bridge;

- (UIView *)view
{
    UnifiedPlayerUIView *playerView = [[UnifiedPlayerUIView alloc] init];
    playerView.bridge = self.bridge;
    
    // Store the event emitter for sending events
    if (eventEmitter == nil) {
        eventEmitter = [self.bridge moduleForClass:[UnifiedPlayerModule class]];
    }
    
    return playerView;
}

// Video URL property
RCT_CUSTOM_VIEW_PROPERTY(videoUrl, NSString, UnifiedPlayerUIView)
{
    [view setupWithVideoUrlString:json];
}

// Autoplay property
RCT_CUSTOM_VIEW_PROPERTY(autoplay, BOOL, UnifiedPlayerUIView)
{
    view.autoplay = [RCTConvert BOOL:json];
}

// Loop property
RCT_CUSTOM_VIEW_PROPERTY(loop, BOOL, UnifiedPlayerUIView)
{
    view.loop = [RCTConvert BOOL:json];
}

// Media options property
RCT_CUSTOM_VIEW_PROPERTY(mediaOptions, NSArray, UnifiedPlayerUIView)
{
    view.mediaOptions = [RCTConvert NSArray:json];
}

// isPaused property
RCT_CUSTOM_VIEW_PROPERTY(isPaused, BOOL, UnifiedPlayerUIView)
{
    view.isPaused = [RCTConvert BOOL:json];
}

@end
