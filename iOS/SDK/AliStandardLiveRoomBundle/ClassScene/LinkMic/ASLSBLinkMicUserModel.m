//
//  ASLSBLinkMicUserModel.m
//  AliStandardClassroomBundle
//
//  Created by 刘再勇 on 2022/1/13.
//

#import "ASLSBLinkMicUserModel.h"

@implementation ASLSBLinkMicUserModel

- (instancetype)init {
    self = [super init];
    if (self) {
        _userID = @"";
        _nickname = @"";
    }
    return self;
}

- (id)copyWithZone:(NSZone *)zone {
    ASLSBLinkMicUserModel *p = [[ASLSBLinkMicUserModel allocWithZone:zone] init];
    //属性也要拷贝赋值
    p.userID = [self.userID mutableCopy];
    p.isCameraOpened = self.isCameraOpened;
    p.isMicOpened = self.isMicOpened;
    p.isAnchor = self.isAnchor;
    p.cameraView = self.cameraView;
    p.nickname = self.nickname;
    p.isScreenSharing = self.isScreenSharing;
    p.screenView = self.screenView;
    return p;
}

@end
