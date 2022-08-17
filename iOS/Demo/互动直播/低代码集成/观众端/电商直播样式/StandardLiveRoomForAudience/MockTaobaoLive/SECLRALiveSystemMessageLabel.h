//
//  ASLRBLiveSystemLabel.h
//  AliStandardLiveRoomBundle
//
//  Created by fernando on 2021/10/15.
//

#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN

@class ASLRBLiveSystemMessageModel;

@interface SECLRALiveSystemMessageLabel : UILabel

- (void) insertLiveSystemMessage:(ASLRBLiveSystemMessageModel*)model;
- (void) stopPresenting;
@end

NS_ASSUME_NONNULL_END
