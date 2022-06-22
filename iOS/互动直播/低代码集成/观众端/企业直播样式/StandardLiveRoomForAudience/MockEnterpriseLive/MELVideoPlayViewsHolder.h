//
//  MELVideoPlayViewsHolder.h
//  AliStandardLiveUIKit
//
//  Created by fernando on 2022/5/31.
//

#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN

typedef enum : NSUInteger {
    MELVideoPlayStatusLiveNotStarted,
    MELVideoPlayStatusLiveStarted,
    MELVideoPlayStatusLiveEnded,
    MELVideoPlayStatusLoadingStart,
    MELVideoPlayStatusLoadingEnd,
    MELVideoPlayStatusLoadFailed,
    MELVideoPlayStatusPlayStarted,
    MELVideoPlayStatusPushStopped,
    MELVideoPlayStatusPushRecovered,
    MELVideoPlayStatusPlayErrored,
} MELVideoPlayStatus;

@interface MELVideoPlayViewsHolder : UIView
@property (copy, nonatomic) void(^onReload)(void);
@property (copy, nonatomic) void(^onExit)(void);
@property (copy, nonatomic) void(^onLandscape)(void);
@property (assign, nonatomic) MELVideoPlayStatus videoPlayStatus;
@property (strong, nonatomic) NSURL* anchorAvartarImageURL;
@property (copy, nonatomic) NSString* anchorNick;
@property (assign, nonatomic) CGFloat portraitHeight;
@property (copy, nonatomic) NSString* livePrestartTimestamp;

- (void)updateLivePV:(int32_t)pv;
- (void)rotateToLandscape:(BOOL)rotate;
@end

NS_ASSUME_NONNULL_END
