//
//  ASLRBLiveRoomBackgroundImageConfig.h
//  AliStandardLiveRoomBundle
//
//  Created by fernando on 2022/4/26.
//

#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN

@interface ASLRBLiveRoomBackgroundImageConfig : NSObject

/**
 *@brief 观众直播间内部默认背景图，默认为NO，即会展示默认图；
 */
@property (nonatomic, assign) BOOL defaultBackgroundImageHidden;
@end

NS_ASSUME_NONNULL_END
