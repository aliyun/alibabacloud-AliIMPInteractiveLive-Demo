//
//  ASLRBLiveCommentViewConfig.h
//  AliStandardLiveRoomBundle
//
//  Created by fernando on 2022/3/31.
//

#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN

@interface ASLRBLiveCommentViewConfig : NSObject

/**
 * 进入直播间时展示的历史弹幕条数，默认0条，最多支持100条；
 */
@property (assign, nonatomic) int32_t countOfHistoryCommentsWhenEntered;

/**
 * 是否隐藏直播弹幕区域的所有view，默认NO，不隐藏
 */
@property (assign ,nonatomic) BOOL liveCommentViewHidden;

/**
 * 是否仅仅隐藏直播弹幕区域的未读消息提醒label，默认NO，不隐藏
 */
@property (assign ,nonatomic) BOOL liveUnreadCommentCountLabelHidden;

/**
 * 是否仅仅隐藏直播弹幕区域的系统消息提醒label，默认NO，不隐藏
 */
@property (assign ,nonatomic) BOOL liveSystemMessageLabelHidden;

/**
 * 进入直播间时的平台通告，默认是 "欢迎大家来到直播间！直播间内严禁出现违法违规、低俗色情、吸烟酗酒等内容，若有违规行为请及时举报。"
 */
@property (copy ,nonatomic) NSString* liveCommentWarningNotice;

/**
 * 进入直播间时的平台通告文本颜色，默认"#12DBE6"
 */
@property (strong ,nonatomic) UIColor* liveCommentWarningNoticeTextColor;

/**
 * 直播弹幕发送人昵称的文本的统一颜色
 * 默认是根据发送人nick hash出的随机颜色
 */
@property (strong, nonatomic) UIColor* defaultLiveCommentSenderColor;

/**
 * 直播弹幕内容的文本颜色
 * 默认是白色
 */
@property (strong, nonatomic) UIColor* defaultLiveCommentContentColor;

/**
 * 直播每条弹幕label的背景色，默认是"#000000"且alpha:0.3
 */
@property (strong, nonatomic) UIColor* liveCommentLabelBackgroundColor;

/**
 * 直播每条弹幕label文本的字体，默认是"PingFangSC-Regular"且size:14
 */
@property (strong, nonatomic) UIFont* liveCommentLabelTextFont;

/**
 * 直播每条弹幕label的圆角值，默认12.0；
 */
@property (assign, nonatomic) CGFloat liveCommentLabelCornerRadius;

/**
 * 直播每条弹幕label的背景色，默认是[UIColor colorWithRed:255/255.0 green:68/255.0 blue:44/255.0 alpha:1/1.0]
 */
@property (strong, nonatomic) UIColor* liveUnreadCommentCountLabelTextColor;

/**
 * 禁掉直播每条弹幕的长按手势的回调，默认NO，即支持长按手势回调
 */
@property (assign ,nonatomic) BOOL disableLiveCommentLongPressGesture;

/**
 * 禁掉直播每条弹幕的单击手势的回调，默认NO，即支持点击手势回调
 */
@property (assign ,nonatomic) BOOL disableLiveCommentTapGesture;
@end

NS_ASSUME_NONNULL_END
