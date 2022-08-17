//
//  SECLRABottomViewHolder.h
//  AliLiveRoomUIForAudience
//
//  Created by fernando on 2022/5/9.
//

#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN

@protocol SECLRABottomViewHolderDelegate <NSObject>

- (void)onLikeButtonClicked;
- (void)onShareButtonClicked;
- (void)onFloatingWindowButtonClicked;
- (void)onGoodsButtonClicked;
- (void)onMoreButtonClicked;
- (void)onCommentSent:(NSString*)comment;

@end

@interface SECLRABottomViewHolder : UIView
@property (weak, nonatomic) id<SECLRABottomViewHolderDelegate> delegate;
@property (strong, nonatomic) UIButton* goodsButton;
@property (strong, nonatomic) UITextField* sendCommentField;
@property (strong, nonatomic) UIButton* floatingWindowButton;
@property (strong, nonatomic) UIButton* shareButton;
@property (strong, nonatomic) UIButton* moreButton;
@property (strong, nonatomic) UIButton* likeButton;
@property (assign, nonatomic) int32_t likeCount;
@end

NS_ASSUME_NONNULL_END
