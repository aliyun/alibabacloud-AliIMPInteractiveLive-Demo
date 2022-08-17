//
//  SECLRALiveCommentView.m
//  StandardECommerceLiveRoomForAudience
//
//  Created by fernando on 2022/4/15.
//

//@import AliStandardLiveRoomBundle;
#import <AliStandardLiveRoomBundle/AliStandardLiveRoomBundle.h>
#import "SECLRALiveCommentView.h"
#import "SECLRALiveCommentCellTableViewCell.h"
#import "ASLUKEdgeInsetLabel.h"
#import "SECLRALiveCommentModel.h"
#import "UIColor+ColorWithHexString.h"
#import "ASLUKResourceManager.h"
#import <Masonry/Masonry.h>

@interface SECLRALiveCommentView ()<UITableViewDelegate,UITableViewDataSource>
@property (strong, nonatomic) NSMutableArray* commentsPresented;
@property (strong, nonatomic) NSMutableArray* commentsWaitingForPresenting;
@property (assign, nonatomic) int32_t unpresentedCommentCount;
@property (assign, nonatomic) int32_t largeIndexOfAlreadyPresentedComment;
@property (nonatomic, strong) dispatch_queue_t presentingTaskQueue;
@property (nonatomic, strong) dispatch_semaphore_t presentingSemaphore;
@property (atomic, assign) BOOL allowPresenting;
@property (strong, nonatomic) NSSet<NSString*>* witticismComments;
@property (strong, nonatomic) UILabel* labelForCalculatingSize;
@end

static NSString* _Nonnull reuseableCellId = @"SECLRALiveCommentViewCell";

@implementation SECLRALiveCommentView

- (UILabel*)labelForCalculatingSize {
    if (!_labelForCalculatingSize) {
        _labelForCalculatingSize = [[UILabel alloc] initWithFrame:CGRectMake(0, 0, self.bounds.size.width, 40)];
        _labelForCalculatingSize.numberOfLines = 0;
        _labelForCalculatingSize.textAlignment = NSTextAlignmentLeft;
    }
    return _labelForCalculatingSize;
}

- (instancetype) init {
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
        [self registerClass:[SECLRALiveCommentCellTableViewCell class] forCellReuseIdentifier:reuseableCellId];
        _largeIndexOfAlreadyPresentedComment = 0;
        _presentingTaskQueue = dispatch_queue_create("com.alivc.imp.livecomment", DISPATCH_QUEUE_SERIAL);
        _presentingSemaphore = dispatch_semaphore_create(0);
        _allowPresenting = YES;
        
        _witticismComments = [[NSSet alloc] initWithArray:@[@"666", @"太棒啦", @"赞", @"真好"]];
        
        [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(handleNSNotification:) name:@"SELRAFollowButtonClicked" object:nil];
    }
    return self;
}

- (void) dealloc {
}

- (void)insertNewComment:(ASLRBLiveCommentModel*)comment {
    
    __weak typeof(self) weakSelf = self;
    dispatch_async(dispatch_get_main_queue(), ^{
        
        SECLRALiveCommentModel* model = [self createLiveCommentModelWithRawComment:comment];
        [weakSelf.commentsWaitingForPresenting addObject:model];
        dispatch_semaphore_signal(weakSelf.presentingSemaphore);
    });
}

