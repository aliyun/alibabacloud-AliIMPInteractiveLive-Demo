//
//  AIRBDStudentView.m
//  AliInteractiveRoomDemo
//
//  Created by fernando on 2021/5/24.
//

#import "AIRBDStudentView.h"
#import "AIRBDStudentListCell.h"
#import <Masonry/Masonry.h>
#import <AliInteractiveRoomBundle/AliInteractiveRoomBundle.h>

#import "AIRBDToast.h"
#import "AIRBDCommentView.h"
#import "Utilities/Utility.h"
#import "AIRBDEnvironments.h"

const int32_t kStudentListRoomMemberPageSizeForStudentView = 10;

@interface AIRBDStudentView() <UITableViewDelegate,UITableViewDataSource,AIRBRoomChannelDelegate,UITextFieldDelegate,AIRBDStudentListCellDelegate,AIRBRTCDelegate, AIRBWhiteBoardDelegate, AIRBLivePlayerDelegate>
@property (copy, nonatomic) NSString* userID;
@property (copy, nonatomic) NSString* roomID;
@property (strong, nonatomic) id<AIRBRoomChannelProtocol> room;
@property (assign, nonatomic) BOOL roomEntered;
@property (nonatomic) int applyForRTCLinkButtonStatus;  // 0:取消 1:申请 2:结束
@property (strong, nonatomic) UILabel* classTitle;
@property (strong, nonatomic) UIView* playerViewHolder;
@property (strong, nonatomic) UILabel* interactiveTab;
@property (strong, nonatomic) UIButton* studentListItem;
@property (strong, nonatomic) UIButton* commentsListItem;
@property (strong, nonatomic) UIButton* documentListItem;
@property (strong, nonatomic) AIRBDCommentView* commentListView;
@property (strong, nonatomic) UITableView* studentsListView;//学生列表
@property (strong, nonatomic) UIView* interactiveContentView;
@property (strong, nonatomic) UITextField* messageInputField;
@property (strong, nonatomic) UIButton* sendButton;
@property (strong, nonatomic) UIView* sendHolderView;
@property (strong, nonatomic) UIButton* applyForRTCLinkButton;
@property (nonatomic, strong) NSMutableArray* studentsListDataSource;
@property (nonatomic, strong) NSMutableDictionary* studentsLists;
@property (strong, nonatomic) NSLock* studentListLock;
@property (assign, nonatomic) BOOL hasMoreRoomMembers;
@property (assign, nonatomic) int32_t currentRoomMemberListPageNum;
@end

@implementation AIRBDStudentView

- (instancetype)init
{
    self = [super init];
    if (self) {
        _studentsLists = [[NSMutableDictionary alloc] init];
        _studentListLock = [[NSLock alloc] init];
        _studentsListDataSource = [[NSMutableArray alloc] init];
        _hasMoreRoomMembers = YES;
        _studentsLists = [[NSMutableDictionary alloc] init];
        _studentListLock = [[NSLock alloc] init];
        _currentRoomMemberListPageNum = 1;
        _hasMoreRoomMembers = YES;
    }
    return self;
}

- (instancetype)initWithFrame:(CGRect)frame
{
    self = [super initWithFrame:frame];
    if (self) {
        _studentsLists = [[NSMutableDictionary alloc] init];
        _studentListLock = [[NSLock alloc] init];
        _studentsListDataSource = [[NSMutableArray alloc] init];
        _hasMoreRoomMembers = YES;
        _studentsLists = [[NSMutableDictionary alloc] init];
        _studentListLock = [[NSLock alloc] init];
        _currentRoomMemberListPageNum = 1;
        _hasMoreRoomMembers = YES;
    }
    return self;
}

- (void) dealloc {
    [[NSNotificationCenter defaultCenter] removeObserver:self];
    [self leaveRoom];
}

-(void) leaveRoom{
    if (self.roomEntered){
        [self.room leaveRoom];
        self.roomEntered = NO;
    }
}

-(void) startWithRoomID:(NSString*)roomID userID:(NSString*)userID {
    self.userID = userID;
    self.roomID = roomID;
    self.room = [[AIRBRoomEngine sharedInstance] getRoomChannelWithRoomID:roomID];
    self.room.delegate = self;
    [self.room enterRoom];
    [self setupUI];
}

- (void) stop {
    [self leaveRoom];
}

#pragma -mark UI

- (UITableView *)studentsListView{
    if(!_studentsListView){
        __weak typeof(self) weakSelf = self;
        self.studentsListView = [[UITableView alloc] init];
        self.studentsListView.backgroundColor = [UIColor whiteColor];
        [self.studentsListView setSeparatorStyle:UITableViewCellSeparatorStyleNone];
        self.studentsListView.delegate = self;
        self.studentsListView.dataSource = self;
        [self.interactiveContentView addSubview:self.studentsListView];
        [self.studentsListView mas_makeConstraints:^(MASConstraintMaker * _Nonnull make) {
            make.edges.equalTo(weakSelf.interactiveContentView).with.insets(UIEdgeInsetsMake(0, 0, 0, 0));
        }];
        [_studentsListView setHidden:YES];
    }
    return _studentsListView;
}

