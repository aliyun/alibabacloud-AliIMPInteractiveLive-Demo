//
//  AIRBDRoomListViewController.m
//  AliInteractiveLiveDemo
//
//  Created by 麦辣 on 2021/6/23.
//

#import "AIRBDRoomListViewController.h"
#import "AIRBDSetRoomViewController.h"
#import "AIRBDRoomInfoModel.h"
#import "AIRBDRoomListCell.h"
#import <Masonry/Masonry.h>
#import <AliInteractiveRoomBundle/AliInteractiveRoomBundle.h>
#import "AIRBDToast.h"

//#import "../View/AIRBDAudienceView.h"
#import "AIRBDAudienceViewController.h"
#import "AIRBDAnchorViewController.h"
@interface AIRBDRoomListViewController ()<UICollectionViewDelegate,UICollectionViewDataSource>

@property (strong, nonatomic) UICollectionView* roomCollectionView;
@property (strong, nonatomic) UIButton* createRoomButton;
@property (strong, nonatomic) NSMutableArray* roomModelArray;
@property (assign, nonatomic) int currentRoomPage;

@end

static const int roomPageSize = 20;

@implementation AIRBDRoomListViewController

#pragma mark - UI

- (UICollectionView *)roomCollectionView{
    if(!_roomCollectionView){
        UICollectionViewFlowLayout *layout = ({
            UICollectionViewFlowLayout *layout = [[UICollectionViewFlowLayout alloc] init];
            layout.itemSize = CGSizeMake(self.view.bounds.size.width*0.5-10,216);//每一个cell的大小
            layout.scrollDirection = UICollectionViewScrollDirectionVertical;//滚动方向
            layout.sectionInset = UIEdgeInsetsMake(5,5,5,5);//四周的边距
            layout.minimumLineSpacing = 10;
            layout.minimumInteritemSpacing = 10;
            layout;
        });
        UICollectionView* collectionView = [[UICollectionView alloc]initWithFrame:self.view.bounds collectionViewLayout:layout];
        collectionView.dataSource = self;
        collectionView.delegate = self;
        [collectionView setBackgroundColor:[UIColor whiteColor]];
        [self.view addSubview:collectionView];
        [collectionView mas_makeConstraints:^(MASConstraintMaker *make) {
            if (@available(iOS 11.0, *)) {
                make.top.equalTo(self.view.mas_safeAreaLayoutGuideTop).with.offset(40);
                make.bottom.equalTo(self.view.mas_safeAreaLayoutGuideBottom);
            } else {
                make.top.equalTo(self.view.mas_top).with.offset(40);
                make.bottom.equalTo(self.view.mas_bottom);
            }
            make.left.equalTo(self.view);
            make.width.equalTo(self.view);
            
        }];
        [collectionView registerClass:[AIRBDRoomListCell class] forCellWithReuseIdentifier:@"room-cell"];
        _roomCollectionView = collectionView;
    }
    return _roomCollectionView;
}

- (UIButton *)createRoomButton{
    if(!_createRoomButton){
        UIButton* button = [[UIButton alloc]init];
        [self.view addSubview:button];
        __weak typeof(self) weakSelf = self;
        [button mas_makeConstraints:^(MASConstraintMaker *make) {
            make.centerX.equalTo(weakSelf.view);
            if (@available(iOS 11.0, *)) {
                make.bottom.equalTo(weakSelf.view.mas_safeAreaLayoutGuideBottom).with.offset(-30);
            } else {
                make.bottom.equalTo(weakSelf.view).with.offset(-30);
            }
            make.width.mas_equalTo(60);
            make.height.mas_equalTo(60);
        }];
        button.layer.masksToBounds = YES;
        button.layer.cornerRadius = 30;
//        [button setBackgroundColor:[UIColor colorWithRed:252.0/255.0 green:119.0/255.0 blue:22.0/255.0 alpha:1.0/1.0]];
        button.contentMode = UIViewContentModeScaleAspectFit;
        [button setBackgroundImage:[UIImage imageNamed:@"img-button_newlive"] forState:UIControlStateNormal];
        [button addTarget:self action:@selector(gotoSetRoomViewController) forControlEvents:UIControlEventTouchUpInside];
        UILabel *label = [[UILabel alloc] init];
        label.frame = CGRectMake(4, 4, 52, 52);
        label.numberOfLines = 0;
        label.text = @"开启\n直播";
        label.lineBreakMode = NSLineBreakByCharWrapping;
        label.textAlignment = NSTextAlignmentCenter;
        label.font = [UIFont fontWithName:@"AlibabaPuHuiTiM" size:16];
        label.textColor = [UIColor colorWithRed:255.0/255.0 green:255.0/255.0 blue:255.0/255.0 alpha:1.0/1.0];
        [button addSubview:label];
        _createRoomButton = button;
    }
    return _createRoomButton;
}

