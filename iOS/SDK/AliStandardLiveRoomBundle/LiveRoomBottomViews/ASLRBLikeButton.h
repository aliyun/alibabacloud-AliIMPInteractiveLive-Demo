//
//  ASLRBLikeButton.h
//  AliStandardLiveRoomBundle
//
//  Created by fernando on 2021/11/16.
//

#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN

@interface ASLRBLikeButton : UIButton

@property (copy, nonatomic) void(^onLikeSent)(void);
@end

NS_ASSUME_NONNULL_END
