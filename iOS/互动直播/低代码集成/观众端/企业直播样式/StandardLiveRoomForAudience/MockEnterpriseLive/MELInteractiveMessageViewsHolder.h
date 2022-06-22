//
//  MELInteractiveMessageViewsHolder.h
//  AliStandardLiveUIKit
//
//  Created by fernando on 2022/6/6.
//

#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN

@class ASLRBLiveCommentModel;

@interface MELInteractiveMessageViewsHolder : UIView<UITableViewDelegate, UITableViewDataSource>
@property (strong, nonatomic) UITableView* liveCommentView;

- (void)insertLiveComment:(ASLRBLiveCommentModel*)model;
- (void)startPresentingLiveComment;
- (void)stopPresentingLiveComment;
@end

NS_ASSUME_NONNULL_END
