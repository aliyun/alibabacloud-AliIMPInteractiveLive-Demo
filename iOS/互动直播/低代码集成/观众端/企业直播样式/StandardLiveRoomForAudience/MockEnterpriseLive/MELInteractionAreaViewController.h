//
//  MELInteractionAreaViewController.h
//  AliStandardLiveUIKit
//
//  Created by fernando on 2022/6/8.
//

#import <UIKit/UIKit.h>
#import "MELLiveRoomBottomViewStatus.h"

NS_ASSUME_NONNULL_BEGIN
@class ASLRBLiveCommentModel;
@interface MELInteractionAreaViewController : UIViewController
@property (copy, nonatomic) void(^onLikeButtonClicked)(void);
@property (copy, nonatomic) void(^onGiftButtonClicked)(void);
@property (copy, nonatomic) void(^onShareButtonClicked)(void);
@property (copy, nonatomic) void(^onCommentSent)(NSString* comment);
@property (copy, nonatomic) void(^onSubscribe)(void(^didSubscribed)(BOOL subscribed));
@property (assign, nonatomic) MELLiveRoomBottomViewStatus status;
@property (strong, nonatomic) NSURL* anchorAvartarImageURL;
@property (copy, nonatomic) NSString* anchorIntroduction;
@property (copy, nonatomic) NSString* liveIntroduction;
@property (copy, nonatomic) NSString* anchorNick;

- (void)insertLiveComment:(ASLRBLiveCommentModel*)model;
- (void)updateLikeCount:(int32_t)count;
@end

NS_ASSUME_NONNULL_END
