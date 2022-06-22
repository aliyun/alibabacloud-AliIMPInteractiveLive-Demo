//
//  SECLRALiveCommentCellTableViewCell.h
//  StandardECommerceLiveRoomForAudience
//
//  Created by fernando on 2022/4/15.
//

#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN

@class SECLRALiveCommentModel;
@class ASLUKEdgeInsetLabel;


@protocol SECLRALiveCommentCellDelegate <NSObject>

//-(void)onASLRBCommentCellLongPressGesture:(UILongPressGestureRecognizer*)recognizer
//                         commentModel:(ASLRBLiveCommentModel*)commentModel;
//
//-(void)onASLRBCommentCellTapGesture:(UITapGestureRecognizer*)recognizer
//                   commentModel:(ASLRBLiveCommentModel*)commentModel;

- (void) onLiveCommentCellInteraction:(NSInteger)interaction extension:(NSDictionary*)extension;

-(void) muteSender:(NSString*)userId;
-(void) deleteComment;

@end

@interface SECLRALiveCommentCellTableViewCell : UITableViewCell
@property(weak, nonatomic) id<SECLRALiveCommentCellDelegate> delegate;

@property(strong,nonatomic) ASLUKEdgeInsetLabel* commentLabel;
@property(strong,nonatomic) SECLRALiveCommentModel* commentModel;

-(void)addLongPressGestureRecognizer;
-(void)addTapGestureRecognizer;
@end

NS_ASSUME_NONNULL_END
