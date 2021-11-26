//
//  AIRBDItemsViewCell.h
//  AliInteractiveRoomDemo
//
//  Created by 麦辣 on 2021/6/7.
//

#import <UIKit/UIKit.h>
#import "AIRBDItemsView.h"
NS_ASSUME_NONNULL_BEGIN


@interface AIRBDItemsViewCell : UITableViewCell
@property(copy,nonatomic)NSString* cellID;
@property(assign,nonatomic) ItemsStyle itemsStyle;

@property(strong,nonatomic)id<AIRBDItemsViewDelegate> cellDelegate;
@end

NS_ASSUME_NONNULL_END
