//
//  ASLRBLiveRoomViewController.h
//  AliStandardLiveRoomBundle
//
//  Created by fernando on 2021/7/20.
//

#import <UIKit/UIKit.h>
#import <AliStandardLiveRoomBundle/ASLRBCommonDefines.h>
#import <AliStandardLiveRoomBundle/ASLRBLiveCommentViewProtocol.h>
#import <AliStandardLiveRoomBundle/ASLRBLiveRoomAnchorProtocol.h>
#import <AliStandardLiveRoomBundle/ASLRBLiveRoomAudienceProtocol.h>
#import <AliStandardLiveRoomBundle/ASLRBLiveRoomPlaybackProtocol.h>
#import <AliStandardLiveRoomBundle/ASLRBLiveRoomBottomViewsHolderProtocol.h>
#import <AliStandardLiveRoomBundle/ASLRBLiveRoomMoreInteractionPanelProtocol.h>
#import <AliStandardLiveRoomBundle/ASLRBLiveRoomInfoViewsHolderProtocol.h>
#import <AliStandardLiveRoomBundle/ASLRBLinkMicDelegate.h>
#import <AliStandardLiveRoomBundle/ASLRBLinkMicUserModel.h>

NS_ASSUME_NONNULL_BEGIN

@class ASLRBLiveRoomViewController,ASLRBLiveInitConfig,ASLRBLiveCommentView;
@protocol ASLRBLivePrestartViewsHolderProtocol;


#pragma mark - ASLRBLiveRoomViewControllerDelegate Interface
@protocol ASLRBLiveRoomViewControllerDelegate <NSObject, ASLRBLinkMicDelegate>
- (void) onASLRBLiveRoomEventInViewController:(ASLRBLiveRoomViewController *)liveRoomVC liveRoomEvent:(ASLRBEvent)liveRoomEvent info:(NSDictionary*)info;


- (void) onASLRBLiveRoomErrorInViewController:(ASLRBLiveRoomViewController *)liveRoomVC liveRoomError:(ASLRBLiveRoomError)liveRoomError withErrorMessage:(NSString*)errorMessage;
@end

@interface ASLRBLiveRoomViewController : UIViewController<ASLRBLiveRoomAnchorProtocol, ASLRBLiveRoomAudienceProtocol, ASLRBLiveRoomPlaybackProtocol>

/**
 *@brief 用来接收事件和错误通知，必传
 */
@property (weak, nonatomic) id<ASLRBLiveRoomViewControllerDelegate> delegate;

/**
 *@brief 直播间（ViewController）的type，只读，当前VC对象构造后即会确定；具体见ASLRBLiveRoomType说明；
 *
 */
@property (nonatomic, readonly) ASLRBLiveRoomType liveRoomType;

/** **********************************主播侧起播页面定制 ************************************ */
/**
   整体介绍：
      1.  主播侧起播页面，想要完全自定义请使用livePrestartCustomizedViewHolder，想要使用内部默认实现请使用livePrestartViewsHolder
      2.  这两个对象都是内部构造好的，外部无需构造；
      3.  仅主播端存在；
      4. 在调用startLive直播开始后自动移除；
      5. livePrestartCustomizedViewHolder和livePrestartViewsHolder是互斥的；
 */
/**
 * 主播端：这是一层覆盖在本VC.view上的透明的开播预览层，空的，用户可以添加预览页的UI
 */
@property(nonatomic, strong) UIView* livePrestartCustomizedViewHolder;

/**
 * 主播端：这是一层覆盖在本VC.view上的透明view，其上已经实现了直播标题设置框、镜像按钮、美颜按钮、开启直播按钮；
 */
@property (nonatomic, strong) UIView<ASLRBLivePrestartViewsHolderProtocol>* livePrestartViewsHolder;


/** **********************************直播间内左上角区域定制 ************************************ */
/**
   整体介绍：
      1.  直播间左上角，一般承载主播头像、关注、观看数据、点赞数据等label；
      2. 这部分，即可以默认使用我们已经定制好的，即liveInfoViewHolder，同时可以在liveInfoViewHolder上面进行修改；
      3. 如果这部分需要完全自定义，建议使用upperLeftCustomizedViewHolder，可以在upperLeftCustomizedViewHolder添加任意子view；
      4. 使用了upperLeftCustomizedViewHolder后，liveInfoViewHolder上的内容会自动隐藏；
      5.对upperLeftCustomizedViewHolder的改动，请在push当前VC前完成；
 */

/**
 *如果想完全自定义这部分区域（头像、关注、观看数据、点赞数据等label），则使用upperLeftCustomizedViewHolder
 * @brief 位于屏幕左上位置的用户自定义区域，便于在上面添加自定义控件，比如主播头像等
 * 注意，请在push当前VC之前完成对upperLeftCustomizedViewHolder的改动；
 * 注意，这个对象内部已经构造好，直接使用即可；
 */
