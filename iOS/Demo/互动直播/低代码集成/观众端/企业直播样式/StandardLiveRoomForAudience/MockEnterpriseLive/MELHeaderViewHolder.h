//
//  MELHeaderViewHolder.h
//  AliStandardLiveUIKit
//
//  Created by fernando on 2022/5/31.
//

#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN

@interface MELHeaderViewHolder : UIView
@property (copy, nonatomic) void(^onExit)(void);
@property (strong, nonatomic) UILabel* liveTitleLabel;
@property (strong, nonatomic) UIButton* exitButton;
@end

NS_ASSUME_NONNULL_END
