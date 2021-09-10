//
//  AIRBDRoomInfoModel.m
//  AliInteractiveLiveDemo
//
//  Created by 麦辣 on 2021/6/18.
//

#import "AIRBDRoomInfoModel.h"

@implementation AIRBDRoomInfoModel
- (instancetype)init
{
    self = [super init];
    if (self) {
        _title = @"MAK选品的直播间";
        _hostName = @"Mak家居";
        _notice = @"defautNotice";
        _userImg = @"img-user-default";
        _roomID = @"";
        _userID = @"777";
    }
    return self;
}

@end
