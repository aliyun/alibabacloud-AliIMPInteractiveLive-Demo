//
//  SceneDelegate.m
//  StandardClassroomForStudents
//
//  Created by fernando on 2021/11/26.
//

#import "SceneDelegate.h"
#import "AIRBDClassroomStudent4iPhoneViewController.h"
#import "AIRBDClassroomStudent4iPadViewController.h"

@interface SceneDelegate ()

@end

@implementation SceneDelegate


- (void)scene:(UIScene *)scene willConnectToSession:(UISceneSession *)session options:(UISceneConnectionOptions *)connectionOptions {
    // Use this method to optionally configure and attach the UIWindow `window` to the provided UIWindowScene `scene`.
    // If using a storyboard, the `window` property will automatically be initialized and attached to the scene.
    // This delegate does not imply the connecti`ng scene or session are new (see `application:configurationForConnectingSceneSession` instead).
    
    // iPhone互动课堂低代码学生端样式
    AIRBDClassroomStudent4iPhoneViewController* mainViewController = [[AIRBDClassroomStudent4iPhoneViewController alloc] init];
//    // iPad互动课堂低代码学生端样式
//    AIRBDClassroomStudent4iPadViewController* mainViewController = [[AIRBDClassroomStudent4iPadViewController alloc] initWithUserID:@"xxxxxxx" userNick:@"xxxx的昵称" classID:@"e4359547-4e31-478c-9fca-ec93cbe71b3c"];
    UINavigationController *mainNavigationController = [[UINavigationController alloc] initWithRootViewController:mainViewController];
    mainNavigationController.navigationBarHidden = YES;
    mainNavigationController.navigationBar.translucent = NO;
    self.window.backgroundColor = [UIColor whiteColor];
    self.window.rootViewController = mainNavigationController;
    [self.window makeKeyAndVisible];
}


- (void)sceneDidDisconnect:(UIScene *)scene {
    // Called as the scene is being released by the system.
    // This occurs shortly after the scene enters the background, or when its session is discarded.
    // Release any resources associated with this scene that can be re-created the next time the scene connects.
    // The scene may re-connect later, as its session was not necessarily discarded (see `application:didDiscardSceneSessions` instead).
}


- (void)sceneDidBecomeActive:(UIScene *)scene {
    // Called when the scene has moved from an inactive state to an active state.
    // Use this method to restart any tasks that were paused (or not yet started) when the scene was inactive.
}


- (void)sceneWillResignActive:(UIScene *)scene {
    // Called when the scene will move from an active state to an inactive state.
    // This may occur due to temporary interruptions (ex. an incoming phone call).
}


- (void)sceneWillEnterForeground:(UIScene *)scene {
    // Called as the scene transitions from the background to the foreground.
    // Use this method to undo the changes made on entering the background.
}


- (void)sceneDidEnterBackground:(UIScene *)scene {
    // Called as the scene transitions from the foreground to the background.
    // Use this method to save data, release shared resources, and store enough scene-specific state information
    // to restore the scene back to its current state.
}


@end
