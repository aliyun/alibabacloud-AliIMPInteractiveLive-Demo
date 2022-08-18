//
//  ASLRBLiveCommentView.m
//  AliStandardLiveRoomBundle
//
//  Created by fernando on 2021/10/25.
//

#import "ASLRBLiveCommentView.h"
#import <Masonry/Masonry.h>
 

#import "ASLRBLiveSystemMessageLabel.h"
#import "ASLRBLiveSystemMessageModel.h"
#import "CommentView/ASLRBCommentTableView.h"
#import "../CommonTools/UIColor+ColorWithHexString.h"
#import "../CommonViews/InsetLabel/ASLRBEdgeInsetLabel.h"
#import "ASLRBLiveCommentViewProtocol.h"
#import "ASLRBLiveCommentModel.h"
#import "../CommonTools/ASLRBCommonMacros.h"
#import "../LiveRoomSetup/ASLRBLiveCommentViewConfig.h"


@interface ASLRBLiveCommentView() <ASLRBCommentViewDelegate,ASLRBLiveCommentViewProtocol, ASLRBLiveSystemMessageLabelDelegate>
@property (strong, nonatomic) ASLRBCommentTableView* internalCommentView;
@property (strong, nonatomic) ASLRBEdgeInsetLabel* unpresentedCommentNotificationLabel;
@property (assign, nonatomic) int32_t commentViewActualHeight;
@property (strong, nonatomic) ASLRBLiveCommentViewConfig* config;
@end

@implementation ASLRBLiveCommentView

@synthesize delegate = _delegate;
@synthesize liveSystemMessageLabel = _liveSystemMessageLabel;

- (UILabel*)liveSystemMessageLabel {
    
    if (_config.liveCommentViewHidden || _config.liveSystemMessageLabelHidden) {
        return nil;
    }
    
    if (!_liveSystemMessageLabel) {
        _liveSystemMessageLabel = [[ASLRBLiveSystemMessageLabel alloc] init];
        ((ASLRBLiveSystemMessageLabel*)_liveSystemMessageLabel).delegate = self;
        _liveSystemMessageLabel.backgroundColor = [UIColor colorWithRed:0.0 green:0.0 blue:0.0 alpha:0.2];
        _liveSystemMessageLabel.layer.cornerRadius = 15;
        _liveSystemMessageLabel.layer.masksToBounds = YES;
        _liveSystemMessageLabel.font = [UIFont fontWithName:@"PingFangSC-Semibold" size:12];
        _liveSystemMessageLabel.textAlignment = NSTextAlignmentCenter;
        _liveSystemMessageLabel.textColor = [UIColor colorWithHexString:@"#FFFFFF" alpha:1.0];
        _liveSystemMessageLabel.alpha = 0.0;
        [self addSubview:_liveSystemMessageLabel];
        [_liveSystemMessageLabel mas_makeConstraints:^(MASConstraintMaker * _Nonnull make) {
            make.size.mas_equalTo(CGSizeMake(150, 26));
            make.left.equalTo(self.mas_left);
            make.bottom.equalTo(self.internalCommentView.mas_top);
        }];
    }
    return _liveSystemMessageLabel;
}

- (ASLRBCommentTableView*)internalCommentView {
    
    if (_config.liveCommentViewHidden) {
        return nil;
    }
    
    if (!_internalCommentView) {
        _internalCommentView = [[ASLRBCommentTableView alloc] initWitConfig:_config];
        _internalCommentView.commentDelegate = self;
        _internalCommentView.alpha = 0.0;
        [self addSubview:_internalCommentView];
        [_internalCommentView mas_makeConstraints:^(MASConstraintMaker *make) {
            make.left.equalTo(self.mas_left);
            make.right.equalTo(self.mas_right);
            make.bottom.equalTo(self.mas_bottom);
            make.top.equalTo(self.mas_bottom).with.offset(-1);
        }];

        ASLRBLiveCommentModel* model = [[ASLRBLiveCommentModel alloc] init];
        model.sentContent = _config.liveCommentWarningNotice;
        model.sentContentColor = _config.liveCommentWarningNoticeTextColor;
        [_internalCommentView insertNewComment:model presentedCompulsorily:YES];
        
        [_internalCommentView startPresenting];
    }
    return _internalCommentView;
}

