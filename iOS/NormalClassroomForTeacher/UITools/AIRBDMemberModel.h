//
//  AIRBDMemberModel.h
//  AliInteractiveRoomDemo
//
//  Created by 麦辣 on 2021/6/8.
//

#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN


typedef enum : NSUInteger {
    OnChatting,
    WaitForChatting,
    NoChatting,
} ChattingState;

@interface AIRBDMemberModel : NSObject

@property(copy,nonatomic)NSURL* imageURL;
@property(copy,nonatomic)NSString* name;
@property(assign,nonatomic)ChattingState chattingstate;
@property(assign,nonatomic)BOOL isMuted;

@end

NS_ASSUME_NONNULL_END
