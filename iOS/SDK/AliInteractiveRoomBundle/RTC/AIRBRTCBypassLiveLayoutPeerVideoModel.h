//
//  AIRBRTCBypassLiveLayoutPeerModel.h
//  AliInteractiveRoomBundle
//
//  Created by 刘再勇 on 2021/9/27.
//  Copyright © 2021 AliYun. All rights reserved.
//

#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

@interface AIRBRTCBypassLiveLayoutPeerVideoModel : NSObject

/**
 * 必填，格子左上角x坐标，归一化后的百分比值, 举例：对于宽为720像素，x=0.1，代表左上角x坐标为720*0.1=72像素
 */
@property (nonatomic) float x;

/**
 * 必填，格子左上角y坐标，归一化后的百分比值
 */
@property (nonatomic) float y;

/**
 *  必填，格子宽，归一化后的百分比值
 */
@property (nonatomic) float width;

/**
 * 必填，格子高，归一化后的百分比值
 */
@property (nonatomic) float height;

/**
 * 必填，格子zOrder，值越大，越在上面
 */
@property (nonatomic) int32_t zOrder;

/**
 * 可选，此格子展示用户，不填则会显示黑色
 */
@property (nonatomic, nonnull) NSString * userId;

@end

NS_ASSUME_NONNULL_END
