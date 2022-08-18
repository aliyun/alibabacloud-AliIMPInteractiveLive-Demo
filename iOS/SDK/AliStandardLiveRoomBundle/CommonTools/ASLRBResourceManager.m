//
//  ASLRBResourceManager.m
//  AliStandardLiveRoomBundle
//
//  Created by fernando on 2021/11/16.
//

#import "ASLRBResourceManager.h"

@implementation ASLRBResourceManager

+ (ASLRBResourceManager*) sharedInstance {
    static ASLRBResourceManager *sharedInstance = nil;
    static dispatch_once_t token;
    dispatch_once(&token, ^{
        sharedInstance = [[ASLRBResourceManager alloc] init];
    });
    return sharedInstance;
}

- (NSBundle*) resourceBundle {
    if (!_resourceBundle) {
        NSString *path = [[NSBundle mainBundle] pathForResource:@"AliStandardLiveRoomResource" ofType:@"bundle"];
        _resourceBundle = [NSBundle bundleWithPath:path];
    }
    return _resourceBundle;
}

@end
