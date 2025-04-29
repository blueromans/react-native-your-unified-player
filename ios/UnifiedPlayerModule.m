#import "UnifiedPlayerModule.h"
#import "UnifiedPlayerUIView.h"
#import <React/RCTLog.h>
#import <React/RCTBridgeModule.h>
#import <React/RCTEventEmitter.h>
#import <React/RCTUIManager.h>
#import <MobileVLCKit/MobileVLCKit.h>

@implementation UnifiedPlayerModule

// Explicitly name the module to match what's expected in JavaScript
RCT_EXPORT_MODULE(UnifiedPlayer);

+ (BOOL)requiresMainQueueSetup {
    return YES;
}

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
RCT_EXPORT_METHOD(play:(nonnull NSNumber *)reactTag
                  resolver:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject) {
    [self.bridge.uiManager addUIBlock:^(RCTUIManager *uiManager, NSDictionary<NSNumber *,UIView *> *viewRegistry) {
        UIView *view = viewRegistry[reactTag];
        if (!view) {
            reject(@"error", @"Invalid view for tag", nil);
            return;
        }
        
        // Cast to UnifiedPlayerUIView class
        Class UnifiedPlayerUIViewClass = NSClassFromString(@"UnifiedPlayerUIView");
        if (![view isKindOfClass:UnifiedPlayerUIViewClass]) {
            reject(@"error", @"View is not a UnifiedPlayerUIView", nil);
            return;
        }
        
        @try {
            // Use direct ivar access for safety
            VLCMediaPlayer *player = [view valueForKey:@"player"];
            if (player && player.media) {
                [player play];
                resolve(@(YES));
            } else {
                reject(@"error", @"Player or media not initialized", nil);
            }
        } @catch (NSException *exception) {
            reject(@"error", [NSString stringWithFormat:@"Error playing: %@", exception.reason], nil);
        }
    }];
}

// Pause video
RCT_EXPORT_METHOD(pause:(nonnull NSNumber *)reactTag
                  resolver:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject) {
    [self.bridge.uiManager addUIBlock:^(RCTUIManager *uiManager, NSDictionary<NSNumber *,UIView *> *viewRegistry) {
        UIView *view = viewRegistry[reactTag];
        if (!view) {
            reject(@"error", @"Invalid view for tag", nil);
            return;
        }
        
        // Cast to UnifiedPlayerUIView class
        Class UnifiedPlayerUIViewClass = NSClassFromString(@"UnifiedPlayerUIView");
        if (![view isKindOfClass:UnifiedPlayerUIViewClass]) {
            reject(@"error", @"View is not a UnifiedPlayerUIView", nil);
            return;
        }
        
        @try {
            // Use direct ivar access for safety
            VLCMediaPlayer *player = [view valueForKey:@"player"];
            if (player) {
                [player pause];
                resolve(@(YES));
            } else {
                reject(@"error", @"Player not initialized", nil);
            }
        } @catch (NSException *exception) {
            reject(@"error", [NSString stringWithFormat:@"Error pausing: %@", exception.reason], nil);
        }
    }];
}

// Seek to specific time
RCT_EXPORT_METHOD(seekTo:(nonnull NSNumber *)reactTag 
                  time:(nonnull NSNumber *)time
                  resolver:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject) {
    [self.bridge.uiManager addUIBlock:^(RCTUIManager *uiManager, NSDictionary<NSNumber *,UIView *> *viewRegistry) {
        UIView *view = viewRegistry[reactTag];
        if (!view) {
            reject(@"error", @"Invalid view for tag", nil);
            return;
        }
        
        // Cast to UnifiedPlayerUIView class
        Class UnifiedPlayerUIViewClass = NSClassFromString(@"UnifiedPlayerUIView");
        if (![view isKindOfClass:UnifiedPlayerUIViewClass]) {
            reject(@"error", @"View is not a UnifiedPlayerUIView", nil);
            return;
        }
        
        @try {
            // Use direct ivar access for safety
            VLCMediaPlayer *player = [view valueForKey:@"player"];
            if (player && player.media) {
                float timeValue = [time floatValue];
                float duration = player.media.length.intValue / 1000.0f;
                float position = duration > 0 ? timeValue / duration : 0;
                position = MAX(0, MIN(1, position)); // Ensure position is between 0 and 1
                
                [player setPosition:position];
                resolve(@(YES));
            } else {
                reject(@"error", @"Player or media not initialized", nil);
            }
        } @catch (NSException *exception) {
            reject(@"error", [NSString stringWithFormat:@"Error seeking: %@", exception.reason], nil);
        }
    }];
}

