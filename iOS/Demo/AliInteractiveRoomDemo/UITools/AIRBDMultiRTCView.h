//
//  AIRBDMultiRTCView.h
//  AliInteractiveRoomDemo
//
//  Created by 麦辣 on 2021/6/15.
//

#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN

@interface AIRBDMultiRTCView : UIView

@property(nonatomic,strong)UIView* sideViewHolder;
@property(nonatomic,strong)UIView* centerViewHolder;
-(void)setUpUI;
-(void)addSideRTCView:(UIView*)view withUserID:(NSString*)userID;
-(void)removeSideRTCViewOfUserID:(NSString*)userID;
-(void)addCenterRTCView:(UIView*)view;
@end

NS_ASSUME_NONNULL_END
