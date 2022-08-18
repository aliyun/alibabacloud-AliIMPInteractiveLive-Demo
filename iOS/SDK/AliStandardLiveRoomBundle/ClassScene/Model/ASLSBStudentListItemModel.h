//
//  ASLSBStudentListItemModel.h
//  AliInteractiveRoomDemo
//
//  Created by fernando on 2021/6/7.
//

#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

typedef NS_ENUM(NSInteger, ASCRBStudentStatus)
{
    ASCRBStudentStatusReadyForCalled = 0, //已进入房间，随时可被呼叫
    ASCRBStudentStatusAlreadyOnTheCall, //已在RTC中（麦克风打开）
    ASCRBStudentStatusAlreadyOnTheCallButMicMuted, //已在RTC中（麦克风关闭）
    ASCRBStudentStatusNowBeenCalling, //正在被呼叫
    ASCRBStudentStatusNowApplying, // 正在申请加入RTC
    
    ASCRBStudentStatusAlreadyLeftRoom, // 已离开房间
};

@interface ASLSBStudentListItemModel : NSObject

@property (copy, nonatomic) NSString* userID;
@property (copy, nonatomic) NSString* userNick;
@property (assign, nonatomic) ASCRBStudentStatus status;

@end

NS_ASSUME_NONNULL_END
