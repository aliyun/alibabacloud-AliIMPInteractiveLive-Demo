//
//  SECLRAGiftsViewController.h
//  AliLiveRoomUIForAudience
//
//  Created by fernando on 2022/5/11.
//

#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN

@protocol MELGiftsDelegate <NSObject>

- (void)onRocketButtonClicked;

@end

@interface MELGiftsViewController : UIViewController
@property (weak, nonatomic) id<MELGiftsDelegate> delegate;
@end

NS_ASSUME_NONNULL_END
