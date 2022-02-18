//
//  AIRBDAbilityDemonstrationViewController.m
//  AliInteractiveRoomDemo
//
//  Created by fernando on 2021/10/9.
//

#import "AIRBDAbilityDemonstrationViewController.h"
#import <Masonry/Masonry.h>
#import "UIColor+HexColor.h"

@interface AIRBDAbilityDemonstrationViewController ()

@end

@implementation AIRBDAbilityDemonstrationViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    // Do any additional setup after loading the view.
    
    self.view.backgroundColor = [UIColor colorWithHexString:@"#FFFFFF" alpha:1.0];
    
    UILabel* header = [[UILabel alloc] init];
    header.font = [UIFont fontWithName:@"PingFangSC-Semibold" size:16];
    
    header.text = @"更多能力";
    header.textColor = [UIColor colorWithRed:0.0 green:0.0 blue:0.0 alpha:0.6];
    [self.view addSubview:header];
    [header mas_makeConstraints:^(MASConstraintMaker * _Nonnull make) {
        make.width.mas_equalTo(64);
        make.height.mas_equalTo(22);
        make.left.equalTo(self.view.mas_left).with.offset(26);
        make.top.equalTo(self.view.mas_top).with.offset(52);
    }];
    
    UIView* line = [[UIView alloc] init];
    line.backgroundColor = [UIColor colorWithRed:0.0 green:0.0 blue:0.0 alpha:0.1];
    [self.view addSubview:line];
    [line mas_makeConstraints:^(MASConstraintMaker * _Nonnull make) {
        make.width.mas_equalTo(210);
        make.height.mas_equalTo(1);
        make.left.equalTo(self.view.mas_left).with.offset(25);
        make.top.equalTo(self.view.mas_top).with.offset(85);
    }];
    
    
    NSMutableArray* mutableAbilityInfo = [[NSMutableArray alloc] init];
    [mutableAbilityInfo addObject:({
        NSMutableDictionary* info = [[NSMutableDictionary alloc] init];
        [info addEntriesFromDictionary:@{
            @"image" : @"直播-更多能力-商品卡片",
            @"text" : @"商品卡片",
            @"index" : @(0),
            @"enable" : @(YES)
        }];
        info;
    })];
    [mutableAbilityInfo addObject:({
        NSMutableDictionary* info = [[NSMutableDictionary alloc] init];
        [info addEntriesFromDictionary:@{
            @"image" : @"直播-更多能力-商品橱窗",
            @"text" : @"商品橱窗",
            @"index" : @(1),
            @"enable" : @(YES)
        }];
        info;
    })];
    [mutableAbilityInfo addObject:({
        NSMutableDictionary* info = [[NSMutableDictionary alloc] init];
        [info addEntriesFromDictionary:@{
            @"image" : @"直播-更多能力-更新直播",
            @"text" : @"更新直播信息",
            @"index" : @(2),
            @"enable" : @(NO)
        }];
        info;
    })];
    [mutableAbilityInfo addObject:({
        NSMutableDictionary* info = [[NSMutableDictionary alloc] init];
        [info addEntriesFromDictionary:@{
            @"image" : @"直播-更多能力-查询直播",
            @"text" : @"查询直播信息",
            @"index" : @(3),
            @"enable" : @(NO)
        }];
        info;
    })];
    [mutableAbilityInfo addObject:({
        NSMutableDictionary* info = [[NSMutableDictionary alloc] init];
        [info addEntriesFromDictionary:@{
            @"image" : @"直播-更多能力-复制直播ID",
            @"text" : @"复制直播ID",
            @"index" : @(4),
            @"enable" : @(NO)
        }];
        info;
    })];
    [mutableAbilityInfo addObject:({
        NSMutableDictionary* info = [[NSMutableDictionary alloc] init];
        [info addEntriesFromDictionary:@{
            @"image" : @"直播-更多能力-全员自定义消息",
            @"text" : @"全员自定义消息",
            @"index" : @(5),
            @"enable" : @(NO)
        }];
        info;
    })];
    [mutableAbilityInfo addObject:({
        NSMutableDictionary* info = [[NSMutableDictionary alloc] init];
        [info addEntriesFromDictionary:@{
            @"image" : @"直播-更多能力-单点自定义消息",
            @"text" : @"单点自定义消息",
            @"index" : @(6),
            @"enable" : @(NO)
        }];
        info;
    })];
    for (NSMutableDictionary* info in mutableAbilityInfo) {
        UIView* itemView = [[UIView alloc] init];
        [self.view addSubview:itemView];
        [info setObject:itemView forKey:@"view"];
        int index = [[info valueForKey:@"index"] intValue];
        [itemView mas_makeConstraints:^(MASConstraintMaker * _Nonnull make) {
            make.width.mas_equalTo(205);
            make.height.mas_equalTo(40);
            if (index == 0) {
                make.top.equalTo(line.mas_bottom).with.offset(2);
            } else {
                make.top.equalTo(((UIView*)([mutableAbilityInfo[index - 1] valueForKey:@"view"])).mas_bottom).with.offset(2);
            }
            make.left.equalTo(self.view.mas_left).with.offset(20);
        }];
        
        UIImageView* itemHeaderImage = [[UIImageView alloc] initWithImage:[UIImage imageNamed:[info valueForKey:@"image"]]];
        [itemView addSubview:itemHeaderImage];
        [itemHeaderImage mas_makeConstraints:^(MASConstraintMaker * _Nonnull make) {
            make.width.mas_equalTo(22);
            make.height.mas_equalTo(22);
            make.top.equalTo(itemView.mas_top).with.offset(9);
            make.left.equalTo(itemView.mas_left).with.offset(1);
        }];
        
        UIButton* itemTextButton = [[UIButton alloc] init];
        itemTextButton.tag = index;
        itemTextButton.contentHorizontalAlignment = UIControlContentHorizontalAlignmentLeft;
        [itemTextButton addTarget:self action:@selector(abilityButtonAction:) forControlEvents:UIControlEventTouchUpInside];
        [itemTextButton setAttributedTitle:[[NSAttributedString alloc] initWithString:info[@"text"]
                                                                          attributes:
                                           @{
                                               NSForegroundColorAttributeName:[UIColor colorWithRed:0/255.0 green:0/255.0 blue:0/255.0 alpha: [info[@"enable"] boolValue] ? 0.8/1.0 : 0.3/1.0],
                                               NSFontAttributeName:[UIFont fontWithName:@"PingFangSC-Regular" size:14]
                                           }] forState:UIControlStateNormal];
        [itemView addSubview:itemTextButton];
        [itemTextButton mas_makeConstraints:^(MASConstraintMaker * _Nonnull make) {
            make.width.mas_equalTo(98);
            make.height.mas_equalTo(20);
            make.top.equalTo(itemView.mas_top).with.offset(11);
            make.left.equalTo(itemView.mas_left).with.offset(34);
        }];
    }
}

- (void) abilityButtonAction:(UIButton*)sender {
    switch (sender.tag) {
        case 0: {
            if ([self.delegate respondsToSelector:@selector(onGoodsCardItemClicked)]) {
                [self.delegate onGoodsCardItemClicked];
            }
        }
            break;
        case 1: {
            if ([self.delegate respondsToSelector:@selector(onShopWindowItemClicked)]) {
                [self.delegate onShopWindowItemClicked];
            }
        }
            break;
            
        default:
            break;
    }
}

/*
#pragma mark - Navigation

// In a storyboard-based application, you will often want to do a little preparation before navigation
- (void)prepareForSegue:(UIStoryboardSegue *)segue sender:(id)sender {
    // Get the new view controller using [segue destinationViewController].
    // Pass the selected object to the new view controller.
}
*/

@end
