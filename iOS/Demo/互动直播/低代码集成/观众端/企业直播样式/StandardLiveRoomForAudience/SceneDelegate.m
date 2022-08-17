//
//  SceneDelegate.m
//  StandardLiveRoomForAudience
//
//  Created by fernando on 2022/2/18.
//

#import "SceneDelegate.h"
#import "ASLUKEnterpriseLiveRoomWrapper.h"
#import "AIRBDEnvironments.h"

@interface SceneDelegate ()
@property (strong, nonatomic) ASLUKEnterpriseLiveRoomWrapper* liveRoomWrapper;
@end

@implementation SceneDelegate


- (void)scene:(UIScene *)scene willConnectToSession:(UISceneSession *)session options:(UISceneConnectionOptions *)connectionOptions {
    // Use this method to optionally configure and attach the UIWindow `window` to the provided UIWindowScene `scene`.
    // If using a storyboard, the `window` property will automatically be initialized and attached to the scene.
    // This delegate does not imply the connecting scene or session are new (see `application:configurationForConnectingSceneSession` instead).
    
    _liveRoomWrapper = [[ASLUKEnterpriseLiveRoomWrapper alloc] init];
    
    __weak typeof(self) weakSelf = self;
    [_liveRoomWrapper setupLiveRoomWithAppID:[AIRBDEnvironments shareInstance].interactiveLiveRoomAppID
                                      appKey:[AIRBDEnvironments shareInstance].interactiveLiveRoomAppKey
                                appServerUrl:[AIRBDEnvironments shareInstance].appServerUrl
                             appServerSecret:[AIRBDEnvironments shareInstance].signSecret
                                      liveID:@"376518bf-3bda-4555-bd2c-7bc95afb15bf"
                                      userID:@"XXXXdfa"
                                    userNick:@"nicksfda"
                                   onSuccess:^(UIViewController * _Nonnull liveRoomViewController) {
        dispatch_async(dispatch_get_main_queue(), ^{
            UINavigationController *mainNavigationController = [[UINavigationController alloc] initWithRootViewController:liveRoomViewController];
            mainNavigationController.navigationBarHidden = YES;
            mainNavigationController.navigationBar.translucent = NO;
            weakSelf.window.backgroundColor = [UIColor whiteColor];
            weakSelf.window.rootViewController = mainNavigationController;
            [weakSelf.window makeKeyAndVisible];
        });
    }
                                   onFailure:^(NSString * _Nonnull errorMessage) {
        NSLog(@"");
    }];
    
    
    
//    AIRBDLiveRoomViewController* mainViewController = [[AIRBDLiveRoomViewController alloc] initWithUserID:@"xxxs" liveId:@"3c1d98fa-ee8a-4820-8880-e5a17e5373dc" role:AIRBDLiveRoomUserRoleAudience title:@"测试直播"];
    
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
