//
//  AIRBDStudentListCell.h
//  AliInteractiveRoomDemo
//
//  Created by fernando on 2021/6/7.
//

#import <UIKit/UIKit.h>

#import "AIRBDStudentListItemModel.h"

NS_ASSUME_NONNULL_BEGIN

typedef NS_ENUM(NSInteger, AIRBDStudentItemAction)
{
    AIRBDStudentItemActionCall = 0, //呼叫
    AIRBDStudentItemActionHangup, //挂断
    AIRBDStudentItemActionCancelCall, //取消呼叫
    AIRBDStudentItemActionAccept, // 同意进入课堂
    AIRBDStudentItemActionReject // 拒绝进入课堂
};

@protocol AIRBDStudentListCellDelegate <NSObject>

- (void)onStudentActionButtonClickedWithUserID:(NSString*)userID action:(AIRBDStudentItemAction)action;

@end

@interface AIRBDStudentListCell : UITableViewCell
@property (weak, nonatomic) id<AIRBDStudentListCellDelegate> delegate;
@property (strong, nonatomic) UIButton* studentActionButton;
@property (strong, nonatomic) UIButton* studentActionButtonEX;
@property (strong, nonatomic) AIRBDStudentListItemModel* model;
@property (assign, nonatomic) int owner;//0为老师,1为学生
@end

NS_ASSUME_NONNULL_END
