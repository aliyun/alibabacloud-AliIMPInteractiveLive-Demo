//
//  ASLSBLinkMicUserModel.h
//  AliStandardClassroomBundle
//
//  Created by 刘再勇 on 2022/1/13.
//

#import <Foundation/Foundation.h>
#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN

@interface ASLSBLinkMicUserModel : NSObject<NSCopying>

@property (nonatomic, copy) NSString* userID;

@property (nonatomic, assign) BOOL isCameraOpened;

@property (nonatomic, assign) BOOL isMicOpened;

@property (nonatomic, assign) BOOL isAnchor;

@property (nonatomic, strong) UIView* cameraView;

@property (nonatomic, copy) NSString* nickname;

@property (nonatomic, assign) BOOL isScreenSharing;

@property (nonatomic, strong) UIView* screenView;

@end

NS_ASSUME_NONNULL_END
