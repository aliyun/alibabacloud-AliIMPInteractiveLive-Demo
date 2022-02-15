//
//  AIRBDStudentListCell.m
//  AliInteractiveRoomDemo
//
//  Created by fernando on 2021/6/7.
//

#import "AIRBDStudentListCell.h"

#import <Masonry/Masonry.h>

@implementation AIRBDStudentListCell

- (void)awakeFromNib {
    [super awakeFromNib];
    // Initialization code
}

- (void)setSelected:(BOOL)selected animated:(BOOL)animated {
    [super setSelected:selected animated:animated];

    // Configure the view for the selected state
}

- (UIButton *)studentActionButton{
    if(!_studentActionButton){
        _studentActionButton = [[UIButton alloc] init];
        _studentActionButton.layer.cornerRadius = 3.0;
        [_studentActionButton addTarget:self action:@selector(onButtonClicked:) forControlEvents:UIControlEventTouchUpInside];
        [_studentActionButton.titleLabel setFont:[UIFont systemFontOfSize:14]];
        [self.contentView addSubview:self.studentActionButton];
        __weak typeof(self) weakSelf = self;
        [self.studentActionButton mas_makeConstraints:^(MASConstraintMaker * _Nonnull make) {
            make.right.equalTo(weakSelf).with.offset(-8);
            make.bottom.equalTo(weakSelf).with.offset(-2);
            make.top.equalTo(weakSelf).with.offset(2);
            make.width.mas_equalTo(40);
        }];
    }
    return _studentActionButton;
}

- (UIButton *)studentActionButtonEX{
    if(!_studentActionButtonEX){
        _studentActionButtonEX = [[UIButton alloc] init];
        _studentActionButtonEX.layer.cornerRadius = 3.0;
        [_studentActionButtonEX addTarget:self action:@selector(onButtonClicked:) forControlEvents:UIControlEventTouchUpInside];
        [_studentActionButtonEX.titleLabel setFont:[UIFont systemFontOfSize:14]];
        [self.contentView addSubview:_studentActionButtonEX];
        __weak typeof(self) weakSelf = self;
        [_studentActionButtonEX mas_makeConstraints:^(MASConstraintMaker * _Nonnull make) {
            make.right.equalTo(weakSelf.studentActionButton.mas_left).with.offset(-2);
            make.bottom.equalTo(weakSelf).with.offset(-2);
            make.top.equalTo(weakSelf).with.offset(2);
            make.width.mas_equalTo(40);
        }];
    }
    return _studentActionButtonEX;
}


- (instancetype)initWithStyle:(UITableViewCellStyle)style reuseIdentifier:(NSString *)reuseIdentifier {
    self = [super initWithStyle:style reuseIdentifier:reuseIdentifier];
    if (self) {
        self.owner = 0;//默认是老师端的cell
        self.selectionStyle = UITableViewCellSelectionStyleNone;
        self.backgroundColor = [UIColor clearColor];
        self.contentView.backgroundColor = [UIColor clearColor];
        [self studentActionButton];
    }
    return self;
}

- (void) setModel:(AIRBDStudentListItemModel *)model {
    _model = model;
    self.textLabel.text = model.userID;
    self.textLabel.textColor = [UIColor blackColor];
    if(_owner == 0){
        switch (model.status) {
            case AIRBDStudentStatusReadyForCalled: {
                [self.studentActionButton setTitle:@"呼叫" forState:UIControlStateNormal];
                self.studentActionButton.backgroundColor = [UIColor blueColor];
                [self.studentActionButtonEX setHidden:YES];
            }
                break;
            case AIRBDStudentStatusAlreadyOnTheCall:
                [self.studentActionButton setTitle:@"挂断" forState:UIControlStateNormal];
                self.studentActionButton.backgroundColor = [UIColor redColor];
                [self.studentActionButtonEX setHidden:YES];
                break;
            case AIRBDStudentStatusNowBeenCalling:
                [self.studentActionButton setTitle:@"取消" forState:UIControlStateNormal];
                self.studentActionButton.backgroundColor = [UIColor redColor];
                [self.studentActionButtonEX setHidden:YES];
                break;
            case AIRBDStudentStatusNowApplying:
                [self.studentActionButton setTitle:@"同意" forState:UIControlStateNormal];
                self.studentActionButton.backgroundColor = [UIColor greenColor];
                [self.studentActionButtonEX setHidden:NO];
                [self.studentActionButtonEX setTitle:@"拒绝" forState:UIControlStateNormal];
                self.studentActionButtonEX.backgroundColor = [UIColor redColor];
                break;
            default:
                break;
        }
    }else{
        switch (model.status) {
            case AIRBDStudentStatusReadyForCalled:
                for (UIView *obj in self.contentView.subviews) {
                    if([obj isMemberOfClass:[UILabel class]]){
                        UILabel* label = (UILabel*)obj;
                        if([label.text isEqualToString:@"连麦中"]){
                            dispatch_async(dispatch_get_main_queue(), ^{
                                [label removeFromSuperview];
                            });
                            label = nil;
                        }
                    }
                }
                break;
            case AIRBDStudentStatusAlreadyOnTheCall:{
                dispatch_async(dispatch_get_main_queue(), ^{
                    [self.studentActionButton removeFromSuperview];
                });
                UILabel* statusLabel = [[UILabel alloc]init];
                [self.contentView addSubview:statusLabel];
                [statusLabel setText:@"连麦中"];
                [statusLabel setFont:[UIFont systemFontOfSize:18]];
                [statusLabel setTextColor:[UIColor colorWithRed:0.6 green:0.2 blue:0.2 alpha:0.8]];
                __weak typeof(self) weakSelf = self;
                [statusLabel mas_makeConstraints:^(MASConstraintMaker * _Nonnull make) {
                    make.right.equalTo(weakSelf).with.offset(-8);
                    make.bottom.equalTo(weakSelf).with.offset(-2);
                    make.top.equalTo(weakSelf).with.offset(2);
                    make.width.mas_equalTo(60);
                }];
            }
                break;
            case AIRBDStudentStatusNowBeenCalling:
                break;
            case AIRBDStudentStatusNowApplying:
                break;
            default:
                break;
        }
    }
}

- (void) onButtonClicked:(UIButton*)sender {
    AIRBDStudentItemAction action;
    if ([sender.currentTitle isEqualToString:@"呼叫"]) {
        action = AIRBDStudentItemActionCall;
    } else if ([sender.currentTitle isEqualToString:@"挂断"]) {
        action = AIRBDStudentItemActionHangup;
    } else if ([sender.currentTitle isEqualToString:@"取消"]) {
        action = AIRBDStudentItemActionCancelCall;
    } else if ([sender.currentTitle isEqualToString:@"同意"]) {
        action = AIRBDStudentItemActionAccept;
    } else if ([sender.currentTitle isEqualToString:@"拒绝"]) {
        action = AIRBDStudentItemActionReject;
    }else {
        return;
    }
    
    if ([self.delegate respondsToSelector:@selector(onStudentActionButtonClickedWithUserID:action:)]) {
        [self.delegate onStudentActionButtonClickedWithUserID:self.model.userID action:action];
    }
}
@end
