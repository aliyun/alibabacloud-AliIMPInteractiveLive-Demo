//
//  AIRBRoomSceneLiveBusinessInfo.h
//  AliInteractiveRoomBundle
//
//  Created by fernando on 2021/12/14.
//  Copyright © 2021 AliYun. All rights reserved.
//

#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

@interface AIRBRoomSceneLiveBusinessInfo : NSObject
@property (copy, nonatomic) NSString* liveID;
@property (copy, nonatomic) NSString* liveTitle;
@property (copy, nonatomic) NSString* liveNotice;
@property (copy, nonatomic) NSString* liveCoverURL;
@property (copy, nonatomic) NSDictionary<NSString*,NSString*>* liveCustomData;
@end

NS_ASSUME_NONNULL_END