- (SECLRALiveCommentModel*)createLiveCommentModelWithRawComment:(ASLRBLiveCommentModel*)comment {
    
    SECLRALiveCommentModel* model = [[SECLRALiveCommentModel alloc] init];
    model.interactionType = SECLRALiveCommentCellInteractionTypeNone;
    model.interactionWidgetWidth = 0;
    model.rawModel = comment;
    
    NSMutableAttributedString* attributedString = [[NSMutableAttributedString alloc] init];
    UIFont* textFont = [UIFont fontWithName:@"PingFangSC-Regular" size:12];
    if ([comment.senderNick isEqualToString:@"公告"] && !comment.senderID) {
        
        [attributedString appendAttributedString:({
            NSAttributedString* nick = [[NSAttributedString alloc] initWithString:comment.senderNick attributes:@{
                NSForegroundColorAttributeName : [UIColor colorWithHexString:@"#FF5722" alpha:1.0],
                NSFontAttributeName : textFont
            }];
            nick;
        })];

        [attributedString appendAttributedString:({
            NSAttributedString* space = [[NSAttributedString alloc] initWithString:@" "];
            space;
        })];
        
        [attributedString appendAttributedString:({
            NSAttributedString* content = [[NSAttributedString alloc] initWithString:comment.sentContent attributes:@{
                NSForegroundColorAttributeName : [UIColor whiteColor],
                NSFontAttributeName : textFont
            }];
            content;
        })];
        
    } else {
        if (comment.senderNick.length >= 16) {
            comment.senderNick = [[comment.senderNick substringToIndex:15] stringByAppendingString:@"**"];
        }
        
        // 配置粉丝等级icon
        [attributedString appendAttributedString:({
            NSTextAttachment *userLevelIcon = [[NSTextAttachment alloc] init];
            NSString* userLevelIconName = nil;
            if (comment.senderID.length > 5) { //这里模仿判断粉丝级别，具体根据自己业务设置
                userLevelIconName = @"粉丝标示-新粉";
            } else if (comment.senderID.length > 2) {
                userLevelIconName = @"粉丝标示-铁粉";
            } else {
                userLevelIconName = @"粉丝标示-老粉";
            }

            [userLevelIcon setImage:[UIImage imageNamed:userLevelIconName inBundle:[ASLUKResourceManager sharedInstance].resourceBundle compatibleWithTraitCollection:nil]];

            [userLevelIcon setBounds:CGRectMake(0, -0.5, userLevelIcon.image.size.width, userLevelIcon.image.size.height)];
            NSAttributedString *userLevelIconString = [NSAttributedString attributedStringWithAttachment:userLevelIcon];
            userLevelIconString;
        })];
        
        // icon后增加一个空格
        [attributedString appendAttributedString:({
            NSAttributedString* spaceForUserLevelIcon = [[NSAttributedString alloc] initWithString:@" " attributes:@{}];
            spaceForUserLevelIcon;
        })];
        
        // 配置昵称
        [attributedString appendAttributedString:({
            NSAttributedString* nick = [[NSAttributedString alloc] initWithString:comment.senderNick attributes:@{
                NSFontAttributeName: textFont,
                NSForegroundColorAttributeName : comment.senderNickColor,
                NSBaselineOffsetAttributeName : @((textFont.ascender - textFont.descender) / 2 + textFont.descender - 1.5)
            }];
            nick;
        })];
        
        // 昵称后配置一个空格
        [attributedString appendAttributedString:({
            NSAttributedString* spaceAfterNick = [[NSAttributedString alloc] initWithString:@" " attributes:@{}];
            spaceAfterNick;
        })];
        
        // 配置弹幕内容
        [attributedString appendAttributedString:({
            
            NSAttributedString* content = [[NSAttributedString alloc] initWithString:comment.sentContent attributes:@{
                NSFontAttributeName: textFont,
                NSForegroundColorAttributeName : [UIColor whiteColor],
                NSBaselineOffsetAttributeName : @((textFont.ascender - textFont.descender) / 2 + textFont.descender - 1.5)
            }];
            content;
        })];
    }
    
    model.textContentAttributedString = attributedString;
    
    if ([comment.sentContent isEqualToString:@"关注了主播"] && !_followedAlready) {
        model.interactionType = SECLRALiveCommentCellInteractionTypeFollow;
        model.interactionWidgetWidth = 70;
    } else if ([_witticismComments containsObject:comment.sentContent] && comment.senderID != @"我的userID") {
        model.interactionType = SECLRALiveCommentCellInteractionTypeComment;
        model.interactionWidgetWidth = 35;
    }
    
    CGRect rect = [attributedString boundingRectWithSize:CGSizeMake(self.bounds.size.width - model.interactionWidgetWidth - 20, CGFLOAT_MAX) options:NSStringDrawingUsesLineFragmentOrigin context:nil];
    
    model.cellWidth = ceil(rect.size.width) + model.interactionWidgetWidth + 20;
    model.cellHeight = ceil(rect.size.height) + 10;
    
    return model;
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
        SECLRALiveCommentModel* model = [self.commentsWaitingForPresenting objectAtIndex:0];
        [self.commentsWaitingForPresenting removeObjectAtIndex:0];

        [self.commentsPresented addObject:model];
        
        if ([self.commentDelegate respondsToSelector:@selector(actionWhenOneCommentPresentedWithActualHeight:)]) {
            [self.commentDelegate actionWhenOneCommentPresentedWithActualHeight:model.cellHeight + self.contentSize.height];
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
        
        if (self.commentsPresented.count >= 2000) {
            [self.commentsPresented removeObjectAtIndex:0]; //先删除数据源，再删除index
            [self deleteRowsAtIndexPaths:@[
                            [NSIndexPath indexPathForRow:self.commentsPresented.count - 1 -indexPath.row inSection:0]]
                        withRowAnimation:UITableViewRowAnimationFade];
            if (self.largeIndexOfAlreadyPresentedComment > 0) {
                self.largeIndexOfAlreadyPresentedComment -= 1;
            }
        }
        
        [self insertRowsAtIndexPaths:@[indexPath] withRowAnimation:UITableViewRowAnimationTop];
        [self endUpdates];
        [CATransaction commit];
    }
}