- (ASLRBEdgeInsetLabel*) unpresentedCommentNotificationLabel {
    
    if (_config.liveCommentViewHidden || _config.liveUnreadCommentCountLabelHidden) {
        return nil;
    }
    
    if (!_unpresentedCommentNotificationLabel) {
        _unpresentedCommentNotificationLabel = [[ASLRBEdgeInsetLabel alloc] init];
        _unpresentedCommentNotificationLabel.textInsets = UIEdgeInsetsMake(0.0, 6.0, 0.0, 6.0);
        _unpresentedCommentNotificationLabel.clipsToBounds = YES;
        _unpresentedCommentNotificationLabel.alpha = 0.0;
        _unpresentedCommentNotificationLabel.backgroundColor = [UIColor whiteColor];
        _unpresentedCommentNotificationLabel.layer.cornerRadius = 8.0;
        _unpresentedCommentNotificationLabel.layer.masksToBounds = YES;
        _unpresentedCommentNotificationLabel.font = [UIFont fontWithName:@"PingFangSC-Regular" size:10];
        _unpresentedCommentNotificationLabel.textColor = _config.liveUnreadCommentCountLabelTextColor;
        _unpresentedCommentNotificationLabel.userInteractionEnabled = YES;
        [self addSubview:_unpresentedCommentNotificationLabel];
        [_unpresentedCommentNotificationLabel mas_makeConstraints:^(MASConstraintMaker * _Nonnull make) {
            make.bottom.equalTo(self.mas_bottom);
            make.left.equalTo(self.mas_left);
            make.height.mas_equalTo(22);
            make.width.mas_equalTo(64);
        }];
        
        UIButton* actionButton = [[UIButton alloc] init];
        actionButton.backgroundColor = [UIColor clearColor];
        [_unpresentedCommentNotificationLabel addSubview:actionButton];
        [actionButton addTarget:self action:@selector(onUnpresentedCommentLabelClicked:) forControlEvents:UIControlEventTouchUpInside];
        [actionButton mas_makeConstraints:^(MASConstraintMaker * _Nonnull make) {
            make.center.equalTo(_unpresentedCommentNotificationLabel);
            make.size.equalTo(_unpresentedCommentNotificationLabel);
        }];
    }
    return _unpresentedCommentNotificationLabel;
}

- (void) setShowComment:(BOOL)showComment {
    dispatch_async(dispatch_get_main_queue(), ^{
        self.internalCommentView.alpha = showComment ? 1.0 : 0.0;
    });
}

- (void) setShowLiveSystemMessage:(BOOL)showLiveSystemMessage {
    ((ASLRBLiveSystemMessageLabel*)_liveSystemMessageLabel).canPresenting = showLiveSystemMessage;
}

- (BOOL) showLiveSystemMessage {
    return ((ASLRBLiveSystemMessageLabel*)_liveSystemMessageLabel).canPresenting;
}

#pragma mark -Lifecycle

- (instancetype) initWithConfig:(ASLRBLiveCommentViewConfig *)config {
    self = [super init];
    if (self) {
        _config = config;
        [self bringSubviewToFront:self.internalCommentView];
        [self bringSubviewToFront:self.liveSystemMessageLabel];
        [self bringSubviewToFront:self.unpresentedCommentNotificationLabel];
    }
    return self;
}

- (void) dealloc {
    [_internalCommentView stopPresenting];
    [((ASLRBLiveSystemMessageLabel*)_liveSystemMessageLabel) stopPresenting];
}

#pragma mark -Public Methods

- (void) insertLiveSystemMessage:(NSString *)message {
    [self insertLiveSystemMessageModel:({
        ASLRBLiveSystemMessageModel* model = [[ASLRBLiveSystemMessageModel alloc] init];
        model.rawMessage = message;
        model;
    })];
}

- (void) insertLiveSystemMessageModel:(ASLRBLiveSystemMessageModel *)messageModel {
    if ([self.delegate respondsToSelector:@selector(onASLRBLiveSystmeMessageJustAboutToPresent:)]) {
        [self.delegate onASLRBLiveSystmeMessageJustAboutToPresent:messageModel];
    }
    [(ASLRBLiveSystemMessageLabel*)self.liveSystemMessageLabel insertLiveSystemMessage:messageModel];
}

- (void) insertLiveComment:(ASLRBLiveCommentModel *)comment presentedCompulsorily:(BOOL)presentedCompulsorily{
    if ([self.delegate respondsToSelector:@selector(onASLRBLiveCommentJustAboutToPresent:)]) {
        [self.delegate onASLRBLiveCommentJustAboutToPresent:comment];
    }
    [self.internalCommentView insertNewComment:comment presentedCompulsorily:presentedCompulsorily];
}

- (void) insertLiveComment:(NSString *)content commentSenderNick:(NSString *)nick commentSenderID:(NSString*)userID presentedCompulsorily:(BOOL)presentedCompulsorily{
    ASLRBLiveCommentModel* model = [[ASLRBLiveCommentModel alloc] init];
    if (nick) {
        model.senderNick = nick;
    }
    model.senderID = userID;
    model.sentContent = content;
    
    [self insertLiveComment:model presentedCompulsorily:presentedCompulsorily];
}

