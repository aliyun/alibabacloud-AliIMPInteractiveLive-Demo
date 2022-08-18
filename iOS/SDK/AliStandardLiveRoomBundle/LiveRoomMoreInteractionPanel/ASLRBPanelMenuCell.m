//
//  AIRBDPanelMenuCell.m
//  AliInteractiveRoomDemo
//
//  Created by 麦辣 on 2021/7/9.
//

#import "ASLRBPanelMenuCell.h"
#import "../CommonTools/UIColor+ColorWithHexString.h"
@interface ASLRBPanelMenuCell()

@property (strong, nonatomic) UILabel* label;

@end

@implementation ASLRBPanelMenuCell


- (void)setTitle:(NSString *)title{
    _title = title;
    if(_label){
        [_label removeFromSuperview];
    }
    _label = ({
        UILabel* label = [[UILabel alloc]initWithFrame:self.bounds];
        label.textColor = [UIColor whiteColor];
        label.textAlignment = NSTextAlignmentCenter;
        label.font = [UIFont fontWithName:@"PingFangSC-Regular" size:14];
        label.text = self.title;
        label;
    });
    [self addSubview:self.label];
}

- (void)setSelected:(BOOL)selected{
    [super setSelected:selected];
    self.label.textColor = [UIColor colorWithHexString:selected? @"#ffffff":@"#999999" alpha:1];
}

@end
