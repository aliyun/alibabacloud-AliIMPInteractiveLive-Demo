//
//  AIRBDMorePanelView.m
//  AliInteractiveRoomDemo
//
//  Created by 麦辣 on 2021/6/28.
//

#import "ASLRBMorePanelView.h"
#import <Masonry/Masonry.h>
#import "../CommonTools/UIColor+ColorWithHexString.h"

#import "../CommonTools/ASLRBResourceManager.h"
#import "ASLRBLiveRoomMoreInteractionPanelProtocol.h"
#import <AliInteractiveRoomBundle/AliInteractiveRoomBundle.h>
#import <objc/message.h>

@interface ASLRBMorePanelView()

@property (strong, nonatomic) UILabel* titleLabel;

@property (strong, nonatomic) UIButton* muteButton;
@property (strong, nonatomic) UILabel* muteLabel;
@property (strong, nonatomic) UIButton* muteSelectedButton;

@property (strong, nonatomic) UIButton* pauseButton;
@property (strong, nonatomic) UILabel* pauseLabel;
@property (strong, nonatomic) UIButton* pauseSelectedButton;

@property (strong, nonatomic) UIButton* cameraButton;

@property (strong, nonatomic) UIButton* mirrorButton;
@property (strong, nonatomic) UILabel* mirrorLabel;
@property (strong, nonatomic) UIButton* mirrorSelectedButton;

@property (strong, nonatomic) UIButton* editButton;

@property (strong, nonatomic) UIButton* banAllCommentsButton;
@property (strong, nonatomic) UILabel* banAllCommentsLabel;
@property (strong, nonatomic) UIButton* banAllCommentsSelectedButton;


@end

@implementation ASLRBMorePanelView

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
        [button setImage:[UIImage imageNamed:@"直播-更多-静音" inBundle:[ASLRBResourceManager sharedInstance].resourceBundle compatibleWithTraitCollection:nil] forState:UIControlStateNormal];
        [button setAdjustsImageWhenHighlighted:NO];
        [button addTarget:self action:@selector(buttonClicked:) forControlEvents:UIControlEventTouchUpInside];
        _muteLabel = [[UILabel alloc]initWithFrame:CGRectMake(-2, 33, 34, 11)];
        _muteLabel.font = [UIFont fontWithName:@"PingFangSC-Regular" size:8];
        _muteLabel.textColor = [UIColor whiteColor];
        _muteLabel.text = @"静音";
        _muteLabel.textAlignment = NSTextAlignmentCenter;
        [button addSubview:_muteLabel];
        
        _muteSelectedButton = [[UIButton alloc] init];
        _muteSelectedButton.alpha = 0.0;
        [_muteSelectedButton setContentMode:UIViewContentModeScaleAspectFit];
        [_muteSelectedButton setImage:[UIImage imageNamed:@"直播-更多-选择" inBundle:[ASLRBResourceManager sharedInstance].resourceBundle compatibleWithTraitCollection:nil] forState:UIControlStateNormal];
        [button addSubview:_muteSelectedButton];
        [_muteSelectedButton mas_makeConstraints:^(MASConstraintMaker * _Nonnull make) {
            make.right.equalTo(button).with.offset(-5);
            make.bottom.equalTo(button).with.offset(-5);
            make.size.mas_equalTo(CGSizeMake(8, 8));
        }];
        
        _muteButton = button;
    }
    return _muteButton;
}

