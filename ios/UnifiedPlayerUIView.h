#import <UIKit/UIKit.h>
#import <React/RCTView.h>
#import <React/RCTComponent.h>
#import <MobileVLCKit/MobileVLCKit.h>

NS_ASSUME_NONNULL_BEGIN

@interface UnifiedPlayerUIView : UIView <VLCMediaPlayerDelegate>

@property (nonatomic, strong) VLCMediaPlayer *player;
@property (nonatomic, copy) NSString *videoUrlString;
@property (nonatomic, assign) BOOL autoplay;
@property (nonatomic, assign) BOOL loop;
@property (nonatomic, assign) BOOL isPaused;
@property (nonatomic, strong) NSArray *mediaOptions;
@property (nonatomic, weak) RCTBridge *bridge;
@property (nonatomic, assign) VLCMediaPlayerState previousState;
@property (nonatomic, assign) BOOL hasRenderedVideo;

// Event callbacks
@property (nonatomic, copy) RCTDirectEventBlock onLoadStart;
@property (nonatomic, copy) RCTDirectEventBlock onReadyToPlay;
@property (nonatomic, copy) RCTDirectEventBlock onError;
@property (nonatomic, copy) RCTDirectEventBlock onProgress;
@property (nonatomic, copy) RCTDirectEventBlock onPlaybackComplete;
@property (nonatomic, copy) RCTDirectEventBlock onPlaybackStalled;
@property (nonatomic, copy) RCTDirectEventBlock onPlaybackResumed;
@property (nonatomic, copy) RCTDirectEventBlock onPlaying;
@property (nonatomic, copy) RCTDirectEventBlock onPaused;

// Method declarations
- (void)setupWithVideoUrlString:(NSString *)videoUrlString;
- (void)play;
- (void)pause;
- (void)seekToTime:(NSNumber *)timeNumber;
- (float)getCurrentTime;
- (float)getDuration;
- (void)captureFrameWithCompletion:(void (^)(NSString * _Nullable base64String, NSError * _Nullable error))completion;

@end

NS_ASSUME_NONNULL_END
