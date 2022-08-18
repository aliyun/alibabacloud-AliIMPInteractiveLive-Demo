//
//  ASLSBSceneUserModel.h
//  AliInteractiveRoomDemo
//
//  Created by 麦辣 on 2021/7/14.
//

#import <Foundation/Foundation.h>
#import "ASLSBCommonDefines.h"

NS_ASSUME_NONNULL_BEGIN

@interface ASLSBSceneUserModel : NSObject

@property (nonatomic, copy) NSString *userId;

@property (nonatomic, copy) NSString *nick;

@property (nonatomic, assign) ASCRBClassroomRole role;

/**
 *用户相关的自定义扩展信息
 */
@property (nonatomic, copy) NSDictionary<NSString*,NSString*>* extension;

@end

NS_ASSUME_NONNULL_END
