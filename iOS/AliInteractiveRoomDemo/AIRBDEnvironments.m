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
}

- (NSString*) interactiveClassRoomAppID {
}

- (NSString*) interactiveLiveRoomAppKey {
}

- (NSString*) interactiveClassRoomAppKey {
}

- (NSString*) appServerHost {
    @"xxx.com";
}

- (NSString*) signSecret {
}

@end
