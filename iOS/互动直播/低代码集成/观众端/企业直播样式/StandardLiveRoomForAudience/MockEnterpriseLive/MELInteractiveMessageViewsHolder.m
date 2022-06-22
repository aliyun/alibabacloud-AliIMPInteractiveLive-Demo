//
//  MELInteractiveMessageViewsHolder.m
//  AliStandardLiveUIKit
//
//  Created by fernando on 2022/6/6.
//

#import "MELInteractiveMessageViewsHolder.h"
#import <Masonry/Masonry.h>
#import "MELLiveCommentTableViewCell.h"
#import "MELLiveCommentModel.h"
#import "UIColor+ColorWithHexString.h"
#import <AliStandardLiveRoomBundle/AliStandardLiveRoomBundle.h>

static NSString* _Nonnull reuseableCellId = @"MELLiveCommentViewCell";

@interface MELInteractiveMessageViewsHolder()<UITableViewDelegate, UITableViewDataSource>
@property (strong, nonatomic) NSMutableArray<MELLiveCommentModel*>* commentsPresented;
@property (strong, nonatomic) NSMutableArray<MELLiveCommentModel*>* commentsWaitingForPresenting;
@property (nonatomic, strong) dispatch_queue_t presentingTaskQueue;
@property (nonatomic, strong) dispatch_semaphore_t presentingSemaphore;
@property (atomic, assign) BOOL allowPresenting;
@end

@implementation MELInteractiveMessageViewsHolder

#pragma mark Properties

- (UITableView*)liveCommentView {
    if (!_liveCommentView) {
        _liveCommentView = [[UITableView alloc] init];
        [self addSubview:_liveCommentView];
        _liveCommentView.delegate = self;
        _liveCommentView.dataSource = self;
        _liveCommentView.showsVerticalScrollIndicator = NO;
        [_liveCommentView registerClass:[MELLiveCommentTableViewCell class] forCellReuseIdentifier:reuseableCellId];
        
        _liveCommentView.backgroundColor = [UIColor colorWithHexString:@"#F5F5F5" alpha:1.0];
        
        [_liveCommentView mas_makeConstraints:^(MASConstraintMaker * _Nonnull make) {
            make.edges.equalTo(self);
        }];
    }
    return _liveCommentView;
}

#pragma mark Public Methods

-(instancetype) init {
    self = [super init];
    if (self) {
        _presentingSemaphore = dispatch_semaphore_create(0);
        _presentingTaskQueue = dispatch_queue_create("com.alivc.imp.livecomment", DISPATCH_QUEUE_SERIAL);
        _allowPresenting = YES;
        _commentsPresented = [[NSMutableArray alloc] init];
        _commentsWaitingForPresenting = [[NSMutableArray alloc] init];
    }
    return self;
}

- (void)insertLiveComment:(ASLRBLiveCommentModel*)model {
    __weak typeof(self) weakSelf = self;
    dispatch_async(dispatch_get_main_queue(), ^{
        
        MELLiveCommentModel* internalModel = [[MELLiveCommentModel alloc] init];
        internalModel.rawModel = model;
        
        CGRect rect = [model.sentContent boundingRectWithSize:CGSizeMake(self.bounds.size.width - 32, CGFLOAT_MAX) options:NSStringDrawingUsesLineFragmentOrigin attributes:@{
            NSFontAttributeName : [UIFont fontWithName:@"PingFangSC-Regular" size:14]
        } context:nil];
        internalModel.cellHeight = ceil(rect.size.height) + 25.0;
        
        [weakSelf.commentsWaitingForPresenting addObject:internalModel];
        dispatch_semaphore_signal(weakSelf.presentingSemaphore);
    });
}

