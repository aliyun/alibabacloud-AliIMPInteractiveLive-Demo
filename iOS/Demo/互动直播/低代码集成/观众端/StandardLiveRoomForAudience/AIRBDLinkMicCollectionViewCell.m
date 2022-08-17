//
//  AIRBDLinkMicCollectionViewCell.m
//  AliInteractiveRoomDemo
//
//  Created by 刘再勇 on 2022/4/28.
//

#import "AIRBDLinkMicCollectionViewCell.h"
#import <Masonry/Masonry.h>

@interface AIRBDLinkMicCollectionViewCell ()

@property(nonatomic, strong) UIView* backgroundCameraView;

@end

@implementation AIRBDLinkMicCollectionViewCell

- (UIView*)backgroundCameraView{
    if (!_backgroundCameraView){
        // 背景图（摄像头已关闭）
        UIView* backgroundView = [[UIView alloc] init];
        [backgroundView addSubview:({
            UILabel* label = [[UILabel alloc] initWithFrame:self.bounds];
            label.text = @"摄像头已关闭";
            label.textAlignment = NSTextAlignmentCenter;
            label.textColor = [UIColor whiteColor];
            label.font =  [UIFont fontWithName:@"PingFangSC-Regular" size:12];
            label;
        })];
        [self addSubview:backgroundView];
        [backgroundView mas_makeConstraints:^(MASConstraintMaker * _Nonnull make) {
            make.edges.equalTo(self);
        }];
        _backgroundCameraView = backgroundView;
    }
    return _backgroundCameraView;
}

- (UILabel *)userNickLabel{
    if(!_userNickLabel){
        UILabel *label = [[UILabel alloc] init];
        label.frame = CGRectMake(0, 0, 56, 16);
        label.font = [UIFont fontWithName:@"PingFangSC-Regular" size:12];
        label.textColor = [UIColor whiteColor];
        _userNickLabel = label;
        
        [self addSubview:_userNickLabel];
        [_userNickLabel mas_makeConstraints:^(MASConstraintMaker * _Nonnull make) {
            make.left.equalTo(self);
            make.top.equalTo(self);
            make.width.equalTo(self);
            make.height.mas_equalTo(16);
        }];
    }
    return _userNickLabel;
}

- (UILabel *)userMicrophoneLabel{
    if(!_userMicrophoneLabel){
        UILabel *label = [[UILabel alloc] init];
        label.frame = CGRectMake(0, 0, 56, 16);
        label.font = [UIFont fontWithName:@"PingFangSC-Regular" size:12];
        label.textColor = [UIColor whiteColor];
        _userMicrophoneLabel = label;
        
        [self addSubview:_userMicrophoneLabel];
        [_userMicrophoneLabel mas_makeConstraints:^(MASConstraintMaker * _Nonnull make) {
            make.right.equalTo(self).offset(-4);
            make.bottom.equalTo(self).offset(-4);
            make.width.mas_equalTo(56);
            make.height.mas_equalTo(16);
        }];
    }
    return _userMicrophoneLabel;
}

- (void) setUserNickLabelText:(NSString*)labelText videoView:(UIView*)videoView camera:(BOOL)cameraOpened microphone:(BOOL)microphoneOpened{
    // 画面
    if (!cameraOpened){
        for (UIView* subview in self.subviews) {
            if ([self.videoView isEqual:subview]){ // 原本的view是subview，需要移除
                [self.videoView removeFromSuperview];
            }
        }
        _videoView = nil;
        [self backgroundCameraView];
    } else{
        if (![self.videoView isEqual:videoView]){
            for (UIView* subview in self.subviews) {
                if ([self.videoView isEqual:subview]){ // 原本的view是subview，需要移除
                    [self.videoView removeFromSuperview];
                }
            }
        }
        self.videoView = videoView;
        [self addSubview:self.videoView];
        [self.videoView mas_makeConstraints:^(MASConstraintMaker * _Nonnull make) {
            make.edges.equalTo(self);
        }];
    }
    
    // 昵称
    self.userNickLabel.text = labelText;
    [self bringSubviewToFront:self.userNickLabel];
        
    // 麦克风状态
    if (microphoneOpened) {
        self.userMicrophoneLabel.text = @"麦克风开";
    } else {
        self.userMicrophoneLabel.text = @"麦克风关";
    }
    [self bringSubviewToFront:self.userMicrophoneLabel];
}

@end
