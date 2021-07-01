//
//  AIRBDetailsButton.m
//  AliInteractiveRoomDemo
//
//  Created by 麦辣 on 2021/6/30.
//

#import "AIRBDetailsButton.h"

@interface AIRBDetailsButton()

@property(strong, nonatomic) UILabel* contentLabel;
@property(assign, nonatomic) BOOL isExtended;
@property(assign, nonatomic) CGFloat originWidth;
@property(assign, nonatomic) CGFloat originHeight;

@end

static const CGFloat widthExtendsTo = 100.0;
static const CGFloat heightExtendsTo = 50.0;

@implementation AIRBDetailsButton

- (UILabel *)contentLabel{
    if(!_contentLabel){
        __weak typeof(self) weakSelf = self;
        UILabel* label = [[UILabel alloc]init];
        label.frame = CGRectMake(0, weakSelf.frame.size.height, widthExtendsTo, heightExtendsTo);
        label.adjustsFontSizeToFitWidth = YES;
        _contentLabel = label;
    }
    return _contentLabel;
}

- (void)setText:(NSString *)detailText{
    _text = detailText;
    if(_isExtended){
        dispatch_async(dispatch_get_main_queue(), ^{
            self.contentLabel.text = self.text;
        });
    }
}

- (instancetype)initWithFrame:(CGRect)frame image:(UIImage *)image title:(NSString *)title{
    self = [super initWithFrame:frame];
    if (self) {
        [self setTitle:[@" " stringByAppendingString:title] forState:UIControlStateNormal];
        [self setImage:image forState:UIControlStateNormal];
        self.originWidth = frame.size.width;
        self.originHeight = frame.size.height;
        self.layer.masksToBounds = NO;
        self.layer.cornerRadius = 10;
        self.backgroundColor = [UIColor colorWithWhite:0 alpha:0.3];
        self.adjustsImageWhenHighlighted = NO;
        self.titleLabel.font = [UIFont fontWithName:@"PingFangSC-Medium" size:12];
        [self setContentMode:UIViewContentModeScaleAspectFit];
        [self addTarget:self action:@selector(changeShape) forControlEvents:UIControlEventTouchUpInside];
    }
    return self;
}

-(void)changeShape{
    if(!_isExtended){
        [UIView animateWithDuration:0.1 animations:^{
            CGRect newFrame = self.frame;
            newFrame.size.width = widthExtendsTo;
            newFrame.size.height = heightExtendsTo;
            self.contentLabel.text = self.text;
            self.frame = newFrame;
            self.layer.frame = newFrame;
            [self setNeedsDisplay];
            self.isExtended = YES;
        }];
        [self addSubview:self.contentLabel];
    }else{
        [UIView animateWithDuration:0.1 animations:^{
            CGRect newFrame = self.frame;
            newFrame.size.width = self.originWidth;
            newFrame.size.height = self.originHeight;
            self.frame = newFrame;
            self.layer.frame = newFrame;
            [self setNeedsDisplay];
            self.isExtended = NO;
        }];
        [self.contentLabel removeFromSuperview];
    }
}


@end