- (void)startPresentingLiveComment {
    __weak typeof(self) weakSelf = self;
    dispatch_async(_presentingTaskQueue, ^{
        while (weakSelf.allowPresenting) {
            
            dispatch_semaphore_wait(weakSelf.presentingSemaphore, DISPATCH_TIME_FOREVER);

            __block NSUInteger count = 0;
            dispatch_sync(dispatch_get_main_queue(), ^{
                count = [weakSelf.commentsWaitingForPresenting count];
            });
            
            while (count > 0) {
                
                NSTimeInterval delayMS = 1.0;
                if (count == 0) {
                    delayMS = 1.0;
                } else if (count <= 2) {
                    delayMS = 0.4;
                } else if (count <= 5) {
                    delayMS = 0.2;
                } else if (count <= 10) {
                    delayMS = 0.1;
                } else if (count <= 20) {
                    delayMS = 0.05;
                } else if (count <= 100) {
                    delayMS = 0.01;
                } else {
                    delayMS = 0.005;
                }
                
                [NSThread sleepForTimeInterval:delayMS];
                
                dispatch_sync(dispatch_get_main_queue(), ^{
                    [weakSelf p_presetLiveCommentRepeately];
                });
                
                if (!weakSelf.allowPresenting) {
                    break;
                }
                
                dispatch_sync(dispatch_get_main_queue(), ^{
                    count = [weakSelf.commentsWaitingForPresenting count];
                });
            }
        }
    });
}

- (void) stopPresentingLiveComment {
    _allowPresenting = NO;
    dispatch_semaphore_signal(_presentingSemaphore);
}

#pragma mark Private Methods
- (void)p_presetLiveCommentRepeately {
    if ([self.commentsWaitingForPresenting count] > 0) {
        
        MELLiveCommentModel* model = [self.commentsWaitingForPresenting objectAtIndex:0];
        [self.commentsWaitingForPresenting removeObjectAtIndex:0];
        [self.commentsPresented addObject:model];
        
//        if ([self.commentDelegate respondsToSelector:@selector(actionWhenOneCommentPresentedWithActualHeight:)]) {
//            [self.commentDelegate actionWhenOneCommentPresentedWithActualHeight:model.cellHeight + self.contentSize.height];
//        }
        
//        self.unpresentedCommentCount = self.commentsPresented.count - self.largeIndexOfAlreadyPresentedComment - 1;
//        BOOL firstCellVisible = NO;
//        for (NSIndexPath* path in self.indexPathsForVisibleRows) {
//            if (path.row == 0) {
//                firstCellVisible = YES;
//                break;
//            }
//        }
//        if (!firstCellVisible) {
//            if ([self.commentDelegate respondsToSelector:@selector(actionWhenUnpresentedCommentCountChange:)]) {
//                [self.commentDelegate actionWhenUnpresentedCommentCountChange:self.unpresentedCommentCount];
//            }
//        }
        [CATransaction begin];
        [self.liveCommentView beginUpdates];
        if (self.commentsPresented.count >= 100) {
            [self.commentsPresented removeObjectAtIndex:0]; //先删除数据源，再删除index
            [self.liveCommentView deleteRowsAtIndexPaths:@[
                            [NSIndexPath indexPathForRow:0 inSection:0]]
                        withRowAnimation:UITableViewRowAnimationFade];
//            if (self.largeIndexOfAlreadyPresentedComment > 0) {
//                self.largeIndexOfAlreadyPresentedComment -= 1;
//            }
        }
        
        NSIndexPath * newIndexPath = [NSIndexPath indexPathForRow:self.commentsPresented.count - 1 inSection:0];
        [self.liveCommentView insertRowsAtIndexPaths:@[newIndexPath] withRowAnimation:UITableViewRowAnimationBottom];
        [self.liveCommentView endUpdates];
        
        [self.liveCommentView scrollToRowAtIndexPath:newIndexPath atScrollPosition:UITableViewScrollPositionNone animated:YES];
        
        [CATransaction commit];
    }
}

#pragma mark UITableViewDelegate
- (CGFloat)tableView:(UITableView *)tableView heightForRowAtIndexPath:(NSIndexPath *)indexPath {
    return _commentsPresented[indexPath.row].cellHeight;
}

#pragma mark UITableViewDataSource
- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section {
    return _commentsPresented.count;
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath {
    MELLiveCommentTableViewCell* cell = [self.liveCommentView dequeueReusableCellWithIdentifier:reuseableCellId];
    if (!cell) {
        cell = [[MELLiveCommentTableViewCell alloc] init];
    }
    
    cell.model = _commentsPresented[indexPath.row];
    return cell;
}
@end
