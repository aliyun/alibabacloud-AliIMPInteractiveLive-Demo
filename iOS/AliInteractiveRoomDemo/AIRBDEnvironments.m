//
//  AIRBDEnvironments.m
//  AliInteractiveRoomDemo
//
//  Created by fernando on 2021/6/30.
//

#import "AIRBDEnvironments.h"

@implementation AIRBDEnvironments

+ (instancetype)shareInstance {
    static AIRBDEnvironments *singleton = nil;
    static dispatch_once_t onceTokenAIRBDEnvironments;
    dispatch_once(&onceTokenAIRBDEnvironments, ^{
        singleton = [[AIRBDEnvironments alloc] init];
    });
    return singleton;
}

- (NSString*) appID {
    return [NSString stringWithFormat:@"%@", @"xxx"];
}

- (NSString*) appKey {
    return [NSString stringWithFormat:@"%@", @"xxxx"];
}

- (NSString*) appServerHost {
    return [NSString stringWithFormat:@"%@", @"xxxx"];
}

- (NSString*) releaseLongLinkURL {
    return [NSString stringWithFormat:@"%@", @"xxx"];
}

- (NSString*) prereleaseLongLinkURL {
    return [NSString stringWithFormat:@"%@", @"xxx"];
}

- (NSString*) signSecret {
    return [NSString stringWithFormat:@"%@", @"xxxx"];
}

@end
