//
//  ASLSBSceneModel.h
//  AliInteractiveRoomDemo
//
//  Created by 麦辣 on 2021/7/14.
//

#import <Foundation/Foundation.h>
//#import "ASLSBCommonDefines.h"

NS_ASSUME_NONNULL_BEGIN

@interface ASLSBSceneModel : NSObject

//@property (nonatomic, copy) NSString *classroomNumber;

@property (nonatomic, copy) NSString *bizType;

@property (nonatomic, copy) NSString *bizID;

@property (nonatomic, copy) NSString *classId;

@property (nonatomic, copy) NSString *liveId;

@property (nonatomic, copy) NSString *conferenceId;

@property (nonatomic, copy) NSString *whiteBoardId;

@property (nonatomic, copy) NSString *roomId;

@property (nonatomic, copy) NSString *roomOwnerId;

@property (nonatomic, copy) NSString *roomOwnerNick;

@property (nonatomic, copy) NSString *title;

@property (nonatomic, copy) NSString *notice;

@property (assign, atomic) int onlineCount;

@property (assign, atomic) int likeCount;

@property (assign, atomic) int uv;

@property (assign, atomic) int pv;

//@property (assign, nonatomic) ASCRBClassroomType type;

/**
 * 是否在上课中
 */
@property (assign, nonatomic) BOOL isInScene;

/**
 * 是否被单独禁言中（评论区）
 */
@property (assign, nonatomic) BOOL isUserCommentBanned;

/**
 * 是否在全体禁言中（评论区）
 */
@property (assign, nonatomic) BOOL isAllCommentBanned;

/**
 * 课程状态，0:未开始 1:上课中 2:已下课
 */
@property (assign, nonatomic) int32_t status;

/**
 * 开始上课时间点
 */
@property (nonatomic) int64_t sceneStartTime;

/**
 * 下课时间点
 */
@property (nonatomic) int64_t sceneEndTime;

@end

NS_ASSUME_NONNULL_END
