//
//  MELLiveCommentTableViewCell.h
//  AliStandardLiveUIKit
//
//  Created by fernando on 2022/6/6.
//

#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN

@class MELLiveCommentModel;

@interface MELLiveCommentTableViewCell : UITableViewCell
@property (strong, nonatomic) MELLiveCommentModel* model;
@end

NS_ASSUME_NONNULL_END
