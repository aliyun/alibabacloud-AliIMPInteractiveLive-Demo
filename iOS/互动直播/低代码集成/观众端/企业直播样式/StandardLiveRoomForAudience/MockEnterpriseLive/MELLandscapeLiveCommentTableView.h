//
//  CommentView.h
//  CommentViewDemo
//
//  Created by 麦辣 on 2021/6/3.
//

#import <UIKit/UIKit.h>
//#import "../ASLRBLiveCommentModel.h"
NS_ASSUME_NONNULL_BEGIN

@class ASLRBLiveCommentModel;
@class ASLRBLiveCommentViewConfig;

@protocol ASLRBCommentViewDelegate <NSObject>

-(void) actionWhenCommentCellLongPressed:(ASLRBLiveCommentModel*)commentModel;
-(void) actionWhenCommentCellTapped:(ASLRBLiveCommentModel*)commentModel;
-(void) actionWhenUnpresentedCommentCountChange:(int32_t)count;
-(void) actionWhenOneCommentPresentedWithActualHeight:(int32_t)height;
-(void) actionWhenCommentJustAboutToPresent:(ASLRBLiveCommentModel*)model;

@end

@interface MELLandscapeLiveCommentTableView : UITableView

@property(nonatomic, weak)id<ASLRBCommentViewDelegate> commentDelegate;

//@property (copy, nonatomic) NSString* commentUniformSenderColor;
//@property (copy, nonatomic) NSString* commentUniformContentColor;
//@property(nonatomic, copy) UIColor* cellBackgroundColorTone;

- (instancetype) initWitConfig:(ASLRBLiveCommentViewConfig*)config;

- (void)insertNewComment:(ASLRBLiveCommentModel*)comment presentedCompulsorily:(BOOL)presentedCompulsorily;

- (void)scrollToNewestComment;

- (void) startPresenting;
- (void) stopPresenting;

@end

NS_ASSUME_NONNULL_END