@property (strong, nonatomic) UIView* upperLeftCustomizedViewHolder;

/**
 * @brief 具体看ASLRBLiveRoomInfoViewsHolderProtocol
 * 注意，这个对象内部已经构造好，直接使用即可；
 */
@property (strong, nonatomic) UIView<ASLRBLiveRoomInfoViewsHolderProtocol>* liveInfoViewHolder;


/** **********************************直播间内右上角区域定制 ************************************ */
/**
   整体介绍：
      1.  直播间右上角，一般退出按钮和观众列表（类似抖音直播间）；
      2. 这部分如果想要完全自定义，可以使用upperRightCustomizedViewHolder；
      3. 如果不想自定义，即可以使用我们默认的exitButton即可；
      4. 使用了upperRightCustomizedViewHolder后exitButton会自动隐藏；
      5. 同样，对upperRightCustomizedViewHolder的修改需要在push当前VC前完成；
 */
/**
 * @brief 位于屏幕右上位置（跟upperLeftCustomizedViewHolder保持水平）的用户自定义区域，便于在上面添加自定义控件，比如退出直播按钮等
 * 注意，这个对象内部已经构造好，直接使用即可；
 */
@property (strong, nonatomic) UIView* upperRightCustomizedViewHolder;

/**
 * @brief 直播间右上角的关闭按钮；支持外部更改样式；
 * 注意，如需隐藏，只要实例化upperRightCustomizedViewHolder即可；
 * 注意，这个对象内部已经构造好，直接使用即可；
 */
@property (strong, nonatomic) UIButton* exitButton;


/** **********************************直播间页面中间位置定制 ************************************ */
/**
   整体介绍：
      1.  这部分是头像区域之下，弹幕区域之上；
      2. 这部分默认实现包括一个直播公告noticeButton和一个观众列表membersButton，可以直接使用这两个button也可以修改他们；
      3. 如果不想要noticeButton和membersButton，可以在push当前VC前添加middleCustomizedViewHolder；
      4. 如果需要在这部分添加自定义UI控件，可以在middleCustomizedViewHolder上添加；
      5. 使用了middleCustomizedViewHolder后，noticeButton和membersButton会自动隐藏；
      6. 同样，对upperRightCustomizedViewHolder的修改需要在push当前VC前完成；
 */
/**
 * @brief 位于中间位置的用户自定义区域，便于在上面添加自定义控件
 * 注意，这个对象内部已经构造好，直接使用即可；
 */
@property (strong, nonatomic) UIView* middleCustomizedViewHolder;

/**
 * @brief 直播间左上角头像下放的公告按钮，外部可以通过设置alpha来隐藏
 * 注意，默认不显示，需要在ASLRBLiveInitConfig中的ASLRBLiveRoomMiddleViewsConfig中配置是否显示；
 * 注意，这个对象内部已经构造好，直接使用即可；
 */
@property (strong, nonatomic) UIButton* noticeButton;

/**
 * @brief 直播间左上角头像下放的直播间内人员按钮，外部可以通过设置alpha来隐藏;
 * 注意，这个仅存在主播直播间模式下存在；
 * 注意，默认不显示，需要在ASLRBLiveInitConfig中的ASLRBLiveRoomMiddleViewsConfig中配置是否显示；
 * 注意，这个对象内部已经构造好，直接使用即可；
 */
@property (strong, nonatomic) UIView* membersButton;

/** **********************************直播间弹幕区域 ************************************ */
/**
   整体介绍：
      1.  直播间弹幕区域默认不支持再添加其他UI控件；
      2. 如果想要定制弹幕中字体、颜色等，可以参考ASLRBLiveInitConfig中ASLRBLiveCommentViewConfig中配置；
 */
/**
 * @brief 展示直播弹幕的view
 * 注意，这个对象内部已经构造好，直接使用即可；
 */
@property (strong, nonatomic) UIView<ASLRBLiveCommentViewProtocol>* liveCommentView;


/** **********************************直播间页面底部位置定制 ************************************ */
/**
   整体介绍：
      1.  直播间底部区域，一般包括输入框、点赞等按钮；
      2. 这部分如果想要完全自定义，可以使用bottomCustomizedViewHolder；
      3. 如果不想自定义，即使用我们默认的实现，可以使用bottomViewsHolder；如果同时想在默认实现上做一定程度的更改，可以直接修改bottomViewsHolder；
      4. 同样，对bottomCustomizedViewHolder的修改需要在push当前VC前完成；
      5 . 使用了bottomCustomizedViewHolder后，bottomViewsHolder上的默认实现会自动隐藏；
 */
/**
 * @brief 位于底部位置的用户自定义区域，便于在上面添加自定义控件，比如弹幕输入框、点赞按钮等
 * 注意，这个对象内部已经构造好，直接使用即可；
 */