- (UIButton *)pauseButton{
    if(!_pauseButton){
        UIButton* button = [[UIButton alloc]initWithFrame:CGRectMake(3*self.frame.size.width*0.125 - 15, 57, 30, 30)];
        [button setContentMode:UIViewContentModeScaleAspectFit];
        [button setImage:[UIImage imageNamed:@"直播-更多-暂停" inBundle:[ASLRBResourceManager sharedInstance].resourceBundle compatibleWithTraitCollection:nil] forState:UIControlStateNormal];
        [button setAdjustsImageWhenHighlighted:NO];
        [button addTarget:self action:@selector(buttonClicked:) forControlEvents:UIControlEventTouchUpInside];
        _pauseLabel = [[UILabel alloc]initWithFrame:CGRectMake(-2, 33, 34, 11)];
        _pauseLabel.font = [UIFont fontWithName:@"PingFangSC-Regular" size:8];
        _pauseLabel.textColor = [UIColor whiteColor];
        _pauseLabel.text = @"暂停直播";
        _pauseLabel.textAlignment = NSTextAlignmentCenter;
        [button addSubview:_pauseLabel];
        
        _pauseSelectedButton = [[UIButton alloc] init];
        _pauseSelectedButton.alpha = 0.0;
        [_pauseSelectedButton setContentMode:UIViewContentModeScaleAspectFit];
        [_pauseSelectedButton setImage:[UIImage imageNamed:@"直播-更多-选择" inBundle:[ASLRBResourceManager sharedInstance].resourceBundle compatibleWithTraitCollection:nil] forState:UIControlStateNormal];
        [button addSubview:_pauseSelectedButton];
        [_pauseSelectedButton mas_makeConstraints:^(MASConstraintMaker * _Nonnull make) {
            make.right.equalTo(button).with.offset(-5);
            make.bottom.equalTo(button).with.offset(-5);
            make.size.mas_equalTo(CGSizeMake(8, 8));
        }];
        
        _pauseButton = button;
    }
    return _pauseButton;
}

- (UIButton *)cameraButton{
    if(!_cameraButton){
        UIButton* button = [[UIButton alloc]initWithFrame:CGRectMake(5*self.frame.size.width*0.125 - 15, 57, 30, 30)];
        [button setContentMode:UIViewContentModeScaleAspectFit];
        [button setImage:[UIImage imageNamed:@"直播-更多-镜头翻转" inBundle:[ASLRBResourceManager sharedInstance].resourceBundle compatibleWithTraitCollection:nil] forState:UIControlStateNormal];
        [button setAdjustsImageWhenHighlighted:NO];
        [button addTarget:self action:@selector(buttonClicked:) forControlEvents:UIControlEventTouchUpInside];
        UILabel* label = [[UILabel alloc]initWithFrame:CGRectMake(-2, 33, 34, 11)];
        label.font = [UIFont fontWithName:@"PingFangSC-Regular" size:8];
        label.textColor = [UIColor whiteColor];
        label.text = @"镜头翻转";
        label.textAlignment = NSTextAlignmentCenter;
        [button addSubview:label];
        _cameraButton = button;
    }
    return _cameraButton;
}

- (UIButton *)mirrorButton{
    if(!_mirrorButton){
        UIButton* button = [[UIButton alloc]initWithFrame:CGRectMake(7*self.frame.size.width*0.125 - 15, 57, 30, 30)];
        [button setContentMode:UIViewContentModeScaleAspectFit];
        [button setImage:[UIImage imageNamed:@"直播-更多-镜相开" inBundle:[ASLRBResourceManager sharedInstance].resourceBundle compatibleWithTraitCollection:nil] forState:UIControlStateNormal];
        [button setAdjustsImageWhenHighlighted:NO];
        button.tag = 1;
        button.titleLabel.adjustsFontSizeToFitWidth = YES;
        [button addTarget:self action:@selector(buttonClicked:) forControlEvents:UIControlEventTouchUpInside];
        _mirrorLabel = [[UILabel alloc]initWithFrame:CGRectMake(-2, 33, 34, 11)];
        _mirrorLabel.font = [UIFont fontWithName:@"PingFangSC-Regular" size:8];
        _mirrorLabel.textColor = [UIColor whiteColor];
        _mirrorLabel.text = @"关闭镜像";
        _mirrorLabel.textAlignment = NSTextAlignmentCenter;
        [button addSubview:_mirrorLabel];
        
        _mirrorSelectedButton = [[UIButton alloc] init];
//        _mirrorSelectedButton.alpha = 0.0;
        [_mirrorSelectedButton setContentMode:UIViewContentModeScaleAspectFit];
        [_mirrorSelectedButton setImage:[UIImage imageNamed:@"直播-更多-选择" inBundle:[ASLRBResourceManager sharedInstance].resourceBundle compatibleWithTraitCollection:nil] forState:UIControlStateNormal];
        [button addSubview:_mirrorSelectedButton];
        [_mirrorSelectedButton mas_makeConstraints:^(MASConstraintMaker * _Nonnull make) {
            make.right.equalTo(button).with.offset(-5);
            make.bottom.equalTo(button).with.offset(-5);
            make.size.mas_equalTo(CGSizeMake(8, 8));
        }];
        
        _mirrorButton = button;
    }
    return _mirrorButton;
}

