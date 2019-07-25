
#import "RNUpdate.h"
#import <Foundation/Foundation.h>
#import <React/RCTBridge.h>

@implementation RNUpdate

@synthesize bridge = _bridge;

RCT_EXPORT_MODULE();

+ (BOOL)requiresMainQueueSetup
{
    return YES;
}

RCT_EXPORT_METHOD(reloadJSBundle) {
    if ([NSThread isMainThread]) {
        [_bridge reload];
    } else {
        dispatch_sync(dispatch_get_main_queue(), ^{
            [_bridge reload];
        });
    }
    return;
}

@end
  
