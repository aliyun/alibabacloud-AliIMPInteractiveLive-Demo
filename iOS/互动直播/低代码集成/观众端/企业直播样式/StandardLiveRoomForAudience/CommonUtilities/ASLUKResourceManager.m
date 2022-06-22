//
//  ASLRBResourceManager.m
//  AliStandardLiveRoomBundle
//
//  Created by fernando on 2021/11/16.
//

#import "ASLUKResourceManager.h"

@implementation ASLUKResourceManager

+ (ASLUKResourceManager*) sharedInstance {
    static ASLUKResourceManager *sharedInstance = nil;
    static dispatch_once_t token;
    dispatch_once(&token, ^{
        sharedInstance = [[ASLUKResourceManager alloc] init];
    });
    return sharedInstance;
}

- (NSBundle*) resourceBundle {
    if (!_resourceBundle) {
        NSString *path = [[NSBundle mainBundle] pathForResource:@"standard-ui-ios" ofType:@"bundle"];
        _resourceBundle = [NSBundle bundleWithPath:path];
    }
    return _resourceBundle;
}

@end