- (void) setupUI {
    self.backgroundColor = [UIColor clearColor];
    
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(keyBoardWillShow:) name:UIKeyboardWillShowNotification object:nil];
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(keyBoardWillHide:) name:UIKeyboardWillHideNotification object:nil];
    
    self.classTitle = [[UILabel alloc] init];
    self.classTitle.backgroundColor = [UIColor lightGrayColor];
    self.classTitle.text = @"课程名称";
    self.classTitle.textColor = [UIColor whiteColor];
    self.classTitle.textAlignment = NSTextAlignmentCenter;
    self.classTitle.layer.borderColor = [UIColor blackColor].CGColor;
    self.classTitle.layer.borderWidth = 1.0;
    [self addSubview:self.classTitle];
    
    __weak typeof(self) weakSelf = self;
    [self.classTitle mas_makeConstraints:^(MASConstraintMaker *make) {
        if (@available(iOS 11.0, *)) {
            make.top.equalTo(weakSelf.mas_safeAreaLayoutGuideTop).with.offset(10);
        } else {
            make.top.equalTo(weakSelf).with.offset(10);
        }
        make.centerX.equalTo(weakSelf.mas_centerX);
        make.size.mas_equalTo(CGSizeMake(weakSelf.bounds.size.width, 30));
    }];
    
    self.playerViewHolder = [[UIView alloc] init];
    self.playerViewHolder.backgroundColor = [UIColor lightGrayColor];
    [self addSubview:self.playerViewHolder];
    [self.playerViewHolder mas_makeConstraints:^(MASConstraintMaker *make) {
        make.top.equalTo(weakSelf.classTitle.mas_bottom);
        make.centerX.equalTo(weakSelf.mas_centerX);
        make.size.mas_equalTo(CGSizeMake(weakSelf.bounds.size.width, weakSelf.bounds.size.width * 9 / 16));
    }];
    
    self.interactiveTab = [[UILabel alloc] init];
    self.interactiveTab.backgroundColor = [UIColor clearColor];
    self.interactiveTab.userInteractionEnabled = YES;
    [self addSubview:self.interactiveTab];
    [self.interactiveTab mas_makeConstraints:^(MASConstraintMaker *make) {
        make.top.equalTo(weakSelf.playerViewHolder.mas_bottom);
        make.left.equalTo(weakSelf.mas_left);
        make.right.equalTo(weakSelf.mas_right);
        make.height.mas_equalTo(40);
    }];
    
    self.studentListItem = [[UIButton alloc] init];
    [self.studentListItem addTarget:self action:@selector(studentListItemAction:) forControlEvents:UIControlEventTouchUpInside];
    self.studentListItem.backgroundColor = [UIColor whiteColor];
    self.studentListItem.layer.borderColor = [[UIColor blackColor] CGColor];
    self.studentListItem.layer.borderWidth = 1.0;
    [self.studentListItem setTitle:@"学员" forState:UIControlStateNormal];
    [self.studentListItem setTitleColor:[UIColor blackColor] forState:UIControlStateNormal];
    [self.studentListItem setTitleColor:[UIColor blueColor] forState:UIControlStateSelected];
    [self.interactiveTab addSubview:self.studentListItem];
    [self.studentListItem mas_makeConstraints:^(MASConstraintMaker *make) {
        make.left.equalTo(weakSelf.interactiveTab);
        make.top.equalTo(weakSelf.interactiveTab);
        make.bottom.equalTo(weakSelf.interactiveTab.mas_bottom);
        make.width.mas_equalTo(weakSelf.bounds.size.width / 3);
    }];
    
    self.commentsListItem = [[UIButton alloc] init];
    self.commentsListItem.backgroundColor = [UIColor whiteColor];
    [self.commentsListItem setTitleColor:[UIColor blackColor] forState:UIControlStateNormal];
    [self.commentsListItem setTitleColor:[UIColor blueColor] forState:UIControlStateSelected];
    [self.commentsListItem setTitle:@"讨论" forState:UIControlStateNormal];
    self.commentsListItem.layer.borderColor = [[UIColor blackColor] CGColor];
    [self.commentsListItem addTarget:self action:@selector(commentsItemAction:) forControlEvents:UIControlEventTouchUpInside];
    self.commentsListItem.layer.borderWidth = 1.0;
    [self.interactiveTab addSubview:self.commentsListItem];
    [self.commentsListItem mas_makeConstraints:^(MASConstraintMaker *make) {
        make.right.equalTo(weakSelf.interactiveTab);
        make.top.equalTo(weakSelf.interactiveTab);
        make.bottom.equalTo(weakSelf.interactiveTab.mas_bottom);
        make.width.mas_equalTo(weakSelf.bounds.size.width / 3);
    }];
    
    self.documentListItem = [[UIButton alloc] init];
    self.documentListItem.backgroundColor = [UIColor lightGrayColor];
    [self.documentListItem setTitleColor:[UIColor blackColor] forState:UIControlStateNormal];
    [self.documentListItem setTitle:@"文档" forState:UIControlStateNormal];
    [self.documentListItem addTarget:self action:@selector(documentItemAction:) forControlEvents:UIControlEventTouchUpInside];
    self.documentListItem.layer.borderWidth = 1.0;
    self.documentListItem.layer.borderColor = [[UIColor blackColor] CGColor];
    [self.interactiveTab addSubview:self.documentListItem];
    [self.documentListItem mas_makeConstraints:^(MASConstraintMaker *make) {
        make.right.equalTo(weakSelf.commentsListItem.mas_left);
        make.left.equalTo(weakSelf.studentListItem.mas_right);
        make.bottom.equalTo(weakSelf.interactiveTab);
        make.top.equalTo(weakSelf.interactiveTab);
    }];
    
    self.sendHolderView = [[UIView alloc] init];
    self.sendHolderView.backgroundColor = [UIColor whiteColor];
    [self addSubview:self.sendHolderView];
    [self.sendHolderView mas_makeConstraints:^(MASConstraintMaker *make) {
        make.bottom.equalTo(weakSelf.mas_bottom);
        make.left.equalTo(weakSelf.mas_left);
        make.right.equalTo(weakSelf.mas_right);
        make.height.mas_equalTo(50);
    }];
    
    self.sendButton = [[UIButton alloc] init];
    [self.sendButton setTitle:@"发送" forState:UIControlStateNormal];
    self.sendButton.layer.cornerRadius = 5.0;
    self.sendButton.backgroundColor = [UIColor blackColor];
    [self.sendHolderView  addSubview:self.sendButton];
    [self.sendButton mas_makeConstraints:^(MASConstraintMaker *make) {
        make.right.equalTo(weakSelf.sendHolderView).with.offset(-2);
        make.bottom.equalTo(weakSelf.sendHolderView).with.offset(-2);
        make.top.equalTo(weakSelf.sendHolderView).with.offset(2);
        make.width.mas_equalTo(60);
    }];
    [self.sendButton addTarget:self action:@selector(sendButtonAction:) forControlEvents:UIControlEventTouchUpInside];
    
    self.messageInputField = [[UITextField alloc] init];
    self.messageInputField.delegate = self;
    self.messageInputField.backgroundColor = [UIColor clearColor];
    [self.messageInputField setTextColor:[UIColor blackColor]];
    self.messageInputField.borderStyle = UITextBorderStyleRoundedRect;
    self.messageInputField.layer.borderColor = [[UIColor blackColor] CGColor];
    self.messageInputField.layer.borderWidth = 1.0;
    self.messageInputField.layer.cornerRadius = 5.0;
    self.messageInputField.keyboardType = UIKeyboardTypeDefault;
    self.messageInputField.returnKeyType = UIReturnKeySend;
    NSAttributedString *attrString = [[NSAttributedString alloc] initWithString:@"说点什么吧"
                                                                     attributes:@{
                                                                         NSForegroundColorAttributeName:[UIColor lightGrayColor],
                                                                         NSFontAttributeName:[UIFont systemFontOfSize:14]
                                                                     }];
    self.messageInputField.attributedPlaceholder = attrString;
    [self.sendHolderView addSubview:self.messageInputField];
    [self.messageInputField mas_makeConstraints:^(MASConstraintMaker *make) {
        make.bottom.equalTo(weakSelf.sendHolderView).with.offset(-2);
        make.left.equalTo(weakSelf.sendHolderView).with.offset(2);
        make.top.equalTo(weakSelf.sendHolderView).with.offset(2);
        make.right.equalTo(weakSelf.sendButton.mas_left).with.offset(-5);
    }];
    
    self.interactiveContentView = [[UIView alloc] init];
    self.interactiveContentView.backgroundColor = [UIColor whiteColor];
    [self addSubview:self.interactiveContentView];
    [self.interactiveContentView mas_makeConstraints:^(MASConstraintMaker *make) {
        make.top.equalTo(weakSelf.interactiveTab.mas_bottom);
        make.bottom.equalTo(weakSelf.sendHolderView.mas_top);
        make.left.equalTo(weakSelf);
        make.right.equalTo(weakSelf);
    }];
    
    self.commentListView = [[AIRBDCommentView alloc] initWithCommentStyle:WhiteboardStyle];
    [self.interactiveContentView addSubview:self.commentListView];
    [self.commentListView mas_makeConstraints:^(MASConstraintMaker *make) {
        make.edges.equalTo(weakSelf.interactiveContentView);
    }];
    [self.commentListView setHidden:YES];
    
    [self studentsListView];
    
    self.applyForRTCLinkButton = [[UIButton alloc] init];
    [self.applyForRTCLinkButton setTitle:@"申请\n连麦" forState:UIControlStateNormal];
    self.applyForRTCLinkButton.titleLabel.lineBreakMode = 0;
    self.applyForRTCLinkButton.layer.cornerRadius = 5.0;
    self.applyForRTCLinkButton.backgroundColor = [UIColor blackColor];
    [self addSubview:self.applyForRTCLinkButton];
    [self.applyForRTCLinkButton mas_makeConstraints:^(MASConstraintMaker *make) {
        make.right.equalTo(weakSelf.sendHolderView).with.offset(-2);
        make.bottom.equalTo(weakSelf.sendHolderView.mas_top).with.offset(-5);
        make.top.equalTo(weakSelf.sendHolderView.mas_top).with.offset(-62);
        make.width.mas_equalTo(50);
    }];
    [self.applyForRTCLinkButton addTarget:self action:@selector(applyForRTCLinkButtonAction:) forControlEvents:UIControlEventTouchUpInside];
    self.applyForRTCLinkButtonStatus = 1;
    
    [self layoutSubviews];
    [_commentListView insertNewComment:@"系统提示：你是🌺🌺🌺🌺好学生"];
    
    
}

