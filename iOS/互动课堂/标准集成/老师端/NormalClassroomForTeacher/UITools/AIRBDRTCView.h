//
//  AIRBDRTCView.h
//  AliInteractiveRoomDemo
//
//  Created by 麦辣 on 2021/6/16.
//

#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN

@interface AIRBDRTCView : UIView
@property(copy,nonatomic)NSString* userID;
@property(strong,nonatomic)UIView* view;
@property(strong,nonatomic)UILabel* idLabel;
-(void)addIDLabel;
@end

NS_ASSUME_NONNULL_END
