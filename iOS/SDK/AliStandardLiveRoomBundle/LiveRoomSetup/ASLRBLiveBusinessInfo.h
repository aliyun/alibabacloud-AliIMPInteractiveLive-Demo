//
//  ASLRBLiveBusinessInfo.h
//  AliStandardLiveRoomBundle
//
//  Created by fernando on 2021/12/14.
//

#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

@interface ASLRBLiveBusinessInfo : NSObject
/**
 *@brief 本场直播的标题，非必传，只有主播侧设置有效
 */
@property (nonatomic, copy) NSString* liveTitle;

/**
 *@brief 本场直播的公告，非必传，只有主播侧设置有效
 */
@property (nonatomic, copy) NSString* liveNotice;

/**
 *@brief 本场直播的封面图地址，非必传，只有主播侧设置有效
 */
@property (nonatomic, copy) NSString* liveCoverURL;

/**
 *@brief 本场直播的自定义数据，非必传，只有主播侧设置有效
 */
@property (nonatomic, copy) NSDictionary<NSString*,NSString*>* liveCustomData;
@end

NS_ASSUME_NONNULL_END
