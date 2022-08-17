//
//  ASLRBLiveSystemLabel.m
//  AliStandardLiveRoomBundle
//
//  Created by fernando on 2021/10/15.
//

#import "SECLRALiveSystemMessageLabel.h"
#import <AliStandardLiveRoomBundle/AliStandardLiveRoomBundle.h>
#import <Masonry/Masonry.h>

#import "ASLUKResourceManager.h"
#import "UIColor+ColorWithHexString.h"


@interface SECLRALiveSystemMessageLabel()
@property (nonatomic,strong) NSMutableArray* unpresentedMessages;;
//@property (nonatomic, strong) NSTimer* timerForShowingMessage;
@property (nonatomic, strong) NSTimer* timer;
@property (atomic, assign) BOOL isPresenting;
//@property (strong, nonatomic) UILabel* userLevelLabel;
//@property (strong, nonatomic) UILabel* messageLabel;
@end

@implementation SECLRALiveSystemMessageLabel

//- (UILabel*)messageLabel {
//    if (!_messageLabel) {
//        _messageLabel = [[UILabel alloc] init];
//        _messageLabel.layer.masksToBounds = YES;
//        [self addSubview:_messageLabel];
//        [_messageLabel mas_makeConstraints:^(MASConstraintMaker * _Nonnull make) {
//            make.left.equalTo(self.userLevelLabel.mas_right).with.offset(4);
//            make.centerY.equalTo(self.mas_centerY);
//            make.width.mas_equalTo(59);
//            make.height.mas_equalTo(18);
//        }];
//    }
//    return _messageLabel;
//}
//
//- (UILabel*)userLevelLabel {
//    if (!_userLevelLabel) {
//        _userLevelLabel = [[UILabel alloc] init];
//        _userLevelLabel.layer.cornerRadius = 7;
//        _userLevelLabel.layer.masksToBounds = YES;
//        [self addSubview:_userLevelLabel];
//        [_userLevelLabel mas_makeConstraints:^(MASConstraintMaker * _Nonnull make) {
//            make.left.equalTo(self).with.offset(6);
//            make.centerY.equalTo(self.mas_centerY);
//            make.size.mas_equalTo(CGSizeMake(40, 14));
//        }];
//    }
//    return _userLevelLabel;
//}

- (instancetype) init {
    self = [super init];
    if (self) {
        _unpresentedMessages = [[NSMutableArray alloc] init];
        _timer = [NSTimer scheduledTimerWithTimeInterval:1.5 target:self selector:@selector(presentMessageRepeatly) userInfo:nil repeats:YES];
    }
    return self;
}

- (void) dealloc {
    
}

- (void) stopPresenting {
    if (_timer) {
        [_timer invalidate];
        _timer = nil;
    }
}

- (void) presentMessageRepeatly {
    
    self.alpha = 0.0;
    
    if (self.unpresentedMessages.count > 0) {
        ASLRBLiveSystemMessageModel* model = [self.unpresentedMessages objectAtIndex:0];
        [self.unpresentedMessages removeObjectAtIndex:0];

        [self rebuildSystemLabelWithSystemMessageModel:model];
        
        // 更新message label长度
        CGSize size = [self.attributedText size];
        [self mas_updateConstraints:^(MASConstraintMaker * _Nonnull make) {
            make.width.mas_equalTo(size.width + 18);
            make.left.equalTo(self.superview.mas_left).with.offset(-150);
        }];
        [self.superview layoutIfNeeded];
        
        self.alpha = 1.0;
        
        __weak typeof(self) weakSelf = self;
        [UIView animateWithDuration:0.5 animations:^{
            if (weakSelf.superview) {
                [(UILabel*)weakSelf mas_updateConstraints:^(MASConstraintMaker * _Nonnull make) {
                    make.left.equalTo(weakSelf.superview.mas_left);
                }];
                
                [weakSelf.superview layoutIfNeeded];
            }
        }];
    }
}

