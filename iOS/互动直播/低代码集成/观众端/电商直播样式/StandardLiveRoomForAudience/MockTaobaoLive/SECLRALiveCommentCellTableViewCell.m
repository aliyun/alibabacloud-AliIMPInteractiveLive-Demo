//
//  SECLRALiveCommentCellTableViewCell.m
//  StandardECommerceLiveRoomForAudience
//
//  Created by fernando on 2022/4/15.
//

#import "SECLRALiveCommentCellTableViewCell.h"
#import <Masonry/Masonry.h>
#import <AliStandardLiveRoomBundle/AliStandardLiveRoomBundle.h>
#import "ASLUKEdgeInsetLabel.h"
#import "UIColor+ColorWithHexString.h"
#import "ASLUKResourceManager.h"
#import "SECLRALiveCommentModel.h"

@interface SECLRALiveCommentCellTableViewCell()
@property(nonatomic, assign) BOOL longPressGestureRecognizerAdded;
@property(nonatomic, assign) BOOL tapGestureRecognizerAdded;
@property (strong, nonatomic) ASLUKEdgeInsetLabel* contentLabel;
@property (strong, nonatomic) UIButton* interactionButton;
@end

@implementation SECLRALiveCommentCellTableViewCell

- (void)setSelected:(BOOL)selected animated:(BOOL)animated {
    [super setSelected:selected animated:animated];

    // Configure the view for the selected state
}

- (void)setFrame:(CGRect)frame{
    frame.origin.y += 3;
    frame.size.height -= 6;
    [super setFrame:frame];
}

- (void)setCommentModel:(SECLRALiveCommentModel *)commentModel{
    _commentModel = commentModel;
    [self createContentLabel];
}

- (void)createContentLabel {
    
    if(self.commentLabel){
        [self.commentLabel removeFromSuperview];
    }
    self.commentLabel = [[ASLUKEdgeInsetLabel alloc] init];
    self.commentLabel.layer.cornerRadius = 12.0;
    self.commentLabel.layer.shouldRasterize = YES;
    self.commentLabel.layer.masksToBounds = YES;
    self.commentLabel.userInteractionEnabled = YES;
    self.commentLabel.backgroundColor = [UIColor colorWithHexString:@"#000000" alpha:0.2];
    self.commentLabel.textInsets = UIEdgeInsetsMake(0.0, 3.0, 0.0, 3.0);
    [self.contentView addSubview:self.commentLabel];
    
    [self.commentLabel mas_makeConstraints:^(MASConstraintMaker * _Nonnull make) {
        make.left.mas_equalTo(self);
        make.width.mas_equalTo(self.commentModel.cellWidth);
        make.height.mas_equalTo(self);
    }];
    
    if (_interactionButton) {
        [_interactionButton removeFromSuperview];
    }
    
    if (_commentModel.interactionType == SECLRALiveCommentCellInteractionTypeFollow) {
        _interactionButton = [[UIButton alloc] init];
        [_interactionButton addTarget:self action:@selector(onFollowCurrentAnchorButtonClicked) forControlEvents:UIControlEventTouchUpInside];
        [self.commentLabel addSubview:_interactionButton];
        _interactionButton.backgroundColor = [UIColor whiteColor];
        _interactionButton.layer.cornerRadius = 8;
        _interactionButton.userInteractionEnabled = YES;
        _interactionButton.layer.masksToBounds = YES;
        [_interactionButton setAttributedTitle:[[NSAttributedString alloc] initWithString:@"我也关注"
                                                                         attributes:
                                          @{
                                              NSForegroundColorAttributeName:[UIColor colorWithHexString:@"#000000" alpha:0.6],
                                              NSFontAttributeName:[UIFont fontWithName:@"PingFangSC-Semibold" size:10]
                                          }]
                                forState:UIControlStateNormal];
        [_interactionButton mas_makeConstraints:^(MASConstraintMaker *make) {
            make.size.mas_equalTo(CGSizeMake(52, 16));
            make.centerY.equalTo(self.commentLabel.mas_centerY);
            make.right.equalTo(self.commentLabel.mas_right).with.offset(-6);
        }];
    } else if (_commentModel.interactionType == SECLRALiveCommentCellInteractionTypeComment){
        _interactionButton = [[UIButton alloc] init];
        [self.commentLabel addSubview:_interactionButton];
        [_interactionButton addTarget:self action:@selector(onSendSameCommentButtonClicked) forControlEvents:UIControlEventTouchUpInside];
        _interactionButton.backgroundColor = [UIColor whiteColor];
        _interactionButton.layer.cornerRadius = 8;
        _interactionButton.layer.masksToBounds = YES;
        _interactionButton.userInteractionEnabled = YES;
        [_interactionButton setAttributedTitle:[[NSAttributedString alloc] initWithString:@"+1"
                                                                         attributes:
                                          @{
                                              NSForegroundColorAttributeName:[UIColor colorWithHexString:@"#000000" alpha:0.6],
                                              NSFontAttributeName:[UIFont fontWithName:@"PingFangSC-Semibold" size:10]
                                          }]
                                forState:UIControlStateNormal];
        [_interactionButton mas_makeConstraints:^(MASConstraintMaker *make) {
            make.size.mas_equalTo(CGSizeMake(24, 16));
            make.centerY.equalTo(self.commentLabel.mas_centerY);
            make.right.equalTo(self.commentLabel.mas_right).with.offset(-5.5);
        }];
    }
    
    if (_contentLabel) {
        [_contentLabel removeFromSuperview];
    }
    
    _contentLabel = [[ASLUKEdgeInsetLabel alloc] init];
    _contentLabel.numberOfLines = 0;
    _contentLabel.textAlignment = NSTextAlignmentLeft;
    [self.commentLabel addSubview:_contentLabel];
    [_contentLabel mas_makeConstraints:^(MASConstraintMaker * _Nonnull make) {
        make.left.equalTo(self.commentLabel.mas_left).with.offset(6);
        make.top.equalTo(self.commentLabel.mas_top);
        make.bottom.equalTo(self.commentLabel.mas_bottom);
        if (_interactionButton) {
            make.right.equalTo(self.interactionButton.mas_left).with.offset(-3);
        } else {
            make.right.equalTo(self.commentLabel.mas_right).with.offset(-6);
        }
    }];
        
    self.contentLabel.attributedText = _commentModel.textContentAttributedString;
}

