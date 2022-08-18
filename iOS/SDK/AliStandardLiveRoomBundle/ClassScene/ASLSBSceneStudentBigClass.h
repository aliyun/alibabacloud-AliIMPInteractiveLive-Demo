//
//  ASLSBSceneStudentBigClass.h
//  AliStandardClassroomBundle
//
//  Created by 刘再勇 on 2021/11/19.
//

#import "ASLSBSceneStudent.h"
#import "LinkMic/ASLSBLinkMicProtocol.h"
#import "LinkMic/ASLSBLinkMicDelegate.h"

NS_ASSUME_NONNULL_BEGIN

@interface ASLSBSceneStudentBigClass : ASLSBSceneStudent<ASLSBLinkMicProtocol, ASLSBLinkMicDelegate>


@end

NS_ASSUME_NONNULL_END
