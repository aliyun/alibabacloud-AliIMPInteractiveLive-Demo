//
//  MELLivePlayBottomViews.h
//  AliStandardLiveUIKit
//
//  Created by fernando on 2022/6/6.
//

#import <UIKit/UIKit.h>
#import "MELLiveRoomBottomViewStatus.h"

NS_ASSUME_NONNULL_BEGIN

@class MELLikeButton;

@interface MELLivePlayBottomViews : UIView
@property (assign, nonatomic) MELLiveRoomBottomViewStatus status;
@property (strong, nonatomic) UITextField* sendCommentField;
@property (strong, nonatomic) UIButton* shareButton;
@property (strong, nonatomic) MELLikeButton* likeButton;
@property (assign, nonatomic) int32_t likeCount;
@property (strong, nonatomic) UIButton* giftButton;
@property (assign, nonatomic) BOOL allCommentBanned;
@property (assign, nonatomic) BOOL yourCommentBanned;

@property (copy, nonatomic) void(^onLikeSent)(void);
@property (copy, nonatomic) void(^onGiftSent)(void);
@property (copy, nonatomic) void(^onShare)(void);
@property (copy, nonatomic) void(^onCommentSent)(NSString* comment);

-(instancetype)initWithStatus:(MELLiveRoomBottomViewStatus)status;
@end

NS_ASSUME_NONNULL_END
