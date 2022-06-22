//
//  MELVideoPlayInteractionViewController.h
//  AliStandardLiveUIKit
//
//  Created by fernando on 2022/6/8.
//

#import <UIKit/UIKit.h>
#import "MELLiveRoomBottomViewStatus.h"

NS_ASSUME_NONNULL_BEGIN

@class ASLRBLiveCommentModel;
@class MELLivePlayBottomViews;
@class MELInteractiveMessageViewsHolder;

@interface MELVideoPlayInteractionViewController : UIViewController
@property (strong, nonatomic) MELLivePlayBottomViews* livePlayBottomViews;
@property (strong, nonatomic) MELInteractiveMessageViewsHolder* interactiveMessageView;
@property (copy, nonatomic) void(^onLikeButtonClicked)(void);
@property (copy, nonatomic) void(^onGiftButtonClicked)(void);
@property (copy, nonatomic) void(^onShareButtonClicked)(void);
@property (copy, nonatomic) void(^onCommentSent)(NSString* comment);
@property (assign, nonatomic) MELLiveRoomBottomViewStatus status;

- (void)insertLiveComment:(ASLRBLiveCommentModel*)model;
- (void)updateLikeCount:(int32_t)count;
@end

NS_ASSUME_NONNULL_END
