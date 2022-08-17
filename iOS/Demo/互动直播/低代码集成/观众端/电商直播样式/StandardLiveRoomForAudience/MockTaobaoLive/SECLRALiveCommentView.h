//
//  SECLRALiveCommentView.h
//  StandardECommerceLiveRoomForAudience
//
//  Created by fernando on 2022/4/15.
//

#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN

@class ASLRBLiveCommentModel;

@protocol SECLRACommentViewDelegate <NSObject>

-(void) actionWhenCommentCellLongPressed:(ASLRBLiveCommentModel*)commentModel;
-(void) actionWhenCommentCellTapped:(ASLRBLiveCommentModel*)commentModel;
-(void) actionWhenUnpresentedCommentCountChange:(int32_t)count;
-(void) actionWhenOneCommentPresentedWithActualHeight:(int32_t)height;
-(void) actionWhenCommentJustAboutToPresent:(ASLRBLiveCommentModel*)model;
- (void) onLiveCommentCellInteraction:(NSInteger)interaction extension:(NSDictionary*)extension;
@end

@interface SECLRALiveCommentView : UITableView

@property(nonatomic, weak)id<SECLRACommentViewDelegate> commentDelegate;
@property (assign, nonatomic) BOOL followedAlready;

- (void)insertNewComment:(ASLRBLiveCommentModel*)comment;
- (void)scrollToNewestComment;
- (void)startPresenting;
- (void)stopPresenting;
@end

NS_ASSUME_NONNULL_END