- (void) updateLayoutRotated:(BOOL)rotated{
    if (!rotated){  // 竖屏
        [self mas_remakeConstraints:^(MASConstraintMaker * _Nonnull make) {
            if (@available(iOS 11.0, *)) {
                make.bottom.equalTo(self.superview.mas_safeAreaLayoutGuideBottom).with.offset(-59);
                make.left.equalTo(self.superview.mas_safeAreaLayoutGuideLeft).with.offset(10);
            } else {
                make.bottom.equalTo(self.superview).with.offset(-59);
                make.left.equalTo(self.superview.mas_left).with.offset(10);
            }
            make.right.equalTo(self.superview.mas_right).with.offset(-1 * kLiveCommentPortraitRightGap);
            make.height.mas_equalTo(kLiveCommentPortraitHeight);
        }];
        
        [self.internalCommentView mas_updateConstraints:^(MASConstraintMaker * _Nonnull make) {
                make.top.equalTo(self.mas_bottom).with.offset(-1 * MIN(self.commentViewActualHeight, MAX(kLiveCommentPortraitHeight - 28, 0)));
            }];
    } else{ // 横屏
        [self mas_remakeConstraints:^(MASConstraintMaker * _Nonnull make) {
            if (@available(iOS 11.0, *)) {
                make.bottom.equalTo(self.superview.mas_safeAreaLayoutGuideBottom).with.offset(-59);
                make.left.equalTo(self.superview.mas_safeAreaLayoutGuideLeft).with.offset(10);
            } else {
                make.bottom.equalTo(self.superview).with.offset(-59);
                make.left.equalTo(self.superview.mas_left).with.offset(10);
            }
            make.right.equalTo(self.superview.mas_right).with.offset(-1 * kLiveCommentLandscapeRightGap);
            make.height.mas_equalTo(kLiveCommentLandscapeHeight);
        }];
        
        [self.internalCommentView mas_updateConstraints:^(MASConstraintMaker * _Nonnull make) {
            make.top.equalTo(self.mas_bottom).with.offset(-1 * MIN(self.commentViewActualHeight, MAX(kLiveCommentLandscapeHeight - 28, 0)));
        }];
    }
}

#pragma mark -unpresentedCommentLabelClicked action

- (void) onUnpresentedCommentLabelClicked:(UIButton*)sender {
    [self.internalCommentView scrollToNewestComment];
}

#pragma mark -ASLRBCommentViewDelegate

- (void) actionWhenUnpresentedCommentCountChange:(int32_t)count {
    if (count > 0) {
        if (self.unpresentedCommentNotificationLabel) {
            self.unpresentedCommentNotificationLabel.text = [NSString stringWithFormat:@"%d条新消息", count];
            CGSize sizeNew = [self.unpresentedCommentNotificationLabel.text sizeWithAttributes:@{NSFontAttributeName:self.unpresentedCommentNotificationLabel.font}];
            [self.unpresentedCommentNotificationLabel mas_updateConstraints:^(MASConstraintMaker * _Nonnull make) {
                make.width.mas_equalTo(sizeNew.width + 18);
            }];
            self.unpresentedCommentNotificationLabel.alpha = 1.0;
        }
    } else {
        self.unpresentedCommentNotificationLabel.alpha = 0.0;
    }
}

- (void) actionWhenOneCommentPresentedWithActualHeight:(int32_t)height {
    self.commentViewActualHeight = height;
    
    if (height > self.bounds.size.height - 28) {
        return;
    }
        
    [self.internalCommentView mas_updateConstraints:^(MASConstraintMaker * _Nonnull make) {
        make.top.equalTo(self.mas_bottom).with.offset(-1 * MIN(height, MAX(self.bounds.size.height - 28, 0)));
    }];
    [self layoutIfNeeded];
}

- (void) actionWhenCommentJustAboutToPresent:(ASLRBLiveCommentModel *)model {
    
}

-(void) actionWhenCommentCellLongPressed:(ASLRBLiveCommentModel *)commentModel{
    if ([self.delegate respondsToSelector:@selector(onASLRBLiveCommentViewCommentLongPressed:)]) {
        [self.delegate onASLRBLiveCommentViewCommentLongPressed:commentModel];
    }
}

-(void) actionWhenCommentCellTapped:(ASLRBLiveCommentModel *)commentModel{
    if ([self.delegate respondsToSelector:@selector(onASLRBLiveCommentViewCommentTapped:)]) {
        [self.delegate onASLRBLiveCommentViewCommentTapped:commentModel];
    }
}

#pragma mark -ASLRBLiveSystemMessageLabelDelegate

- (void) actionWhenSystemMessageJustAboutToPresent:(ASLRBLiveSystemMessageModel *)model {
    
}

@end
