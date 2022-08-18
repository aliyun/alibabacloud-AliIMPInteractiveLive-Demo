//
//  ASLRBAudienceLiveRoomBottomViewHolder.h
//  AliStandardLiveRoomBundle
//
//  Created by fernando on 2021/11/16.
//

#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN

@protocol ASLRBLiveRoomBottomViewActionsDelegate;
@protocol ASLRBLiveRoomBottomViewsHolderProtocol;

@interface ASLRBAudienceLiveRoomBottomViewHolder : UIView<ASLRBLiveRoomBottomViewsHolderProtocol>
@property (weak, nonatomic) id<ASLRBLiveRoomBottomViewActionsDelegate> actionsDelegate;
- (void)updateLayoutRotated:(BOOL)rotated;

@end

NS_ASSUME_NONNULL_END
