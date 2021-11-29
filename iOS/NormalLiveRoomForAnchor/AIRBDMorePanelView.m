//
//  AIRBDMorePanelView.m
//  AliInteractiveRoomDemo
//
//  Created by 麦辣 on 2021/6/28.
//

#import "AIRBDMorePanelView.h"
#import <Masonry/Masonry.h>
#import "UIColor+HexColor.h"

@interface AIRBDMorePanelView()

@property (strong, nonatomic) UILabel* titleLabel;
@property (strong, nonatomic) UIButton* muteButton;
@property (strong, nonatomic) UIButton* pauseButton;
@property (strong, nonatomic) UIButton* cameraButton;
@property (strong, nonatomic) UIButton* mirrorButton;
@property (strong, nonatomic) UIButton* editButton;

@end

@implementation AIRBDMorePanelView

- (UILabel *)titleLabel{
    if(!_titleLabel){
        UILabel* label = [[UILabel alloc]init];
        [self addSubview:label];
        [label mas_makeConstraints:^(MASConstraintMaker *make) {
            make.centerX.equalTo(self);
            make.top.equalTo(self).with.offset(12);
            make.width.mas_equalTo(32);
            make.height.mas_equalTo(22);
        }];
        label.font = [UIFont fontWithName:@"PingFangSC-Medium" size:16];
        label.textColor = [UIColor whiteColor];
        label.text = @"更多";
        _titleLabel = label;
    }
    return _titleLabel;
}

- (UIButton *)muteButton{
    if(!_muteButton){
        UIButton* button = [[UIButton alloc]initWithFrame:CGRectMake(1*self.frame.size.width*0.125 - 15, 57, 30, 30)];
        [button setContentMode:UIViewContentModeScaleAspectFit];
        [button setImage:[UIImage imageNamed:@"icon-mute"] forState:UIControlStateNormal];
        [button addTarget:self action:@selector(buttonClicked:) forControlEvents:UIControlEventTouchUpInside];
        _muteButton = button;
    }
    return _muteButton;
}

- (UIButton *)pauseButton{
    if(!_pauseButton){
        UIButton* button = [[UIButton alloc]initWithFrame:CGRectMake(3*self.frame.size.width*0.125 - 15, 57, 30, 30)];
        [button setContentMode:UIViewContentModeScaleAspectFit];
        [button setImage:[UIImage imageNamed:@"icon-pause"] forState:UIControlStateNormal];
        [button addTarget:self action:@selector(buttonClicked:) forControlEvents:UIControlEventTouchUpInside];
        _pauseButton = button;
    }
    return _pauseButton;
}

- (UIButton *)cameraButton{
    if(!_cameraButton){
        UIButton* button = [[UIButton alloc]initWithFrame:CGRectMake(5*self.frame.size.width*0.125 - 15, 57, 30, 30)];
        [button setContentMode:UIViewContentModeScaleAspectFit];
        [button setImage:[UIImage imageNamed:@"icon-camera_switch"] forState:UIControlStateNormal];
        [button addTarget:self action:@selector(buttonClicked:) forControlEvents:UIControlEventTouchUpInside];
        _cameraButton = button;
    }
    return _cameraButton;
}

- (UIButton *)mirrorButton{
    if(!_mirrorButton){
        UIButton* button = [[UIButton alloc]initWithFrame:CGRectMake(7*self.frame.size.width*0.125 - 15, 57, 30, 30)];
        [button setContentMode:UIViewContentModeScaleAspectFit];
        [button setImage:[UIImage imageNamed:@"icon-mirror"] forState:UIControlStateNormal];
//        [button setTitle:@"开启镜像" forState:UIControlStateNormal];
        button.titleLabel.adjustsFontSizeToFitWidth = YES;
        [button addTarget:self action:@selector(buttonClicked:) forControlEvents:UIControlEventTouchUpInside];
        _mirrorButton = button;
    }
    return _mirrorButton;
}

- (UIButton *)editButton{
    if(!_editButton){
        UIButton* button = [[UIButton alloc]initWithFrame:CGRectMake(1*self.frame.size.width*0.125 - 15, 117, 30, 30)];
        [button setContentMode:UIViewContentModeScaleAspectFit];
        [button setImage:[UIImage imageNamed:@"icon-edit"] forState:UIControlStateNormal];
        [button addTarget:self action:@selector(buttonClicked:) forControlEvents:UIControlEventTouchUpInside];
        _editButton = button;
    }
    return _editButton;
}



- (instancetype)initWithFrame:(CGRect)frame
{
    self = [super initWithFrame:frame];
    if (self) {
        [self setBackgroundColor:[UIColor colorWithHexString:@"#333333" alpha:1]];
        self.layer.masksToBounds = YES;
        self.layer.cornerRadius = 10;
        [self bringSubviewToFront:self.titleLabel];
        [self addSubview:self.muteButton];
        [self addSubview:self.pauseButton];
        [self addSubview:self.cameraButton];
        [self addSubview:self.mirrorButton];
        [self addSubview:self.editButton];
    }
    return self;
}

-(void)buttonClicked:(UIButton*)sender{
    if(sender == self.muteButton){
        if([self.delegate respondsToSelector:@selector(muteButtonAction:)]){
            [self.delegate muteButtonAction:sender];
        }
    }else if(sender == self.pauseButton){
        if([self.delegate respondsToSelector:@selector(pauseButtonAction:)]){
            [self.delegate pauseButtonAction:sender];
        }
    }else if(sender == self.cameraButton){
        if([self.delegate respondsToSelector:@selector(cameraButtonAction:)]){
            [self.delegate cameraButtonAction:sender];
        }
    }else if(sender == self.mirrorButton){
        if([self.delegate respondsToSelector:@selector(mirrorButtonAction:)]){
            [self.delegate mirrorButtonAction:sender];
        }
    }
    else if(sender == self.editButton){
        if([self.delegate respondsToSelector:@selector(editButtonAction:)]){
            [self.delegate editButtonAction:sender];
        }
    }
}

@end
