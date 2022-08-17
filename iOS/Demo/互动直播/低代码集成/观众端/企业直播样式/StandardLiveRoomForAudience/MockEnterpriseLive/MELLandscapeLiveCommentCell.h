//
//  AIRBDCommentCell.h
//  AliInteractiveLiveDemo
//
//  Created by 麦辣 on 2021/6/7.
//

#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN

@class ASLRBLiveCommentModel;
@class ASLUKEdgeInsetLabel;

//@protocol ASLRBCommentCellDelegate <NSObject>
//
//-(void)onASLRBCommentCellLongPressGesture:(UILongPressGestureRecognizer*)recognizer
//                         commentModel:(ASLRBLiveCommentModel*)commentModel;
//
//-(void)onASLRBCommentCellTapGesture:(UITapGestureRecognizer*)recognizer
//                   commentModel:(ASLRBLiveCommentModel*)commentModel;
//
//-(void) muteSender:(NSString*)userId;
//-(void) deleteComment;
//
//@end

@interface MELLandscapeLiveCommentCell : UITableViewCell

//@property(weak, nonatomic) id<ASLRBCommentCellDelegate> delegate;

@property(strong,nonatomic) ASLUKEdgeInsetLabel* commentLabel;
@property(strong,nonatomic) ASLRBLiveCommentModel* commentModel;

-(void)addLongPressGestureRecognizer;
-(void)addTapGestureRecognizer;
@end

NS_ASSUME_NONNULL_END
