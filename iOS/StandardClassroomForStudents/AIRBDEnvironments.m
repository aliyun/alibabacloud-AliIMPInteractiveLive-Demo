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
    return [NSString stringWithFormat:@"%@", @""];
}

- (NSString*) interactiveClassRoomAppID {
    return [NSString stringWithFormat:@"%@", @""];
}

- (NSString*) interactiveLiveRoomAppKey {
    return [NSString stringWithFormat:@"%@", @""];
}

- (NSString*) interactiveClassRoomAppKey {
    return [NSString stringWithFormat:@"%@", @""];
}

- (NSString*) appServerUrl {
    return [NSString stringWithFormat:@"%@", @""];
}

- (NSString*) signSecret {
    return [NSString stringWithFormat:@"%@", @""];
}

@end
