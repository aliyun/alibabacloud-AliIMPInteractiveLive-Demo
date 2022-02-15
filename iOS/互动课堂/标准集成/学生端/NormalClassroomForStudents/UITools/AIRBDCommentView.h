//
//  CommentView.h
//  CommentViewDemo
//
//  Created by 麦辣 on 2021/6/3.
//

#import <UIKit/UIKit.h>
#import "AIRBDCommentModel.h"
NS_ASSUME_NONNULL_BEGIN



@interface AIRBDCommentView : UITableView

@property(assign,nonatomic)AIRBDCommentStyle commentStyle;

- (void)insertNewComment:(NSString*) comment;
- (void)insertNewComments:(NSArray*) comments;
- (instancetype)initWithCommentStyle:(AIRBDCommentStyle)commentStyle;
@end

NS_ASSUME_NONNULL_END
