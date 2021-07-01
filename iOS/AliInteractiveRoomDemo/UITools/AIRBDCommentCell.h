//
//  AIRBDCommentCell.h
//  AliInteractiveLiveDemo
//
//  Created by 麦辣 on 2021/6/7.
//

#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN
@class AIRBDCommentModel;
@interface AIRBDCommentCell : UITableViewCell

@property(strong,nonatomic)UILabel* commentLabel;
@property(strong,nonatomic)AIRBDCommentModel* commentModel;
@end

NS_ASSUME_NONNULL_END
