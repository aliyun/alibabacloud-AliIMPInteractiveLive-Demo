//
//  AIRBDCommentModel.h
//  AliInteractiveLiveDemo
//
//  Created by 麦辣 on 2021/6/8.
//

#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN
typedef enum : NSUInteger {
    BulletStyle,
    BulletStyleNew,
    WhiteboardStyle
} AIRBDCommentStyle;


@interface AIRBDCommentModel : NSObject

@property(copy,nonatomic)NSString* senderName;
@property(copy,nonatomic)NSString* senderID;
@property(copy,nonatomic)NSString* content;
@property(strong,nonatomic)UIColor* color;
@property(assign,nonatomic)AIRBDCommentStyle commentStyle;


@end

NS_ASSUME_NONNULL_END
