//
//  ASLRBLivePushStatusView.h
//  AliStandardLiveRoomBundle
//
//  Created by fernando on 2022/3/24.
//

#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN

typedef NS_ENUM(NSInteger, ASLRBLivePushStatus)
{
    ASLRBLivePushStatusFluent = 0,
    ASLRBLivePushStatusStuttering,
    ASLRBLivePushStatusBrokenOff,
};

@interface ASLRBLivePushStatusView : UIView
@property (assign, nonatomic) ASLRBLivePushStatus pushStatus;
@property (strong, nonatomic) UIButton* statusColorButton;
@property (strong, nonatomic) UILabel* statusTextLabel;
@end

NS_ASSUME_NONNULL_END
