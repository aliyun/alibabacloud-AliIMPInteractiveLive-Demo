//
//  ASLRBLiveRoomInfoViewsHolderProtocol.h
//  AliStandardLiveRoomBundle
//
//  Created by fernando on 2021/11/17.
//

#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

@protocol ASLRBLiveRoomInfoViewsHolderProtocol <NSObject>
@property (strong, nonatomic) UILabel* anchorNickLabel;
@property (strong, nonatomic) UIImageView* anchorAvatarView;
@property (strong, nonatomic) UILabel* pvLabel;
@property (strong, nonatomic) UILabel* likeCountLabel;

- (void) updateLikeCount:(int32_t)count;
- (void) updatePV:(int32_t)pv;
@end

NS_ASSUME_NONNULL_END
