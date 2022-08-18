//
//  ASLRBLiveRoomPlaybackProtocol.h
//  AliStandardLiveRoomBundle
//
//  Created by fernando on 2021/12/6.
//

#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

@protocol ASLRBLiveRoomPlaybackProtocol <NSObject>
/**
 * 直播回放观看时画面的伸缩模式，目前仅支持三种，即UIViewContentModeScaleToFill, UIViewContentModeScaleAspectFit和UIViewContentModeScaleAspectFill
 * 默认为UIViewContentModeScaleAspectFit；
 */
@property (assign, nonatomic) UIViewContentMode playerViewContentMode;
@end

NS_ASSUME_NONNULL_END