// Get current playback time
RCT_EXPORT_METHOD(getCurrentTime:(nonnull NSNumber *)reactTag
                  resolver:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject) {
    [self.bridge.uiManager addUIBlock:^(RCTUIManager *uiManager, NSDictionary<NSNumber *,UIView *> *viewRegistry) {
        UIView *view = viewRegistry[reactTag];
        if (!view) {
            reject(@"error", @"Invalid view for tag", nil);
            return;
        }
        
        // Cast to UnifiedPlayerUIView class
        Class UnifiedPlayerUIViewClass = NSClassFromString(@"UnifiedPlayerUIView");
        if (![view isKindOfClass:UnifiedPlayerUIViewClass]) {
            reject(@"error", @"View is not a UnifiedPlayerUIView", nil);
            return;
        }
        
        // Use direct method call with proper type safety
        float currentTime = 0;
        @try {
            // Use direct ivar access for safety
            VLCMediaPlayer *player = [view valueForKey:@"player"];
            if (player) {
                currentTime = player.time.intValue / 1000.0f;
                resolve(@(currentTime));
            } else {
                reject(@"error", @"Player not initialized", nil);
            }
        } @catch (NSException *exception) {
            reject(@"error", [NSString stringWithFormat:@"Error getting current time: %@", exception.reason], nil);
        }
    }];
}

// Get video duration
RCT_EXPORT_METHOD(getDuration:(nonnull NSNumber *)reactTag
                  resolver:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject)
{
    [self.bridge.uiManager addUIBlock:^(__unused RCTUIManager *uiManager, NSDictionary<NSNumber *, UIView *> *viewRegistry) {
        UIView *view = viewRegistry[reactTag];
        if (![view isKindOfClass:[UnifiedPlayerUIView class]]) {
            RCTLogError(@"Invalid view returned from registry, expecting UnifiedPlayerUIView, got: %@", view);
            reject(@"E_INVALID_VIEW", @"Expected UnifiedPlayerUIView", nil);
        } else {
            UnifiedPlayerUIView *playerView = (UnifiedPlayerUIView *)view;
            float duration = [playerView getDuration];
            resolve(@(duration));
        }
    }];
}

RCT_EXPORT_METHOD(capture:(nonnull NSNumber *)reactTag
                  resolver:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject)
{
    [self.bridge.uiManager addUIBlock:^(__unused RCTUIManager *uiManager, NSDictionary<NSNumber *, UIView *> *viewRegistry) {
        UIView *view = viewRegistry[reactTag];
        if (![view isKindOfClass:[UnifiedPlayerUIView class]]) {
            RCTLogError(@"Invalid view returned from registry, expecting UnifiedPlayerUIView, got: %@", view);
            reject(@"E_INVALID_VIEW", @"Expected UnifiedPlayerUIView", nil);
            return;
        }
        
        UnifiedPlayerUIView *playerView = (UnifiedPlayerUIView *)view;
        [playerView captureFrameWithCompletion:^(NSString * _Nullable base64String, NSError * _Nullable error) {
            if (error) {
                reject(@"E_CAPTURE_FAILED", error.localizedDescription, error);
            } else if (base64String) {
                resolve(base64String);
            } else {
                reject(@"E_CAPTURE_FAILED", @"Unknown capture error", nil);
            }
        }];
    }];
}

RCT_EXPORT_METHOD(startRecording:(nonnull NSNumber *)reactTag
                  outputPath:(NSString *)outputPath
                  resolver:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject)
{
    [self.bridge.uiManager addUIBlock:^(__unused RCTUIManager *uiManager, NSDictionary<NSNumber *, UIView *> *viewRegistry) {
        UIView *view = viewRegistry[reactTag];
        if (![view isKindOfClass:[UnifiedPlayerUIView class]]) {
            RCTLogError(@"Invalid view returned from registry, expecting UnifiedPlayerUIView, got: %@", view);
            reject(@"E_INVALID_VIEW", @"Expected UnifiedPlayerUIView", nil);
            return;
        }
        
        UnifiedPlayerUIView *playerView = (UnifiedPlayerUIView *)view;
        
        // If no output path is provided, create a default one in the Documents directory
        NSString *finalOutputPath = outputPath;
        if (!finalOutputPath || [finalOutputPath isEqualToString:@""]) {
            NSString *documentsDirectory = NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES).firstObject;
            NSString *timestamp = [NSString stringWithFormat:@"%f", [[NSDate date] timeIntervalSince1970]];
            finalOutputPath = [documentsDirectory stringByAppendingPathComponent:[NSString stringWithFormat:@"recording_%@.mp4", timestamp]];
        }
        
        BOOL success = [playerView startRecordingToPath:finalOutputPath];
        if (success) {
            resolve(@YES);
        } else {
            reject(@"E_RECORDING_FAILED", @"Failed to start recording", nil);
        }
    }];
}

