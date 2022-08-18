//
//  ASLRBLiveRoomBottomViewActionsDelegate.h
//  AliStandardLiveRoomBundle
//
//  Created by fernando on 2021/11/16.
//

#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

@protocol ASLRBLiveRoomBottomViewActionsDelegate <NSObject>
- (void) onShareButtonClicked;
- (void) onBeautyButtonClicked;
- (void) onMoreInteractionButtonClicked;
- (void) onCommentSent:(NSString*)comment;
- (void) onLikeSent;
@end

NS_ASSUME_NONNULL_END
