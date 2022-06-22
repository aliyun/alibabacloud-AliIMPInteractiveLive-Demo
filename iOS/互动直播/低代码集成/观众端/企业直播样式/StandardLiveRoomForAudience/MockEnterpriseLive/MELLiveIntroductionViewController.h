//
//  MELLiveIntroductionViewController.h
//  AliStandardLiveUIKit
//
//  Created by fernando on 2022/6/8.
//

#import <UIKit/UIKit.h>
#import "MELLiveRoomBottomViewStatus.h"

NS_ASSUME_NONNULL_BEGIN

@interface MELLiveIntroductionViewController : UIViewController
@property (copy, nonatomic) void(^onShareButtonClicked)(void);
@property (copy, nonatomic) void(^onSubscribe)(void(^didSubscribed)(BOOL subscribed));
@property (assign, nonatomic) MELLiveRoomBottomViewStatus status;
@property (strong, nonatomic) NSURL* anchorAvartarImageURL;
@property (copy, nonatomic) NSString* anchorNick;
@property (copy, nonatomic) NSString* anchorIntroduction;
@property (copy, nonatomic) NSString* liveIntroduction;
@end

NS_ASSUME_NONNULL_END