- (UIViewController *)getViewController {
    //通过响应者链，取得此视图所在的视图控制器
    UIResponder *next = self.nextResponder;
    do {
        //判断响应者对象是否是视图控制器类型
        if ([next isKindOfClass:[UIViewController class]]) {
            return (UIViewController *)next;
        }
        
        next = next.nextResponder;
    }while(next != nil);
    
    return nil;
}

-(NSDictionary *) getDataDicFromInfo:(NSDictionary *)info{
    NSData *turnData = [[info valueForKey:@"data"] dataUsingEncoding:NSUTF8StringEncoding];
    NSDictionary *dataDic = [NSJSONSerialization JSONObjectWithData:turnData options:NSJSONReadingMutableLeaves error:nil];
    return dataDic;
}

- (void) startTRCLink{
    self.applyForRTCLinkButtonStatus = 2;
    dispatch_async(dispatch_get_main_queue(), ^{
        [self.room.livePlayer stop];
        [self.playerViewHolder.subviews makeObjectsPerformSelector:@selector(removeFromSuperview)];
        
        [[AIRBDToast shareInstance] makeToast:[NSString stringWithFormat:@"开始连麦"] duration:1.0];
        [self.applyForRTCLinkButton setTitle:@"结束\n连麦" forState:UIControlStateNormal];
    });
    [self.room.rtc startLocalPreview];
    [self.room.rtc joinChannel];
}

- (void) stopRTCLinkAndStartLivePlayer{
    self.applyForRTCLinkButtonStatus = 1;
    [self.room.rtc leaveChannel];
    dispatch_async(dispatch_get_main_queue(), ^{
        [self.playerViewHolder.subviews makeObjectsPerformSelector:@selector(removeFromSuperview)];
        [self.applyForRTCLinkButton setTitle:@"申请\n连麦" forState:UIControlStateNormal];
    });
    [self.room.livePlayer start];
}

