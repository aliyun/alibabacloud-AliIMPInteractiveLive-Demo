//
//  ASLRBLiveSystemLabel.h
//  AliStandardLiveRoomBundle
//
//  Created by fernando on 2021/10/15.
//

#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN

@class ASLRBLiveSystemMessageModel;

@protocol ASLRBLiveSystemMessageLabelDelegate <NSObject>

//-(void) actionWhenSystemMessageJustAboutToPresent:(ASLRBLiveSystemMessageModel*)model;
@end

@interface ASLRBLiveSystemMessageLabel : UILabel
@property (assign, atomic) BOOL canPresenting;
@property (weak, nonatomic) id<ASLRBLiveSystemMessageLabelDelegate> delegate;

- (void) insertLiveSystemMessage:(ASLRBLiveSystemMessageModel*)model;
- (void) stopPresenting;
@end

NS_ASSUME_NONNULL_END
