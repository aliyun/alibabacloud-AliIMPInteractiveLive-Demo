//
//  AIRBDRoomInfoModel.m
//  AliInteractiveLiveDemo
//
//  Created by 麦辣 on 2021/6/18.
//

#import "ASLRBRoomInfoModel.h"

@implementation ASLRBLiveInfoModel
- (instancetype)init
{
    self = [super init];
    if (self) {
        _title = @"";
//        _hostName = @"";
        _notice = @"defautNotice";
        _userImg = @"img-user-default";
//        _roomID = @"";
//        _userID = @"777";
    }
    return self;
}

@end
