//
//  AIRBDetailsButton.h
//  AliInteractiveRoomDemo
//
//  Created by 麦辣 on 2021/6/30.
//

#import <UIKit/UIKit.h>

/**
 * Button默认显示 图标（左侧，若有图标）+ 按钮标题（右侧，比如“系统公告”、“公告”）
 * Button被点击根据当前的状态（伸展/未伸展）进行frame宽和高变化，在进行伸展变形的范围添加/移除一个UILabel——用于显示detailText属性的内容
 * 使用注意：使用initWithFrame:image: title:方法初始化, 默认仅支持向右、向下伸展
 */

NS_ASSUME_NONNULL_BEGIN

@interface ASLRBDetailsButton : UIButton
@property (strong, nonatomic) NSString* text;
-(instancetype)initWithFrame:(CGRect)frame image:(UIImage*)image title:(NSString*) title;
@end

NS_ASSUME_NONNULL_END
