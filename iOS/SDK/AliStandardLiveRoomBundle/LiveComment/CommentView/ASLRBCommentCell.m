//
//  AIRBDCommentCell.m
//  AliInteractiveLiveDemo
//
//  Created by 麦辣 on 2021/6/7.
//

#import "ASLRBCommentCell.h"
#import "../ASLRBLiveCommentModel.h"
#import "../../CommonTools/UIColor+ColorWithHexString.h"
#import "ASLRBLogger.h"
#import <Masonry/Masonry.h>
#import "../../LiveRoomSetup/ASLRBLiveCommentViewConfig.h"

@interface ASLRBCommentCell()

@property(nonatomic, assign) BOOL longPressGestureRecognizerAdded;
@property(nonatomic, assign) BOOL tapGestureRecognizerAdded;

@end

@implementation ASLRBCommentCell

- (void)awakeFromNib {
    [super awakeFromNib];
}

- (void)setFrame:(CGRect)frame{
    frame.origin.y += 3;
    frame.size.height -= 6;
    [super setFrame:frame];
}

- (void) layoutSubviews {
    [super layoutSubviews];
    
    NSDictionary *atrri = @{NSFontAttributeName: _config.liveCommentLabelTextFont};
    CGFloat height = [self.commentModel.fullCommentString boundingRectWithSize:CGSizeMake(self.bounds.size.width - 6, MAXFLOAT)
                                                                       options:NSStringDrawingUsesLineFragmentOrigin|NSStringDrawingUsesFontLeading
                                                                    attributes:atrri
                                                                       context:nil].size.height + 20;
    CGFloat width = MIN([self.commentModel.fullCommentString boundingRectWithSize:CGSizeMake(MAXFLOAT, height)
                                                                          options:NSStringDrawingUsesLineFragmentOrigin|NSStringDrawingUsesFontLeading
                                                                       attributes:atrri
                                                                          context:nil].size.width + 6, self.bounds.size.width - 6);
    
    [self.commentLabel mas_updateConstraints:^(MASConstraintMaker * _Nonnull make) {
        make.width.mas_equalTo(width + 6);
    }];
}

- (void)setCommentModel:(ASLRBLiveCommentModel *)commentModel{
    _commentModel = commentModel;
    if(self.commentLabel){
        [self.commentLabel removeFromSuperview];
    }
    self.backgroundColor = [UIColor clearColor];
    self.layer.masksToBounds = YES;
    //        self.layer.cornerRadius = 12;
    self.commentLabel = [[ASLRBEdgeInsetLabel alloc] init];
    self.commentLabel.layer.cornerRadius = _config.liveCommentLabelCornerRadius;
    self.commentLabel.layer.shouldRasterize = YES; //圆角缓存
    self.commentLabel.layer.masksToBounds = YES;
    self.commentLabel.backgroundColor = _config.liveCommentLabelBackgroundColor;
    self.commentLabel.textInsets = UIEdgeInsetsMake(0.0, 3.0, 0.0, 3.0);
    [self.contentView addSubview:self.commentLabel];
    
    [self.commentLabel mas_makeConstraints:^(MASConstraintMaker * _Nonnull make) {
        make.left.mas_equalTo(self);
        make.width.mas_equalTo(self.bounds.size.width);
        make.height.mas_equalTo(self);
    }];
    self.commentLabel.numberOfLines = 0;
    self.commentLabel.textColor = commentModel.sentContentColor;
    self.commentLabel.textAlignment = NSTextAlignmentLeft;
    [self.commentLabel setFont:_config.liveCommentLabelTextFont];
    self.transform = CGAffineTransformMakeRotation(M_PI);
    NSMutableAttributedString* attributedString = [[NSMutableAttributedString alloc] initWithString:commentModel.fullCommentString];
    if([_commentModel.senderNick length] > 0) {
        NSRange nickRange = {0, [_commentModel.senderNick length] + 1};
        [attributedString addAttribute:NSForegroundColorAttributeName value:_commentModel.senderNickColor range:nickRange];
        
        NSRange contentRange = {[_commentModel.senderNick length] + 1, [_commentModel.sentContent length]};
        [attributedString addAttribute:NSForegroundColorAttributeName value:_commentModel.sentContentColor range:contentRange];
    } else {
        [attributedString addAttribute:NSForegroundColorAttributeName value:_commentModel.sentContentColor range:[[attributedString string] rangeOfString:_commentModel.sentContent]];
    }
    self.commentLabel.attributedText = attributedString;
    //    }
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
    if ([self.delegate respondsToSelector:@selector(onASLRBCommentCellLongPressGesture:commentModel:)]){
        [self.delegate onASLRBCommentCellLongPressGesture:recognizer commentModel:self.commentModel];
    }
}

- (void)tapGestureAction:(UITapGestureRecognizer*)recognizer{
    if ([self.delegate respondsToSelector:@selector(onASLRBCommentCellTapGesture:commentModel:)]){
        [self.delegate onASLRBCommentCellTapGesture:recognizer commentModel:self.commentModel];
    }
}

- (void)showMenuController:(UILongPressGestureRecognizer*)recognizer{
    if(recognizer.state == UIGestureRecognizerStateBegan){
        [self becomeFirstResponder];
        self.backgroundColor = [UIColor colorWithWhite:0.7 alpha:0.2];
        UIMenuItem *one = [[UIMenuItem alloc] initWithTitle:@"复制"action:@selector(menuOne:)];
        UIMenuItem *two = [[UIMenuItem alloc] initWithTitle:@"禁言"action:@selector(menuTwo:)];
        UIMenuItem *three = [[UIMenuItem alloc] initWithTitle:@"删除"action:@selector(menuThree:)];
        
        UIMenuController *menu = [UIMenuController sharedMenuController];
        menu.arrowDirection = UIMenuControllerArrowDefault;
        [menu setMenuItems:[NSArray arrayWithObjects:one, two, three, nil]];
        [menu setTargetRect:self.frame inView:self.superview];
        [menu setMenuVisible:YES animated:YES];
    }
}

//- (BOOL)canBecomeFirstResponder
//{
//    return YES;
//}
//
//- (BOOL)resignFirstResponder{
//    self.backgroundColor = [UIColor colorWithWhite:0.2 alpha:0.2];
//    return YES;
//}

- (BOOL)canPerformAction:(SEL)action withSender:(id)sender{
    if(action == @selector(menuOne:)
       || action == @selector(menuTwo:)
       || action == @selector(menuThree:)){
        return YES;
    }
    return NO;
}

- (void)menuOne:(id)sender{
    LOG("复制");
}
- (void)menuTwo:(id)sender{
    LOG("删除");
}
- (void)menuThree:(id)sender{
    LOG("转发");
}

@end
