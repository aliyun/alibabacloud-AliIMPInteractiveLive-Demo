//
//  AIRBDTeacherView.h
//  AliInteractiveRoomDemo
//
//  Created by fernando on 2021/5/31.
//

#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN

@class AIRBRoomEngineConfig;

@interface AIRBDTeacherView : UIView
- (void) createRoomWithConfig:(AIRBRoomEngineConfig*)config userID:(NSString*)userID completion:(void(^)(NSString* roomID))onGotRoomID;
-(void) startWithRoomID:(NSString*)roomID userID:(NSString*)userID;
- (void) stop;
@end

NS_ASSUME_NONNULL_END
