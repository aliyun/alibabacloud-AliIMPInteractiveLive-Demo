//
//  AIRBDRoomListCell.m
//  AliInteractiveLiveDemo
//
//  Created by 麦辣 on 2021/6/23.
//

#import "AIRBDRoomListCell.h"
#import "AIRBDRoomInfoModel.h"
@interface AIRBDRoomListCell()

@property (strong, nonatomic) UIImageView* imageView;
@property (strong, nonatomic) UIImageView* userImage;
@property (strong, nonatomic) UILabel* roomTitleLabel;
@property (strong, nonatomic) UILabel* roomIDLabel;

@end

@implementation AIRBDRoomListCell

- (UIImageView *)imageView{
    if(!_imageView){
        UIImageView* imageView = [[UIImageView alloc] initWithFrame:self.bounds];
        imageView.contentMode = UIViewContentModeScaleAspectFill;
        [imageView setImage:[UIImage imageNamed:@"img-roomCell"]];
        imageView.layer.masksToBounds = YES;
        _imageView = imageView;
    }
    return _imageView;
}

- (UIImageView *)userImage{
    if(!_userImage){
        UIImageView* imageView = [[UIImageView alloc] initWithFrame:CGRectMake(7, 180, 30, 30)];
        imageView.contentMode = UIViewContentModeScaleAspectFill;
        imageView.layer.masksToBounds = YES;
        imageView.layer.cornerRadius = 20;
        [imageView setImage:[UIImage imageNamed:@"img-user-default"]];
        _userImage = imageView;
    }
    return _userImage;
}

- (UILabel *)roomTitleLabel{
    if(!_roomTitleLabel){
        UILabel *label = [[UILabel alloc]initWithFrame:CGRectMake(43, 172, 183, 16)];
        label.textAlignment = NSTextAlignmentLeft;
        label.font = [UIFont fontWithName:@"PingFangSC-Semibold" size:13];
        label.textColor = [UIColor colorWithRed:255.0/255.0 green:255.0/255.0 blue:255.0/255.0 alpha:1.0/1.0];
        _roomTitleLabel = label;
    }
    return _roomTitleLabel;
}

- (UILabel *)roomIDLabel{
    if(!_roomIDLabel){
        UILabel *label = [[UILabel alloc]initWithFrame:CGRectMake(43, 196, 104, 14)];
        label.textAlignment = NSTextAlignmentLeft;
        label.font = [UIFont fontWithName:@"PingFangSC-Regular" size:12];
        label.textColor = [UIColor colorWithRed:255.0/255.0 green:255.0/255.0 blue:255.0/255.0 alpha:1.0/1.0];
        _roomIDLabel = label;
    }
    return _roomIDLabel;
}

- (void)setModel:(AIRBDRoomInfoModel *)model{
    //在这里加载UI
    _model = model;
    self.layer.masksToBounds = YES;
    self.layer.cornerRadius = 10;
    dispatch_async(dispatch_get_main_queue(), ^{
        [self addSubview:self.imageView];
        UIImageView *shadow = [[UIImageView alloc]initWithFrame:self.bounds];
        [shadow setBackgroundColor:[UIColor colorWithWhite:0 alpha:0.2]];
        [self addSubview:shadow];
        [self addSubview:self.userImage];

        [self addSubview:self.roomTitleLabel];
        [self addSubview:self.roomIDLabel];
        self.roomTitleLabel.text = model.title;
        if([self.model.ownerID isEqualToString:self.model.userID]){
            [self.roomTitleLabel setTextColor:[UIColor redColor]];
        }else{
            //重设为白色是不可省略的，否则重用cell时别的cell也会变红
            [self.roomTitleLabel setTextColor:[UIColor whiteColor]];
        }
        self.roomIDLabel.text = [NSString stringWithFormat:@"ID:%@",model.roomID];
    });

}

@end
