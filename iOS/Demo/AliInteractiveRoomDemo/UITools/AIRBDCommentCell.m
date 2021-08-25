//
//  AIRBDCommentCell.m
//  AliInteractiveLiveDemo
//
//  Created by 麦辣 on 2021/6/7.
//

#import "AIRBDCommentCell.h"
#import "AIRBDCommentModel.h"
#import <Masonry/Masonry.h>
@interface AIRBDCommentCell()

@end
@implementation AIRBDCommentCell

- (void)awakeFromNib {
    [super awakeFromNib];
}

- (void)setFrame:(CGRect)frame{
    frame.origin.y +=3;
    frame.size.height -=6;
    [super setFrame:frame];
}

- (void)setCommentModel:(AIRBDCommentModel *)commentModel{
    _commentModel = commentModel;
    if(self.commentLabel){
        [self.commentLabel removeFromSuperview];
    }
    if(_commentModel.commentStyle == BulletStyle){
        self.commentLabel = [[UILabel alloc] init];
        self.commentLabel.layer.cornerRadius = 3.0;
        [self.contentView addSubview:self.commentLabel];
        __weak typeof(self) weakSelf = self;
        [self.commentLabel mas_makeConstraints:^(MASConstraintMaker * _Nonnull make) {
            make.right.equalTo(weakSelf).with.offset(-3);
            make.bottom.equalTo(weakSelf).with.offset(-2);
            make.top.equalTo(weakSelf).with.offset(2);
            make.left.equalTo(weakSelf).with.offset(3);
        }];
        [self.commentLabel setTextColor:_commentModel.color];
        self.backgroundColor = [UIColor colorWithWhite:0.9 alpha:0.2];
        self.commentLabel.numberOfLines = 0;
        self.commentLabel.textAlignment = NSTextAlignmentLeft;
        [self.commentLabel setFont:[UIFont systemFontOfSize:13]];
        self.commentLabel.text = [_commentModel description];
        self.transform = CGAffineTransformMakeRotation(M_PI);
    }else if (_commentModel.commentStyle == WhiteboardStyle){
        self.commentLabel = [[UILabel alloc] init];
        self.commentLabel.layer.cornerRadius = 3.0;
        [self.contentView addSubview:self.commentLabel];
        __weak typeof(self) weakSelf = self;
        [self.commentLabel mas_makeConstraints:^(MASConstraintMaker * _Nonnull make) {
            make.right.equalTo(weakSelf).with.offset(-3);
            make.bottom.equalTo(weakSelf).with.offset(-2);
            make.top.equalTo(weakSelf).with.offset(2);
            make.left.equalTo(weakSelf).with.offset(3);
        }];
        [self.commentLabel setTextColor:[UIColor blackColor]];
        self.backgroundColor = [UIColor colorWithWhite:0.9 alpha:0.2];
        self.commentLabel.numberOfLines = 0;
        self.commentLabel.textAlignment = NSTextAlignmentLeft;
        [self.commentLabel setFont:[UIFont systemFontOfSize:self.bounds.size.width*13.0/203]];
        self.commentLabel.text = [_commentModel description];
    }else if (_commentModel.commentStyle == BulletStyleNew){
        self.backgroundColor = [UIColor colorWithWhite:0.2 alpha:0.2];
        self.layer.masksToBounds = YES;
        self.layer.cornerRadius = 3;
        self.commentLabel = [[UILabel alloc] init];
        self.commentLabel.layer.cornerRadius = 3.0;
        [self.contentView addSubview:self.commentLabel];
        __weak typeof(self) weakSelf = self;
        [self.commentLabel mas_makeConstraints:^(MASConstraintMaker * _Nonnull make) {
            make.right.equalTo(weakSelf).with.offset(-3);
            make.bottom.equalTo(weakSelf).with.offset(-2);
            make.top.equalTo(weakSelf).with.offset(2);
            make.left.equalTo(weakSelf).with.offset(3);
        }];
        self.commentLabel.numberOfLines = 0;
        self.commentLabel.textColor = [UIColor whiteColor];
        self.commentLabel.textAlignment = NSTextAlignmentLeft;
        [self.commentLabel setFont:[UIFont systemFontOfSize:14]];
        self.transform = CGAffineTransformMakeRotation(M_PI);
        NSMutableAttributedString* attributedString = [[NSMutableAttributedString alloc]initWithString:[commentModel description]];
        if([_commentModel.senderName length]>0){
            [attributedString addAttribute:NSForegroundColorAttributeName value:_commentModel.color range:[[attributedString string] rangeOfString:_commentModel.senderName]];
        }
        self.commentLabel.attributedText = attributedString;
    }
}

- (instancetype)initWithStyle:(UITableViewCellStyle)style reuseIdentifier:(NSString *)reuseIdentifier {
    self = [super initWithStyle:style reuseIdentifier:reuseIdentifier];
    if (self) {
        self.selectionStyle = UITableViewCellSelectionStyleNone;
    }
    return self;
}

- (void)setSelected:(BOOL)selected animated:(BOOL)animated {
    [super setSelected:selected animated:animated];
}

//-(CGFloat)height{
//    NSDictionary *atrri = @{NSFontAttributeName: [UIFont systemFontOfSize:14]};
//    CGRect rect = [self.textLabel.text boundingRectWithSize:CGSizeMake(self.bounds.size.width, 1000) options:NSStringDrawingUsesLineFragmentOrigin attributes:atrri context:nil];
//    return rect.size.height+20;
//}



//-(void)setCommentStyle:(CommentStyle)commentStyle{
//    _commentStyle = commentStyle;
//    if(_commentStyle == CommentStyleBullets){
//        self.transform = CGAffineTransformMakeRotation(M_PI);
//        self.backgroundColor = [UIColor clearColor];
//        self.textLabel.numberOfLines = 0;
//        self.textLabel.textAlignment = NSTextAlignmentLeft;
//        [self.textLabel setFont:[UIFont systemFontOfSize:16]];
//        [self.textLabel sizeToFit];
//    }else if (_commentStyle == CommentStyleWhite){
//        self.backgroundColor = [UIColor whiteColor];
//        self.textLabel.numberOfLines = 0;
//        self.textLabel.textAlignment = NSTextAlignmentLeft;
//        [self.textLabel setFont:[UIFont systemFontOfSize:16]];
//        [self.textLabel sizeToFit];
//    }else{
//        self.backgroundColor = [UIColor whiteColor];
//        self.textLabel.numberOfLines = 0;
//        self.textLabel.textAlignment = NSTextAlignmentLeft;
//        [self.textLabel setFont:[UIFont systemFontOfSize:16]];
//        [self.textLabel sizeToFit];
//    }
//}


@end
