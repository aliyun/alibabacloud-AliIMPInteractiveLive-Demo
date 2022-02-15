//
//  AIRBDStudentListItemModel.h
//  AliInteractiveRoomDemo
//
//  Created by fernando on 2021/6/7.
//

#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

typedef NS_ENUM(NSInteger, AIRBDStudentStatus)
{
    AIRBDStudentStatusReadyForCalled = 0, //随时可被呼叫
    AIRBDStudentStatusAlreadyOnTheCall, //已在RTC中
    AIRBDStudentStatusNowBeenCalling, //正在被呼叫
    AIRBDStudentStatusNowApplying // 已申请了进入RTC
    
};

@interface AIRBDStudentListItemModel : NSObject

@property (copy, nonatomic) NSString* userID;
@property (assign, nonatomic) AIRBDStudentStatus status; // 0 表示正在看直播，1 表示在RTC中， 2 表示正在被呼叫，3 表示正在申请

@end

NS_ASSUME_NONNULL_END
