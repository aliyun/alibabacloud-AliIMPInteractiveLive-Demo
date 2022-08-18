//
//  ASLRBEdgeInsetLabel.h
//  AliStandardLiveRoomBundle
//
//  Created by fernando on 2021/9/27.
//

#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN

typedef NS_ENUM(NSInteger, ASLRBEdgeInsetLabelTextLineMode) {
    ASLRBEdgeInsetLabelTextLineModeSingle = 0,
    ASLRBEdgeInsetLabelTextLineModeSingleMultiple
};

@interface ASLRBEdgeInsetLabel : UILabel
@property (nonatomic, assign) UIEdgeInsets textInsets;
@end

NS_ASSUME_NONNULL_END