@property (strong, nonatomic) UIView* bottomCustomizedViewHolder;

/**
 *@brief  直播间底部的承载view，包括输入框、点赞、分享按钮等；外部可以在push当前vc前修改；
 * 注意，如何隐藏，只要实例化bottomCustomizedViewHolder即可；
 * 注意，这个对象内部已经构造好，直接使用即可；
 */
@property (strong, nonatomic) UIView<ASLRBLiveRoomBottomViewsHolderProtocol>* bottomViewsHolder;

/**
 * @brief 仅主播侧直播间底部“...”按钮点击后出现的面板；暂不支持外部修改内容；
 * 注意，这个对象内部已经构造好，直接使用即可；
 */
@property (strong, nonatomic) UIView<ASLRBLiveRoomMoreInteractionPanelProtocol>* moreInteractionPanel;


/** **********************************直播间观众端的占位图 ************************************ */
/**
 *@brief 直播间背景图，会在直播前和直播结束都出现；
 *注意，只会出现在观众端；
 */
@property (nonatomic, strong) UIImage* backgroundImage DEPRECATED_MSG_ATTRIBUTE("建议使用新接口");

/**
 *@brief 直播间直播开始前的背景图；优先级高于backgroundImage；
 *仅直播模式下观众端有效；
 */
@property (nonatomic, strong) UIImage* backgroundImageBeforeLiving;

/**
 *@brief 直播间直播结束后的背景图；优先级高于backgroundImage；
 *仅直播模式下观众端有效；
 */
@property (nonatomic, strong) UIImage* backgroundImageAfterLiving;

/**
 * @brief 在push当前vc之前调用，用来做进入直播页面前的准备工作；注意在onSuccess之后才可以push当前vc；
 * @param onSuccess  setup成功时回调，如果之前没有传入live-id，此时会回传创建直播成功后的live-id；注意避免block内强引用外部对象造成循环引用
 * @param onFailure  setup失败时回调，会有具体的错误信息；注意避免block内强引用外部对象造成循环引用;
 */
- (void) setupOnSuccess:(void(^)(NSString* liveID))onSuccess
              onFailure:(void(^)(NSString* errorMessage))onFailure;

/**
 * @brief 主播端调用的效果：结束当前直播，退出直播间，;
 * 观众端调用的效果：退出直播间;
 */
- (void) exitLiveRoom;

/**
 * @brief 主播端调用的效果：结束当前直播，退出直播间; 观众端调用相当于exitLiveRoom，无论写入YES or NO
 * @param stopLive  是否退出页面的同时结束当前直播；YES会结束当前直播，并退出直播间；NO不会结束当前直播，只会退出当前直播间；
 */
- (void) exitLiveRoom:(BOOL)stopLive;

/**
 * @brief 弹幕区发送消息;
 * @param message 要发送的消息内容
 * @param onSuccess 成功的回调
 * @param onFailure 失败的回调
 */
- (void) sendComment:(NSString *)message
           onSuccess:(void (^)(void))onSuccess
           onFailure:(void (^)(ASLRBLiveRoomError code, NSString * errorMessage))onFailure;

/**
 * @brief 弹幕区发送消息;
 * @param message 要发送的消息内容
 * @param extension 自定义扩展字段
 * @param onSuccess 成功的回调
 * @param onFailure 失败的回调
 */
- (void) sendComment:(NSString *)message
           extension:(NSDictionary<NSString *,NSString *> *)extension
           onSuccess:(void (^)(void))onSuccess
           onFailure:(void (^)(ASLRBLiveRoomError code, NSString * errorMessage))onFailure;

/**
 * @brief 点赞，一次一个，内部会对短时间内的多次点赞进行聚合;
 */
- (void) sendLike;

/**
 * @brief 发送自定义消息给特定用户
 */
- (void) sendCustomMessage:(NSString*)message
                   toUsers:(NSArray<NSString*>*)userIDs
                 onSuccess:(void (^)(void))onSuccess
                 onFailure:(void (^)(NSString* errorMessage))onFailure;

/**
 * @brief 发送自定义消息给直播间内的所有用户
 */
- (void) sendCustomMessageToAll:(NSString*)message
                      onSuccess:(void (^)(void))onSuccess
                      onFailure:(void (^)(NSString* errorMessage))onFailure;

/**
 * @brief 获取直播间管理员列表;
 */
- (void) getLiveRoomAdministers:(void(^)(NSArray* administers))onGotten;

/**
 * @brief 获取直播详细情况, setup成功后才能调用
 */
- (void) getLiveDetail:(void (^)(NSDictionary* detail))onSuccess
             onFailure:(void (^)(NSString* errorMessage))onFailure;


@end

NS_ASSUME_NONNULL_END
