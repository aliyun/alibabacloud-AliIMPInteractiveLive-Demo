//
//  ASLRBLivePrestartViewsHolder.h
//  AliStandardLiveRoomBundle
//
//  Created by fernando on 2022/1/13.
//

#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN

@protocol ASLRBLivePrestartViewsHolderDelegate <NSObject>

- (void) onPrestartStartLiveButtonClicked:(NSString*)liveTitle;
- (void) onPrestartSwitchCameraButtonClicked;
- (void) onPrestartBeautyButtonClicked;
- (void) onPrestartExitButtonClicked;
@end

@interface ASLRBLivePrestartViewsHolder : UIView

@property (weak, nonatomic) id<ASLRBLivePrestartViewsHolderDelegate> delegate;
- (void) updateLayoutRotated:(BOOL)rotated;

@end

NS_ASSUME_NONNULL_END
