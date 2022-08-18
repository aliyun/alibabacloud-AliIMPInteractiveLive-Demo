//
//  ASLRBLiveRestartPushAlertController.m
//  AliStandardLiveRoomBundle
//
//  Created by fernando on 2022/3/24.
//

#import "ASLRBLiveRestartPushAlertController.h"
#import "../CommonTools/UIColor+ColorWithHexString.h"


@interface ASLRBLiveRestartPushAlertController ()

@end

@implementation ASLRBLiveRestartPushAlertController

- (void)viewDidLoad {
    [super viewDidLoad];
    // Do any additional setup after loading the view.
}

+ (void)showAlertWithMessage:(NSString *)message
               restartAction:(void (^)(void))restartAction
                    parentVC:(UIViewController *)parentVC {
    
    void (^showAlert)(void) = ^{
        ASLRBLiveRestartPushAlertController* alertController = [ASLRBLiveRestartPushAlertController alertControllerWithTitle:nil message:message preferredStyle:UIAlertControllerStyleAlert];
        
        NSMutableAttributedString *alertControllerMessageStr = [[NSMutableAttributedString alloc] initWithString:message];
        [alertControllerMessageStr addAttribute:NSForegroundColorAttributeName value:[UIColor colorWithRed:0.0 green:0.0 blue:0.0 alpha:0.8] range:NSRangeFromString(message)];
        [alertControllerMessageStr addAttribute:NSFontAttributeName value:[UIFont systemFontOfSize:16] range:NSRangeFromString(message)];
        [alertController setValue:alertControllerMessageStr forKey:@"attributedMessage"];
        
        [alertController addAction:({
            UIAlertAction* action = [UIAlertAction actionWithTitle:@"确定" style:UIAlertActionStyleDefault handler:^(UIAlertAction * _Nonnull action) {
                restartAction();
            }];
            [action setValue:[UIColor colorWithHexString:@"#FB622B" alpha:1.0] forKey:@"titleTextColor"];
            action;
        })];
        
        [alertController addAction:({
            UIAlertAction* action = [UIAlertAction actionWithTitle:@"取消" style:UIAlertActionStyleCancel handler:^(UIAlertAction * _Nonnull action) {
            }];
            [action setValue:[UIColor colorWithRed:0.0 green:0.0 blue:0.0 alpha:0.6] forKey:@"titleTextColor"];
            action;
        })];
        [parentVC presentViewController:alertController animated:NO completion:nil];
    };
    
    void (^showAlertAfterDismissPreAlertIfNeeded)(void) = ^{
        if (!parentVC.presentedViewController) {
            showAlert();
        } else if (parentVC.presentedViewController && [parentVC.presentedViewController isKindOfClass:[ASLRBLiveRestartPushAlertController class]]){
            [parentVC.presentedViewController dismissViewControllerAnimated:NO completion:^{
                showAlert();
            }];
        }
    };
    
    if ([NSThread isMainThread]) {
        showAlertAfterDismissPreAlertIfNeeded();
    } else {
        dispatch_async(dispatch_get_main_queue(), ^{
            showAlertAfterDismissPreAlertIfNeeded();
        });
    }
}

@end
