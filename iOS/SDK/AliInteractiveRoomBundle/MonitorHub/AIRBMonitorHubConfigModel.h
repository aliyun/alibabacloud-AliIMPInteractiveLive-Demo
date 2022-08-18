//
//  AIRBMonitorHubConfigModel.h
//  AliInteractiveRoomBundle
//
//  Created by fernando on 2021/12/21.
//  Copyright © 2021 AliYun. All rights reserved.
//

#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

@interface AIRBMonitorHubConfigModel : NSObject
@property (copy, nonatomic) NSString* appID;
@property (copy, nonatomic) NSString* userID;
@property (copy, nonatomic) NSString* deviceID;
@property (copy, nonatomic) NSString* roomID;
@property (copy, nonatomic) NSString* roomOwnerID;
@property (assign, nonatomic) BOOL prereleaseEnvironment;
@property (copy, nonatomic) NSString* bizID; //liveID or classID
@property (copy, nonatomic) NSString* bizType;
@end

NS_ASSUME_NONNULL_END
