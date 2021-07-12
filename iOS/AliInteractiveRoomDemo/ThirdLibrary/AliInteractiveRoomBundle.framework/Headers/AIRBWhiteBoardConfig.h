//
//  AIRBWhiteBoardConfig.h
//  AliInteractiveRoomBundle
//
//  Created by fernando on 2021/5/24.
//  Copyright © 2021 AliYun. All rights reserved.
//

#import <Foundation/Foundation.h>

#import "AIRBCommonDefines.h"

NS_ASSUME_NONNULL_BEGIN

@interface AIRBWhiteBoardConfig : NSObject
@property (assign, nonatomic) int32_t whiteboardContentWidth;
@property (assign, nonatomic) int32_t whiteboardContentHeight;
@property (assign, nonatomic) BOOL autoRecording; //是否白板打开后自动进行录制，默认NO
@property (assign, nonatomic) AIRBWhiteBoardPlayMode playMode; //默认是AIRBWhiteBoardPlayModeLiving
@property (copy, nonatomic) NSString* recordID; //回放模式下传入

@end

NS_ASSUME_NONNULL_END
