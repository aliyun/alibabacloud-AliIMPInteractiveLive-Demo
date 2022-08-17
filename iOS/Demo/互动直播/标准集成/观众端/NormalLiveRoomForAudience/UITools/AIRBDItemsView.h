//
//  AIRBDItemsView.h
//  AliInteractiveRoomDemo
//
//  Created by 麦辣 on 2021/6/7.
//

#import <UIKit/UIKit.h>
NS_ASSUME_NONNULL_BEGIN


typedef enum : NSUInteger {
    ItemsViewGoodsStyle,
    ItemsViewMembersStyle
} ItemsStyle;

typedef enum : NSUInteger {
    MuteItem,
    UnmuteItem
} itemChoice;

@protocol AIRBDItemsViewDelegate <NSObject>
@optional
-(void)deleteItem:(NSString*)itemID;//删除
-(void)useItem:(NSString*)itemID;//禁言等操作
-(void)useItem:(NSString *)itemID withChoice:(itemChoice)choice;
@end


@interface AIRBDItemsView : UITableView
@property(assign,nonatomic) ItemsStyle itemsStyle;
@property(strong,nonatomic) id<AIRBDItemsViewDelegate> ItemsViewdelegate;

-(void)updateItems:(NSArray *)items;
@end

NS_ASSUME_NONNULL_END
