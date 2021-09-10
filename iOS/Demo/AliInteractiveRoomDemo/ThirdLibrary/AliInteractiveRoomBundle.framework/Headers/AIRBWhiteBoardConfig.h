//
//  AIRBWhiteBoardConfig.h
//  AliInteractiveRoomBundle
//
//  Created by fernando on 2021/5/24.
//  Copyright © 2021 AliYun. All rights reserved.
//

#import <UIKit/UIKit.h>

#import "AIRBCommonDefines.h"

NS_ASSUME_NONNULL_BEGIN

@interface AIRBWhiteBoardConfig : NSObject
@property (assign, nonatomic) CGFloat whiteboardContentWidth;
@property (assign, nonatomic) CGFloat whiteboardContentHeight;
@property (assign, nonatomic) AIRBWhiteBoardPlayMode playMode; //默认是AIRBWhiteBoardPlayModeLiving
@property (copy, nonatomic) NSString* recordID; //回放模式下传入
@property (copy, nonatomic) NSString* recordDocKey; //回放模式下传入


@end

NS_ASSUME_NONNULL_END
