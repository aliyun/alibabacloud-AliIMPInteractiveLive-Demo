//
//  MELLiveCommentTableViewCell.m
//  AliStandardLiveUIKit
//
//  Created by fernando on 2022/6/6.
//

#import "MELLiveCommentTableViewCell.h"
#import <Masonry/Masonry.h>
#import "MELLiveCommentModel.h"
#import "UIColor+ColorWithHexString.h"
#import <AliStandardLiveRoomBundle/AliStandardLiveRoomBundle.h>

@interface MELLiveCommentTableViewCell()
@property (strong, nonatomic) UILabel* nickLabel;
@property (strong, nonatomic) UILabel* commentLabel;
@end

@implementation MELLiveCommentTableViewCell

- (void)awakeFromNib {
    [super awakeFromNib];
    // Initialization code
}

- (void)addSubview:(UIView *)view
{
    if (![view isKindOfClass:[NSClassFromString(@"_UITableViewCellSeparatorView") class]] && view)
        [super addSubview:view];
}

- (void)setSelected:(BOOL)selected animated:(BOOL)animated {
    [super setSelected:selected animated:animated];

    // Configure the view for the selected state
}

-(UILabel*)nickLabel {
    if (!_nickLabel) {
        _nickLabel = [[UILabel alloc] init];
        _nickLabel.font = [UIFont fontWithName:@"PingFangSC-Medium" size:12];
        _nickLabel.textColor = [UIColor colorWithHexString:@"#000000" alpha:0.4];
        [self.contentView addSubview:_nickLabel];
        [_nickLabel mas_makeConstraints:^(MASConstraintMaker * _Nonnull make) {
            make.left.equalTo(self.contentView);
            make.top.equalTo(self.contentView).with.offset(5);
            make.right.equalTo(self.contentView);
            make.height.mas_equalTo(20);
        }];
    }
    return _nickLabel;
}

- (UILabel*)commentLabel {
    if (!_commentLabel) {
        _commentLabel = [[UILabel alloc] init];
        _commentLabel.font = [UIFont fontWithName:@"PingFangSC-Medium" size:14];
        _commentLabel.textColor = [UIColor colorWithHexString:@"#000000" alpha:0.8];
        _commentLabel.numberOfLines = 0;
        _commentLabel.textAlignment = NSTextAlignmentLeft;
        [self.contentView addSubview:_commentLabel];
        [_commentLabel mas_makeConstraints:^(MASConstraintMaker * _Nonnull make) {
            make.top.equalTo(self.nickLabel.mas_bottom);
            make.left.equalTo(self.contentView);
            make.right.equalTo(self.contentView);
            make.bottom.equalTo(self.contentView);
        }];
    }
    return _commentLabel;
}

- (void) setModel:(MELLiveCommentModel *)model {
    _model = model;
    self.nickLabel.text = model.rawModel.senderNick;
    self.commentLabel.text = model.rawModel.sentContent;
}

- (instancetype)initWithStyle:(UITableViewCellStyle)style reuseIdentifier:(NSString *)reuseIdentifier {
    self = [super initWithStyle:style reuseIdentifier:reuseIdentifier];
    if (self) {
        self.selectionStyle = UITableViewCellSelectionStyleNone;
        self.backgroundColor = [UIColor clearColor];
        self.layer.masksToBounds = YES;
    }
    return self;
}

#pragma mark Private Methods

@end
