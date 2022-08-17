//
//  SECLRALiveCommentModel.h
//  AliLiveRoomUIForAudience
//
//  Created by fernando on 2022/5/7.
//

#import <Foundation/Foundation.h>

@class ASLRBLiveCommentModel;

typedef NS_ENUM(NSInteger, SECLRALiveCommentCellInteractionType) {
    SECLRALiveCommentCellInteractionTypeNone = 0,
    SECLRALiveCommentCellInteractionTypeFollow,
    SECLRALiveCommentCellInteractionTypeComment
};

NS_ASSUME_NONNULL_BEGIN

@interface SECLRALiveCommentModel : NSObject
@property (strong, nonatomic) NSAttributedString* textContentAttributedString;
@property (assign, nonatomic) int32_t cellHeight;
@property (assign, nonatomic) int32_t cellWidth;
@property (assign, nonatomic) SECLRALiveCommentCellInteractionType interactionType;
@property (assign, nonatomic) int32_t interactionWidgetWidth;
@property (strong, nonatomic) ASLRBLiveCommentModel* rawModel;

@end

NS_ASSUME_NONNULL_END
