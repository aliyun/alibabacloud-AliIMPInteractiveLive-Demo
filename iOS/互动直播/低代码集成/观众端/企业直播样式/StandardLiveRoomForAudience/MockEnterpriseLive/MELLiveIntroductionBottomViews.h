//
//  MELLiveIntroductionBottomViews.h
//  AliStandardLiveUIKit
//
//  Created by fernando on 2022/6/6.
//

#import <UIKit/UIKit.h>
#import "MELLiveRoomBottomViewStatus.h"

NS_ASSUME_NONNULL_BEGIN

@interface MELLiveIntroductionBottomViews : UIView
@property (assign, nonatomic) MELLiveRoomBottomViewStatus status;
@property (strong, nonatomic) UIButton* subscribeButton;
@property (strong, nonatomic) UIButton* shareButton;

@property (copy, nonatomic) void(^onShare)(void);
@property (copy, nonatomic) void(^onSubscribe)(void(^didSubscribed)(BOOL subscribed));
@end

NS_ASSUME_NONNULL_END