- (UIButton *)editButton{
    if(!_editButton){
        UIButton* button = [[UIButton alloc]initWithFrame:CGRectMake(1*self.frame.size.width*0.125 - 15, 117, 30, 30)];
        [button setContentMode:UIViewContentModeScaleAspectFit];
        [button setImage:[UIImage imageNamed:@"直播-更多-修改标题" inBundle:[ASLRBResourceManager sharedInstance].resourceBundle compatibleWithTraitCollection:nil] forState:UIControlStateNormal];
        [button setAdjustsImageWhenHighlighted:NO];
        [button addTarget:self action:@selector(buttonClicked:) forControlEvents:UIControlEventTouchUpInside];
        UILabel* label = [[UILabel alloc]initWithFrame:CGRectMake(-2, 33, 34, 11)];
        label.font = [UIFont fontWithName:@"PingFangSC-Regular" size:8];
        label.textColor = [UIColor whiteColor];
        label.text = @"编辑公告";
        label.textAlignment = NSTextAlignmentCenter;
        [button addSubview:label];
        _editButton = button;
    }
    return _editButton;
}

//- (UIButton*) beautyButton {
//    if (!_beautyButton) {
//        UIButton* button = [[UIButton alloc]initWithFrame:CGRectMake(3*self.frame.size.width*0.125 - 15, 117, 30, 30)];
//        [button setContentMode:UIViewContentModeScaleAspectFit];
//        [button setImage:[UIImage imageNamed:@"icon-beauty"] forState:UIControlStateNormal];
//        [button setAdjustsImageWhenHighlighted:NO];
//        [button addTarget:self action:@selector(buttonClicked:) forControlEvents:UIControlEventTouchUpInside];
//        UILabel* label = [[UILabel alloc]initWithFrame:CGRectMake(-2, 33, 34, 11)];
//        label.font = [UIFont fontWithName:@"PingFangSC-Regular" size:8];
//        label.textColor = [UIColor whiteColor];
//        label.text = @"美颜";
//        label.textAlignment = NSTextAlignmentCenter;
//        [button addSubview:label];
//        _beautyButton = button;
//    }
//    return _beautyButton;
//}

- (UIButton *)banAllCommentsButton{
    if (!_banAllCommentsButton) {
        UIButton* button = [[UIButton alloc]initWithFrame:CGRectMake(3*self.frame.size.width*0.125 - 15, 117, 30, 30)];
        [button setContentMode:UIViewContentModeScaleAspectFit];
        [button setImage:[UIImage imageNamed:@"直播-更多-取消禁言" inBundle:[ASLRBResourceManager sharedInstance].resourceBundle compatibleWithTraitCollection:nil] forState:UIControlStateNormal];
        [button setAdjustsImageWhenHighlighted:NO];
        [button addTarget:self action:@selector(buttonClicked:) forControlEvents:UIControlEventTouchUpInside];
        _banAllCommentsLabel = [[UILabel alloc]initWithFrame:CGRectMake(-2, 33, 34, 11)];
        _banAllCommentsLabel.font = [UIFont fontWithName:@"PingFangSC-Regular" size:8];
        _banAllCommentsLabel.textColor = [UIColor whiteColor];
        _banAllCommentsLabel.text = @"全员禁言";
        _banAllCommentsLabel.textAlignment = NSTextAlignmentCenter;
        [button addSubview:_banAllCommentsLabel];
        
        _banAllCommentsSelectedButton = [[UIButton alloc] init];
        _banAllCommentsSelectedButton.alpha = 0.0;
        [_banAllCommentsSelectedButton setContentMode:UIViewContentModeScaleAspectFit];
        [_banAllCommentsSelectedButton setImage:[UIImage imageNamed:@"直播-更多-选择" inBundle:[ASLRBResourceManager sharedInstance].resourceBundle compatibleWithTraitCollection:nil] forState:UIControlStateNormal];
        [button addSubview:_banAllCommentsSelectedButton];
        [_banAllCommentsSelectedButton mas_makeConstraints:^(MASConstraintMaker * _Nonnull make) {
            make.right.equalTo(button).with.offset(-5);
            make.bottom.equalTo(button).with.offset(-5);
            make.size.mas_equalTo(CGSizeMake(8, 8));
        }];
        
        _banAllCommentsButton = button;
    }
    return _banAllCommentsButton;
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
        [self addSubview:self.banAllCommentsButton];
//        [self addSubview:self.beautyButton];
    }
    return self;
}

