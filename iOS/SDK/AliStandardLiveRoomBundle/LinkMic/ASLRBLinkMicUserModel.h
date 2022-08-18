//
//  ASLRBLinkMicUserModel.h
//  AliStandardLiveRoomBundle
//
//  Created by 刘再勇 on 2022/1/13.
//

#import <Foundation/Foundation.h>
#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN

@interface ASLRBLinkMicUserModel : NSObject

/**
 * 成员的用户Id
 */
@property (nonatomic, copy) NSString* userID;

/**
 * 摄像头是否开启
 */
@property (nonatomic, assign) BOOL isCameraOpened;

/**
 * 麦克风是否开启
 */
@property (nonatomic, assign) BOOL isMicOpened;

/**
 * 是否是主播
 */
@property (nonatomic, assign) BOOL isAnchor;

/**
 * 相机流画面
 */
@property (nonatomic, strong) UIView* cameraView;

/**
 * 成员的昵称
 */
@property (nonatomic, copy) NSString* nickname;

@end

NS_ASSUME_NONNULL_END
