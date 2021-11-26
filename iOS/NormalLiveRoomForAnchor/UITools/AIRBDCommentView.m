//
//  CommentView.m
//  CommentViewDemo
//
//  Created by 麦辣 on 2021/6/3.
//

#import "AIRBDCommentView.h"
#import "AIRBDCommentModel.h"
#import "AIRBDCommentCell.h"
@interface AIRBDCommentView ()<UITableViewDelegate,UITableViewDataSource>
@property(nonatomic,strong)NSMutableArray* comments;
@end

static NSString* _Nonnull reuseableCellId = @"commentCell";

@implementation AIRBDCommentView



- (instancetype)init
{
    self = [super init];
    if (self) {
        _commentStyle = BulletStyle;
        self.transform = CGAffineTransformMakeRotation(M_PI);
        self.separatorStyle = UITableViewCellSeparatorStyleNone;
        _comments = [[NSMutableArray alloc]init];
        self.dataSource = self;
        self.delegate = self;
        
        [self setBackgroundColor:[UIColor colorWithWhite:0.4 alpha:0.4]];
        self.layer.cornerRadius = 8;
        self.layer.masksToBounds = YES;
        [self registerClass:[AIRBDCommentCell class] forCellReuseIdentifier:reuseableCellId];
    }
    return self;
}

- (instancetype)initWithCommentStyle:(AIRBDCommentStyle)commentStyle{
    self = [super init];
    if (self) {
        self.commentStyle = commentStyle;
        self.separatorStyle = UITableViewCellSeparatorStyleNone;
        _comments = [[NSMutableArray alloc]init];
        self.dataSource = self;
        self.delegate = self;
        if(self.commentStyle == BulletStyle){
            [self setBackgroundColor:[UIColor colorWithWhite:0.4 alpha:0.4]];
            self.transform = CGAffineTransformMakeRotation(M_PI);
            self.layer.cornerRadius = 8;
            self.layer.masksToBounds = YES;
        }else if(self.commentStyle == WhiteboardStyle){
            [self setBackgroundColor:[UIColor whiteColor]];
        }else if (self.commentStyle == BulletStyleNew){
            self.transform = CGAffineTransformMakeRotation(M_PI);
            [self setBackgroundColor:[UIColor clearColor]];
        }
        [self registerClass:[AIRBDCommentCell class] forCellReuseIdentifier:reuseableCellId];
    }
    return self;
    
}

- (void)insertNewComment:(NSString*) comment{
    
    if(self.commentStyle == BulletStyle){
        dispatch_async(dispatch_get_main_queue(), ^{
            [CATransaction begin];
            [self beginUpdates];
            NSIndexPath *indexPath = [NSIndexPath indexPathForRow:0 inSection:0];
            AIRBDCommentModel* model = [[AIRBDCommentModel alloc]init];
            model.content = comment;
            model.commentStyle = self.commentStyle;
            [self->_comments addObject:model];
            [self insertRowsAtIndexPaths:@[indexPath] withRowAnimation:UITableViewRowAnimationTop];
            [self endUpdates];
            [CATransaction commit];
            
        });
    }else if(self.commentStyle == WhiteboardStyle){
        dispatch_async(dispatch_get_main_queue(), ^{
            [CATransaction begin];
            [self beginUpdates];
            NSIndexPath *indexPath = [NSIndexPath indexPathForRow:self.comments.count inSection:0];
            AIRBDCommentModel* model = [[AIRBDCommentModel alloc]init];
            model.content = comment;
            model.commentStyle = self.commentStyle;
            [self->_comments addObject:model];
            [self insertRowsAtIndexPaths:@[indexPath] withRowAnimation:UITableViewRowAnimationBottom];
            [self endUpdates];
            [CATransaction commit];
            CGPoint offset = CGPointMake(0, self.contentSize.height - self.bounds.size.height);
            [self setContentOffset:offset animated:YES];
        });
    }else if (self.commentStyle == BulletStyleNew){
        dispatch_async(dispatch_get_main_queue(), ^{
            [CATransaction begin];
            [self beginUpdates];
            NSIndexPath *indexPath = [NSIndexPath indexPathForRow:0 inSection:0];
            AIRBDCommentModel* model = [[AIRBDCommentModel alloc]init];
            model.content = comment;
            model.commentStyle = self.commentStyle;
            [self->_comments addObject:model];
            [self insertRowsAtIndexPaths:@[indexPath] withRowAnimation:UITableViewRowAnimationTop];
            [self endUpdates];
            [CATransaction commit];
            
        });
    }
}

- (void)insertNewComments:(NSArray *)comments{
    if (self.commentStyle == BulletStyleNew){
        dispatch_async(dispatch_get_main_queue(), ^{
            int i = 0;
            [CATransaction begin];
            [self beginUpdates];
            NSMutableArray* paths = [[NSMutableArray alloc]initWithCapacity:[comments count]];
            for(NSString* comment in comments){
                AIRBDCommentModel* model = [[AIRBDCommentModel alloc]init];
                model.content = comment;
                model.commentStyle = self.commentStyle;
                [self->_comments addObject:model];
                [paths addObject:[NSIndexPath indexPathForRow:i++ inSection:0]];
            }
            [self insertRowsAtIndexPaths:paths withRowAnimation:UITableViewRowAnimationTop];
            [self endUpdates];
            [CATransaction commit];
        });
    }
}




- (CGFloat)tableView:(UITableView *)tableView heightForRowAtIndexPath:(NSIndexPath *)indexPath {
    NSDictionary *atrri = @{NSFontAttributeName: [UIFont systemFontOfSize:self.bounds.size.width*13.0/203+2]};
    
    AIRBDCommentModel* model = nil;
    if(self.commentStyle == BulletStyle){
        model = (AIRBDCommentModel*)self.comments[self.comments.count - 1 -indexPath.row];
    }else if(self.commentStyle == WhiteboardStyle){
        model = (AIRBDCommentModel*)self.comments[indexPath.row];
    }else if (self.commentStyle == BulletStyleNew){
        model = (AIRBDCommentModel*)self.comments[self.comments.count - 1 -indexPath.row];
    }
    
    CGRect rect = [[model description] boundingRectWithSize:CGSizeMake(self.bounds.size.width, 1000) options:NSStringDrawingUsesLineFragmentOrigin attributes:atrri context:nil];
    return rect.size.height+10;
}

- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView {
    return 1;
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section {
    return self.comments.count;
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath {
    NSIndexPath* realIndexPath;
    if(self.commentStyle == BulletStyle){
        realIndexPath = [NSIndexPath indexPathForRow:self.comments.count - 1 -indexPath.row inSection:0];
    }else if(self.commentStyle == WhiteboardStyle){
        realIndexPath = [NSIndexPath indexPathForRow:indexPath.row inSection:0];
    }else if(self.commentStyle == BulletStyleNew){
        realIndexPath = [NSIndexPath indexPathForRow:self.comments.count - 1 -indexPath.row inSection:0];
    }
    AIRBDCommentCell* cell = [self dequeueReusableCellWithIdentifier:reuseableCellId forIndexPath:realIndexPath];
    if(cell == nil){
        cell = [[AIRBDCommentCell alloc]initWithStyle:UITableViewCellStyleDefault reuseIdentifier:reuseableCellId];
    }
    
    AIRBDCommentModel* model = nil;
    model = (AIRBDCommentModel*)self.comments[realIndexPath.row];
    cell.commentModel = model;//重写的model的set方法中会根据model构造cell的UI
    [cell sizeToFit];
    
    return cell;
}

@end
