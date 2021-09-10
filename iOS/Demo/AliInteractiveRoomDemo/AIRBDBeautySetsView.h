//
//  AIRBDBeautySetsView.h
//  AliInteractiveLiveDemo
//
//  Created by 麦辣 on 2021/6/21.
//

#import <UIKit/UIKit.h>
@class AIRBLivePusherFaceBeautyOptions;
@protocol BeautySetsDelegate <NSObject>

-(void)beautySetsValueChanged:(AIRBLivePusherFaceBeautyOptions* _Nonnull) beautyOptions;

@end

NS_ASSUME_NONNULL_BEGIN

@interface AIRBDBeautySetsView : UIScrollView
@property (strong, nonatomic) AIRBLivePusherFaceBeautyOptions* beautyOptions;
@property (weak, nonatomic) id<BeautySetsDelegate> beautySetsDelegate;
-(void)loadSubviews;
@end

NS_ASSUME_NONNULL_END
