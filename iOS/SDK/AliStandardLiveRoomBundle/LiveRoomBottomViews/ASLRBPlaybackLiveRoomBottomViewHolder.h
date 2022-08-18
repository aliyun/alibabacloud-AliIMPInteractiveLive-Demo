//
//  ASLRBPlaybackLiveRoomBottomViewHolder.h
//  AliStandardLiveRoomBundle
//
//  Created by fernando on 2021/11/16.
//

#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN

@protocol ASLRBLiveRoomBottomViewActionsDelegate, ASLRBLiveRoomBottomViewsHolderProtocol;

@interface ASLRBPlaybackLiveRoomBottomViewHolder : UIView<ASLRBLiveRoomBottomViewsHolderProtocol>
@property (weak, nonatomic) id<ASLRBLiveRoomBottomViewActionsDelegate> actionsDelegate;
@end

NS_ASSUME_NONNULL_END
