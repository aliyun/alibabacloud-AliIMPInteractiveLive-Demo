//
//  AIRBDMultiRTCView.m
//  AliInteractiveRoomDemo
//
//  Created by 麦辣 on 2021/6/15.
//

#import "AIRBDMultiRTCView.h"
#import <Masonry/Masonry.h>
#import "AIRBDRTCView.h"
const int maxRTCNum = 4;

@interface AIRBDMultiRTCView()
@property(assign,nonatomic) int sideOffset;
@property(strong,nonatomic) NSMutableDictionary* userIDDictionary;
@end

@implementation AIRBDMultiRTCView

- (UIView *)centerViewHolder{
    if(!_centerViewHolder){
        __weak typeof(self) weakSelf = self;
        _centerViewHolder = [[UIView alloc]init];
        [self addSubview:_centerViewHolder];
        [_centerViewHolder setBackgroundColor:[UIColor colorWithRed:0.4 green:0.6 blue:0.3 alpha:0.3]];
        [_centerViewHolder mas_makeConstraints:^(MASConstraintMaker *make) {
            make.centerX.equalTo(weakSelf.mas_centerX);
            make.bottom.equalTo(weakSelf.mas_bottom);
            make.width.equalTo(weakSelf.mas_width);
            make.height.equalTo(weakSelf.mas_height).multipliedBy(0.74);
        }];
    }
    return _centerViewHolder;
}

- (UIView *)sideViewHolder{
    if(!_sideViewHolder){
        _sideOffset = 0;
        __weak typeof(self) weakSelf = self;
        _sideViewHolder = [[UIView alloc]init];
        [_sideViewHolder setBackgroundColor:[UIColor colorWithRed:0.3 green:0.3 blue:0.6 alpha:0.3]];
        [self addSubview:_sideViewHolder];
        [_sideViewHolder mas_makeConstraints:^(MASConstraintMaker *make) {
            make.top.equalTo(weakSelf);
            make.width.equalTo(weakSelf);
            make.centerX.equalTo(weakSelf);
            make.height.equalTo(weakSelf.mas_height).multipliedBy(0.26);
        }];
    }
    return _sideViewHolder;
}

- (void)setUpUI{
    [self centerViewHolder];
    [self sideViewHolder];
    self.userIDDictionary = [[NSMutableDictionary alloc]init];
}

- (void)addSideRTCView:(UIView *)view withUserID:(NSString *)userID{
    if([self.userIDDictionary valueForKey:userID]!=nil){
        return;
    }
    AIRBDRTCView* rtcView = [[AIRBDRTCView alloc]init];
    self.userIDDictionary[userID] = rtcView;
    rtcView.view = view;
    rtcView.userID = userID;
    [rtcView addIDLabel];
    __weak typeof(self) weakSelf = self;
    [weakSelf.sideViewHolder addSubview:rtcView];
    [view mas_makeConstraints:^(MASConstraintMaker *make) {
        make.left.equalTo(weakSelf.sideViewHolder).with.offset(weakSelf.sideViewHolder.frame.size.width*0.25*weakSelf.sideOffset);
        make.width.equalTo(weakSelf.sideViewHolder.mas_height).multipliedBy(16.0/9.0);
        make.height.equalTo(weakSelf.sideViewHolder);
        make.top.equalTo(weakSelf.sideViewHolder);
    }];
    _sideOffset = (_sideOffset + 1) % maxRTCNum;
}


- (void)removeSideRTCViewOfUserID:(NSString *)userID{
    if([self.userIDDictionary valueForKey:userID]!=nil){
        __weak typeof(self) weakSelf = self;
        dispatch_async(dispatch_get_main_queue(), ^{
            AIRBDRTCView* rtcView = [self.userIDDictionary valueForKey:userID];
            [rtcView removeFromSuperview];
            [self.userIDDictionary removeObjectForKey:userID];
            weakSelf.sideOffset = 0;
            for (NSString* userID in self.userIDDictionary) {
                AIRBDRTCView* rtcView = [self.userIDDictionary valueForKey:userID];
                [rtcView mas_remakeConstraints:^(MASConstraintMaker *make) {
                    make.left.equalTo(weakSelf.sideViewHolder).with.offset(weakSelf.sideViewHolder.frame.size.width*0.25*weakSelf.sideOffset);
                    make.width.equalTo(weakSelf.sideViewHolder.mas_height).multipliedBy(16.0/9.0);
                    make.height.equalTo(weakSelf.sideViewHolder);
                    make.top.equalTo(weakSelf.sideViewHolder);
                }];
                self->_sideOffset = (self->_sideOffset + 1) % maxRTCNum;
            }
        });
    }
}



- (void) addCenterRTCView:(UIView *)view{
    __weak typeof(self) weakSelf = self;
    [weakSelf.centerViewHolder addSubview:view];
    [view mas_makeConstraints:^(MASConstraintMaker *make) {
        make.height.equalTo(weakSelf.centerViewHolder.mas_height);
        make.width.equalTo(weakSelf.centerViewHolder.mas_height).multipliedBy(16.0/9.0);
        make.centerX.equalTo(weakSelf.centerViewHolder);
        make.centerY.equalTo(weakSelf.centerViewHolder);
    }];
}


@end
