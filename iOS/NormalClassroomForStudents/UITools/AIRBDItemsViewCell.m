//
//  AIRBDItemsViewCell.m
//  AliInteractiveRoomDemo
//
//  Created by 麦辣 on 2021/6/7.
//

#import "AIRBDItemsViewCell.h"
#import <Masonry/Masonry.h>
@interface AIRBDItemsViewCell()
@end

@implementation AIRBDItemsViewCell

- (instancetype)initWithStyle:(UITableViewCellStyle)style reuseIdentifier:(NSString *)reuseIdentifier{
    self = [super initWithStyle:style reuseIdentifier:reuseIdentifier];
    self.backgroundColor = [UIColor clearColor];
    self.textLabel.numberOfLines = 0;
    self.textLabel.textAlignment = NSTextAlignmentLeft;
    [self.textLabel setFont:[UIFont systemFontOfSize:16]];
    self.textLabel.textColor = [UIColor whiteColor];
    [self sizeToFit];
    
    return self;
}

- (void)awakeFromNib {
    [super awakeFromNib];
    // Initialization code
}

- (void)setCellID:(NSString *)cellID{
    _cellID = cellID;
}


- (void)touchesBegan:(NSSet<UITouch *> *)touches withEvent:(UIEvent *)event{
    [_cellDelegate useItem:_cellID];
}


@end
