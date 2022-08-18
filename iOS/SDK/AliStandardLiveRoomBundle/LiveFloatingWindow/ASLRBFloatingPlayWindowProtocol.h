//
//  ASLRBFloatingPlayWindowProtocol.h
//  AliStandardLiveRoomBundle
//
//  Created by fernando on 2022/3/9.
//

#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

@protocol ASLRBFloatingPlayWindowProtocol <NSObject>
@property (assign, nonatomic) CGRect initialFrame;
@property (assign, nonatomic) BOOL enableTappedToBack; //默认YES，支持点击小窗后返回直播间
@property (assign, nonatomic) CGFloat borderWidth; // 默认为2，
@property (assign, nonatomic) BOOL disappearAfterResignActive; // 默认YES，即切后台再回到前台后小窗会自动消失；
@end

NS_ASSUME_NONNULL_END
