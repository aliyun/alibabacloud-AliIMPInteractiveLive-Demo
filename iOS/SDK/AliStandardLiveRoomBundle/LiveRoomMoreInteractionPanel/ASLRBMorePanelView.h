//
//  AIRBDMorePanelView.h
//  AliInteractiveRoomDemo
//
//  Created by 麦辣 on 2021/6/28.
//

#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN

@protocol ASLRBMorePanelDelegate <NSObject>

@optional
-(void)muteButtonAction:(UIButton*)sender;
-(void)pauseButtonAction:(UIButton*)sender;
-(void)cameraButtonAction:(UIButton*)sender;
-(void)mirrorButtonAction:(UIButton*)sender;

//暂时放在morePanel上的按钮
-(void)editButtonAction:(UIButton*)sender;
-(void)beautyButtonAction:(UIButton*)sender;
-(void) banAllCommentsButtonAction:(UIButton*)sender;

@end

@interface ASLRBMorePanelView : UIView

@property(weak, nonatomic) id<ASLRBMorePanelDelegate> delegate;

@end

NS_ASSUME_NONNULL_END