-(void) buttonClicked:(UIButton*)sender{
    if(sender == self.muteButton){
        if([self.delegate respondsToSelector:@selector(muteButtonAction:)]){
            [self.delegate muteButtonAction:sender];
        }
        if ([self.muteLabel.text isEqualToString:@"静音"]) {
            self.muteLabel.text = @"取消静音";
            self.muteSelectedButton.alpha = 1.0;
        } else {
            self.muteLabel.text = @"静音";
            self.muteSelectedButton.alpha = 0.0;
        }
    }else if(sender == self.pauseButton){
        if([self.delegate respondsToSelector:@selector(pauseButtonAction:)]){
            [self.delegate pauseButtonAction:sender];
        }
        if ([self.pauseLabel.text isEqualToString:@"暂停直播"]) {
            self.pauseLabel.text = @"取消暂停";
            self.pauseSelectedButton.alpha = 1.0;
        } else {
            self.pauseLabel.text = @"暂停直播";
            self.pauseSelectedButton.alpha = 0.0;
        }
    }else if(sender == self.cameraButton){
        if([self.delegate respondsToSelector:@selector(cameraButtonAction:)]){
            [self.delegate cameraButtonAction:sender];
        }
    }else if(sender == self.mirrorButton){
        if([self.delegate respondsToSelector:@selector(mirrorButtonAction:)]){
            [self.delegate mirrorButtonAction:sender];
        }
        if ([self.mirrorLabel.text isEqualToString:@"开启镜像"]) {
            self.mirrorLabel.text = @"关闭镜像";
            self.mirrorSelectedButton.alpha = 1.0;
        } else {
            self.mirrorLabel.text = @"开启镜像";
            self.mirrorSelectedButton.alpha = 0.0;
        }
    }
    else if(sender == self.editButton){
        if([self.delegate respondsToSelector:@selector(editButtonAction:)]){
            [self.delegate editButtonAction:sender];
        }
        return;
    }else if(sender == self.banAllCommentsButton){
        if([self.delegate respondsToSelector:@selector(banAllCommentsButtonAction:)]){
            [self.delegate banAllCommentsButtonAction:sender];
        }
        if ([self.banAllCommentsLabel.text isEqualToString:@"全员禁言"]) {
            self.banAllCommentsLabel.text = @"取消禁言";
            self.banAllCommentsSelectedButton.alpha = 1.0;
        } else {
            self.banAllCommentsLabel.text = @"全员禁言";
            self.banAllCommentsSelectedButton.alpha = 0.0;
        }
    }else{
        return;
    }
//    if(sender.tag == 0){
//        UIImageView* imageView = [[UIImageView alloc]initWithFrame:CGRectMake(sender.frame.size.width-6,sender.frame.size.height-6, 6, 6)];
//        [imageView setImage:[UIImage imageNamed:@"icon-start" inBundle:[ASLRBResourceManager sharedInstance].resourceBundle compatibleWithTraitCollection:nil]];
//        [sender addSubview:imageView];
//    }else{
//        for (id obj in [sender subviews]) {
//            if([obj isKindOfClass:[UIImageView class]]&&obj!=sender.imageView){
//                [obj removeFromSuperview];
//                break;
//            }
//        }
//    }
    //tag的改变放在代理之后，原因是在调用代理时需要传入点击时的tag
    sender.tag = 1 - sender.tag;
}



@end