- (void) queryMoreRoomMemberInfoOnce {
    if (self.hasMoreRoomMembers) {
        [self.room getRoomUserListWithPageNum:self.currentRoomMemberListPageNum
                                     pageSize:kStudentListRoomMemberPageSizeForStudentView
                                    onSuccess:^(AIRBRoomChannelUserListResponse * _Nonnull response) {
            for (AIRBRoomChannelUser* user in response.userList) {
                if ([user.openID isEqualToString:self.userID]) {
                    continue;
                } else {
                    AIRBDStudentListItemModel* model = [[AIRBDStudentListItemModel alloc] init];
                    model.userID = user.openID;
                    model.status = AIRBDStudentStatusReadyForCalled;
                    [self addStudentListItemModel:model index:self.studentsListDataSource.count];
                    [self.studentsLists setValue:model forKey:user.openID];
                }
            }
            dispatch_async(dispatch_get_main_queue(), ^{
                [self.studentsListView reloadData];
            });
            if (response.hasMore) {
                self.currentRoomMemberListPageNum++;
            } else {
                self.hasMoreRoomMembers = NO;
            }
        } onFailure:^(NSString * _Nonnull errorMessage) {
            
        }];
    }
    //请求在RTC的列表
    [self.room.rtc queryCurrentPeerListWithType:AIRBRTCPeerTypeJoinedAlready pageNum:self.currentRoomMemberListPageNum pageSize:kStudentListRoomMemberPageSizeForStudentView onSuccess:^(AIRBRoomChannelUserListResponse * _Nonnull rsp) {
        for (AIRBRoomChannelUser* user in rsp.userList) {
            if ([user.openID isEqualToString:self.userID]) {
                continue;
            } else {
                AIRBDStudentListItemModel* model = [[AIRBDStudentListItemModel alloc] init];
                if (model) {
                    [self removeStudentListItemModel:model];
                }
                [self.studentsLists removeObjectForKey:user.openID];
                model.userID = user.openID;
                model.status = AIRBDStudentStatusAlreadyOnTheCall;
                [self addStudentListItemModel:model index:self.studentsListDataSource.count];
                [self.studentsLists setValue:model forKey:user.openID];
            }
        }
        dispatch_async(dispatch_get_main_queue(), ^{
            [self.studentsListView reloadData];
        });
    } onFailure:^(NSString * _Nonnull errorMessage) {
        
    }];
}

#pragma -mark AIRBRoomChannelDelegate

