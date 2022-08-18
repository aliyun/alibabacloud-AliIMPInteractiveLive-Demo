//
//  CommentView.m
//  CommentViewDemo
//
//  Created by 麦辣 on 2021/6/3.
//

#import "ASLRBCommentTableView.h"
#import "../ASLRBLiveCommentModel.h"
#import "ASLRBCommentCell.h"
#import <Masonry/Masonry.h>
#import "../../CommonTools/UIColor+ColorWithHexString.h"
#import "../../LiveRoomSetup/ASLRBLiveCommentViewConfig.h"

@interface ASLRBCommentTableView ()<UITableViewDelegate,UITableViewDataSource>
@property (strong, nonatomic) NSMutableArray* commentsPresented;
@property (strong, nonatomic) NSMutableArray* commentsWaitingForPresenting;
@property (assign, nonatomic) int32_t unpresentedCommentCount;
@property (assign, nonatomic) int32_t largeIndexOfAlreadyPresentedComment;
@property (nonatomic, strong) dispatch_queue_t presentingTaskQueue;
@property (nonatomic, strong) dispatch_semaphore_t presentingSemaphore;
@property (strong, nonatomic) ASLRBLiveCommentViewConfig* config;
@property (atomic, assign) BOOL allowPresenting;
@end

static NSString* _Nonnull reuseableCellId = @"ASLRBCommentViewCell";

@implementation ASLRBCommentTableView

- (instancetype) initWitConfig:(ASLRBLiveCommentViewConfig *)config {
    self = [super init];
    if (self) {
        self.separatorStyle = UITableViewCellSeparatorStyleNone;
//        self.estimatedRowHeight = 0;
        self.showsVerticalScrollIndicator = NO;
        _commentsPresented = [[NSMutableArray alloc] init];
        _commentsWaitingForPresenting = [[NSMutableArray alloc] init];
        self.dataSource = self;
        self.delegate = self;
        self.transform = CGAffineTransformMakeRotation(M_PI);
        [self setBackgroundColor:[UIColor clearColor]];
        [self registerClass:[ASLRBCommentCell class] forCellReuseIdentifier:reuseableCellId];
        _largeIndexOfAlreadyPresentedComment = 0;
        _presentingTaskQueue = dispatch_queue_create("com.alivc.imp.livecomment", DISPATCH_QUEUE_SERIAL);
        _presentingSemaphore = dispatch_semaphore_create(0);
        _config = config;
        _allowPresenting = YES;
    }
    return self;
}

- (void) dealloc {
}

- (void)insertNewComment:(ASLRBLiveCommentModel*)comment presentedCompulsorily:(BOOL)presentedCompulsorily {
    
    __weak typeof(self) weakSelf = self;
    dispatch_async(dispatch_get_main_queue(), ^{
        [weakSelf.commentsWaitingForPresenting addObject:comment];
        dispatch_semaphore_signal(weakSelf.presentingSemaphore);
    });
}

