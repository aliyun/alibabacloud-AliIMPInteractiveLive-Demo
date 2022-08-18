//
//  ASLRBLiveRoomMemberButton.h
//  AliStandardLiveRoomBundle
//
//  Created by fernando on 2022/3/24.
//

#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN

@interface ASLRBLiveRoomMemberButton : UIView
@property (strong, nonatomic) UIButton* memberHeaderImageButton;
@property (strong, nonatomic) UIButton* memberTextButton;
@property (strong, nonatomic) UIButton* memberDowndropFlagImageButton;
@property (copy, nonatomic) void(^onMemberButtonClicked)(void);
@end

NS_ASSUME_NONNULL_END
