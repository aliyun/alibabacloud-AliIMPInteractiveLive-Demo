//
//  ASLRBLiveRoomMiddleViewsConfig.h
//  AliStandardLiveRoomBundle
//
//  Created by fernando on 2022/3/24.
//

#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

@interface ASLRBLiveRoomMiddleViewsConfig : NSObject

@property (assign, nonatomic) BOOL liveNoticeButtonHidden; //是否隐藏直播公告按钮，默认YES，即隐藏
@property (assign, nonatomic) BOOL liveMembersButtonHidden; //是否隐藏直播间成员列表触发按钮，默认YES，即隐藏
@property (assign, nonatomic) BOOL livePushStatusLabelHidden; //是否隐藏直播推流流畅情况label，默认NO，即展示
@property (assign, nonatomic) BOOL livePlayLoadingIndicatorHidden; //是否隐藏观众拉流卡顿提示indicator，默认NO，即展示
@property (assign, nonatomic) BOOL livePushRestartAlertHidden; //是否隐藏直播重推alert，默认NO，即展示
@property (assign, nonatomic) BOOL livePushLoadingIndicatorHidden; //是否隐藏主播推流卡顿提示indicator，默认NO，即展示
@end

NS_ASSUME_NONNULL_END
