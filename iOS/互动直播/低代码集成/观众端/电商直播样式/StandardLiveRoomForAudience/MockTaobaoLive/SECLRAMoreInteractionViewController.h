//
//  SECLRAMoreInteractionViewController.h
//  AliLiveRoomUIForAudience
//
//  Created by fernando on 2022/5/11.
//

#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN

@protocol SECLRAMoreInteractionDelegate <NSObject>

- (void)onGiftButtonClicked;

@end

@interface SECLRAMoreInteractionViewController : UIViewController
@property (weak, nonatomic) id<SECLRAMoreInteractionDelegate> delegate;
@end

NS_ASSUME_NONNULL_END
