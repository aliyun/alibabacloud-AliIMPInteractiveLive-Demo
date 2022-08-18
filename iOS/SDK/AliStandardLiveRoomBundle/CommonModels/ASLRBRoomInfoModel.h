//
//  AIRBDRoomInfoModel.h
//  AliInteractiveLiveDemo
//
//  Created by 麦辣 on 2021/6/18.
//

#import <Foundation/Foundation.h>
#import <AliInteractiveRoomBundle/AliInteractiveRoomBundle.h>
NS_ASSUME_NONNULL_BEGIN

@interface ASLRBLiveInfoModel : NSObject

//@property (copy, nonatomic) NSString* userID;

@property (copy, nonatomic) NSString* userImg;

@property (copy, nonatomic) NSString* title;

@property (copy, nonatomic) NSString* notice;

@property (strong, nonatomic) UIImage* avatar;

@property (copy, nonatomic) NSString* roomID;

@property (copy, nonatomic) NSString* anchorID;

@property (copy, nonatomic) NSString* userNickName;

@property (copy, nonatomic) NSString* anchorNick;

//@property (copy, nonatomic) NSString* hostName;

@property (assign, atomic) int onlineCount;

@property (assign, atomic) int likeCount;

@property (assign, atomic) int uv;

@property (assign, atomic) int pv;

//@property (strong, nonatomic) AIRBRoomEngineConfig* config;

@property (strong, nonatomic) AIRBLivePusherFaceBeautyOptions* beautyOptions;

@end

NS_ASSUME_NONNULL_END
