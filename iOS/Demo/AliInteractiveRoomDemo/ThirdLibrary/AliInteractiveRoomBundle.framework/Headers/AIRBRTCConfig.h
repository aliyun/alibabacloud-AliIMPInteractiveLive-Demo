//
//  AIRBRTCConfig.h
//  AliInteractiveRoomBundle
//
//  Created by 刘再勇 on 2021/8/19.
//  Copyright © 2021 AliYun. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN

@interface AIRBRTCConfig : NSObject
@property (nonatomic, assign) CGSize videoStreamTypeHighDimensions;     // 高分辨率视频流（大流）的编码分辨率，默认为640x480
@property (nonatomic, assign) BOOL videoStreamTypeLowPublished;         // 是否同时推送低分辨率视频流（小流），默认开启（小流的分辨率无法修改）
@end

NS_ASSUME_NONNULL_END