- (void) startPresenting {
    __weak typeof(self) weakSelf = self;
    dispatch_async(self.presentingTaskQueue, ^{
        
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
                    [weakSelf presentCommentRepeatly];
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

- (void) stopPresenting {
    _allowPresenting = NO;
    dispatch_semaphore_signal(_presentingSemaphore);
}

- (void) scrollToNewestComment {
    [self scrollToRowAtIndexPath:[NSIndexPath indexPathForRow:0 inSection:0] atScrollPosition:UITableViewScrollPositionNone animated:NO];
}

- (void) presentCommentRepeatly {
    if ([self.commentsWaitingForPresenting count] > 0) {
        [CATransaction begin];
        [self beginUpdates];
        NSIndexPath *indexPath = [NSIndexPath indexPathForRow:0 inSection:0];
        ASLRBLiveCommentModel* model = [self.commentsWaitingForPresenting objectAtIndex:0];
        [self.commentsWaitingForPresenting removeObjectAtIndex:0];
        
        if (_config.defaultLiveCommentSenderColor) {
            model.senderNickColor = self.config.defaultLiveCommentSenderColor;
        }
        if (_config.defaultLiveCommentContentColor) {
            model.sentContentColor = _config.defaultLiveCommentContentColor;
        }
        
//        if ([self.commentDelegate respondsToSelector:@selector(actionWhenCommentJustAboutToPresent:)]) {
//            [self.commentDelegate actionWhenCommentJustAboutToPresent:model];
//        }
        
        if ([self.commentDelegate respondsToSelector:@selector(actionWhenOneCommentPresentedWithActualHeight:)]) {
            [self.commentDelegate actionWhenOneCommentPresentedWithActualHeight:[self getTextSizeWithCommentModel:model].height + self.contentSize.height];
        }
        
        self.unpresentedCommentCount = self.commentsPresented.count - self.largeIndexOfAlreadyPresentedComment - 1;
        BOOL firstCellVisible = NO;
        for (NSIndexPath* path in self.indexPathsForVisibleRows) {
            if (path.row == 0) {
                firstCellVisible = YES;
                break;
            }
        }
        if (!firstCellVisible) {
            if ([self.commentDelegate respondsToSelector:@selector(actionWhenUnpresentedCommentCountChange:)]) {
                [self.commentDelegate actionWhenUnpresentedCommentCountChange:self.unpresentedCommentCount];
            }
        }
        
        if (self.commentsPresented.count >= 200) {
            NSUInteger countOfModelsToDelete = 200 / 3.0;
            [self.commentsPresented removeObjectsInRange:NSMakeRange(0, countOfModelsToDelete)];
            NSMutableArray* indexes = [[NSMutableArray alloc] init];
            for (int i = 0; i < countOfModelsToDelete; i++) {
                [indexes addObject:[NSIndexPath indexPathForRow:self.commentsPresented.count - 1 - i inSection:0]];
            }
            [self deleteRowsAtIndexPaths:indexes withRowAnimation:UITableViewRowAnimationFade];
            if (self.largeIndexOfAlreadyPresentedComment > countOfModelsToDelete) {
                self.largeIndexOfAlreadyPresentedComment -= countOfModelsToDelete;
            }
        }
        
        [self insertRowsAtIndexPaths:@[indexPath] withRowAnimation:UITableViewRowAnimationTop];
        [self.commentsPresented addObject:model];
        
        [self endUpdates];
        [CATransaction commit];
    }
}

- (CGSize)getTextSizeWithCommentModel:(ASLRBLiveCommentModel*)model {
    NSDictionary *atrri = @{NSFontAttributeName: _config.liveCommentLabelTextFont};
    CGFloat height = [model.fullCommentString boundingRectWithSize:CGSizeMake(self.bounds.size.width - 6, MAXFLOAT)
                                                   options:NSStringDrawingUsesLineFragmentOrigin|NSStringDrawingUsesFontLeading
                                                attributes:atrri
                                                   context:nil].size.height + 20;
    CGFloat width = MIN([model.fullCommentString boundingRectWithSize:CGSizeMake(MAXFLOAT, height)
                                              options:NSStringDrawingUsesLineFragmentOrigin|NSStringDrawingUsesFontLeading
                                           attributes:atrri
                                              context:nil].size.width + 6, self.bounds.size.width - 6);
    return CGSizeMake(width, height);
}


- (CGFloat)tableView:(UITableView *)tableView heightForRowAtIndexPath:(NSIndexPath *)indexPath {
    ASLRBLiveCommentModel* model = nil;
    model = (ASLRBLiveCommentModel*)self.commentsPresented[self.commentsPresented.count - 1 -indexPath.row];
    return [self getTextSizeWithCommentModel:model].height;
}

- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView {
    return 1;
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section {
    return self.commentsPresented.count;
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath {
    NSIndexPath* realIndexPath = [NSIndexPath indexPathForRow:self.commentsPresented.count - 1 - indexPath.row inSection:0];
    
    ASLRBCommentCell* cell = [self dequeueReusableCellWithIdentifier:reuseableCellId];
    if(cell == nil){
        cell = [[ASLRBCommentCell alloc] initWithStyle:UITableViewCellStyleDefault reuseIdentifier:reuseableCellId];
    }
    cell.delegate = self;
    cell.config = _config;
    
    if (!_config.disableLiveCommentLongPressGesture) {
        [cell addLongPressGestureRecognizer];
    }
    
    if (!_config.disableLiveCommentTapGesture) {
        [cell addTapGestureRecognizer];
    }
    
    cell.commentModel = (ASLRBLiveCommentModel*)self.commentsPresented[realIndexPath.row];//重写的model的set方法中会根据model构造cell的UI
    
    [cell.commentLabel mas_updateConstraints:^(MASConstraintMaker * _Nonnull make) {
        make.width.mas_equalTo([self getTextSizeWithCommentModel:cell.commentModel].width + 6);
    }];
    
    int32_t lastIndex = self.largeIndexOfAlreadyPresentedComment;
    self.largeIndexOfAlreadyPresentedComment = MAX(realIndexPath.row, self.largeIndexOfAlreadyPresentedComment);
    if (self.largeIndexOfAlreadyPresentedComment > lastIndex) {
        self.unpresentedCommentCount = self.commentsPresented.count - self.largeIndexOfAlreadyPresentedComment - 1;
        if ([self.commentDelegate respondsToSelector:@selector(actionWhenUnpresentedCommentCountChange:)]) {
            [self.commentDelegate actionWhenUnpresentedCommentCountChange:self.unpresentedCommentCount];
        }
    }
        
    [cell sizeToFit];
        
    return cell;
}

#pragma mark - 长按事件处理

-(void)onASLRBCommentCellLongPressGesture:(UILongPressGestureRecognizer*)recognizer commentModel:(ASLRBLiveCommentModel*)commentModel{
    if(recognizer.state == UIGestureRecognizerStateBegan){
        if([self.commentDelegate respondsToSelector:@selector(actionWhenCommentCellLongPressed:)]){
            [self.commentDelegate actionWhenCommentCellLongPressed:commentModel];
        }
    }
}

-(void)onASLRBCommentCellTapGesture:(UITapGestureRecognizer*)recognizer
                       commentModel:(ASLRBLiveCommentModel*)commentModel {
    if([self.commentDelegate respondsToSelector:@selector(actionWhenCommentCellTapped:)]){
        [self.commentDelegate actionWhenCommentCellTapped:commentModel];
    }
}

@end