- (instancetype)initWithStyle:(UITableViewCellStyle)style reuseIdentifier:(NSString *)reuseIdentifier {
    self = [super initWithStyle:style reuseIdentifier:reuseIdentifier];
    if (self) {
        self.selectionStyle = UITableViewCellSelectionStyleNone;
        self.transform = CGAffineTransformMakeRotation(M_PI);
        self.backgroundColor = [UIColor clearColor];
        self.layer.masksToBounds = YES;
    }
    return self;
}

- (void)addLongPressGestureRecognizer {
    if(self.longPressGestureRecognizerAdded){
        //防止重用cell时重复添加手势
        return;
    }
    UILongPressGestureRecognizer *recognizer = [[UILongPressGestureRecognizer alloc] initWithTarget:self action:@selector(longPressGestureAction:)];
    recognizer.minimumPressDuration = 1;
    [self.contentView addGestureRecognizer:recognizer];
    self.longPressGestureRecognizerAdded = YES;
}

- (void)addTapGestureRecognizer {
    if(self.tapGestureRecognizerAdded){
        //防止重用cell时重复添加手势
        return;
    }
    UITapGestureRecognizer *recognizer = [[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(tapGestureAction:)];
    [self.contentView addGestureRecognizer:recognizer];
    self.tapGestureRecognizerAdded = YES;
}

- (void)longPressGestureAction:(UILongPressGestureRecognizer*)recognizer{
//    if ([self.delegate respondsToSelector:@selector(onASLRBCommentCellLongPressGesture:commentModel:)]){
//        [self.delegate onASLRBCommentCellLongPressGesture:recognizer commentModel:self.commentModel];
//    }
}

- (void)tapGestureAction:(UITapGestureRecognizer*)recognizer{
//    if ([self.delegate respondsToSelector:@selector(onASLRBCommentCellTapGesture:commentModel:)]){
//        [self.delegate onASLRBCommentCellTapGesture:recognizer commentModel:self.commentModel];
//    }
}

#pragma mark InteractionButton Selectors

- (void) onSendSameCommentButtonClicked {
    [self.delegate onLiveCommentCellInteraction:SECLRALiveCommentCellInteractionTypeComment extension:@{
        @"comment" : self.commentModel.rawModel.sentContent ?: @""
    }];
}

- (void) onFollowCurrentAnchorButtonClicked {
    [self.delegate onLiveCommentCellInteraction:SECLRALiveCommentCellInteractionTypeFollow extension:nil];
    
    [self.interactionButton removeFromSuperview];
    
    [self.commentLabel mas_updateConstraints:^(MASConstraintMaker * _Nonnull make) {
        make.width.mas_equalTo(self.commentModel.cellWidth - self.commentModel.interactionWidgetWidth);
    }];
}

@end
