//
//  AIRBDLiveRoomViewController.h
//  AliInteractiveRoomDemo
//
//  Created by 麦辣 on 2021/8/17.
//

#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN

typedef enum : NSUInteger {
    AIRBDLiveRoomUserRoleAnchor,
    AIRBDLiveRoomUserRoleAudience,
} AIRBDLiveRoomUserRole;

@interface AIRBDLiveRoomViewController : UIViewController

-(instancetype) initWithUserID:(NSString*)userID liveId:(NSString* _Nullable) liveID role:(AIRBDLiveRoomUserRole)role title:(NSString*)title;

@end

NS_ASSUME_NONNULL_END
