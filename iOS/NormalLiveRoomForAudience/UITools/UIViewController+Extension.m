//
//  UIViewController+Extension.m
//  AliInteractiveRoomDemo
//
//  Created by 麦辣 on 2021/7/8.
//

#import "UIViewController+Extension.h"
#import <objc/runtime.h>

static AIRBDViewControllerPresentDirection s_AIRBDPresentingChildViewControllerDirection;

@implementation UIViewController(Extension)

- (UIViewController *)presentedChildViewController{
    return objc_getAssociatedObject(self, @"presentedChildViewController");
}

- (void)setPresentedChildViewController:(UIViewController *)presentedChildViewController{
    objc_setAssociatedObject(self, @"presentedChildViewController", presentedChildViewController, OBJC_ASSOCIATION_RETAIN_NONATOMIC);
}

- (UIViewController *)presentingChildViewController{
    return objc_getAssociatedObject(self, @"presentingChildViewController");
}

- (void)setPresentingChildViewController:(UIViewController *)presentingChildViewController{
    objc_setAssociatedObject(self, @"presentingChildViewController", presentingChildViewController, OBJC_ASSOCIATION_RETAIN_NONATOMIC);
}

- (AIRBDViewControllerPresentDirection)presentingChildViewControllerDirection {
    return s_AIRBDPresentingChildViewControllerDirection;
}

- (void)setPresentingChildViewControllerDirection:(AIRBDViewControllerPresentDirection)presentingChildViewControllerDirection {
    s_AIRBDPresentingChildViewControllerDirection = presentingChildViewControllerDirection;
}

- (UIViewController *)dismissChildViewController{
    return objc_getAssociatedObject(self, @"dismissChildViewController");
}

- (void)setDismissChildViewController:(UIViewController *)dismissChildViewController{
    objc_setAssociatedObject(self, @"dismissChildViewController", dismissChildViewController, OBJC_ASSOCIATION_RETAIN_NONATOMIC);
}



- (void)presentChildViewController:(UIViewController *)viewController animated:(BOOL)animated presentedFrame:(CGRect)presentedframe{
    //一次只能展示一个childViewController
    if(viewController == nil){
        return;
    }
    if(self.presentingChildViewController){
        [self dismissChildViewController:self.presentingChildViewController animated:YES];
        return;
    }
    self.presentingChildViewController = viewController;
    [self.view addSubview:viewController.view];
    CGRect originRect = CGRectMake(presentedframe.origin.x, [UIScreen mainScreen].bounds.size.height, presentedframe.size.width, presentedframe.size.height);
    viewController.view.frame = originRect;
    viewController.view.layer.masksToBounds = YES;
    viewController.view.layer.cornerRadius = 10;
    if(animated){
        [UIView animateWithDuration:0.2 animations:^{
            viewController.view.frame = presentedframe;
        }];
    }else{
        viewController.view.frame = presentedframe;
    }
    [self addChildViewController:viewController];
    [viewController didMoveToParentViewController:self];
    
}

-(void) presentChildViewController:(UIViewController*)viewController
                          animated:(BOOL)animated
                    presentedFrame:(CGRect)frame
                         direction:(AIRBDViewControllerPresentDirection)direction {
    if(viewController == nil){
        return;
    }
    if(self.presentingChildViewController) {
        [self dismissChildViewController:self.presentingChildViewController animated:YES direction:self.presentingChildViewControllerDirection];
        return;
    }
    self.presentingChildViewController = viewController;
    self.presentingChildViewControllerDirection = direction;
    [self.view addSubview:viewController.view];
    CGRect originRect = CGRectMake(frame.origin.x, [UIScreen mainScreen].bounds.size.height, frame.size.width, frame.size.height);
    if (direction == AIRBDViewControllerPresentFromRight) {
        originRect.origin.x = [UIScreen mainScreen].bounds.size.width;
        originRect.origin.y = frame.origin.y;
    } else if (direction == AIRBDViewControllerPresentFromTop) {
        originRect.origin.x = frame.origin.x;
        originRect.origin.y = -1 * frame.size.height;
    } else if (direction == AIRBDViewControllerPresentFromLeft) {
        originRect.origin.x = -1 * frame.size.width;
        originRect.origin.y = frame.origin.y;
    }
    viewController.view.frame = originRect;
    viewController.view.layer.masksToBounds = YES;
    viewController.view.layer.cornerRadius = 10;
    if(animated){
        [UIView animateWithDuration:0.2 animations:^{
            viewController.view.frame = frame;
        }];
    }else{
        viewController.view.frame = frame;
    }
    [self addChildViewController:viewController];
    [viewController didMoveToParentViewController:self];
}

- (void)dismissChildViewController:(UIViewController *)viewController animated:(BOOL)animated{
    if(viewController == nil){
        return;
    }
    CGRect endRect = viewController.view.frame;
    endRect.origin.y = [UIScreen mainScreen].bounds.size.height;
    [viewController willMoveToParentViewController:nil];
    if(animated){
        [UIView animateWithDuration:0.2 animations:^{
            viewController.view.frame = endRect;
        } completion:^(BOOL finished) {
            [viewController.view removeFromSuperview];
            [viewController removeFromParentViewController];
        }];
    }else{
        viewController.view.frame = endRect;
        [viewController.view removeFromSuperview];
        [viewController removeFromParentViewController];
    }
    self.presentingChildViewController = nil;
}

-(void) dismissChildViewController:(UIViewController*)viewController
                          animated:(BOOL)animated
                         direction:(AIRBDViewControllerPresentDirection)direction {
    if(viewController == nil){
        return;
    }
    CGRect endRect = viewController.view.frame;
    if (direction == AIRBDViewControllerPresentFromRight) {
        endRect.origin.x = [UIScreen mainScreen].bounds.size.width;
        endRect.origin.y = viewController.view.frame.origin.y;
    } else if (direction == AIRBDViewControllerPresentFromTop) {
        endRect.origin.x = viewController.view.frame.origin.x;
        endRect.origin.y = -1 * viewController.view.frame.size.height;
    } else if (direction == AIRBDViewControllerPresentFromLeft) {
        endRect.origin.x = -1 * viewController.view.frame.size.width;
        endRect.origin.y = viewController.view.frame.origin.y;
    }
    [viewController willMoveToParentViewController:nil];
    if(animated){
        [UIView animateWithDuration:0.2 animations:^{
            viewController.view.frame = endRect;
        } completion:^(BOOL finished) {
            [viewController.view removeFromSuperview];
            [viewController removeFromParentViewController];
        }];
    }else{
        viewController.view.frame = endRect;
        [viewController.view removeFromSuperview];
        [viewController removeFromParentViewController];
    }
    self.presentingChildViewController = nil;
}

@end