- (CGFloat)tableView:(UITableView *)tableView heightForRowAtIndexPath:(NSIndexPath *)indexPath {
    SECLRALiveCommentModel* model = nil;
    model = (SECLRALiveCommentModel*)self.commentsPresented[self.commentsPresented.count - 1 -indexPath.row];
    return model.cellHeight;
}

- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView {
    return 1;
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section {
    return self.commentsPresented.count;
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath {
    NSIndexPath* realIndexPath = [NSIndexPath indexPathForRow:self.commentsPresented.count - 1 - indexPath.row inSection:0];
    
    SECLRALiveCommentCellTableViewCell* cell = [self dequeueReusableCellWithIdentifier:reuseableCellId];
    if(cell == nil){
        cell = [[SECLRALiveCommentCellTableViewCell alloc] initWithStyle:UITableViewCellStyleDefault reuseIdentifier:reuseableCellId];
    }
    cell.delegate = self;
    
    cell.commentModel = (SECLRALiveCommentModel*)self.commentsPresented[realIndexPath.row];//重写的model的set方法中会根据model构造cell的UI
    
    int32_t lastIndex = self.largeIndexOfAlreadyPresentedComment;
    self.largeIndexOfAlreadyPresentedComment = MAX(realIndexPath.row, self.largeIndexOfAlreadyPresentedComment);
    if (self.largeIndexOfAlreadyPresentedComment > lastIndex) {
        self.unpresentedCommentCount = self.commentsPresented.count - self.largeIndexOfAlreadyPresentedComment - 1;
        if ([self.commentDelegate respondsToSelector:@selector(actionWhenUnpresentedCommentCountChange:)]) {
            [self.commentDelegate actionWhenUnpresentedCommentCountChange:self.unpresentedCommentCount];
        }
    }
        
    return cell;
}

#pragma mark - SECLRALiveCommentCellDelegate

- (void) onLiveCommentCellInteraction:(NSInteger)interaction extension:(NSDictionary*)extension {
    [self.commentDelegate onLiveCommentCellInteraction:interaction extension:extension];
    
    if (interaction == SECLRALiveCommentCellInteractionTypeFollow) {
        _followedAlready = YES;
    }
}

#pragma mark - NSNotification

- (void)handleNSNotification:(NSNotification*)notification {
    if ([notification.name isEqualToString:@"SELRAFollowButtonClicked"]) {
        _followedAlready = YES;
    }
}

@end
