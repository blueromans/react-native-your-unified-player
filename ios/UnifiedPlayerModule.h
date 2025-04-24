#import <React/RCTEventEmitter.h>
#import <React/RCTBridgeModule.h>
#import <MobileVLCKit/MobileVLCKit.h> // Import MobileVLCKit

// Forward declaration for UnifiedPlayerUIView
@class UnifiedPlayerUIView;

@interface UnifiedPlayerModule : RCTEventEmitter <RCTBridgeModule>
@end