- (void) onAIRBRoomChannelEvent:(AIRBRoomChannelEvent) event info:(NSDictionary*)info {
    switch (event) {
        case AIRBRoomChannelEventEntered: {
            self.roomEntered = YES;
            self.room.whiteboard.delegate = self;
            self.room.rtc.delegate = self;
            self.room.livePlayer.delegate = self;
            dispatch_async(dispatch_get_main_queue(), ^{
                AIRBWhiteBoardConfig* config = [[AIRBWhiteBoardConfig alloc] init];
                config.whiteboardContentWidth = self.interactiveContentView.bounds.size.width;
                config.whiteboardContentHeight = self.interactiveContentView.bounds.size.width * 9.0 / 16.0;
                
                [self.room.whiteboard openWithConfig:config];
                [self queryMoreRoomMemberInfoOnce];
            });
            [self.room.livePlayer start];
            
        }
            break;
        case AIRBRoomChannelEventLeft:
            self.roomEntered = NO;
            break;
        case AIRBRoomChannelEventMessageReceived: {
            AIRBRoomChannelMessageType messageType = [[info valueForKey:@"type"] integerValue];
            switch (messageType) {
                case AIRBRoomChannelMessageTypeRoomMembersInOut:{
                    NSDictionary *dataDic = [self getDataDicFromInfo:info];
                    BOOL enter = [[dataDic valueForKey:@"enter"] boolValue];
                    NSString* userID = [dataDic valueForKey:@"userId"];
                    if (userID.length > 0) {
                        if (enter) {
                            AIRBDStudentListItemModel* model = [[AIRBDStudentListItemModel alloc] init];
                            model.userID = userID;
                            model.status = AIRBDStudentStatusReadyForCalled;
                            [self addStudentListItemModel:model index:self.studentsListDataSource.count];
                            [self.studentsLists setValue:model forKey:userID];
                        } else {
                            AIRBDStudentListItemModel* model = [self.studentsLists valueForKey:userID];
                            if (model) {
                                [self removeStudentListItemModel:model];
                            }
                            [self.studentsLists removeObjectForKey:userID];
                        }
                        dispatch_async(dispatch_get_main_queue(), ^{

                            [self.studentsListView reloadData];
                        });
                    }
                }
                    break;
                case AIRBRoomChannelMessageTypePeerJoinRTCSucceeded:{
                    NSDictionary *dataDic = [self getDataDicFromInfo:info];
                    NSArray* userList = [dataDic valueForKey:@"userList"];
                    for (NSDictionary* userInfo in userList) {
                        [self updateStudent:[userInfo valueForKey:@"userId"] toNewStatus:AIRBDStudentStatusAlreadyOnTheCall];
                    }
                    dispatch_async(dispatch_get_main_queue(), ^{
                        [self.studentsListView reloadData];
                    });
                }
                    
                    break;
                case AIRBRoomChannelMessageTypePeerJoinRTCFailed: // 某人加入RTC超时或者拒绝加入RTC
                    break;
                case AIRBRoomChannelMessageTypePeerLeaveRTC:{
                    NSDictionary *dataDic = [self getDataDicFromInfo:info];
                    NSArray* userList = [dataDic valueForKey:@"userList"];
                    if ([userList isKindOfClass:[NSArray class]] && userList.count > 0) {
                        for (NSDictionary* userInfo in userList) {
                            [self updateStudent:[userInfo valueForKey:@"userId"] toNewStatus:AIRBDStudentStatusReadyForCalled];
                        }
                    }
                    dispatch_async(dispatch_get_main_queue(), ^{
                        [self.studentsListView reloadData];
                    });
                }
                    break;
                case AIRBRoomChannelMessageTypePeerKickedFromRTC:{ // 某人被踢出了RTC
                    /*
                     type:4
                     data:{"confId":"AliRtcbed04681daba472995","type":4,"userList":[{"cameraStatus":0,"deviceId":"","duration":0,"micphoneStatus":0,"nickname":"","source":0,"sourceId":"345","status":5,"tenantId":"","userId":"345"}],"version":1576074671583281939}>
                     */
                    NSDictionary *dataDic = [self getDataDicFromInfo:info];
                    NSArray* userList = [dataDic valueForKey:@"userList"];
                    if ([userList isKindOfClass:[NSArray class]] && userList.count > 0) {
                        for (NSDictionary* userInfo in userList) {
                            [self updateStudent:[userInfo valueForKey:@"userId"] toNewStatus:AIRBDStudentStatusReadyForCalled];
                        }
                    }
                    dispatch_async(dispatch_get_main_queue(), ^{
                        [self.studentsListView reloadData];
                    });
                    NSString *userIDKicked = [[[dataDic valueForKey:@"userList"] objectAtIndex:0] valueForKey:@"userId"];
                    if ([self.userID isEqualToString:userIDKicked]){
                        [self stopRTCLinkAndStartLivePlayer];
                    }
                }
                    
                    break;
                case AIRBRoomChannelMessageTypeRTCStarted:
                    break;
                case AIRBRoomChannelMessageTypeRTCStopped:
                    break;
                case AIRBRoomChannelMessageTypeChatCommentReceived:{
                    NSDictionary *dataDic = [self getDataDicFromInfo:info];
                    NSString* dataString = [NSString stringWithFormat:@"%@:%@",[dataDic valueForKey:@"creatorNick"],[dataDic valueForKey:@"content"]];
                    [_commentListView insertNewComment:dataString];
                }
                    break;
                case AIRBRoomChannelMessageTypeOnRTCCalled:{
                    /*
                     type:7
                     data:{"calleeList":[{"cameraStatus":1,"deviceId":"","duration":0,"enterTime":0,"errorCode":"","extension":"","leaveTime":0,"micphoneStatus":1,"nickname":"567","source":0,"sourceId":"","status":2,"tenantId":"","userId":"567"}],"caller":{"userId":"890"},"type":7,"version":1532224144663793248}
                     */
                    NSDictionary *dataDic = [self getDataDicFromInfo:info];
                    NSString *userIDCalled = [[[dataDic valueForKey:@"calleeList"] objectAtIndex:0] valueForKey:@"userId"];
//                    NSString *userIDCaller = [[dataDic valueForKey:@"caller"] valueForKey:@"userId"];

                    if ([self.userID isEqualToString:userIDCalled]){
                        if (self.applyForRTCLinkButtonStatus == 0){
                            [self startTRCLink];
                        } else if (self.applyForRTCLinkButtonStatus == 1) {    // 呼叫：接受/拒绝
                            dispatch_async(dispatch_get_main_queue(), ^{
                                    [[AIRBDToast shareInstance] makeToast:[NSString stringWithFormat:@"收到老师的连麦呼叫"] duration:1.0];
                            
                            
                            UIAlertController *alertController = [UIAlertController alertControllerWithTitle:@"收到老师的连麦呼叫" message:@"" preferredStyle:UIAlertControllerStyleAlert];
                            [alertController addAction:[UIAlertAction actionWithTitle:@"拒绝" style:UIAlertActionStyleCancel handler:^(UIAlertAction * _Nonnull action) {
                                [self.room.rtc acceptCall:NO];
                            }]];
                            [alertController addAction:[UIAlertAction actionWithTitle:@"接受" style:UIAlertActionStyleDefault handler:^(UIAlertAction * _Nonnull action) {
                                [self startTRCLink];
                            }]];
                            
                            [[self getViewController] presentViewController:alertController animated:YES completion:nil];
                            });
                        }
                        
                    }
                    
                    break;
                }
                case AIRBRoomChannelMessageTypeOnReceiveRTCJoiningApplication:
                    break;
                case AIRBRoomChannelMessageTypeOnReceiveRTCJoiningApplicationRespond:{
                    /*
                     data:{"approve":true,"confId":"AliRtc560e5cd7ff4b408aa6","type":9,"uid":"56788","version":6461602022442420323}
                     */
                    NSDictionary *dataDic = [self getDataDicFromInfo:info];
                    NSString *userIDApproved = [dataDic valueForKey:@"uid"];
                    BOOL approve = [[dataDic valueForKey:@"approve"] boolValue];
                    if ([self.userID isEqualToString:userIDApproved] && !approve){
                        self.applyForRTCLinkButtonStatus = 1;
                        dispatch_async(dispatch_get_main_queue(), ^{
                                [[AIRBDToast shareInstance] makeToast:[NSString stringWithFormat:@"连麦申请被拒绝"] duration:1.0];
                                [self.applyForRTCLinkButton setTitle:@"申请\n连麦" forState:UIControlStateNormal];
                        });
                    }
                }
                    
                    break;
                case AIRBRoomChannelMessageTypeLiveStartedByOther:
                    [self.room.livePlayer start];
                    [_commentListView insertNewComment:@"开始上课"];
                    break;
                case AIRBRoomChannelMessageTypeLiveStoppedByOther:{
                    dispatch_async(dispatch_get_main_queue(), ^{
                        [[AIRBDToast shareInstance] makeToast:@"课堂已结束" duration:3.0];
                        [self.room.livePlayer stop];
                    });
                    [_commentListView insertNewComment:@"课堂已结束"];
                }
                default:
                    return;
            }
        }
            break;
        default:
            break;
    }
}

- (void) onAIRBLivePlayerErrorWithCode:(AIRBErrorCode)code message:(NSString*)msg{
    dispatch_async(dispatch_get_main_queue(), ^{
        [[AIRBDToast shareInstance] makeToast:[NSString stringWithFormat:@"onAIRBLivePlayerErrorWithCode:(0x%lx, %@)", (long)code, msg] duration:3.0];
    });
}

