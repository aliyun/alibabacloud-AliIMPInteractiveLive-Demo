//
//  AIRBDStudentView.h
//  AliInteractiveRoomDemo
//
//  Created by fernando on 2021/5/24.
//

#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN

@interface AIRBDStudentView : UIView
-(void) startWithRoomID:(NSString*)roomID userID:(NSString*)userID roomOwnerID:(NSString*)roomOwnerID;
- (void) stop;
@end

NS_ASSUME_NONNULL_END
