//
//  AIRBDLinkMicCollectionViewHolder.h
//  AliInteractiveRoomDemo
//
//  Created by 刘再勇 on 2022/4/28.
//

#import <UIKit/UIKit.h>
#import <AliStandardLiveRoomBundle/AliStandardLiveRoomBundle.h>

NS_ASSUME_NONNULL_BEGIN

@protocol AIRBDLinkMicCollectionViewHolderDelegate <NSObject>

@property(nonatomic, strong) NSMutableArray<NSString*>* linkMicUserArray;
@property(nonatomic, strong) id<ASLRBLiveRoomAudienceProtocol> liveRoomVC;

@end

@interface AIRBDLinkMicCollectionViewHolder : UIView

@property(nonatomic, weak) id<AIRBDLinkMicCollectionViewHolderDelegate> delegate;
@property(nonatomic, copy) NSString* userID;    // 本地的ID
@property(nonatomic, copy) NSString* userNick;  // 本地的昵称
- (instancetype)initWithFrame:(CGRect)frame userID:(NSString*)userID userNick:(NSString*)nick;
- (void) reloadCollectionViewData;

@end

NS_ASSUME_NONNULL_END
