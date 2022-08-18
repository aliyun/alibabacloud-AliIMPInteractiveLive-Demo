//
//  AIRBRTCUserVolumeInfo.h
//  AliInteractiveRoomBundle
//
//  Created by 刘再勇 on 2021/12/10.
//  Copyright © 2021 AliYun. All rights reserved.
//

#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

@interface AIRBRTCUserVolumeInfo : NSObject

/*! 发言者的userID */
@property (nonatomic, copy) NSString * _Nonnull uid;
/*! 发言状态，YES：正在说话，NO：没有说话 */
@property (nonatomic, assign) BOOL speech_state;
/*! 音量值，取值范围[0,255] */
@property (nonatomic, assign) int volume;


@end

NS_ASSUME_NONNULL_END
