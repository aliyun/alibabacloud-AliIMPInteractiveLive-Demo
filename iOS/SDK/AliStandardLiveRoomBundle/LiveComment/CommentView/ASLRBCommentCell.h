//
//  AIRBDCommentCell.h
//  AliInteractiveLiveDemo
//
//  Created by 麦辣 on 2021/6/7.
//

#import <UIKit/UIKit.h>
#import "../../CommonViews/InsetLabel/ASLRBEdgeInsetLabel.h"

NS_ASSUME_NONNULL_BEGIN

@class ASLRBLiveCommentModel;
@class ASLRBLiveCommentViewConfig;

@protocol ASLRBCommentCellDelegate <NSObject>

-(void)onASLRBCommentCellLongPressGesture:(UILongPressGestureRecognizer*)recognizer
                         commentModel:(ASLRBLiveCommentModel*)commentModel;

-(void)onASLRBCommentCellTapGesture:(UITapGestureRecognizer*)recognizer
                   commentModel:(ASLRBLiveCommentModel*)commentModel;

-(void) muteSender:(NSString*)userId;
-(void) deleteComment;

@end

@interface ASLRBCommentCell : UITableViewCell

@property(weak, nonatomic) id<ASLRBCommentCellDelegate> delegate;

@property(strong,nonatomic) ASLRBEdgeInsetLabel* commentLabel;
@property(strong,nonatomic) ASLRBLiveCommentModel* commentModel;
@property (strong, nonatomic) ASLRBLiveCommentViewConfig* config;

-(void)addLongPressGestureRecognizer;
-(void)addTapGestureRecognizer;
@end

NS_ASSUME_NONNULL_END
