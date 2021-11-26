//
//  AIRBDRTCView.m
//  AliInteractiveRoomDemo
//
//  Created by 麦辣 on 2021/6/16.
//

#import "AIRBDRTCView.h"

@implementation AIRBDRTCView

/*
// Only override drawRect: if you perform custom drawing.
// An empty implementation adversely affects performance during animation.
- (void)drawRect:(CGRect)rect {
    // Drawing code
}
*/

- (void)setView:(UIView *)view{
    view.frame = self.bounds;
    [self addSubview:view];

}

- (void)addIDLabel{
    if([self.userID length]>0){
        self.idLabel = [[UILabel alloc]initWithFrame:CGRectMake(0, self.frame.size.height-20, self.bounds.size.width, 20)];
        [self addSubview:self.idLabel];
        [self.idLabel setText:self.userID];
        [self.idLabel setTextColor:[UIColor colorWithWhite:0.9 alpha:0.7]];
    }
}
@end
