//
//  AIRBDAbilityDemonstrationViewController.h
//  AliInteractiveRoomDemo
//
//  Created by fernando on 2021/10/9.
//

#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN

@protocol AIRBDAbilityDemonstrationVCProtocol <NSObject>
- (void) onShopWindowItemClicked;
- (void) onGoodsCardItemClicked;
@end

@interface AIRBDAbilityDemonstrationViewController : UIViewController
@property (weak, nonatomic) id<AIRBDAbilityDemonstrationVCProtocol> delegate;
@end

NS_ASSUME_NONNULL_END