RCT_EXPORT_METHOD(toggleFullscreen:(nonnull NSNumber *)reactTag
                  isFullscreen:(BOOL)isFullscreen
                  resolver:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject)
{
    [self.bridge.uiManager addUIBlock:^(__unused RCTUIManager *uiManager, NSDictionary<NSNumber *, UIView *> *viewRegistry) {
        UIView *view = viewRegistry[reactTag];
        if (![view isKindOfClass:[UnifiedPlayerUIView class]]) {
            RCTLogError(@"Invalid view returned from registry, expecting UnifiedPlayerUIView, got: %@", view);
            reject(@"E_INVALID_VIEW", @"Expected UnifiedPlayerUIView", nil);
            return;
        }

        UnifiedPlayerUIView *playerView = (UnifiedPlayerUIView *)view;
        [playerView toggleFullscreen:isFullscreen];
        resolve(@(YES));
    }];
}

RCT_EXPORT_METHOD(stopRecording:(nonnull NSNumber *)reactTag
                  resolver:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject)
{
    [self.bridge.uiManager addUIBlock:^(__unused RCTUIManager *uiManager, NSDictionary<NSNumber *, UIView *> *viewRegistry) {
        UIView *view = viewRegistry[reactTag];
        if (![view isKindOfClass:[UnifiedPlayerUIView class]]) {
            RCTLogError(@"Invalid view returned from registry, expecting UnifiedPlayerUIView, got: %@", view);
            reject(@"E_INVALID_VIEW", @"Expected UnifiedPlayerUIView", nil);
            return;
        }
        
        UnifiedPlayerUIView *playerView = (UnifiedPlayerUIView *)view;
        NSString *filePath = [playerView stopRecording];
        
        if (filePath && ![filePath isEqualToString:@""]) {
            resolve(filePath);
        } else {
            reject(@"E_RECORDING_FAILED", @"Failed to stop recording or no recording in progress", nil);
        }
    }];
}

RCT_EXPORT_METHOD(saveVideo:(nonnull NSNumber *)reactTag
                  resolver:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject)
{
    [self.bridge.uiManager addUIBlock:^(__unused RCTUIManager *uiManager, NSDictionary<NSNumber *, UIView *> *viewRegistry) {
        UIView *view = viewRegistry[reactTag];
        if (![view isKindOfClass:[UnifiedPlayerUIView class]]) {
            RCTLogError(@"Invalid view returned from registry, expecting UnifiedPlayerUIView, got: %@", view);
            reject(@"E_INVALID_VIEW", @"Expected UnifiedPlayerUIView", nil);
            return;
        }
        
        UnifiedPlayerUIView *playerView = (UnifiedPlayerUIView *)view;
        
        // Create a file path in the Documents directory
        NSString *documentsDirectory = NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES).firstObject;
        NSString *timestamp = [NSString stringWithFormat:@"%f", [[NSDate date] timeIntervalSince1970]];
        NSString *filePath = [documentsDirectory stringByAppendingPathComponent:[NSString stringWithFormat:@"saved_video_%@.mp4", timestamp]];
        
        // Start recording
        BOOL success = [playerView startRecordingToPath:filePath];
        if (!success) {
            reject(@"E_RECORDING_FAILED", @"Failed to start recording", nil);
            return;
        }
        
        // Record for 5 seconds (or adjust as needed)
        dispatch_after(dispatch_time(DISPATCH_TIME_NOW, (int64_t)(5 * NSEC_PER_SEC)), dispatch_get_main_queue(), ^{
            NSString *savedFilePath = [playerView stopRecording];
            
            if (savedFilePath && ![savedFilePath isEqualToString:@""]) {
                resolve(savedFilePath);
            } else {
                reject(@"E_RECORDING_FAILED", @"Failed to stop recording or no recording in progress", nil);
            }
        });
    }];
}

@end