- (void) rebuildSystemLabelWithSystemMessageModel:(ASLRBLiveSystemMessageModel*)model {
    if ([model.rawMessage containsString:@"进入了直播间"]) {
        
        NSMutableAttributedString *attriStr =  [[NSMutableAttributedString alloc] init];
        NSTextAttachment *userLevelIcon = [[NSTextAttachment alloc] init];
        
        //这里模仿判断粉丝级别，具体根据自己业务设置
        NSString* userID = [model.extension valueForKey:@"userID"];
        NSString* userLevelIconName = nil;
        UIColor* messageBackgroundColor = nil;
        if (userID.length > 5) {
            userLevelIconName = @"粉丝标示-新粉";
            messageBackgroundColor = [UIColor colorWithHexString:@"0x00BD20" alpha:0.5];
        } else if (userID.length > 2) {
            userLevelIconName = @"粉丝标示-铁粉";
            messageBackgroundColor = [UIColor colorWithHexString:@"0x00B4FF" alpha:0.5];
        } else {
            userLevelIconName = @"粉丝标示-老粉";
            messageBackgroundColor = [UIColor colorWithHexString:@"0xA180FF" alpha:0.5];
        }
        
        [userLevelIcon setImage:[UIImage imageNamed:userLevelIconName inBundle:[ASLUKResourceManager sharedInstance].resourceBundle compatibleWithTraitCollection:nil]];
        
        [userLevelIcon setBounds:CGRectMake(0, -0.5, userLevelIcon.image.size.width, userLevelIcon.image.size.height)];
        NSAttributedString *userLevelIconString = [NSAttributedString attributedStringWithAttachment:userLevelIcon];
        [attriStr appendAttributedString:userLevelIconString];
        
        NSString* userNick = [model.extension valueForKey:@"userNick"];
        if (userNick.length > 5) {
            userNick = [userNick substringToIndex:5];
            userNick = [userNick stringByAppendingString:@"..."];
        }
        
        UIFont* textFont = [UIFont fontWithName:@"PingFangSC-Regular" size:10];
        [attriStr appendAttributedString:({
            NSAttributedString *content = [[NSAttributedString alloc] initWithString:[NSString stringWithFormat:@" %@ 来了", userNick] attributes:@{
                NSFontAttributeName : textFont,
                NSForegroundColorAttributeName : [UIColor whiteColor],
                NSBaselineOffsetAttributeName : @((textFont.ascender - textFont.descender) / 2 + textFont.descender - 1.5)
            }];
            content;
        })];
        
        self.backgroundColor = messageBackgroundColor;
        
        self.attributedText = attriStr;
    } else if ([model.rawMessage containsString:@"去购买了"]) {
        NSMutableAttributedString *attriStr =  [[NSMutableAttributedString alloc] init];
        
        [attriStr appendAttributedString:({
            NSTextAttachment *userLevelIcon = [[NSTextAttachment alloc] init];
            [userLevelIcon setImage:[UIImage imageNamed:@"正在购买" inBundle:[ASLUKResourceManager sharedInstance].resourceBundle compatibleWithTraitCollection:nil]];
            [userLevelIcon setBounds:CGRectMake(0, -0.5, userLevelIcon.image.size.width, userLevelIcon.image.size.height)];
            [NSAttributedString attributedStringWithAttachment:userLevelIcon];
        })];
        
        NSString* userNick = [model.extension valueForKey:@"userNick"];
        NSString* reducedNick = nil;
        if (userNick.length > 1) {
            reducedNick = [userNick substringToIndex:1];
            reducedNick = [reducedNick stringByAppendingString:@"**"];
            reducedNick = [reducedNick stringByAppendingString:[userNick substringFromIndex:userNick.length - 1]];
        }
        
        UIFont* textFont = [UIFont fontWithName:@"PingFangSC-Regular" size:10];
        [attriStr appendAttributedString:({
            NSAttributedString* nick = [[NSAttributedString alloc] initWithString:[NSString stringWithFormat:@"%@ 正在去买", reducedNick] attributes:@{
                NSFontAttributeName : textFont,
                NSForegroundColorAttributeName : [UIColor whiteColor],
                NSBaselineOffsetAttributeName : @((textFont.ascender - textFont.descender) / 2 + textFont.descender - 1.5)
            }];
            nick;
        })];
        
        self.backgroundColor = [UIColor colorWithHexString:@"#FF5722" alpha:1.0];
        self.attributedText = attriStr;
    }
}

- (void) insertLiveSystemMessage:(ASLRBLiveSystemMessageModel*)model {
    dispatch_async(dispatch_get_main_queue(), ^{
        if (self.unpresentedMessages.count < 10) {
            [self.unpresentedMessages addObject:model];
        }
    });
}

@end
