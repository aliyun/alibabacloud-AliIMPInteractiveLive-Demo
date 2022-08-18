//
//  ASLRBLiveCommentView.h
//  AliStandardLiveRoomBundle
//
//  Created by fernando on 2021/10/25.
//

#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN

@class ASLRBLiveCommentViewConfig;

@interface ASLRBLiveCommentView : UIView

@property (assign, nonatomic) BOOL showComment;
@property (assign, nonatomic) BOOL showLiveSystemMessage;

- (instancetype) initWithConfig:(ASLRBLiveCommentViewConfig*)config;
- (void) updateLayoutRotated:(BOOL)rotated;

@end

NS_ASSUME_NONNULL_END
