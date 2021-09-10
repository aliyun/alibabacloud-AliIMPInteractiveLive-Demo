//
//  AIRBDItemsView.m
//  AliInteractiveRoomDemo
//
//  Created by 麦辣 on 2021/6/7.
//

#import "AIRBDItemsView.h"
#import "AIRBDItemsViewCell.h"
#import <AliInteractiveRoomBundle/AliInteractiveRoomBundle.h>
#import <Masonry/Masonry.h>
@interface AIRBDItemsView ()<UITableViewDelegate,UITableViewDataSource>
@property(nonatomic,strong)NSMutableArray* items;
@end
@implementation AIRBDItemsView


- (instancetype)init
{
    self = [super init];
    if (self) {
        _items = [[NSMutableArray alloc]init];
        self.dataSource = self;
        self.delegate = self;
        [self setBackgroundColor:[UIColor colorWithWhite:0.1 alpha:0.4]];
        self.layer.cornerRadius = 8;
        self.layer.masksToBounds = YES;
        self.itemsStyle = ItemsViewMembersStyle;
    }
    return self;
}

-(void)updateItems:(NSArray *)items{
    if(_itemsStyle == ItemsViewMembersStyle){
        dispatch_async(dispatch_get_main_queue(), ^{
            [self beginUpdates];
            self->_items = [[NSMutableArray alloc]initWithArray:items];
            [self reloadData];
            [self endUpdates];

        });
    }
}

- (CGFloat)tableView:(UITableView *)tableView heightForRowAtIndexPath:(NSIndexPath *)indexPath {
    return 40;
}

- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView {
    return 1;
}


- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section {
    return 20;
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath {
    static NSString *reuseIdentifier = @"reuseableCell";
//    AIRBDItemsViewCell* cell = [self dequeueReusableCellWithIdentifier:reuseIdentifier];
    AIRBDItemsViewCell* cell = [self cellForRowAtIndexPath:indexPath];
    if(cell == nil){
        cell = [[AIRBDItemsViewCell alloc]initWithStyle:UITableViewCellStyleDefault reuseIdentifier:reuseIdentifier];
        cell.cellDelegate = _ItemsViewdelegate;
    }
    
    if(_itemsStyle == ItemsViewMembersStyle){
        if(indexPath.row<_items.count){
            cell.textLabel.text = [NSString stringWithFormat:@"%@  %@",[_items[indexPath.row] valueForKey:@"nick"],[_items[indexPath.row] valueForKey:@"role"]];
            cell.cellID = [_items[indexPath.row] valueForKey:@"openID"];
        }
    }
    return cell;
}


@end