- (void)viewDidLoad {
    [super viewDidLoad];
    [self.view setBackgroundColor:[UIColor whiteColor]];
    [self.view bringSubviewToFront:self.roomCollectionView];
    [self.view bringSubviewToFront:self.createRoomButton];
//    UILabel* label = [[UILabel alloc]init];
//    [self.view addSubview:label];
//    label.text = @"房间列表:标红的为自己的房间";
//    [label mas_makeConstraints:^(MASConstraintMaker *make) {
//        if (@available(iOS 11.0, *)) {
//            make.top.equalTo(self.view.mas_safeAreaLayoutGuideTop);
//        } else {
//            make.top.equalTo(self.view.mas_top);
//        }
//        make.left.equalTo(self.view).with.offset(5);
//        make.width.mas_equalTo(300);
//        make.height.mas_equalTo(40);
//    }];
    self.currentRoomPage = 1;
//    [self pullRoomModels];
}

- (void)viewWillAppear:(BOOL)animated{
    if(!_roomModelArray){
        _roomModelArray = [[NSMutableArray alloc]init];
    }
    self.currentRoomPage = 1;
    [self pullRoomModels];
}

- (void)viewDidDisappear:(BOOL)animated{
    if(_roomModelArray){
        [_roomModelArray removeAllObjects];
    }
}

-(void) pullRoomModels{
    if(!_roomModelArray){
        _roomModelArray = [[NSMutableArray alloc]init];
    }
    [[AIRBRoomEngine sharedInstance] getRoomListWithPageNum:(int32_t)self.currentRoomPage pageSize:roomPageSize onSuccess:^(AIRBRoomEngineRoomListResponse * _Nonnull response) {
        for (AIRBRoomBasicInfo* info in response.roomBasicInfoList) {
            AIRBDRoomInfoModel* model = [[AIRBDRoomInfoModel alloc]init];
            model.roomID = info.roomID;
            model.title = info.title;
            model.userID = self.userID;
            model.ownerID = info.ownerID;
            [self->_roomModelArray addObject:model];
        }
//        if(response.hasMore){
//            self.currentRoomPage += 1;
//        }
        dispatch_async(dispatch_get_main_queue(), ^{
            [self.roomCollectionView reloadData];
        });
    } onFailure:^(NSString * _Nonnull errorMessage) {
        dispatch_async(dispatch_get_main_queue(), ^{
            [[AIRBDToast shareInstance]makeToast:[NSString stringWithFormat:@"获取房间列表失败,%@",errorMessage] duration:3.0];
        });
    }];
}

#pragma mark - UICollectionViewDataSource

- (NSInteger)numberOfSectionsInCollectionView:(UICollectionView *)collectionView{
    return 1;
}

- (NSInteger)collectionView:(UICollectionView *)collectionView numberOfItemsInSection:(NSInteger)section{
    return self.roomModelArray.count;
}

- (__kindof UICollectionViewCell *)collectionView:(UICollectionView *)collectionView cellForItemAtIndexPath:(NSIndexPath *)indexPath{
    AIRBDRoomListCell* cell = [collectionView dequeueReusableCellWithReuseIdentifier:@"room-cell" forIndexPath:indexPath];
    if(self.roomModelArray.count > indexPath.row){
        cell.model = self.roomModelArray[indexPath.row];
    }
    return cell;
}

#pragma mark - UICollectionViewDelegate

- (void)collectionView:(UICollectionView *)collectionView didSelectItemAtIndexPath:(NSIndexPath *)indexPath{
    if(self.roomModelArray.count > indexPath.row){
        AIRBDRoomInfoModel* model = self.roomModelArray[indexPath.row];
        if(![model.ownerID isEqualToString:self.userID]){
            [self gotoAudienceViewControllerWithRoomModel:model];
        }else{
            [self gotoAnchorViewControllerWithRoomModel:model];
        }
    }
}

- (void) gotoSetRoomViewController{
    AIRBDSetRoomViewController *setRoomViewController = [[AIRBDSetRoomViewController alloc]initWithUserID:self.userID config:self.config];
    setRoomViewController.edgesForExtendedLayout = UIRectEdgeNone;
    setRoomViewController.hidesBottomBarWhenPushed = YES;
    [self.navigationController pushViewController:setRoomViewController animated:YES];
}

-(void)gotoAudienceViewControllerWithRoomModel:(AIRBDRoomInfoModel*)model{
    AIRBDAudienceViewController* audienceCiewController = [[AIRBDAudienceViewController alloc]init];
    audienceCiewController.roomModel = model;
    [self.navigationController pushViewController:audienceCiewController animated:YES];
    [self.navigationController setNavigationBarHidden:YES];
    [audienceCiewController enterRoom];
}

-(void)gotoAnchorViewControllerWithRoomModel:(AIRBDRoomInfoModel*)model{
    AIRBDAnchorViewController* anchorViewController = [[AIRBDAnchorViewController alloc]init];
    anchorViewController.roomModel = model;
    [self.navigationController pushViewController:anchorViewController animated:YES];
    [self.navigationController setNavigationBarHidden:YES];
    [anchorViewController enterRoom];
}


@end
