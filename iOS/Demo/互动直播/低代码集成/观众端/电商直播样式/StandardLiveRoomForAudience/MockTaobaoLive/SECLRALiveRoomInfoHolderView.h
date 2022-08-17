//
//  ASLRBLiveRoomInfoHolderView.h
//  AliStandardLiveRoomBundle
//
//  Created by fernando on 2021/11/17.
//

#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN


@interface SECLRALiveRoomInfoHolderView : UIView
@property (strong, nonatomic) UILabel* anchorNickLabel;
@property (strong, nonatomic) UIImageView* anchorAvatarView;
@property (strong, nonatomic) UILabel* pvLabel;
@property (strong, nonatomic) UIButton* followButton;

- (void) updateLikeCount:(int32_t)count;
- (void) updatePV:(int32_t)pv;
- (void) follow;
@end

NS_ASSUME_NONNULL_END
