//
//  ASLRBLivePrestartViewsHolderProtocol.h
//  AliStandardLiveRoomBundle
//
//  Created by fernando on 2022/1/13.
//

#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

@protocol ASLRBLivePrestartViewsHolderProtocol <NSObject>

@property (strong, nonatomic) UIButton* exitButton;

@property (strong, nonatomic) UITextView* liveTitleTextView;
@property (strong, nonatomic) UIButton* liveTitleEditButton;
@property (assign, nonatomic) NSUInteger liveTitleMaxLength; //默认最多35个字符

@property (strong, nonatomic) UIButton* switchCameraButton;

@property (strong, nonatomic) UIButton* beautyButton;

@property (strong, nonatomic) UIButton* startLiveButton;
@end

NS_ASSUME_NONNULL_END
