//
//  ASLRBLiveRoomBottomViewsHolderProtocol.h
//  AliStandardLiveRoomBundle
//
//  Created by fernando on 2021/11/16.
//

#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

@protocol ASLRBLiveRoomBottomViewsHolderProtocol <NSObject>


@property (strong, nonatomic) UITextField* commentInputField;

@property (strong, nonatomic) UIButton* shareButton;

@property (strong, nonatomic) UIButton* likeButton;

@property (strong, nonatomic) UIButton* beautyButton;

@property (strong, nonatomic) UIButton* moreInteractionButton;

@end

NS_ASSUME_NONNULL_END
