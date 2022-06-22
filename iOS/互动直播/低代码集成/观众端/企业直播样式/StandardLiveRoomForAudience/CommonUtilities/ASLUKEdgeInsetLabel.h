//
//  SECLRAEdgeInsetLabel.h
//  AliStandardLiveRoomBundle
//
//  Created by fernando on 2021/9/27.
//

#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN

typedef NS_ENUM(NSInteger, ASLUKEdgeInsetLabelTextLineMode) {
    ASLUKEdgeInsetLabelTextLineModeSingle = 0,
    ASLUKEdgeInsetLabelTextLineModeSingleMultiple
};

@interface ASLUKEdgeInsetLabel : UILabel
@property (nonatomic, assign) UIEdgeInsets textInsets;
@end

NS_ASSUME_NONNULL_END
