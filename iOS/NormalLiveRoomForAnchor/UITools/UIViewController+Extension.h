//
//  UIViewController+Extension.h
//  AliInteractiveRoomDemo
//
//  Created by 麦辣 on 2021/7/8.
//

#import <UIKit/UIKit.h>

typedef NS_ENUM(NSInteger, AIRBDViewControllerPresentDirection)
{
    AIRBDViewControllerPresentFromBottom = 0,
    AIRBDViewControllerPresentFromLeft,
    AIRBDViewControllerPresentFromTop,
    AIRBDViewControllerPresentFromRight
};

@interface UIViewController(Extension)

@property (nonatomic,strong)UIViewController* presentingChildViewController;
@property (nonatomic,strong)UIViewController* presentedChildViewController;
@property (nonatomic,strong)UIViewController* dismissChildViewController;
@property (nonatomic, assign) AIRBDViewControllerPresentDirection presentingChildViewControllerDirection;

-(void) presentChildViewController:(UIViewController*)viewController animated:(BOOL)animated presentedFrame:(CGRect)frame;
-(void) dismissChildViewController:(UIViewController*)viewController animated:(BOOL)animated;

-(void) presentChildViewController:(UIViewController*)viewController animated:(BOOL)animated presentedFrame:(CGRect)frame direction:(AIRBDViewControllerPresentDirection)direction;
-(void) dismissChildViewController:(UIViewController*)viewController animated:(BOOL)animated direction:(AIRBDViewControllerPresentDirection)direction;


@end


