//
//  ASLRBFloatingPlayWindow.h
//  AliStandardLiveRoomBundle
//
//  Created by fernando on 2022/3/9.
//

#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN

@protocol ASLRBFloatingPlayWindowDelegate <NSObject>
- (void) onASLRBFloatingPlayWindowTapped;
- (void) onASLRBFloatingPlayWindowExited;
@end

@interface ASLRBFloatingPlayWindow : UIView
@property (weak, nonatomic) id<ASLRBFloatingPlayWindowDelegate> delegate;
@property (weak, nonatomic) UIView* playerView;
@property (weak, nonatomic) UIView* parentViewControllerView;
@property (assign, atomic) BOOL onFloatingMode;

- (void) enterFloatingMode:(BOOL)enter;
@end

NS_ASSUME_NONNULL_END
