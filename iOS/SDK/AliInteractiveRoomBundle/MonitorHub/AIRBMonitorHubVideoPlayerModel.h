//
//  AIRBMonitorHubLivePlayerModel.h
//  AliInteractiveRoomBundle
//
//  Created by fernando on 2021/12/21.
//  Copyright © 2021 AliYun. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "AIRBMonitorHubDefines.h"

NS_ASSUME_NONNULL_BEGIN

@interface AIRBMonitorHubVideoPlayerModel : NSObject
@property (assign, atomic) AIRBMonitorHubComponentStatus status;
@property (copy, nonatomic) NSString* contentID;
@property (copy, atomic) NSString* playType;
@property (copy, nonatomic) NSString* url;
@property (copy, nonatomic) NSString* protocol;
@property (assign, atomic) int32_t videoWidth;
@property (assign, atomic) int32_t videoHeight;
@property (assign, nonatomic) int64_t renderedVideoFrameCount;
@property (copy, nonatomic) NSString* rtsTraceID;
@end

NS_ASSUME_NONNULL_END
