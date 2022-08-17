//
//  AIRBDLinkMicCollectionViewHolder.m
//  AliInteractiveRoomDemo
//
//  Created by 刘再勇 on 2022/4/28.
//

#import "AIRBDLinkMicCollectionViewHolder.h"
#import "AIRBDLinkMicCollectionViewCell.h"
#import <Masonry/Masonry.h>

@interface AIRBDLinkMicCollectionViewHolder ()<UICollectionViewDelegate,UICollectionViewDataSource>

@property(nonatomic, strong) UICollectionView* linkMicCollectionView;

@end

@implementation AIRBDLinkMicCollectionViewHolder

- (instancetype)initWithFrame:(CGRect)frame userID:(NSString*)userID userNick:(NSString*)nick{
    self = [super initWithFrame:frame];
    if (self) {
        _userID = userID;
        _userNick = nick;
        
        UICollectionView* collectionView = [[UICollectionView alloc]initWithFrame:CGRectMake(0, 64, self.bounds.size.width, (self.bounds.size.width/3.0 - 8) * 9.0 / 16.0) collectionViewLayout:({
            UICollectionViewFlowLayout *layout = [[UICollectionViewFlowLayout alloc] init];
            layout.itemSize = CGSizeMake(self.bounds.size.width, self.bounds.size.width);//每一个cell的大小
            layout.scrollDirection = UICollectionViewScrollDirectionVertical;//滚动方向
            layout.sectionInset = UIEdgeInsetsMake(0,0,0,0);//四周的边距
            layout.minimumLineSpacing = 4;//滚动方向的间距
            layout.minimumInteritemSpacing = 4;//与滚动方向垂直的间距
            layout;
        })];
        collectionView.dataSource = self;
        collectionView.delegate = self;
        [collectionView setBackgroundColor:[UIColor clearColor]];
        [collectionView registerClass:[AIRBDLinkMicCollectionViewCell class] forCellWithReuseIdentifier:@"linkMicCollectionViewCell"];
        _linkMicCollectionView = collectionView;
        
        [self addSubview:_linkMicCollectionView];
        [_linkMicCollectionView mas_makeConstraints:^(MASConstraintMaker * _Nonnull make) {
            make.edges.equalTo(self);
        }];
    }
    return self;
}

- (void) reloadCollectionViewData{
    dispatch_async(dispatch_get_main_queue(), ^{
        [self.linkMicCollectionView reloadData];
        [self.linkMicCollectionView layoutIfNeeded];
    });
}

#pragma mark - UICollectionViewDataSource

- (NSInteger)numberOfSectionsInCollectionView:(UICollectionView *)collectionView{
    return 1;
}

- (NSInteger)collectionView:(UICollectionView *)collectionView numberOfItemsInSection:(NSInteger)section{
    return [self.delegate.linkMicUserArray count];
}

- (__kindof UICollectionViewCell *)collectionView:(UICollectionView *)collectionView cellForItemAtIndexPath:(NSIndexPath *)indexPath{
    AIRBDLinkMicCollectionViewCell* cell = [collectionView dequeueReusableCellWithReuseIdentifier:@"linkMicCollectionViewCell" forIndexPath:indexPath];
    if(!cell){
        cell = [[AIRBDLinkMicCollectionViewCell alloc]init];
    }
    cell.backgroundColor = [UIColor blackColor];;
    
    if (indexPath.row >= self.delegate.linkMicUserArray.count){
        return cell;
    }
    
    // 本地预览
    NSString* userIDToShow = self.delegate.linkMicUserArray[indexPath.row];
    if ([userIDToShow isEqualToString:self.userID]){
        cell.userID = self.userID;
        [cell setUserNickLabelText:self.userNick videoView:self.delegate.liveRoomVC.linkMicLocalPreview camera:self.delegate.liveRoomVC.isCameraOpened microphone:self.delegate.liveRoomVC.isMicOpened];
        return cell;
    }
    // 他人画面
    ASLRBLinkMicUserModel* userModel = [self.delegate.liveRoomVC.linkMicJoinedUsers objectForKey:userIDToShow];
    cell.userID = userModel.userID;
    [cell setUserNickLabelText:userModel.nickname videoView:userModel.cameraView camera:userModel.isCameraOpened microphone:userModel.isMicOpened];
    return cell;
}

@end
