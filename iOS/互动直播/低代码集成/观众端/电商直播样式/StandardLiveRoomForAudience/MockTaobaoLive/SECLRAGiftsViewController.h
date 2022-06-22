//
//  SECLRAGiftsViewController.h
//  AliLiveRoomUIForAudience
//
//  Created by fernando on 2022/5/11.
//

#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN

@protocol SECLRAGiftsDelegate <NSObject>

- (void)onRocketButtonClicked;

@end

@interface SECLRAGiftsViewController : UIViewController
@property (weak, nonatomic) id<SECLRAGiftsDelegate> delegate;
@end

NS_ASSUME_NONNULL_END
