//
//  AIRBDetailsButton.m
//  AliInteractiveRoomDemo
//
//  Created by 麦辣 on 2021/6/30.
//

#import "AIRBDetailsButton.h"

@interface AIRBDetailsButton()

@property (strong, nonatomic) UILabel* contentLabel;
@property (strong, nonatomic) UIView* backgroundView;
@property (assign, nonatomic) BOOL isExtended;
@property (assign, nonatomic) CGFloat originWidth;
@property (assign, nonatomic) CGFloat originHeight;

@end

static const CGFloat widthExtendsTo = 167.0;
static const CGFloat heightExtendsTo = 70.0;

@implementation AIRBDetailsButton

- (UILabel *)contentLabel{
    if(!_contentLabel){
        UILabel* label = [[UILabel alloc]init];
        label.frame = CGRectMake(10, self.frame.size.height, widthExtendsTo - 20, heightExtendsTo - self.frame.size.height);
        label.font =  [UIFont fontWithName:@"PingFangSC-Regular" size:12];
        label.numberOfLines = 4;
        label.textColor = [UIColor whiteColor];
        _contentLabel = label;
    }
    return _contentLabel;
}

- (UIView *)backgroundView{
    if(!_backgroundView){
        UIView* view = [[UIView alloc]initWithFrame:self.frame];
        view.layer.masksToBounds = YES;
        view.layer.cornerRadius = 10;
        view.backgroundColor = [UIColor colorWithWhite:0 alpha:0.3];
        view.userInteractionEnabled = NO;
        _backgroundView = view;
    }
    return _backgroundView;
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
        [self addSubview:self.backgroundView];
        [self setTitle:[@" " stringByAppendingString:title] forState:UIControlStateNormal];
        [self setImage:image forState:UIControlStateNormal];
        self.originWidth = frame.size.width;
        self.originHeight = frame.size.height;
        [self sendSubviewToBack:self.backgroundView];
        self.adjustsImageWhenHighlighted = NO;
        self.titleLabel.font = [UIFont fontWithName:@"PingFangSC-Medium" size:12];
        [self setContentMode:UIViewContentModeScaleAspectFit];
        [self bringSubviewToFront:self.imageView];
        [self addTarget:self action:@selector(changeShape) forControlEvents:UIControlEventTouchUpInside];
    }
    return self;
}

-(void)changeShape{
    if(!_isExtended){
        [UIView animateWithDuration:0.1 animations:^{
            CGRect newFrame = self.backgroundView.frame;
            newFrame.size.width = widthExtendsTo;
            newFrame.size.height = heightExtendsTo;
            self.contentLabel.text = self.text;
            self.backgroundView.frame = newFrame;
            self.isExtended = YES;
        }];
        [self addSubview:self.contentLabel];
    }else{
        [UIView animateWithDuration:0.1 animations:^{
            CGRect newFrame = self.backgroundView.frame;
            newFrame.size.width = self.originWidth;
            newFrame.size.height = self.originHeight;
            self.backgroundView.frame = newFrame;
            self.isExtended = NO;
        }];
        [self.contentLabel removeFromSuperview];
    }
}


@end