- (void) onAIRBLivePlayerEvent:(AIRBLivePlayerEvent)event info:(NSDictionary*)info {
    switch (event) {
        case AIRBLivePlayerEventStarted: {
            dispatch_async(dispatch_get_main_queue(), ^{
                self.room.livePlayer.playerView.frame = self.playerViewHolder.bounds;
                [self.playerViewHolder addSubview:self.room.livePlayer.playerView];
                //                __weak typeof(self) weakSelf = self;
                //                [self.playerViewHolder addSubview:[self.room playerView]];
                //                [[self.room playerView] mas_makeConstraints:^(MASConstraintMaker *make) {
                //                    make.top.equalTo(weakSelf.playerViewHolder.mas_top);
                //                    make.left.equalTo(weakSelf.playerViewHolder.mas_left);
                //                    make.size.mas_equalTo(CGSizeMake(weakSelf.bounds.size.width, weakSelf.bounds.size.width*9/16));
                //                }];
            });
        }
            break;
        case AIRBLivePlayerEventStartLoading:
            break;
        case AIRBLivePlayerEventEndLoading:
            break;
        default:
            break;
    }
}

- (void) onAIRBRTCErrorWithCode:(AIRBErrorCode)code message:(NSString*)msg{
    dispatch_async(dispatch_get_main_queue(), ^{
        [[AIRBDToast shareInstance] makeToast:[NSString stringWithFormat:@"onAIRBRTCErrorWithCode:(0x%lx, %@)", (long)code, msg] duration:3.0];
    });
}

- (void) onAIRBRTCEvent:(AIRBRTCEvent)event info:(NSDictionary *)info{
    switch (event) {
        case AIRBRTCEventLocalPreviewStarted:{
            dispatch_async(dispatch_get_main_queue(), ^{
                self.room.rtc.rtcLocalView.frame = self.playerViewHolder.bounds;
                [self.playerViewHolder addSubview:self.room.rtc.rtcLocalView];
                
                //                __weak typeof(self) weakSelf = self;
                //                [self.playerViewHolder addSubview:[self.room rtcLocalView]];
                //                [[self.room rtcLocalView] mas_makeConstraints:^(MASConstraintMaker *make) {
                //                    make.top.equalTo(weakSelf.playerViewHolder.mas_top);
                //                    make.left.equalTo(weakSelf.playerViewHolder.mas_left);
                //                    make.size.mas_equalTo(CGSizeMake(weakSelf.bounds.size.width, weakSelf.bounds.size.width*9/16));
                //                }];
                
            });
        }
            break;
            
        case AIRBRTCEventJoinSucceeded:
            break;
        case AIRBRTCEventLeaveSucceeded:
            break;
        case AIRBRTCEventBypassLiveStarted:
            break;
        case AIRBRTCEventStatusNotificationReceived:{
            dispatch_async(dispatch_get_main_queue(), ^{
                [[AIRBDToast shareInstance] makeToast:[info valueForKey:@"data"] duration:3.0];
            });
        }
            break;
        default:
            break;
    }
}



- (void) onAIRBRTCRemotePeerViewAvailable:(NSString *)userID view:(UIView *)view{
    dispatch_async(dispatch_get_main_queue(), ^{
        __weak typeof(self) weakSelf = self;
        [self.playerViewHolder addSubview:view];
        [view mas_makeConstraints:^(MASConstraintMaker *make) {
            make.top.equalTo(weakSelf.playerViewHolder.mas_top);
            make.left.equalTo(weakSelf.playerViewHolder.mas_left);
            make.size.mas_equalTo(CGSizeMake(weakSelf.playerViewHolder.bounds.size.width/4, weakSelf.playerViewHolder.bounds.size.height/4));
        }];
    });
}

- (void) onAIRBWhiteBoardEvent:(AIRBWhiteBoardEvent)event info:(NSDictionary*)info {
    switch (event) {
        case AIRBWhiteBoardEventOpened: {
            dispatch_async(dispatch_get_main_queue(), ^{
//                self.room.whiteBoardContentView.frame = self.interactiveContentView.bounds;
                self.room.whiteboard.whiteboardView.frame = CGRectMake(0, (self.interactiveContentView.bounds.size.height - self.room.whiteboard.whiteboardView.bounds.size.height) / 2, self.room.whiteboard.whiteboardView.bounds.size.width, self.room.whiteboard.whiteboardView.bounds.size.height);
                [self.interactiveContentView addSubview:self.room.whiteboard.whiteboardView];
            });
        }
            break;
        case AIRBWhiteBoardEventDestroied:
            break;
        default:
            break;
    }
}

- (void) onAIRBRoomChannelErrorWithCode:(AIRBErrorCode)code message:(NSString *)message {
    dispatch_async(dispatch_get_main_queue(), ^{
        [[AIRBDToast shareInstance] makeToast:[NSString stringWithFormat:@"onAIRBRoomChannelErrorWithCode:(0x%lx, %@)", (long)code, message] duration:3.0];
    });
}


#pragma -mark UITextFieldDelegate
- (BOOL)textFieldShouldReturn:(UITextField *)textField {
    if([textField isEqual:self.messageInputField]){
        if (textField.text.length > 0) {
            [self.room.chat sendMessage:self.messageInputField.text onSuccess:^{
                dispatch_async(dispatch_get_main_queue(), ^{
                    [[AIRBDToast shareInstance] makeToast:@"发送成功" duration:1.0];
                    textField.text = nil;
                });;
            } onFailure:^(NSString * _Nonnull errorMessage) {
                dispatch_async(dispatch_get_main_queue(), ^{
                    [[AIRBDToast shareInstance] makeToast:@"发送失败" duration:1.0];
                });;
            }];
        }
    }
    [textField resignFirstResponder];
    return YES;
}

