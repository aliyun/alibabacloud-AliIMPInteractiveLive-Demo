//
//  AIRBDAudienceView.h
//  AliInteractiveRoomBundleDemo
//
//  Created by fernando on 2021/5/14.
//  Copyright © 2021 AliYun. All rights reserved.
//

#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN

@interface AIRBDAudienceView : UIView

- (void) enterRoomWithID:(NSString*)roomID userID:(nonnull NSString *)userID;
- (void) leaveRoom;
@property(strong,nonatomic)UINavigationController* exitActionDelegate;
@end

NS_ASSUME_NONNULL_END
