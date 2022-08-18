//
//  AIRBDUserListViewController.h
//  AliInteractiveRoomDemo
//
//  Created by 麦辣 on 2021/7/8.
//

#import <UIKit/UIKit.h>
#import <AliInteractiveRoomBundle/AliInteractiveRoomBundle.h>

///用户列表的控制器

NS_ASSUME_NONNULL_BEGIN

@protocol AIRBDUserListViewDelegate <NSObject>
@optional
-(void)deleteItem:(NSString*)itemID;//删除
-(void)useItem:(NSString *)itemID;//禁言等操作
@end

@interface ASLRBUserListViewController : UIViewController

@property(strong,nonatomic) id<AIRBDUserListViewDelegate> userListViewDelegate;

-(void)updateUsersWithArray:(NSArray<AIRBRoomChannelUser*>*)users;
@end

NS_ASSUME_NONNULL_END