- (void) requestWhiteBoardAccessTokenWithDocKey:(NSString*)docKey completion:(void (^)(AIRBWhiteBoardToken* token))onGotToken {
    NSString* path = [NSString stringWithFormat:@"http://%@/whiteboard/open", [AIRBDEnvironments shareInstance].appServerHost];
    NSString* s = [NSString stringWithFormat:@"%@?docKey=%@&userId=%@", path, docKey, self.userID];
    
    NSString* dateString = [Utility currentDateString];
    NSString* nonce = [Utility randomNumString];
    
    NSDictionary* headers = @{
        @"a-app-id" : @"imp-room",
        @"a-signature-method" : @"HMAC-SHA1",
        @"a-signature-version" : @"1.0",
        @"a-timestamp" : dateString,
        @"a-signature-nonce" : nonce,
    };
    
    NSDictionary* params = @{
        @"docKey" : docKey,
        @"userId" : self.userID
    };
    
    NSString* signedString = [Utility AIRBRequestSignWithSignSecret:[AIRBDEnvironments shareInstance].signSecret method:@"POST" path:path parameters:params headers:headers];
    NSLog(@"signedString:%@", signedString);
    
    NSURL* url = [[NSURL alloc] initWithString:s];
    
    NSMutableURLRequest* request = [NSMutableURLRequest requestWithURL:url];
    request.HTTPMethod = @"POST";
    [request setValue:@"imp-room" forHTTPHeaderField:@"a-app-id"];
    [request setValue:@"HMAC-SHA1" forHTTPHeaderField:@"a-signature-method"];
    [request setValue:@"1.0" forHTTPHeaderField:@"a-signature-version"];
    [request setValue:signedString forHTTPHeaderField:@"a-signature"];
    [request setValue:dateString forHTTPHeaderField:@"a-timestamp"];
    [request setValue:nonce forHTTPHeaderField:@"a-signature-nonce"];
    
    NSURLSession* session = [NSURLSession sharedSession];
    NSURLSessionTask* task = [session dataTaskWithRequest:request completionHandler:^(NSData * _Nullable data, NSURLResponse * _Nullable response, NSError * _Nullable error) {
        if (data && !error) {
            NSDictionary* dic = [NSJSONSerialization JSONObjectWithData:data
                                                                options:NSJSONReadingMutableContainers
                                                                  error:nil];
            NSLog(@"getWBToken data:%@", dic);
            if ([dic isKindOfClass:[NSDictionary class]] && dic.count > 0 && [dic valueForKey:@"result"]) {
                NSDictionary* resultDic = [dic valueForKey:@"result"];
                if ([resultDic isKindOfClass:[NSDictionary class]] && resultDic.count > 0 && [resultDic valueForKey:@"documentAccessInfo"]) {
                    NSDictionary* documentAccessInfo = [resultDic valueForKey:@"documentAccessInfo"];
                    if (documentAccessInfo.count >= 3) {
                        AIRBWhiteBoardToken* tokenInfo = [[AIRBWhiteBoardToken alloc] init];
                        tokenInfo.accessToken = [documentAccessInfo valueForKey:@"accessToken"];
                        tokenInfo.collabHost = [documentAccessInfo valueForKey:@"collabHost"];
//                        tokenInfo.permission = [[documentAccessInfo valueForKey:@"permission"] charValue];
                        /* 临时逻辑：只读权限 */
                        tokenInfo.permission = 1;
                        /* *************** */
                        onGotToken(tokenInfo);
                    }
                }
            }
        }
    }];
    [task resume];
}

- (BOOL)textFieldShouldClear:(UITextField *)textField{
    return YES;
}

#pragma -mark NSNotification
- (void)keyBoardWillShow:(NSNotification *) note {
    if([self.messageInputField isFirstResponder]){
        // 获取用户信息
        NSDictionary *userInfo = [NSDictionary dictionaryWithDictionary:note.userInfo];
        // 获取键盘高度
        CGRect keyBoardBounds  = [[userInfo objectForKey:UIKeyboardFrameEndUserInfoKey] CGRectValue];
        CGFloat keyBoardHeight = keyBoardBounds.size.height;
        // 获取键盘动画时间
        CGFloat animationTime  = [[userInfo objectForKey:UIKeyboardAnimationDurationUserInfoKey] floatValue];
        [self bringSubviewToFront:self.sendHolderView];
        // 定义好动作
        void (^animation)(void) = ^void(void) {
            self.messageInputField.transform = CGAffineTransformMakeTranslation(0, -(keyBoardHeight));
//            self.sendButton.transform = CGAffineTransformMakeTranslation(0, -(keyBoardHeight));
        };
        
        if (animationTime > 0) {
            [UIView animateWithDuration:animationTime animations:animation];
        } else {
            animation();
        }
    }
}

- (void)keyBoardWillHide:(NSNotification *) note {
    
    // 获取用户信息
    NSDictionary *userInfo = [NSDictionary dictionaryWithDictionary:note.userInfo];
    // 获取键盘动画时间
    CGFloat animationTime  = [[userInfo objectForKey:UIKeyboardAnimationDurationUserInfoKey] floatValue];
    
    // 定义好动作
    void (^animation)(void) = ^void(void) {
        self.messageInputField.transform = CGAffineTransformIdentity;
//        self.sendButton.transform = CGAffineTransformIdentity;
    };
    
    if (animationTime > 0) {
        [UIView animateWithDuration:animationTime animations:animation];
    } else {
        animation();
    }
}

#pragma -mark UIButton Selector
- (void)studentListItemAction:(UIButton*)sender {
    sender.backgroundColor = [UIColor lightGrayColor];
    
    self.commentsListItem.backgroundColor = [UIColor whiteColor];
    self.documentListItem.backgroundColor = [UIColor whiteColor];
    
    [_commentListView setHidden:YES];
    [_studentsListView setHidden:NO];
    [self.room.whiteboard.whiteboardView setHidden:YES];
}

- (void)documentItemAction:(UIButton*)sender {
    sender.backgroundColor = [UIColor lightGrayColor];
    
    self.commentsListItem.backgroundColor = [UIColor whiteColor];
    self.studentListItem.backgroundColor = [UIColor whiteColor];
    
    [_commentListView setHidden:YES];
    [_studentsListView setHidden:YES];
    [self.room.whiteboard.whiteboardView setHidden:NO];
}

- (void)commentsItemAction:(UIButton*)sender {
    sender.backgroundColor = [UIColor lightGrayColor];
    
    self.studentListItem.backgroundColor = [UIColor whiteColor];
    self.documentListItem.backgroundColor = [UIColor whiteColor];
    
    [_commentListView setHidden:NO];
    [_studentsListView setHidden:YES];
    [self.room.whiteboard.whiteboardView setHidden:YES];
}

