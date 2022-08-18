//
//  AIRBDUserListViewController.m
//  AliInteractiveRoomDemo
//
//  Created by 麦辣 on 2021/7/8.
//

#import "ASLRBUserListViewController.h"
#import "../../CommonTools/ASLRBResourceManager.h"

@interface ASLRBUserListViewController ()<UITableViewDelegate,UITableViewDataSource>

@property(nonatomic,strong) UILabel* titleLabel;
@property(nonatomic,strong) UITableView* userListView;
@property(nonatomic,strong) NSArray<AIRBRoomChannelUser*>* userModels;

@end

static NSString *reuseIdentifier = @"reuseableUserListCell";

@implementation ASLRBUserListViewController

- (UILabel *)titleLabel{
    if(!_titleLabel){
        UILabel* label = [[UILabel alloc]initWithFrame:CGRectMake(0, 10, self.view.bounds.size.width, 30)];
        label.text = @"观众列表";
        label.textAlignment = NSTextAlignmentCenter;
        label.font = [UIFont fontWithName:@"PingFangSC-Semibold" size:16];
        _titleLabel = label;
    }
    return _titleLabel;
}

- (UITableView *)userListView{
    if(!_userListView){
        UITableView* tableView = [[UITableView alloc]initWithFrame:CGRectMake(0, 50, self.view.bounds.size.width, self.view.bounds.size.height-50)];
        tableView.dataSource = self;
        tableView.delegate = self;
        tableView.separatorStyle = UITableViewCellSeparatorStyleNone;
        [tableView registerClass:[UITableViewCell class] forCellReuseIdentifier:reuseIdentifier];
        _userListView = tableView;
    }
    return _userListView;
}

- (void)viewDidLoad {
    [super viewDidLoad];
    self.view.backgroundColor = [UIColor whiteColor];
    [self.view addSubview:self.titleLabel];
    [self.view addSubview:self.userListView];
    self.userModels = [[NSArray alloc]init];
}

- (void)updateUsersWithArray:(NSArray<AIRBRoomChannelUser*>*)users{
    self.userModels = users;
    dispatch_async(dispatch_get_main_queue(), ^{
        [self.userListView reloadData];
    });
}

- (CGFloat)tableView:(UITableView *)tableView heightForRowAtIndexPath:(NSIndexPath *)indexPath {
    return 50;
}

- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView {
    return 1;
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section {
    return self.userModels.count;
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath {

//    AIRBDItemsViewCell* cell = [self dequeueReusableCellWithIdentifier:reuseIdentifier];
    UITableViewCell* cell = [self.userListView cellForRowAtIndexPath:indexPath];
    if(cell == nil){
        cell = [[UITableViewCell alloc]initWithStyle:UITableViewCellStyleDefault reuseIdentifier:reuseIdentifier];
    }
    AIRBRoomChannelUser* userModel = self.userModels[indexPath.row];
    cell.contentMode = UIViewContentModeScaleAspectFit;
    cell.imageView.contentMode = UIViewContentModeScaleAspectFit;
    cell.imageView.image = [UIImage imageNamed:@"img-user-default" inBundle:[ASLRBResourceManager sharedInstance].resourceBundle compatibleWithTraitCollection:nil];
    cell.textLabel.text = userModel.nick;
    cell.textLabel.font = [UIFont fontWithName:@"PingFangSC-Regular" size:16];
    cell.detailTextLabel.text = userModel.openID;
    cell.detailTextLabel.font = [UIFont fontWithName:@"PingFangSC-Regular" size:10];
    
    return cell;
}

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath{
    [self.userListViewDelegate useItem:self.userModels[indexPath.row].openID];
    [tableView deselectRowAtIndexPath:indexPath animated:NO];
}
@end
