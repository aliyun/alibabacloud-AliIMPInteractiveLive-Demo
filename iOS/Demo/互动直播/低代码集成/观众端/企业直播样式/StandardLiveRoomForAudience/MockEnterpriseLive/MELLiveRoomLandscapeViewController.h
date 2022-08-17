//
//  MELLiveRoomLandscapeViewController.h
//  AliStandardLiveUIKit
//
//  Created by fernando on 2022/6/10.
//

#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN

typedef NS_ENUM(NSInteger, MELLiveRoomLandscapeViewStatus)
{
    MELLiveRoomLandscapeViewStatusStatusLiveNotStarted = 0,
    MELLiveRoomLandscapeViewStatusStatusLiveStarted
};

@class MELLikeButton;
@class MELLiveInfoViewHolder;
@class ASLRBLiveCommentModel;

@interface MELLiveRoomLandscapeViewController : UIViewController
@property (strong, nonatomic) MELLikeButton* likeButton;

@property (copy, nonatomic) void(^onLikeSend)(void);
@property (copy, nonatomic) void(^onBack)(void);
@property (copy, nonatomic) void(^onGiftSend)(void);
@property (copy, nonatomic) void(^onCommentSent)(NSString* comment);
@property (assign, nonatomic) int32_t likeCount;
@property (assign, nonatomic) MELLiveRoomLandscapeViewStatus status;
@property (copy, nonatomic) NSString* liveTitle;


- (void)insertLiveComment:(ASLRBLiveCommentModel*)model;
@end

NS_ASSUME_NONNULL_END
