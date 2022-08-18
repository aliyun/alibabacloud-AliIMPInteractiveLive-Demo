//
//  ASLRBAnchorLiveRoomBottomViewHolder.h
//  AliStandardLiveRoomBundle
//
//  Created by fernando on 2021/11/16.
//

#import <UIKit/UIKit.h>

@protocol ASLRBLiveRoomBottomViewActionsDelegate;

NS_ASSUME_NONNULL_BEGIN

@interface ASLRBAnchorLiveRoomBottomViewHolder : UIView
@property (weak, nonatomic) id<ASLRBLiveRoomBottomViewActionsDelegate> actionsDelegate;
- (void)updateLayoutRotated:(BOOL)rotated;
@end

NS_ASSUME_NONNULL_END
