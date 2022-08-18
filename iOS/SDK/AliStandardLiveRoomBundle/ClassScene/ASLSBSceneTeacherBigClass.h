//
//  ASLSBSceneTeacherBigClass.h
//  AliStandardClassroomBundle
//
//  Created by 刘再勇 on 2021/11/19.
//

#import "ASLSBSceneTeacher.h"
#import "LinkMic/ASLSBLinkMicProtocol.h"
#import "LinkMic/ASLSBLinkMicDelegate.h"
#import "ASLSBPusherEngine.h"

NS_ASSUME_NONNULL_BEGIN

@interface ASLSBSceneTeacherBigClass : ASLSBSceneTeacher<ASLSBLinkMicProtocol, ASLSBLinkMicDelegate>

@property(nonatomic, strong) ASLSBPusherEngine* pusherEngine;

@end


NS_ASSUME_NONNULL_END
