//
//  AppDelegate.m
//  AliInteractiveRoomDemo
//
//  Created by fernando on 2021/5/18.
//

#import "AppDelegate.h"
#import "AIRBDBigClassViewController.h"
#import "AIRBDLoginViewController.h"
@interface AppDelegate ()
@end

@implementation AppDelegate


- (BOOL)application:(UIApplication *)application didFinishLaunchingWithOptions:(NSDictionary *)launchOptions {
    // Override point for customization after application launch.
    if (@available(iOS 13.0, *)) {
    } else {
//        AIRBDBigClassViewController *mainViewController = [[AIRBDBigClassViewController alloc] init];
        AIRBDLoginViewController* mainViewController = [[AIRBDLoginViewController alloc]init];
        UINavigationController *mainNavigationController = [[UINavigationController alloc] initWithRootViewController:mainViewController];
        mainNavigationController.navigationBarHidden = YES;
        mainNavigationController.navigationBar.translucent = NO;
        self.window.backgroundColor = [UIColor whiteColor];
        self.window.rootViewController = mainNavigationController;
        [self.window makeKeyAndVisible];
    }
    return YES;
}


#pragma mark - UISceneSession lifecycle



- (UISceneConfiguration *)application:(UIApplication *)application configurationForConnectingSceneSession:(UISceneSession *)connectingSceneSession options:(UISceneConnectionOptions *)options {
    // Called when a new scene session is being created.
    // Use this method to select a configuration to create the new scene with.
    return [[UISceneConfiguration alloc] initWithName:@"Default Configuration" sessionRole:connectingSceneSession.role];
}


- (void)application:(UIApplication *)application didDiscardSceneSessions:(NSSet<UISceneSession *> *)sceneSessions {
    // Called when the user discards a scene session.
    // If any sessions were discarded while the application was not running, this will be called shortly after application:didFinishLaunchingWithOptions.
    // Use this method to release any resources that were specific to the discarded scenes, as they will not return.
}

- (void)applicationDidBecomeActive:(UIApplication *)application{
    
}

- (void)applicationDidEnterBackground:(UIApplication *)application{
    
}


@end
