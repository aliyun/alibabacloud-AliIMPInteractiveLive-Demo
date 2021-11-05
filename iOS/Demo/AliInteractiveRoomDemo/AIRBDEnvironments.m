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

- (NSString*) interactiveLiveRoomAppID {
    return [NSString stringWithFormat:@"%@", @"lq60cu"];
}

- (NSString*) interactiveClassRoomAppID {
    return [NSString stringWithFormat:@"%@", @"9x8o9v"];
}

- (NSString*) interactiveLiveRoomAppKey {
    return [NSString stringWithFormat:@"%@", @"633176a81841d2cd4f222e1afd5c423d"];
}

- (NSString*) interactiveClassRoomAppKey {
    return [NSString stringWithFormat:@"%@", @"b9960baeb956e8c4fefa0bbf59a67527"];
}

- (NSString*) appServerUrl {
    return [NSString stringWithFormat:@"%@", @"https://demo-appserver-livepaas.dingtalk.com"];
}

- (NSString*) signSecret {
    return [NSString stringWithFormat:@"%@", @"h92kz2"];
}

@end
