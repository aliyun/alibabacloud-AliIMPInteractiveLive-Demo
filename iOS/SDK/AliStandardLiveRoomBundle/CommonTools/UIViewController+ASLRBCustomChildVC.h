//
//  UIViewController+CustomChildVC.h
//  AliInteractiveRoomDemo
//
//  Created by 麦辣 on 2021/7/8.
//

#import <UIKit/UIKit.h>



typedef NS_ENUM(NSInteger, ASLRBViewControllerPresentDirection)
{
    ASLRBViewControllerPresentFromBottom = 0,
    ASLRBViewControllerPresentFromLeft,
    ASLRBViewControllerPresentFromTop,
    ASLRBViewControllerPresentFromRight
};

@interface UIViewController(ASLRBCustomChildVC)

@property (nonatomic, strong) UIViewController* presentingChildViewController;
@property (nonatomic, strong) UIViewController* presentedChildViewController;
@property (nonatomic, strong) UIViewController* dismissChildViewController;
@property (nonatomic, assign) ASLRBViewControllerPresentDirection presentingChildViewControllerDirection;

-(void) presentChildViewController:(UIViewController*)viewController
                          animated:(BOOL)animated
                    presentedFrame:(CGRect)frame
                         direction:(ASLRBViewControllerPresentDirection)direction;

-(void) dismissChildViewController:(UIViewController*)viewController
                          animated:(BOOL)animated;

@end


