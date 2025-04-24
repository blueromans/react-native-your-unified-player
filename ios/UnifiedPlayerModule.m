#import "UnifiedPlayerModule.h"
#import <React/RCTLog.h>
#import <React/RCTBridgeModule.h>
#import <React/RCTEventEmitter.h>
#import <React/RCTUIManager.h>

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
RCT_EXPORT_METHOD(play:(nonnull NSNumber *)reactTag) {
    [self.bridge.uiManager addUIBlock:^(RCTUIManager *uiManager, NSDictionary<NSNumber *,UIView *> *viewRegistry) {
        UIView *view = viewRegistry[reactTag];
        if (!view) {
            RCTLogError(@"Invalid view for tag %@", reactTag);
            return;
        }
        
        SEL playSelector = NSSelectorFromString(@"play");
        if ([view respondsToSelector:playSelector]) {
            [view performSelector:playSelector];
        } else {
            RCTLogError(@"View does not respond to play method");
        }
    }];
}

// Pause video
RCT_EXPORT_METHOD(pause:(nonnull NSNumber *)reactTag) {
    [self.bridge.uiManager addUIBlock:^(RCTUIManager *uiManager, NSDictionary<NSNumber *,UIView *> *viewRegistry) {
        UIView *view = viewRegistry[reactTag];
        if (!view) {
            RCTLogError(@"Invalid view for tag %@", reactTag);
            return;
        }
        
        SEL pauseSelector = NSSelectorFromString(@"pause");
        if ([view respondsToSelector:pauseSelector]) {
            [view performSelector:pauseSelector];
        } else {
            RCTLogError(@"View does not respond to pause method");
        }
    }];
}

// Seek to specific time
RCT_EXPORT_METHOD(seekTo:(nonnull NSNumber *)reactTag time:(nonnull NSNumber *)time) {
    [self.bridge.uiManager addUIBlock:^(RCTUIManager *uiManager, NSDictionary<NSNumber *,UIView *> *viewRegistry) {
        UIView *view = viewRegistry[reactTag];
        if (!view) {
            RCTLogError(@"Invalid view for tag %@", reactTag);
            return;
        }
        
        SEL seekToTimeSelector = NSSelectorFromString(@"seekToTime:");
        if ([view respondsToSelector:seekToTimeSelector]) {
            #pragma clang diagnostic push
            #pragma clang diagnostic ignored "-Warc-performSelector-leaks"
            [view performSelector:seekToTimeSelector withObject:time];
            #pragma clang diagnostic pop
        } else {
            RCTLogError(@"View does not respond to seekToTime: method");
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
        
        SEL getCurrentTimeSelector = NSSelectorFromString(@"getCurrentTime");
        if ([view respondsToSelector:getCurrentTimeSelector]) {
            #pragma clang diagnostic push
            #pragma clang diagnostic ignored "-Warc-performSelector-leaks"
            NSNumber *currentTime = [NSNumber numberWithFloat:[[view performSelector:getCurrentTimeSelector] floatValue]];
            #pragma clang diagnostic pop
            resolve(currentTime);
        } else {
            reject(@"error", @"View does not respond to getCurrentTime method", nil);
        }
    }];
}

// Get video duration
RCT_EXPORT_METHOD(getDuration:(nonnull NSNumber *)reactTag
                  resolver:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject) {
    [self.bridge.uiManager addUIBlock:^(RCTUIManager *uiManager, NSDictionary<NSNumber *,UIView *> *viewRegistry) {
        UIView *view = viewRegistry[reactTag];
        if (!view) {
            reject(@"error", @"Invalid view for tag", nil);
            return;
        }
        
        SEL getDurationSelector = NSSelectorFromString(@"getDuration");
        if ([view respondsToSelector:getDurationSelector]) {
            #pragma clang diagnostic push
            #pragma clang diagnostic ignored "-Warc-performSelector-leaks"
            NSNumber *duration = [NSNumber numberWithFloat:[[view performSelector:getDurationSelector] floatValue]];
            #pragma clang diagnostic pop
            resolve(duration);
        } else {
            reject(@"error", @"View does not respond to getDuration method", nil);
        }
    }];
}

@end
