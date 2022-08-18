//
//  ASLSBSceneInitConfig.h
//  AliStandardClassroomBundle
//
//  Created by 刘再勇 on 2022/2/16.
//

#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

@interface ASLSBSceneInitConfig : NSObject

/**
 * 本堂课程的ID，老师侧非必传，观众侧必传
 * @note 老师侧classID为空则会创建课程
 */
@property (nonatomic, copy) NSString* classID;

/**
 * 本堂课程的标题，非必传
 */
@property (nonatomic, copy) NSString* classTitle;

/**
 * 是否禁止录屏，非必传，默认为否
 */
@property (nonatomic, assign) BOOL screenSecure;

@end

NS_ASSUME_NONNULL_END
