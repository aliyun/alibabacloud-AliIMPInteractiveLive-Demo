//
//  AIRBDetailsButton.m
//  AliInteractiveRoomDemo
//
//  Created by 麦辣 on 2021/6/30.
//

#import "ASLRBDetailsButton.h"
#import <Masonry/Masonry.h>
#import "ASLRBResourceManager.h"

@interface ASLRBDetailsButton()

@property (strong, nonatomic) UILabel* contentLabel;
@property (strong, nonatomic) UIView* backgroundView;
@property (assign, nonatomic) BOOL isExtended;
@property (assign, nonatomic) CGFloat originWidth;
@property (assign, nonatomic) CGFloat originHeight;

@end

static const CGFloat widthExtendsTo = 167.0;
static const CGFloat heightExtendsTo = 70.0;

@implementation ASLRBDetailsButton

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
        view.backgroundColor = [UIColor colorWithRed:0.0 green:0.0 blue:0.0 alpha:0.2];
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
        [self setTitle:[NSString stringWithFormat:@" %@", title] forState:UIControlStateNormal];
        [self setImage:image forState:UIControlStateNormal];
        
        UIButton* memberdowndropFlagImageButton = [[UIButton alloc] init];
        [self addSubview:memberdowndropFlagImageButton];
        [memberdowndropFlagImageButton setImage:[UIImage imageNamed:@"按钮-返回" inBundle:[ASLRBResourceManager sharedInstance].resourceBundle compatibleWithTraitCollection:nil] forState:UIControlStateNormal];
        memberdowndropFlagImageButton.transform = CGAffineTransformMakeRotation(M_PI * 1.5);
//        [memberdowndropFlagImageButton addTarget:self action:@selector(liveMemberButtonAction:) forControlEvents:UIControlEventTouchUpInside];
        [memberdowndropFlagImageButton mas_makeConstraints:^(MASConstraintMaker * _Nonnull make) {
            make.right.equalTo(self.mas_right).with.offset(-4);
            make.top.equalTo(self.mas_top).with.offset(8);
            make.size.mas_equalTo(CGSizeMake(3.2, 6.3));
        }];
        
        self.originWidth = frame.size.width;
        self.originHeight = frame.size.height;
        [self sendSubviewToBack:self.backgroundView];
        self.adjustsImageWhenHighlighted = NO;
        self.titleLabel.font = [UIFont fontWithName:@"PingFangSC-Medium" size:10];
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
