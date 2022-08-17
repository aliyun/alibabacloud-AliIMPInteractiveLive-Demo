//
//  MELLiveCommentModel.h
//  AliStandardLiveUIKit
//
//  Created by fernando on 2022/6/6.
//

#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN
@class ASLRBLiveCommentModel;

@interface MELLiveCommentModel : NSObject
@property (assign, nonatomic) CGFloat cellHeight;
@property (strong, nonatomic) ASLRBLiveCommentModel* rawModel;
@end

NS_ASSUME_NONNULL_END
