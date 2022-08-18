//
//  ASLRBLiveRoomUserModel.h
//  AliStandardLiveRoomBundle
//
//  Created by fernando on 2021/12/15.
//

#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

@interface ASLRBLiveRoomUserModel : NSObject
/**
 * 用户id
 */
@property (nonatomic, nonnull) NSString * openID;

/**
 * 用户昵称
 */
@property (nonatomic, nonnull) NSString * nick;

/**
 * 用户角色
 */
@property (nonatomic, nonnull) NSString * role;

/**
 * 用户扩展信息
 */
@property (nonatomic, nonnull) NSDictionary<NSString *, NSString *> * extension;
@end

NS_ASSUME_NONNULL_END