- (void)sendButtonAction:(UIButton*)sender{
    if (self.messageInputField.text.length > 0) {
        [self.room.chat sendMessage:self.messageInputField.text onSuccess:^{
            dispatch_async(dispatch_get_main_queue(), ^{
                [[AIRBDToast shareInstance] makeToast:@"发送成功" duration:1.0];
            });;
        } onFailure:^(NSString * _Nonnull errorMessage) {
            dispatch_async(dispatch_get_main_queue(), ^{
                [[AIRBDToast shareInstance] makeToast:@"发送失败" duration:1.0];
            });;
        }];
    }
    self.messageInputField.text = nil;
    [self.messageInputField resignFirstResponder];
}

- (void)applyForRTCLinkButtonAction:(UIButton*)sender{
    if (self.applyForRTCLinkButtonStatus == 1){
        self.applyForRTCLinkButtonStatus = 0;
        dispatch_async(dispatch_get_main_queue(), ^{
            [[AIRBDToast shareInstance] makeToast:[NSString stringWithFormat:@"申请连麦中..."] duration:1.0];
            [self.applyForRTCLinkButton setTitle:@"取消\n连麦" forState:UIControlStateNormal];
        });
        [self.room.rtc applyForJoining:YES];
    }
    else if (self.applyForRTCLinkButtonStatus == 0){
        self.applyForRTCLinkButtonStatus = 1;
        dispatch_async(dispatch_get_main_queue(), ^{
            [[AIRBDToast shareInstance] makeToast:[NSString stringWithFormat:@"取消申请连麦中..."] duration:1.0];
            [self.applyForRTCLinkButton setTitle:@"申请\n连麦" forState:UIControlStateNormal];
        });
        [self.room.rtc applyForJoining:NO];
    }
    else if (self.applyForRTCLinkButtonStatus == 2){
        [self stopRTCLinkAndStartLivePlayer];
        dispatch_async(dispatch_get_main_queue(), ^{
                [[AIRBDToast shareInstance] makeToast:[NSString stringWithFormat:@"结束连麦中..."] duration:1.0];
        });
    }
}

#pragma -mark UITableViewDataSource
- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section
{
    if (tableView == self.studentsListView) {
        return self.studentsListDataSource.count + 1;
    } else {
        return 0;
    }
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath
{
    if (tableView == self.studentsListView) {
        static NSString *reusedStr = @"AIRBDTeacherViewStudentList";
        AIRBDStudentListCell *cell = [tableView dequeueReusableCellWithIdentifier:reusedStr];
        if (!cell) {
            cell = [[AIRBDStudentListCell alloc] initWithStyle:UITableViewCellStyleDefault
                                               reuseIdentifier:reusedStr];
            cell.selectionStyle = UITableViewCellSelectionStyleNone;
            cell.owner = 1;
            cell.delegate = self;
        }
        
        if ([indexPath row] == ([self.studentsListDataSource count])) {
            // 定制最后一行的cell
            cell.textLabel.text = @"加载更多..";
            cell.textLabel.textColor = [UIColor blackColor];
            cell.studentActionButton.hidden = YES;
        } else {
            AIRBDStudentListItemModel* model = [self.studentsListDataSource objectAtIndex:[indexPath row]];
            cell.model = model;
            cell.studentActionButton.hidden = NO;
        }
        
        return cell;
    } else {
        static NSString *reusedStr = @"AIRBDTeacherViewCommentsList";
        UITableViewCell *cell = [tableView dequeueReusableCellWithIdentifier:reusedStr];
        if (cell == nil)
        {
            cell = [[UITableViewCell alloc] initWithStyle:UITableViewCellStyleDefault
                                          reuseIdentifier:reusedStr];
        }
        return cell;
    }
    
}

#pragma -mark UITableViewDelegate
- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath
{
    // 如果是最后一行点击事件,则触发一个事件
    if (indexPath.row == ([self.studentsListDataSource count]))
    {
        if (self.hasMoreRoomMembers) {
            [self performSelectorInBackground:@selector(queryMoreRoomMemberInfoOnce)
                                   withObject:nil];
            [tableView deselectRowAtIndexPath:indexPath animated:YES];
        } else {
            
        }
    }
}

- (CGFloat)tableView:(UITableView *)tableView heightForRowAtIndexPath:(NSIndexPath *)indexPath {
    return 44;
}


- (void)onStudentActionButtonClickedWithUserID:(nonnull NSString *)userID action:(AIRBDStudentItemAction)action {
    
}

- (void) addStudentListItemModel:(AIRBDStudentListItemModel*)model index:(int64_t)index {
    [self.studentListLock lock];
    [self.studentsListDataSource insertObject:model atIndex:index];
    [self.studentListLock unlock];
}

- (void) removeStudentListItemModel:(AIRBDStudentListItemModel*)model {
    [self.studentListLock lock];
    [self.studentsListDataSource removeObject:model];
    [self.studentListLock unlock];
}

-(void)updateStudent:(NSString*)userID toNewStatus:(AIRBDStudentStatus)status{
    //注意只是更新数据源(单个model),需要列表视图reloadData
    if (userID.length > 0) {
        if ([userID isEqualToString:self.userID]) {
            return;
        } else {
            AIRBDStudentListItemModel* model = [self.studentsLists valueForKey:userID];
            if (model) {
                [self removeStudentListItemModel:model];
            }
            [self.studentsLists removeObjectForKey:userID];
            model = [[AIRBDStudentListItemModel alloc] init];
            model.userID = userID;
            model.status = status;
            if(status == AIRBDStudentStatusAlreadyOnTheCall||status == AIRBDStudentStatusNowApplying){
                [self addStudentListItemModel:model index:0];
            }else{
                [self addStudentListItemModel:model index:self.studentsListDataSource.count];
            }
            
            [self.studentsLists setValue:model forKey:userID];
        }
    }
}


@end
