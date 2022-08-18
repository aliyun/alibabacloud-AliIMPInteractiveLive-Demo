//
//  ASLSBSceneTeacher.h
//  AliStandardClassroomBundle
//
//  Created by 刘再勇 on 2021/11/19.
//

#import "ASLSBScene.h"

NS_ASSUME_NONNULL_BEGIN

@interface ASLSBSceneTeacher : ASLSBScene

- (void) startClass;
- (void) endClass;

- (void) muteLocalMicphone:(BOOL)mute;
- (void) muteLocalCamera:(BOOL)mute;

- (void) startRecord;
- (void) endRecord;
- (void) pauseRecord;
- (void) resumeRecord;

@end

NS_ASSUME_NONNULL_END
