//
//  AIRBDLinkMicCollectionViewCell.h
//  AliInteractiveRoomDemo
//
//  Created by 刘再勇 on 2022/4/28.
//

#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN

@interface AIRBDLinkMicCollectionViewCell : UICollectionViewCell

@property(nonatomic, strong) NSString* userID;

@property(nonatomic, strong) UILabel* userNickLabel;

@property(nonatomic, strong) UILabel* userMicrophoneLabel;

@property(nonatomic, strong) UIView* videoView;

- (void) setUserNickLabelText:(NSString*)labelText videoView:(UIView*)videoView camera:(BOOL)cameraOpened microphone:(BOOL)microphoneOpened;

@end

NS_ASSUME_NONNULL_END
