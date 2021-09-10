//
//  AIRBDTeacherView.m
//  AliInteractiveRoomDemo
//
//  Created by fernando on 2021/5/31.
//

#import "AIRBDTeacherView.h"

#import <Masonry/Masonry.h>
#import <AliInteractiveRoomBundle/AliInteractiveRoomBundle.h>
#import "AIRBDCommentView.h"
#import "AIRBDToast.h"
#import "AIRBDStudentListCell.h"
#import "AIRBDStudentListItemModel.h"
#import "AIRBDMultiRTCView.h"
#import "Utilities/Utility.h"
#import "AIRBDEnvironments.h"

const int32_t kStudentListRoomMemberPageSize = 10;

@interface AIRBDTeacherView() <AIRBRoomChannelDelegate,UITextFieldDelegate,UITableViewDataSource, UITableViewDelegate,AIRBDStudentListCellDelegate, AIRBRTCDelegate, AIRBWhiteBoardDelegate>
@property (strong, nonatomic) AIRBRoomEngineConfig* config;
@property (copy, nonatomic) NSString* userID;
@property (copy, nonatomic) NSString* roomID;
@property (strong, nonatomic) id<AIRBRoomChannelProtocol> room;
@property (assign, nonatomic) BOOL roomEntered;

@property (strong, nonatomic) UILabel* performanceLabel;
@property (strong, nonatomic) UIView* controlPanel;
@property (strong, nonatomic) UIButton* startClassButton;
@property (strong, nonatomic) UIButton* interactiveWhiteBoardButton;
@property (strong, nonatomic) UIButton* interactiveVideoButton;
@property (strong, nonatomic) UIView* mainContentView;
@property (strong, nonatomic) UIView* whiteBoardContentView;
@property (strong, nonatomic) AIRBDMultiRTCView* videoContentView;
@property (strong, nonatomic) UIButton* exchangeContentButton;
@property (strong, nonatomic) UIView* interactiveHolderView;
@property (strong, nonatomic) UIView* localCameraHolder;
@property (strong, nonatomic) UILabel* interactiveTab;

@property (strong, nonatomic) UIButton* studentListTab;
@property (strong, nonatomic) UITableView* studentsListView;
@property (nonatomic, strong) NSMutableArray* studentsListDataSource;
@property (nonatomic, strong) NSMutableDictionary* studentsLists;
@property (assign, nonatomic) int32_t studentJoinedListEndIndex;
@property (assign, nonatomic) int32_t studentApplyingListEndIndex;
@property (strong, nonatomic) NSLock* studentListLock;
@property (assign, nonatomic) int32_t currentRTCPeersJoinedListPageNum;
@property (assign, nonatomic) int32_t currentRTCPeersApplyingListPageNum;
@property (assign, nonatomic) int32_t currentRoomMemberListPageNum;

@property (strong, nonatomic) UIButton* commentsListTab;
@property (strong, nonatomic) AIRBDCommentView* commentsListView;
@property (strong, nonatomic) UIView* chatHolderView;
@property (strong, nonatomic) UITextField* messageInputField;
@property (strong, nonatomic) UIView* sendHolderView;
@property (strong, nonatomic) UIButton* chatForbiddenedPoint;
@property (strong, nonatomic) UIButton* chatForbiddenedLabel;
@property (strong, nonatomic) UIButton* RTCForbiddenedPoint;
@property (strong, nonatomic) UIButton* RTCForbiddenedLabel;
@property (assign, nonatomic) BOOL isChatForbiddened;
@property (assign, nonatomic) BOOL isRTCForbiddened;
@property (assign, nonatomic) BOOL hasMoreRoomMembers;

@property (strong, nonatomic) UIButton* setButton;
@property (strong, nonatomic) UIButton* uploadFileButton;
@property (strong, nonatomic) UIButton* recordButton;
@property (strong, nonatomic) UIButton* recordPauseButton;
@property (strong, nonatomic) UISlider* volumeSlider;
@property (strong, nonatomic) UIButton* replayButton;
@property (strong, nonatomic) UIButton* sendButton;
@end

@implementation AIRBDTeacherView

#pragma -mark UI-performanceLabel

- (UILabel *)performanceLabel{
    if(!_performanceLabel){
        __weak typeof(self) weakSelf = self;
        const CGFloat kSelfWidth = self.bounds.size.width;
        _performanceLabel = [[UILabel alloc] init];
        _performanceLabel.backgroundColor = [UIColor lightGrayColor];
        _performanceLabel.text = @"   cpu:  内存:  网络延迟:  ";
        _performanceLabel.textColor = [UIColor whiteColor];
        _performanceLabel.textAlignment = NSTextAlignmentLeft;
        _performanceLabel.layer.borderColor = [UIColor blackColor].CGColor;
        _performanceLabel.layer.borderWidth = 1.0;
        [self addSubview:_performanceLabel];
        [_performanceLabel mas_makeConstraints:^(MASConstraintMaker *make) {
            make.top.equalTo(weakSelf).with.offset(5);
            if (@available(iOS 11.0, *)) {
                make.left.equalTo(weakSelf.mas_safeAreaLayoutGuideLeft);
                make.right.equalTo(weakSelf.mas_safeAreaLayoutGuideRight);
                make.height.mas_equalTo(30);
            } else {
                make.size.mas_equalTo(CGSizeMake(kSelfWidth, 30));
            }
            
        }];
    }
//    [self setButton];
    return _performanceLabel;
}

- (UIButton *)setButton{
    if(!_setButton){
        __weak typeof(self) weakSelf = self;
        _setButton = [[UIButton alloc] init];
        [_setButton addTarget:self action:@selector(setButtonAction:) forControlEvents:UIControlEventTouchUpInside];
        [_setButton setTitle:@"⚙️" forState:UIControlStateNormal];
        _setButton.layer.cornerRadius = 2.0;
        [_performanceLabel addSubview:_setButton];
        [_setButton mas_makeConstraints:^(MASConstraintMaker *make) {
            make.centerY.equalTo(weakSelf.performanceLabel);
            make.top.equalTo(weakSelf.performanceLabel).with.offset(1);
            make.right.equalTo(weakSelf.performanceLabel.mas_right).with.offset(-10);
            make.width.mas_equalTo(30);
        }];
    }
    return _setButton;
}

#pragma -mark UI-controlPanel

- (UIView *)controlPanel{
    if(!_controlPanel){
        const CGFloat kSelfWidth = self.bounds.size.width;
        __weak typeof(self) weakSelf = self;
        _controlPanel = [[UIView alloc] init];
        _controlPanel.backgroundColor = [UIColor lightGrayColor];
        [self addSubview:_controlPanel];
        [_controlPanel mas_makeConstraints:^(MASConstraintMaker *make) {
            make.bottom.equalTo(weakSelf);
            
            if (@available(iOS 11.0, *)) {
                make.right.equalTo(weakSelf.mas_safeAreaLayoutGuideRight).with.offset(-1 * (kSelfWidth / 4));
                make.left.equalTo(weakSelf.mas_safeAreaLayoutGuideLeft);
            } else {
                make.right.equalTo(weakSelf.mas_right).with.offset(-1 * (kSelfWidth / 4));
                make.left.equalTo(weakSelf);
            }
            make.height.mas_equalTo(60);
        }];
        [self startClassButton];
//        [self uploadFileButton];
//        [self recordButton];
//        [self recordPauseButton];
//        [self volumeSlider];
//        [self replayButton];
    }
    return _controlPanel;
}

- (UIButton *)startClassButton{
    if(!_startClassButton){
        __weak typeof(self) weakSelf = self;
        _startClassButton = [[UIButton alloc] init];
        [_startClassButton addTarget:self action:@selector(startClassButtonAction:) forControlEvents:UIControlEventTouchUpInside];
        _startClassButton.backgroundColor = [UIColor colorWithRed:0 green:142.0/255.0 blue:189.0/255.0 alpha:1.0];
        [_startClassButton setTitle:@"开始上课" forState:UIControlStateNormal];
        _startClassButton.layer.cornerRadius = 2.0;
        [_controlPanel addSubview:_startClassButton];
        [_startClassButton mas_makeConstraints:^(MASConstraintMaker *make) {
            make.centerY.equalTo(weakSelf.controlPanel);
            make.top.equalTo(weakSelf.controlPanel).with.offset(5);
            make.centerX.equalTo(weakSelf.controlPanel);
            make.width.mas_equalTo(80);
        }];
    }
    return _startClassButton;
}

- (UIButton *)uploadFileButton{
    if(!_uploadFileButton){
        __weak typeof(self) weakSelf = self;
        _uploadFileButton = [[UIButton alloc] init];
        [_uploadFileButton addTarget:self action:@selector(uploadFileButtonAction:) forControlEvents:UIControlEventTouchUpInside];
        _uploadFileButton.backgroundColor = [UIColor colorWithRed:0 green:142.0/255.0 blue:189.0/255.0 alpha:1.0];
        [_uploadFileButton setTitle:@"文档上传" forState:UIControlStateNormal];
        _uploadFileButton.layer.cornerRadius = 5;
        [_controlPanel addSubview:_uploadFileButton];
        [_uploadFileButton mas_makeConstraints:^(MASConstraintMaker *make) {
            make.centerY.equalTo(weakSelf.controlPanel);
            make.top.equalTo(weakSelf.controlPanel).with.offset(5);
            make.left.equalTo(weakSelf.controlPanel);
            make.width.mas_equalTo(80);
        }];
    }
    return _uploadFileButton;
}

- (UIButton *)recordButton{
    if(!_recordButton){
        __weak typeof(self) weakSelf = self;
        _recordButton = [[UIButton alloc] init];
        [_recordButton addTarget:self action:@selector(recordButtonAction:) forControlEvents:UIControlEventTouchUpInside];
        [_recordButton setTitle:@"⏺️" forState:UIControlStateNormal];
        _recordButton.layer.cornerRadius = 5;
        _recordButton.enabled = NO;
        [_controlPanel addSubview:_recordButton];
        [_recordButton mas_makeConstraints:^(MASConstraintMaker *make) {
            make.centerY.equalTo(weakSelf.controlPanel);
            make.top.equalTo(weakSelf.controlPanel).with.offset(5);
            make.left.equalTo(weakSelf.startClassButton.mas_right).with.offset(5);
            make.width.mas_equalTo(30);
        }];
    }
    return _recordButton;
}

- (UIButton *)recordPauseButton{
    if(!_recordPauseButton){
        __weak typeof(self) weakSelf = self;
        _recordPauseButton = [[UIButton alloc] init];
        [_recordPauseButton addTarget:self action:@selector(recordPauseButtonAction:) forControlEvents:UIControlEventTouchUpInside];
        [_recordPauseButton setTitle:@"⏸️" forState:UIControlStateNormal];
        _recordPauseButton.layer.cornerRadius = 2.0;
        _recordPauseButton.enabled = NO;
        [_controlPanel addSubview:_recordPauseButton];
        [_recordPauseButton mas_makeConstraints:^(MASConstraintMaker *make) {
            make.centerY.equalTo(weakSelf.controlPanel);
            make.top.equalTo(weakSelf.controlPanel).with.offset(5);
            make.left.equalTo(weakSelf.recordButton.mas_right).with.offset(5);
            make.width.mas_equalTo(30);
        }];
    }
    return _recordPauseButton;
}

- (UISlider *)volumeSlider{
    if(!_volumeSlider){
        __weak typeof(self) weakSelf = self;
        _volumeSlider = [[UISlider alloc] init];
        [_volumeSlider addTarget:self action:@selector(volumeSliderAction:) forControlEvents:UIControlEventValueChanged];
        [_controlPanel addSubview:_volumeSlider];
        [_volumeSlider mas_makeConstraints:^(MASConstraintMaker *make) {
            make.centerY.equalTo(weakSelf.controlPanel);
            make.top.equalTo(weakSelf.controlPanel).with.offset(5);
            make.left.equalTo(weakSelf.recordPauseButton.mas_right).with.offset(3);
            make.width.mas_equalTo(80);
        }];
        [_volumeSlider setMinimumValue:0];
        [_volumeSlider setMaximumValue:100];
        [_volumeSlider setValue:60];
    }
    return _volumeSlider;
}

- (UIButton *)replayButton{
    if(!_replayButton){
        __weak typeof(self) weakSelf = self;
        _replayButton = [[UIButton alloc] init];
        [_replayButton addTarget:self action:@selector(replayButtonAction:) forControlEvents:UIControlEventTouchUpInside];
        _replayButton.backgroundColor = [UIColor colorWithRed:0 green:142.0/255.0 blue:189.0/255.0 alpha:1.0];
        [_replayButton setTitle:@"点播回放" forState:UIControlStateNormal];
        _replayButton.layer.cornerRadius = 5;
        [_controlPanel addSubview:_replayButton];
        [_replayButton mas_makeConstraints:^(MASConstraintMaker *make) {
            make.centerY.equalTo(weakSelf.controlPanel);
            make.top.equalTo(weakSelf.controlPanel).with.offset(5);
            make.right.equalTo(weakSelf.controlPanel.mas_right);
            make.width.mas_equalTo(80);
        }];
    }
    return _replayButton;
}

#pragma -mark UI-mainContentView

- (UIView *)mainContentView{
    if(!_mainContentView){
        __weak typeof(self) weakSelf = self;
        const CGFloat kSelfWidth = self.bounds.size.width;
        _mainContentView = [[UIView alloc] init];
        _mainContentView.backgroundColor = [UIColor whiteColor];
        [self addSubview:_mainContentView];
        [_mainContentView mas_makeConstraints:^(MASConstraintMaker *make) {
            make.top.equalTo(weakSelf.performanceLabel.mas_bottom);
            make.bottom.equalTo(weakSelf.controlPanel.mas_top);
            if (@available(iOS 11.0, *)) {
                make.left.equalTo(weakSelf.mas_safeAreaLayoutGuideLeft);
                make.right.equalTo(weakSelf.mas_safeAreaLayoutGuideRight).with.offset(-1 * (kSelfWidth / 4));
            } else {
                make.left.equalTo(weakSelf.mas_left);
                make.right.equalTo(weakSelf.mas_right).with.offset(-1 * (kSelfWidth / 4));
            }
        }];
        [self whiteBoardContentView];
        [self videoContentView];
        [self interactiveWhiteBoardButton];
        [self interactiveVideoButton];
        [self exchangeContentButton];
    }
    return _mainContentView;
}

- (UIView *)whiteBoardContentView{
    if(!_whiteBoardContentView){
        __weak typeof(self) weakSelf = self;
        const CGFloat kSelfWidth = self.bounds.size.width;
        _whiteBoardContentView = [[UIView alloc] init];
        _whiteBoardContentView.backgroundColor = [UIColor cyanColor];
        [_mainContentView addSubview:_whiteBoardContentView];
        [_mainContentView sendSubviewToBack:_whiteBoardContentView];
        [_whiteBoardContentView mas_makeConstraints:^(MASConstraintMaker *make) {
            make.top.equalTo(weakSelf.performanceLabel.mas_bottom);
            make.bottom.equalTo(weakSelf.controlPanel.mas_top);
            if (@available(iOS 11.0, *)) {
                make.left.equalTo(weakSelf.mas_safeAreaLayoutGuideLeft);
                make.right.equalTo(weakSelf.mas_safeAreaLayoutGuideRight).with.offset(-1 * (kSelfWidth / 4));
            } else {
                make.left.equalTo(weakSelf.mas_left);
                make.right.equalTo(weakSelf.mas_right).with.offset(-1 * (kSelfWidth / 4));
            }
        }];
    }
    return _whiteBoardContentView;
}

- (UIView *)videoContentView{
    if(!_videoContentView){
        __weak typeof(self) weakSelf = self;
        const CGFloat kSelfWidth = self.bounds.size.width;
        _videoContentView = [[AIRBDMultiRTCView alloc] init];
        [_mainContentView addSubview:_videoContentView];
        [_mainContentView sendSubviewToBack:_videoContentView];
        [_videoContentView mas_makeConstraints:^(MASConstraintMaker *make) {
            make.top.equalTo(weakSelf.performanceLabel.mas_bottom);
            make.bottom.equalTo(weakSelf.controlPanel.mas_top);
            if (@available(iOS 11.0, *)) {
                make.left.equalTo(weakSelf.mas_safeAreaLayoutGuideLeft);
                make.right.equalTo(weakSelf.mas_safeAreaLayoutGuideRight).with.offset(-1 * (kSelfWidth / 4));
            } else {
                make.left.equalTo(weakSelf.mas_left);
                make.right.equalTo(weakSelf.mas_right).with.offset(-1 * (kSelfWidth / 4));
            }
            
        }];
        [_videoContentView setUpUI];
        [_videoContentView setHidden:YES];
    }
    return _videoContentView;
}

- (UIButton *)interactiveWhiteBoardButton{
    if(!_interactiveWhiteBoardButton){
        __weak typeof(self) weakSelf = self;
        _interactiveWhiteBoardButton = [[UIButton alloc] init];
        [_interactiveWhiteBoardButton addTarget:self action:@selector(interactiveWhiteBoardButtonAction:) forControlEvents:UIControlEventTouchUpInside];
        _interactiveWhiteBoardButton.backgroundColor = [UIColor lightGrayColor];
        _interactiveWhiteBoardButton.layer.borderColor = [[UIColor blackColor] CGColor];
        _interactiveWhiteBoardButton.layer.borderWidth = 1.0;
        _interactiveWhiteBoardButton.layer.cornerRadius = 3.0;
        [_interactiveWhiteBoardButton setTitle:@"白板" forState:UIControlStateNormal];
        [_interactiveWhiteBoardButton setTitleColor:[UIColor blackColor] forState:UIControlStateNormal];
        [_mainContentView addSubview:_interactiveWhiteBoardButton];
        [_interactiveWhiteBoardButton mas_makeConstraints:^(MASConstraintMaker *make) {
            make.left.equalTo(weakSelf.mainContentView).with.offset(5);
            make.bottom.equalTo(weakSelf.mainContentView.mas_bottom).with.offset(-5);
            make.height.mas_equalTo(30);
            make.width.mas_equalTo(40);
        }];
    }
    return _interactiveWhiteBoardButton;
}

- (UIButton *)interactiveVideoButton{
    if(!_interactiveVideoButton){
        __weak typeof(self) weakSelf = self;
        UIButton* interactiveVideoButton = [[UIButton alloc] init];
        [interactiveVideoButton addTarget:self action:@selector(interactiveVideoButtonAction:) forControlEvents:UIControlEventTouchUpInside];
        interactiveVideoButton.backgroundColor = [UIColor clearColor];
        interactiveVideoButton.layer.borderColor = [[UIColor blackColor] CGColor];
        interactiveVideoButton.layer.borderWidth = 1.0;
        interactiveVideoButton.layer.cornerRadius = 3.0;
        [interactiveVideoButton setTitle:@"视频" forState:UIControlStateNormal];
        [interactiveVideoButton setTitleColor:[UIColor blackColor] forState:UIControlStateNormal];
        [_mainContentView addSubview:interactiveVideoButton];
        [interactiveVideoButton mas_makeConstraints:^(MASConstraintMaker *make) {
            make.left.equalTo(weakSelf.interactiveWhiteBoardButton.mas_right);
            make.bottom.equalTo(weakSelf.mainContentView.mas_bottom).with.offset(-5);
            make.height.mas_equalTo(30);
            make.width.mas_equalTo(40);
        }];
        self.interactiveVideoButton = interactiveVideoButton;
    }
    return _interactiveVideoButton;
}

- (UIButton *)exchangeContentButton{
    if(!_exchangeContentButton){
        __weak typeof(self) weakSelf = self;
        _exchangeContentButton = [[UIButton alloc] init];
        [_exchangeContentButton setImage:[UIImage imageNamed:@"exchange"] forState:UIControlStateNormal];
        [_exchangeContentButton addTarget:self action:@selector(exchangeContentButtonAction:) forControlEvents:UIControlEventTouchUpInside];
        [_mainContentView addSubview:_exchangeContentButton];
        [_exchangeContentButton mas_makeConstraints:^(MASConstraintMaker * _Nonnull make) {
            make.right.equalTo(weakSelf.mainContentView).with.offset(-5);
            make.bottom.equalTo(weakSelf.mainContentView).with.offset(-5);
            make.size.mas_equalTo(CGSizeMake(35, 35));
        }];
    }
    return _exchangeContentButton;
}

#pragma -mark UI-interactiveHolderView

- (UIView *)interactiveHolderView{
    if(!_interactiveHolderView){
        __weak typeof(self) weakSelf = self;
        const CGFloat kSelfWidth = self.bounds.size.width;
        _interactiveHolderView = [[UIView alloc] init];
        _interactiveHolderView.backgroundColor = [UIColor whiteColor];
        _interactiveHolderView.layer.borderColor = [UIColor blackColor].CGColor;
        _interactiveHolderView.layer.borderWidth = 1.0;
        [self addSubview:_interactiveHolderView];
        [_interactiveHolderView mas_makeConstraints:^(MASConstraintMaker *make) {
            make.top.equalTo(weakSelf.performanceLabel.mas_bottom);
            make.bottom.equalTo(weakSelf);
            if (@available(iOS 11.0, *)) {
                make.right.equalTo(weakSelf.mas_safeAreaLayoutGuideRight);
            } else {
                make.right.equalTo(weakSelf);
            }
            make.width.mas_equalTo(kSelfWidth / 4);
        }];
        [self localCameraHolder];
        [self interactiveTab];
        [self sendHolderView];
        [self chatHolderView];
        [self chatForbiddenedLabel];
        [self chatForbiddenedPoint];
        [self RTCForbiddenedLabel];
        
        [self RTCForbiddenedPoint];
        [_RTCForbiddenedLabel setHidden:YES];
        [_RTCForbiddenedPoint setHidden:YES];
        
    }
    return _interactiveHolderView;
}

- (UIView *)localCameraHolder{
    if(!_localCameraHolder){
        __weak typeof(self) weakSelf = self;
        _localCameraHolder = [[UIView alloc] init];
        _localCameraHolder.backgroundColor = [UIColor lightGrayColor];
        [_interactiveHolderView addSubview:_localCameraHolder];
        [_localCameraHolder mas_makeConstraints:^(MASConstraintMaker *make) {
            make.top.equalTo(weakSelf.interactiveHolderView);
            make.right.equalTo(weakSelf.interactiveHolderView);
            make.left.equalTo(weakSelf.interactiveHolderView);
            make.height.equalTo(weakSelf.interactiveHolderView.mas_width).multipliedBy(9.0/16.0);
        }];
    }
    return _localCameraHolder;
}

- (UILabel *)interactiveTab{
    if(!_interactiveTab){
        __weak typeof(self) weakSelf = self;
        _interactiveTab = [[UILabel alloc] init];
        _interactiveTab.backgroundColor = [UIColor clearColor];
        _interactiveTab.userInteractionEnabled = YES;
        [_interactiveHolderView addSubview:_interactiveTab];
        [_interactiveTab mas_makeConstraints:^(MASConstraintMaker *make) {
            make.top.equalTo(weakSelf.localCameraHolder.mas_bottom);
            make.left.equalTo(weakSelf.interactiveHolderView);
            make.right.equalTo(weakSelf.interactiveHolderView);
            make.height.equalTo(weakSelf.localCameraHolder.mas_height).multipliedBy(0.2);
        }];
        [self studentListTab];
        [self commentsListTab];
    }
    return _interactiveTab;
}

- (UIButton *)studentListTab{
    if(!_studentListTab){
        __weak typeof(self) weakSelf = self;
        _studentListTab = [[UIButton alloc] init];
        [_studentListTab addTarget:self action:@selector(studentListTabAction:) forControlEvents:UIControlEventTouchUpInside];
        _studentListTab.backgroundColor = [UIColor lightGrayColor];
        _studentListTab.layer.borderColor = [[UIColor blackColor] CGColor];
        _studentListTab.layer.borderWidth = 1.0;
        [_studentListTab setTitle:@"学员" forState:UIControlStateNormal];
        [_studentListTab setTitleColor:[UIColor blackColor] forState:UIControlStateNormal];
        [_studentListTab setTitleColor:[UIColor blueColor] forState:UIControlStateSelected];
        [_interactiveTab addSubview:_studentListTab];
        [_studentListTab mas_makeConstraints:^(MASConstraintMaker *make) {
            make.left.equalTo(weakSelf.interactiveTab);
            make.top.equalTo(weakSelf.interactiveTab);
            make.bottom.equalTo(weakSelf.interactiveTab);
            make.width.equalTo(weakSelf.interactiveTab.mas_width).multipliedBy(0.5);
        }];
    }
    return _studentListTab;
}

- (UIButton *)commentsListTab{
    if(!_commentsListTab){
        __weak typeof(self) weakSelf = self;
        _commentsListTab = [[UIButton alloc] init];
        _commentsListTab.backgroundColor = [UIColor whiteColor];
        [_commentsListTab setTitleColor:[UIColor blackColor] forState:UIControlStateNormal];
        [_commentsListTab setTitleColor:[UIColor blueColor] forState:UIControlStateSelected];
        [_commentsListTab setTitle:@"讨论" forState:UIControlStateNormal];
        _commentsListTab.layer.borderColor = [[UIColor blackColor] CGColor];
        [_commentsListTab addTarget:self action:@selector(commentsListTabAction:) forControlEvents:UIControlEventTouchUpInside];
        _commentsListTab.layer.borderWidth = 1.0;
        [_interactiveTab addSubview:_commentsListTab];
        [_commentsListTab mas_makeConstraints:^(MASConstraintMaker *make) {
            make.right.equalTo(weakSelf.interactiveTab);
            make.top.equalTo(weakSelf.interactiveTab);
            make.bottom.equalTo(weakSelf.interactiveTab);
            make.width.equalTo(weakSelf.interactiveTab.mas_width).multipliedBy(0.5);
        }];
    }
    return _commentsListTab;
}

- (UIView *)chatHolderView{
    if(!_chatHolderView){
        __weak typeof(self) weakSelf = self;
        _chatHolderView = [[UIView alloc] init];
        _chatHolderView.backgroundColor = [UIColor whiteColor];
        [_interactiveHolderView addSubview:_chatHolderView];
        [_chatHolderView mas_makeConstraints:^(MASConstraintMaker *make) {
            make.top.equalTo(weakSelf.interactiveTab.mas_bottom);
            make.bottom.equalTo(weakSelf.sendHolderView.mas_top);
            make.right.equalTo(weakSelf.interactiveHolderView);
            make.left.equalTo(weakSelf.interactiveHolderView);
        }];
        [self commentsListView];
        [self studentsListView];
    }
    return _chatHolderView;
}

- (AIRBDCommentView *)commentsListView{
    if(!_commentsListView){
        __weak typeof(self) weakSelf = self;
        _commentsListView = [[AIRBDCommentView alloc] initWithCommentStyle:WhiteboardStyle];
        [_chatHolderView addSubview:_commentsListView];
        [_commentsListView mas_makeConstraints:^(MASConstraintMaker * _Nonnull make) {
            make.edges.equalTo(weakSelf.chatHolderView).with.insets(UIEdgeInsetsMake(0, 0, 0, 0));
        }];
        _commentsListView.contentInset = UIEdgeInsetsMake(0, 0, 15, 0);
        [_commentsListView setHidden:NO];
    }
    return _commentsListView;
}

- (UITableView *)studentsListView{
    if(!_studentsListView){
        __weak typeof(self) weakSelf = self;
        _studentsListView = [[UITableView alloc] init];
        _studentsListView.backgroundColor = [UIColor whiteColor];
        [_studentsListView setSeparatorStyle:UITableViewCellSeparatorStyleNone];
        _studentsListView.delegate = self;
        _studentsListView.dataSource = self;
        [_chatHolderView addSubview:_studentsListView];
        [_studentsListView mas_makeConstraints:^(MASConstraintMaker * _Nonnull make) {
            make.edges.equalTo(weakSelf.chatHolderView).with.insets(UIEdgeInsetsMake(0, 0, 0, 0));
        }];
        [_studentsListView setHidden:YES];
    }
    return _studentsListView;
}

- (UIView *)sendHolderView{
    if(!_sendHolderView){
        __weak typeof(self) weakSelf = self;
        _sendHolderView = [[UIView alloc] init];
        _sendHolderView.backgroundColor = [UIColor whiteColor];
        [_interactiveHolderView addSubview:_sendHolderView];
        [_sendHolderView mas_makeConstraints:^(MASConstraintMaker *make) {
            make.bottom.equalTo(weakSelf.interactiveHolderView.mas_bottom);
            make.left.equalTo(weakSelf.interactiveHolderView);
            make.right.equalTo(weakSelf.interactiveHolderView);
            make.height.mas_equalTo(30);
        }];
        [self messageInputField];
        [self sendButton];
    }
    return _sendHolderView;
}

- (UITextField *)messageInputField{
    if(!_messageInputField){
        __weak typeof(self) weakSelf = self;
        _messageInputField = [[UITextField alloc] init];
        _messageInputField.delegate = self;
        _messageInputField.backgroundColor = [UIColor clearColor];
        _messageInputField.borderStyle = UITextBorderStyleRoundedRect;
        _messageInputField.layer.borderColor = [[UIColor blackColor] CGColor];
        _messageInputField.layer.borderWidth = 1.0;
        _messageInputField.layer.cornerRadius = 5.0;
        _messageInputField.keyboardType = UIKeyboardTypeDefault;
        _messageInputField.returnKeyType = UIReturnKeySend;
        _messageInputField.textColor = [UIColor blackColor];
        NSAttributedString *attrString = [[NSAttributedString alloc] initWithString:@"说点什么吧"
                                                                         attributes:@{
                                                                             NSForegroundColorAttributeName:[UIColor lightGrayColor],
                                                                             NSFontAttributeName:[UIFont systemFontOfSize:14]
                                                                         }];
        _messageInputField.attributedPlaceholder = attrString;
        [_sendHolderView addSubview:_messageInputField];
        [_messageInputField mas_makeConstraints:^(MASConstraintMaker *make) {
            make.bottom.equalTo(weakSelf.sendHolderView).with.offset(-2);
            make.left.equalTo(weakSelf.sendHolderView).with.offset(2);
            make.top.equalTo(weakSelf.sendHolderView).with.offset(2);
            make.width.equalTo(weakSelf.sendHolderView).multipliedBy(0.7);
        }];
    }
    return _messageInputField;
}

- (UIButton *)sendButton{
    if(!_sendButton){
        __weak typeof(self) weakSelf = self;
        _sendButton = [[UIButton alloc] init];
        [_sendButton setTitle:@"发送" forState:UIControlStateNormal];
        _sendButton.layer.cornerRadius = 5.0;
        _sendButton.backgroundColor = [UIColor blackColor];
        [_sendHolderView  addSubview:_sendButton];
        [_sendButton mas_makeConstraints:^(MASConstraintMaker *make) {
            make.right.equalTo(weakSelf.sendHolderView).with.offset(-2);
            make.bottom.equalTo(weakSelf.sendHolderView).with.offset(-2);
            make.top.equalTo(weakSelf.sendHolderView).with.offset(2);
            make.width.equalTo(weakSelf.sendHolderView).multipliedBy(0.29);
        }];
        [_sendButton addTarget:self action:@selector(sendButtonAction:) forControlEvents:UIControlEventTouchUpInside];
    }
    return _sendButton;
}

- (UIButton *)chatForbiddenedLabel{
    if(!_chatForbiddenedLabel){
        __weak typeof(self) weakSelf = self;
        _chatForbiddenedLabel = [[UIButton alloc] init];
        [_chatForbiddenedLabel addTarget:self action:@selector(chatForbiddenedButtonAction:) forControlEvents:UIControlEventTouchUpInside];
        NSAttributedString *buttonAttrString = [[NSAttributedString alloc] initWithString:@"允许学员讨论"
                                                                               attributes:
                                                @{
                                                    NSForegroundColorAttributeName:[UIColor blackColor],
                                                    NSFontAttributeName:[UIFont systemFontOfSize:12]
                                                }];
        [_chatForbiddenedLabel setAttributedTitle:buttonAttrString forState:UIControlStateNormal];
        _chatForbiddenedLabel.backgroundColor = [UIColor clearColor];
        [_interactiveHolderView addSubview:_chatForbiddenedLabel];
        [_interactiveHolderView bringSubviewToFront:_chatForbiddenedLabel];
        [_chatForbiddenedLabel mas_makeConstraints:^(MASConstraintMaker *make) {
            make.right.equalTo(weakSelf.chatHolderView).with.offset(-10);
            make.bottom.equalTo(weakSelf.chatHolderView.mas_bottom).with.offset(10);
            make.size.mas_equalTo(CGSizeMake(80, 30));
        }];
    }
    return _chatForbiddenedLabel;
}

- (UIButton *)chatForbiddenedPoint{
    if(!_chatForbiddenedPoint){
        __weak typeof(self) weakSelf = self;
        _chatForbiddenedPoint = [[UIButton alloc] init];
        [_chatForbiddenedPoint addTarget:self action:@selector(chatForbiddenedButtonAction:) forControlEvents:UIControlEventTouchUpInside];
        _chatForbiddenedPoint.backgroundColor = [UIColor clearColor];
        _chatForbiddenedPoint.layer.borderColor = [UIColor blackColor].CGColor;
        _chatForbiddenedPoint.layer.borderWidth = 1.0;
        [_interactiveHolderView addSubview:_chatForbiddenedPoint];
        [_interactiveHolderView bringSubviewToFront:_chatForbiddenedPoint];
        [_chatForbiddenedPoint mas_makeConstraints:^(MASConstraintMaker *make) {
            make.centerY.equalTo(weakSelf.chatForbiddenedLabel);
            make.right.equalTo(weakSelf.chatForbiddenedLabel.mas_left);
            make.size.mas_equalTo(CGSizeMake(8, 8));
        }];
    }
    return _chatForbiddenedPoint;
}

- (UIButton *)RTCForbiddenedLabel{
    if(!_RTCForbiddenedLabel){
        __weak typeof(self) weakSelf = self;
        _RTCForbiddenedLabel = [[UIButton alloc] init];
        [_RTCForbiddenedLabel addTarget:self action:@selector(RTCForbiddenedButtonAction:) forControlEvents:UIControlEventTouchUpInside];
        NSAttributedString *buttonAttrString = [[NSAttributedString alloc] initWithString:@"允许学员连麦"
                                                                               attributes:
                                                @{
                                                    NSForegroundColorAttributeName:[UIColor blackColor],
                                                    NSFontAttributeName:[UIFont systemFontOfSize:12]
                                                }];
        [_RTCForbiddenedLabel setAttributedTitle:buttonAttrString forState:UIControlStateNormal];
        _RTCForbiddenedLabel.backgroundColor = [UIColor clearColor];
        [_interactiveHolderView addSubview:_RTCForbiddenedLabel];
        [_interactiveHolderView bringSubviewToFront:_RTCForbiddenedLabel];
        [_RTCForbiddenedLabel mas_makeConstraints:^(MASConstraintMaker *make) {
            make.right.equalTo(weakSelf.chatHolderView).with.offset(-10);
            make.bottom.equalTo(weakSelf.chatHolderView.mas_bottom).with.offset(10);
            make.size.mas_equalTo(CGSizeMake(80, 30));
        }];
    }
    return _RTCForbiddenedLabel;
}

- (UIButton *)RTCForbiddenedPoint{
    if(!_RTCForbiddenedPoint){
        __weak typeof(self) weakSelf = self;
        _RTCForbiddenedPoint = [[UIButton alloc] init];
        [_RTCForbiddenedPoint addTarget:self action:@selector(RTCForbiddenedButtonAction:) forControlEvents:UIControlEventTouchUpInside];
        _RTCForbiddenedPoint.backgroundColor = [UIColor clearColor];
        _RTCForbiddenedPoint.layer.borderColor = [UIColor blackColor].CGColor;
        _RTCForbiddenedPoint.layer.borderWidth = 1.0;
        [_interactiveHolderView addSubview:_RTCForbiddenedPoint];
        [_interactiveHolderView bringSubviewToFront:_RTCForbiddenedPoint];
        [_RTCForbiddenedPoint mas_makeConstraints:^(MASConstraintMaker *make) {
            make.centerY.equalTo(weakSelf.RTCForbiddenedLabel);
            make.right.equalTo(weakSelf.RTCForbiddenedLabel.mas_left);
            make.size.mas_equalTo(CGSizeMake(8, 8));
        }];
        [_RTCForbiddenedPoint setHidden:YES];
    }
    return _RTCForbiddenedPoint;
}

- (void) setupUI {
    self.backgroundColor = [UIColor blackColor];
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(keyBoardWillShow:) name:UIKeyboardWillShowNotification object:nil];
        [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(keyBoardWillHide:) name:UIKeyboardWillHideNotification object:nil];
    });
    [self performanceLabel];
    [self controlPanel];
    [self mainContentView];
    [self interactiveHolderView];
    [self layoutSubviews];
//    [_commentsListView insertNewComment:@"老师进入了房间"];
}

- (void) queryAllRTCPeers {
    [self.room.rtc queryCurrentPeerListWithType:AIRBRTCPeerTypeJoinedAlready
                                       pageNum:1
                                      pageSize:100000
                                     onSuccess:^(AIRBRoomChannelUserListResponse * _Nonnull rsp) {
        for (AIRBRoomChannelUser* user in rsp.userList) {
            if ([user.openID isEqualToString:self.userID]) {
                continue;
            } else {
                AIRBDStudentListItemModel* model = [[AIRBDStudentListItemModel alloc] init];
                model.userID = user.openID;
                model.status = AIRBDStudentStatusAlreadyOnTheCall;
                [self addStudentListItemModel:model index:0];
                [self.studentsLists setValue:model forKey:user.openID];
            }
        }
        
        [self.room.rtc queryCurrentPeerListWithType:AIRBRTCPeerTypeApplyingToJoinNow
                                           pageNum:1
                                          pageSize:100000
                                         onSuccess:^(AIRBRoomChannelUserListResponse * _Nonnull rsp) {
            for (AIRBRoomChannelUser* user in rsp.userList) {
                if ([user.openID isEqualToString:self.userID]) {
                    continue;
                } else {
                    AIRBDStudentListItemModel* model = [[AIRBDStudentListItemModel alloc] init];
                    model.userID = user.openID;
                    model.status = AIRBDStudentStatusNowApplying;
                    [self addStudentListItemModel:model index:0];
                    [self.studentsLists setValue:model forKey:user.openID];
                }
            }
            
            dispatch_async(dispatch_get_main_queue(), ^{
                [self.studentsListView reloadData];
            });
            
        } onFailure:^(NSString * _Nonnull errorMessage) {
            
        }];
    } onFailure:^(NSString * _Nonnull errorMessage) {
        ;
    }];
}

#pragma -mark LifeCircle

- (instancetype) init {
    self = [super init];
    if (self) {
        _isChatForbiddened = YES;
        _studentsListDataSource = [[NSMutableArray alloc] init];
//        _studentsLists = [[NSMutableSet alloc] init];
        _hasMoreRoomMembers = YES;
        _isChatForbiddened = NO;
        _isRTCForbiddened = NO;
        _studentsLists = [[NSMutableDictionary alloc] init];
        _studentListLock = [[NSLock alloc] init];
        _currentRoomMemberListPageNum = 1;
        _hasMoreRoomMembers = YES;
        _studentJoinedListEndIndex = 0;
        _studentApplyingListEndIndex = 0;
        
    }
    return self;
}

- (instancetype)initWithFrame:(CGRect)frame
{
    self = [super initWithFrame:frame];
    if (self) {
        _isChatForbiddened = YES;
        _studentsListDataSource = [[NSMutableArray alloc] init];
//        _studentsLists = [[NSMutableSet alloc] init];
        _hasMoreRoomMembers = YES;
        _isChatForbiddened = NO;
        _isRTCForbiddened = NO;
        _studentsLists = [[NSMutableDictionary alloc] init];
        _studentListLock = [[NSLock alloc] init];
        _currentRoomMemberListPageNum = 1;
        _hasMoreRoomMembers = YES;
        _studentJoinedListEndIndex = 0;
        _studentApplyingListEndIndex = 0;
        
    }
    return self;
}

- (void) dealloc {
    [[NSNotificationCenter defaultCenter] removeObserver:self];
    [self leaveRoom];
}

-(void) leaveRoom{
//    [self.room stopLiveStreaming];
    if (self.roomEntered){
        [self.room leaveRoom];
        self.room = nil;
        self.roomEntered = NO;
    }
}

- (void) stop {
    [self leaveRoom];
}

- (void) createRoomWithConfig:(AIRBRoomEngineConfig *)config userID:(NSString *)userID completion:(void (^)(NSString * _Nonnull))onGotRoomID {
    self.userID = userID;
    self.config = config;
    
    NSString* templateId = @"default";
    NSString* title = [NSString stringWithFormat:@"%@的课堂", self.userID];
    NSString* notice = [NSString stringWithFormat:@"%@的课堂公告", self.userID];
    
    NSString* path = [NSString stringWithFormat:@"%@/api/login/createRoom", [AIRBDEnvironments shareInstance].appServerHost];
    NSString* s = [NSString stringWithFormat:@"%@?appId=%@&templateId=%@&title=%@&notice=%@&roomOwnerId=%@", path, self.config.appID, templateId, title, notice, self.userID];
    
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
        @"appId" : self.config.appID,
        @"templateId" : templateId,
        @"title" : title,
        @"notice" : notice,
        @"roomOwnerId" : self.userID
    };
    
    NSString* signedString = [Utility AIRBRequestSignWithSignSecret:[AIRBDEnvironments shareInstance].signSecret method:@"POST" path:path parameters:params headers:headers];
    NSLog(@"signedString:%@", signedString);
    
    s = [NSString stringWithFormat:@"%@?appId=%@&templateId=%@&title=%@&notice=%@&roomOwnerId=%@", path, self.config.appID, templateId, [Utility encodeToPercentEscapeString:title], [Utility encodeToPercentEscapeString:notice], self.userID];
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
            NSLog(@"createRoom data:%@", dic);
            if ([dic isKindOfClass:[NSDictionary class]] && dic.count > 0 && [dic valueForKey:@"result"]) {
                NSDictionary* resultDic = [dic valueForKey:@"result"];
                if ([resultDic isKindOfClass:[NSDictionary class]] && resultDic.count > 0 && [resultDic valueForKey:@"roomId"]) {
                    onGotRoomID([resultDic valueForKey:@"roomId"]);
                }
            }
        } else if (error) {
            ;
        }
    }];
    [task resume];
}

-(void) startWithRoomID:(NSString*)roomID userID:(NSString*)userID {
    self.userID = userID;
    self.roomID = roomID;
    self.room = [[AIRBRoomEngine sharedInstance] getRoomChannelWithRoomID:roomID];
    self.room.delegate = self;
    dispatch_async(dispatch_get_main_queue(), ^{
        [self setupUI];
    });
    [self.room enterRoomWithUserNick:@"nick"];
    [[AIRBDToast shareInstance] makeToast:@"初始化" duration:0.0];
}

- (void) queryMoreRoomMemberInfoOnce {
    if (self.hasMoreRoomMembers) {
        [self.room getRoomUserListWithPageNum:self.currentRoomMemberListPageNum
                                     pageSize:kStudentListRoomMemberPageSize
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
            //请求申请RTC的列表
            [self.room.rtc queryCurrentPeerListWithType:AIRBRTCPeerTypeApplyingToJoinNow pageNum:self.currentRoomMemberListPageNum pageSize:kStudentListRoomMemberPageSize onSuccess:^(AIRBRoomChannelUserListResponse * _Nonnull rsp) {
                for (AIRBRoomChannelUser* user in rsp.userList) {
                    [self updateStudent:user.openID toNewStatus:AIRBDStudentStatusNowApplying];
                }
                dispatch_async(dispatch_get_main_queue(), ^{
                    [self.studentsListView reloadData];
                });
                //请求在RTC的列表
                [self.room.rtc queryCurrentPeerListWithType:AIRBRTCPeerTypeJoinedAlready pageNum:self.currentRoomMemberListPageNum pageSize:kStudentListRoomMemberPageSize onSuccess:^(AIRBRoomChannelUserListResponse * _Nonnull rsp) {
                    for (AIRBRoomChannelUser* user in rsp.userList) {
                        [self updateStudent:user.openID toNewStatus:AIRBDStudentStatusAlreadyOnTheCall];
                    }
                    dispatch_async(dispatch_get_main_queue(), ^{
                        [self.studentsListView reloadData];
                    });
                } onFailure:^(NSString * _Nonnull errorMessage) {
                    dispatch_async(dispatch_get_main_queue(), ^{
                        [[AIRBDToast shareInstance] makeToast:[NSString stringWithFormat:@"获取RTC成员列表失败%@",errorMessage] duration:3.0];
                    });
                }];
            } onFailure:^(NSString * _Nonnull errorMessage) {
//                dispatch_async(dispatch_get_main_queue(), ^{
//                    [[AIRBDToast shareInstance] makeToast:[NSString stringWithFormat:@"获取申请连麦列表失败%@",errorMessage] duration:3.0];
//                });
            }];
        } onFailure:^(NSString * _Nonnull errorMessage) {
            dispatch_async(dispatch_get_main_queue(), ^{
                [[AIRBDToast shareInstance] makeToast:[NSString stringWithFormat:@"获取成员列表失败%@",errorMessage] duration:3.0];
            });
        }];
    }
    
    
    
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

#pragma -mark AIRBRoomChannelDelegate

- (void) onAIRBRoomChannelEvent:(AIRBRoomChannelEvent) event info:(NSDictionary*)info {
    switch (event) {
        case AIRBRoomChannelEventEntered: {
            self.roomEntered = YES;
            self.room.whiteboard.delegate = self;
            self.room.rtc.delegate = self;
            dispatch_async(dispatch_get_main_queue(), ^{
                AIRBWhiteBoardConfig* config = [[AIRBWhiteBoardConfig alloc] init];
                config.whiteboardContentWidth = self.mainContentView.bounds.size.width;
                config.whiteboardContentHeight = self.mainContentView.bounds.size.width * 9.0 / 16.0;
                [self.room.whiteboard openWithConfig:config];
                [self queryMoreRoomMemberInfoOnce];
            });
            
            [self.room.rtc muteLocalCamera:NO onSuccess:^{
                
            } onFailure:^(NSString * _Nonnull errorMessage) {
                
            }]; // 开启本地预览

        }
            break;
        case AIRBRoomChannelEventLeft:
            self.roomEntered = NO;
            break;
        case AIRBRoomChannelEventMessageReceived: {
            NSData* rawData = [[info valueForKey:@"data"] dataUsingEncoding:NSUTF8StringEncoding];
            NSDictionary* dicData = [NSJSONSerialization JSONObjectWithData:rawData
                                                                    options:NSJSONReadingMutableContainers
                                                                      error:nil];
            NSInteger messageType = [[info valueForKey:@"type"] integerValue];
            NSString* dataString = @"";
            NSData *turnData = [[info valueForKey:@"data"] dataUsingEncoding:NSUTF8StringEncoding];
            NSDictionary *dataDic = [NSJSONSerialization JSONObjectWithData:turnData options:NSJSONReadingMutableLeaves error:nil];
            switch (messageType) {
                case AIRBRoomChannelMessageTypeRoomMembersInOut:{
                    /*
                     {
                     "enter":true,
                     "nick":"5556",
                     "onlineCount":1,
                     "userId":"5556",
                     "uv":1
                     }
                     */
                    BOOL enter = [[dicData valueForKey:@"enter"] boolValue];
                    NSString* userID = [dicData valueForKey:@"userId"];
                    if (userID.length > 0 && ![userID isEqualToString:self.userID]) {
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
                case AIRBRoomChannelMessageTypePeerJoinRTCSucceeded: {  // 某人加入了RTC
                    /*
                     {
                     "confId":"AliRtcxxxxxxxxxxx",
                     "type":1,
                     "userList":[
                     {
                     "cameraStatus":0,
                     "micphoneStatus":0,
                     "nickname":"",
                     "sourceId":"156164",
                     "status":3,
                     "userId":"156164"
                     }
                     ],
                     "version":4663202654767825955
                     }
                     */
                    NSArray* userList = [dicData valueForKey:@"userList"];
                    for (NSDictionary* userInfo in userList) {
                        NSString* userID = [userInfo valueForKey:@"userId"];
                        [self updateStudent:userID toNewStatus:AIRBDStudentStatusAlreadyOnTheCall];
                    }
                    dispatch_async(dispatch_get_main_queue(), ^{
                        [self.studentsListView reloadData];
                    });
                }
                    break;
                case AIRBRoomChannelMessageTypePeerJoinRTCFailed:{   // 某人加入RTC超时或者拒绝加入RTC
                    NSArray* userList = [dicData valueForKey:@"userList"];
                    if ([userList isKindOfClass:[NSArray class]] && userList.count > 0) {
                        for (NSDictionary* userInfo in userList) {
                            [self updateStudent:[userInfo valueForKey:@"userId"] toNewStatus:AIRBDStudentStatusReadyForCalled];
                            
                            dispatch_async(dispatch_get_main_queue(), ^{
                                [[AIRBDToast shareInstance] makeToast:[NSString stringWithFormat:@"%@拒绝了连麦邀请", [userInfo valueForKey:@"userId"]] duration:3.0];
                            });
                        }
                    }
                    dispatch_async(dispatch_get_main_queue(), ^{
                        [self.studentsListView reloadData];
                    });
                }
                    break;
                case AIRBRoomChannelMessageTypePeerLeaveRTC: {  // 某人离开RTC
                    NSArray* userList = [dicData valueForKey:@"userList"];
                    if ([userList isKindOfClass:[NSArray class]] && userList.count > 0) {
                        for (NSDictionary* userInfo in userList) {
                            [self updateStudent:[userInfo valueForKey:@"userId"] toNewStatus:AIRBDStudentStatusReadyForCalled];
                            [self.videoContentView removeSideRTCViewOfUserID:[userInfo valueForKey:@"userId"]];
                        }
                    }
                    dispatch_async(dispatch_get_main_queue(), ^{
                        [self.studentsListView reloadData];
                        
                    });
                }
                    break;
                case AIRBRoomChannelMessageTypePeerKickedFromRTC: { // 某人被踢出了RTC
                    /*
                     {
                     "confId":"AliRtcbed04681daba472995",
                     "type":4,
                     "userList":[
                     {
                     "cameraStatus":0,
                     "deviceId":"",
                     "duration":0,
                     "micphoneStatus":0,
                     "nickname":"",
                     "source":0,
                     "sourceId":"345",
                     "status":5,
                     "tenantId":"",
                     "userId":"345"
                     }
                     ],
                     "version":1576074671583281939
                     }
                     */
                    
                    NSArray* userList = [dicData valueForKey:@"userList"];
                    if ([userList isKindOfClass:[NSArray class]] && userList.count > 0) {
                        for (NSDictionary* userInfo in userList) {
                            [self updateStudent:[userInfo valueForKey:@"userId"] toNewStatus:AIRBDStudentStatusReadyForCalled];
                            [self.videoContentView removeSideRTCViewOfUserID:[userInfo valueForKey:@"userId"]];
                        }
                    }
                    dispatch_async(dispatch_get_main_queue(), ^{
                        [self.studentsListView reloadData];
                    });
                }
                    break;
                case AIRBRoomChannelMessageTypeRTCStarted:
                    break;
                case AIRBRoomChannelMessageTypeRTCStopped:
                    break;
                case AIRBRoomChannelMessageTypeOnRTCCalled:
                    break;
                case AIRBRoomChannelMessageTypeOnReceiveRTCJoiningApplication: {
                    /*
                     {
                     "applyUser":{
                     "userId":"678"
                     },
                     "confId":"AliRtc409e60efcf564782ab",
                     "isApply":false,
                     "type":8,
                     "version":9045665597740303614
                     }
                     */
                    NSDictionary* applyingUser = [dicData valueForKey:@"applyUser"];
                    if ([applyingUser isKindOfClass:[NSDictionary class]] && applyingUser.count > 0) {
                        NSString* userID = [applyingUser valueForKey:@"userId"];
                        BOOL applyingOrCancel = [[dicData valueForKey:@"isApply"] boolValue];
                        if (applyingOrCancel) {
                            [self updateStudent:userID toNewStatus:AIRBDStudentStatusNowApplying];
                            dispatch_async(dispatch_get_main_queue(), ^{
                                [[AIRBDToast shareInstance] makeToast:[NSString stringWithFormat:@"%@申请连麦", userID] duration:3.0];
                            });
                        } else {
                            [self updateStudent:userID toNewStatus:AIRBDStudentStatusReadyForCalled];
                        }
                    }
                    dispatch_async(dispatch_get_main_queue(), ^{
                        [self.studentsListView reloadData];
                    });
                }
                    break;
                    
                case AIRBRoomChannelMessageTypeOnReceiveRTCJoiningApplicationRespond:{
                    NSString *userIDApproved = [dataDic valueForKey:@"uid"];
                    BOOL approve = [[dataDic valueForKey:@"approve"] boolValue];
                    if (!approve){
                        [self updateStudent:userIDApproved toNewStatus:AIRBDStudentStatusReadyForCalled];
                        dispatch_async(dispatch_get_main_queue(), ^{
                            [self.studentsListView reloadData];
                            
                        });
                    }
                }
                    break;
                    
                case AIRBRoomChannelMessageTypeChatCommentReceived:{
                    dataString = [NSString stringWithFormat:@"%@:%@",[dataDic valueForKey:@"creatorNick"],[dataDic valueForKey:@"content"]];
                    [_commentsListView insertNewComment:dataString];
                }
                    break;
                default:
                    return;
            }
        }
            break;
        default:
            break;
    }
}

- (void) onAIRBRTCErrorWithCode:(AIRBErrorCode)code message:(NSString*)message{
    dispatch_async(dispatch_get_main_queue(), ^{
        [[AIRBDToast shareInstance] makeToast:[NSString stringWithFormat:@"onAIRBRTCErrorWithCode:(0x%lx, %@)", (long)code, message] duration:3.0];
    });
}

- (void) onAIRBRTCEvent:(AIRBRTCEvent)event info:(NSDictionary*)info {
    switch (event) {
        case AIRBRTCEventLocalPreviewStarted: {
            dispatch_async(dispatch_get_main_queue(), ^{
                self.room.rtc.rtcLocalView.frame = self.localCameraHolder.bounds;
                [self.localCameraHolder addSubview:self.room.rtc.rtcLocalView];

//                [self.videoContentView addCenterRTCView:self.room.rtcLocalView];

            });
        }
            break;
            
        case AIRBRTCEventJoinSucceeded: {
            [self.room.rtc startPublishingBypassLive];
            break;
        }
        case AIRBRTCEventLeaveSucceeded: {
            dispatch_async(dispatch_get_main_queue(), ^{
                if(![self.startClassButton.titleLabel.text isEqualToString:@"开播错误"]){
                    [self.startClassButton setTitle:@"已结束" forState:UIControlStateNormal];
                    self.startClassButton.backgroundColor = [UIColor grayColor];
                    self.startClassButton.enabled = NO;
                }
            });
            break;
        }
        case AIRBRTCEventBypassLiveStarted: {
            dispatch_async(dispatch_get_main_queue(), ^{
                [self.startClassButton setTitle:@"结束上课" forState:UIControlStateNormal];
                self.startClassButton.backgroundColor = [UIColor redColor];
                self.startClassButton.enabled = YES;
            });
        }
            break;
        case AIRBRTCEventNotification:{
            dispatch_async(dispatch_get_main_queue(), ^{
                [[AIRBDToast shareInstance] makeToast:[info valueForKey:@"data"] duration:3.0];
            });
        }
            break;
        default:
            break;
    }
}

- (void) onAIRBWhiteBoardEvent:(AIRBWhiteBoardEvent)event info:(NSDictionary*)info {
    switch (event) {
        case AIRBWhiteBoardEventOpened: {
            dispatch_async(dispatch_get_main_queue(), ^{
                self.room.whiteboard.whiteboardView.frame = CGRectMake(0, (self.whiteBoardContentView.bounds.size.height - self.room.whiteboard.whiteboardView.bounds.size.height) / 2, self.room.whiteboard.whiteboardView.bounds.size.width, self.room.whiteboard.whiteboardView.bounds.size.height);
                [self.whiteBoardContentView addSubview:self.room.whiteboard.whiteboardView];
                [self.mainContentView sendSubviewToBack:self.whiteBoardContentView];
//                self.recordButton.enabled = YES;
//                self.recordPauseButton.enabled = YES;
            });
        }
            
            break;
        case AIRBWhiteBoardEventDestroied:
            break;
        default:
            break;
    }
}

- (void) onAIRBRoomChannelErrorWithCode:(AIRBErrorCode)code message:(NSString*)message {
    dispatch_async(dispatch_get_main_queue(), ^{
        [[AIRBDToast shareInstance] makeToast:[NSString stringWithFormat:@"onAIRBRoomChannelErrorWithCode:(0x%lx, %@)", (long)code, message] duration:3.0];
    });
    
    if(code == AIRBRTCFailedToPublishBypassLive){
        dispatch_async(dispatch_get_main_queue(), ^{
            [self.startClassButton setTitle:@"开播错误" forState:UIControlStateNormal];
        });
    }
    
}

- (void) requestWhiteBoardAccessTokenWithDocKey:(NSString*)docKey completion:(void (^)(AIRBWhiteBoardToken* token))onGotToken {
    
    NSString* path = [NSString stringWithFormat:@"%@/whiteboard/open", [AIRBDEnvironments shareInstance].appServerHost];
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
                        tokenInfo.permission = [[documentAccessInfo valueForKey:@"permission"] charValue];
                        onGotToken(tokenInfo);
                    }
                }
            }
        }
    }];
    [task resume];
}

- (void) onAIRBRTCRemotePeerViewAvailable:(NSString *)userID view:(UIView *)view type:(AIRBRTCVideoViewType)type{
    if (type == AIRBRTCVideoViewTypeCamera){
        [self.room.rtc subscribeRemoteVideoStream:YES type:AIRBRTCVideoStreamTypeHigh fromUser:userID]; // 主动订阅视频流，如果取消订阅，视频流会停止接收
        dispatch_async(dispatch_get_main_queue(), ^{
            [self.videoContentView addSideRTCView:view withUserID:userID];
        });
    }
}

#pragma -mark AIRBDStudentListCellDelegate

- (void) onStudentActionButtonClickedWithUserID:(NSString *)userID action:(AIRBDStudentItemAction)action {
    switch (action) {
        case AIRBDStudentItemActionCall:
            [self.room.rtc addPeers:@[userID]];
            break;
        case AIRBDStudentItemActionHangup:
            [self.room.rtc removePeers:@[userID]];
            break;
        case AIRBDStudentItemActionCancelCall:
            break;
        case AIRBDStudentItemActionAccept:
            [self.room.rtc approveJoiningApplication:YES fromPeer:userID];
            break;
        case AIRBDStudentItemActionReject:
            [self.room.rtc approveJoiningApplication:NO fromPeer:userID];
            break;
        default:
            break;
    }
}

#pragma -mark UITextFieldDelegate
- (BOOL)textFieldShouldReturn:(UITextField *)textField {
    if([textField isEqual:self.messageInputField]){
        if (textField.text.length > 0) {
            [self.room.chat sendComment:self.messageInputField.text onSuccess:^{
                dispatch_async(dispatch_get_main_queue(), ^{
                    [[AIRBDToast shareInstance] makeToast:@"发送成功" duration:1.0];
                });;
            } onFailure:^(NSString * _Nonnull errorMessage) {
                dispatch_async(dispatch_get_main_queue(), ^{
                    [[AIRBDToast shareInstance] makeToast:@"发送失败" duration:1.0];
                });;
            }];
        }
    }
    textField.text = nil;
    [textField resignFirstResponder];
    return YES;
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
        __weak typeof(self) weakSelf = self;
        void (^animation)(void) = ^void(void) {
            self.messageInputField.transform = CGAffineTransformMakeTranslation(0, -(keyBoardHeight));
            self.messageInputField.backgroundColor = [UIColor whiteColor];
            [self.messageInputField mas_remakeConstraints:^(MASConstraintMaker *make) {
                if (@available(iOS 11.0, *)) {
                    make.left.equalTo(weakSelf.mas_safeAreaLayoutGuideLeft);
                } else {
                    make.left.equalTo(weakSelf.mas_left);
                }
                make.right.equalTo(weakSelf.sendHolderView);
            }];
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
    __weak typeof(self) weakSelf = self;
    void (^animation)(void) = ^void(void) {
        self.messageInputField.transform = CGAffineTransformIdentity;
        self.messageInputField.backgroundColor = [UIColor clearColor];
        [self.messageInputField mas_remakeConstraints:^(MASConstraintMaker *make) {
            make.bottom.equalTo(weakSelf.sendHolderView).with.offset(-2);
            make.left.equalTo(weakSelf.sendHolderView).with.offset(2);
            make.top.equalTo(weakSelf.sendHolderView).with.offset(2);
            make.width.equalTo(weakSelf.sendHolderView).multipliedBy(0.7);
        }];
    };
    
    if (animationTime > 0) {
        [UIView animateWithDuration:animationTime animations:animation];
    } else {
        animation();
    }
}

#pragma -mark UIButton Selector

- (void)interactiveWhiteBoardButtonAction:(UIButton*)sender {
    if(self.whiteBoardContentView.hidden == YES){
        sender.backgroundColor = [UIColor lightGrayColor];
        self.interactiveVideoButton.backgroundColor = [UIColor clearColor];
        __weak typeof(self) weakSelf = self;
        [self.room.rtc.rtcLocalView removeFromSuperview];
        
        for (UIView* subview in self.localCameraHolder.subviews) {
            if([subview isKindOfClass:[UIImageView class]]){
                [subview removeFromSuperview];
            }
        }
        [self.localCameraHolder addSubview:self.room.rtc.rtcLocalView];
        [self.room.rtc.rtcLocalView mas_remakeConstraints:^(MASConstraintMaker *make) {
            make.edges.equalTo(weakSelf.localCameraHolder).with.insets(UIEdgeInsetsMake(0, 1, 0, 1));
        }];
    }
    [self.whiteBoardContentView setHidden:NO];
    [self.videoContentView setHidden:YES];
}

- (void)interactiveVideoButtonAction:(UIButton*)sender {
    if(self.whiteBoardContentView.hidden == NO){
        sender.backgroundColor = [UIColor lightGrayColor];
        self.interactiveWhiteBoardButton.backgroundColor = [UIColor clearColor];
        __weak typeof(self) weakSelf = self;
        UIImageView* whiteBoardSnap = [[UIImageView alloc]init];
        [self.localCameraHolder addSubview:whiteBoardSnap];
        [whiteBoardSnap mas_remakeConstraints:^(MASConstraintMaker *make) {
            make.edges.equalTo(weakSelf.localCameraHolder).with.insets(UIEdgeInsetsMake(0, 1, 0, 1));
        }];
        UIGraphicsBeginImageContext(self.localCameraHolder.frame.size);
        [[self snapshot:self.whiteBoardContentView] drawInRect:CGRectMake(0, 0, self.localCameraHolder.frame.size.width, self.localCameraHolder.frame.size.height)];
        UIImage *newImage = UIGraphicsGetImageFromCurrentImageContext();
        UIGraphicsEndImageContext();
        [whiteBoardSnap setImage:newImage];
        [self.room.rtc.rtcLocalView removeFromSuperview];
        [self.videoContentView addCenterRTCView:self.room.rtc.rtcLocalView];
    }
    
    
    [self.whiteBoardContentView setHidden:YES];
    [self.videoContentView setHidden:NO];
}

- (void)setButtonAction:(UIButton*)sender {
    
}

- (void)startClassButtonAction:(UIButton*)sender {
    if ([sender.currentTitle isEqualToString:@"开始上课"]) {
        AIRBRTCConfig* config = [[AIRBRTCConfig alloc] init];
        [self.room.rtc joinChannelWithConfig:config];
        sender.enabled = NO;
        sender.backgroundColor = [UIColor grayColor];
        [sender setTitle:@"正在启动" forState:UIControlStateNormal];
    } else if ([sender.currentTitle isEqualToString:@"结束上课"]) {
        [self.room.rtc leaveChannel];
        sender.backgroundColor = [UIColor grayColor];
        [sender setTitle:@"正在结束" forState:UIControlStateNormal];
    }
}

- (void)uploadFileButtonAction:(UIButton*)sender {
    
}

- (void)recordButtonAction:(UIButton*)sender {
}

- (void)recordPauseButtonAction:(UIButton*)sender {
}

- (void)replayButtonAction:(UIButton*)sender {
    
}

- (void)sendButtonAction:(UIButton*)sender {
    if (self.messageInputField.text.length > 0) {
        [self.room.chat sendComment:self.messageInputField.text onSuccess:^{
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
}

- (void)studentListTabAction:(UIButton*)sender {
    sender.backgroundColor = [UIColor whiteColor];
    self.commentsListTab.backgroundColor = [UIColor lightGrayColor];
    [self.commentsListView setHidden:YES];
    [self.studentsListView setHidden:NO];
    [self.chatForbiddenedLabel setHidden:YES];
    [self.chatForbiddenedPoint setHidden:YES];
    [self.RTCForbiddenedLabel setHidden:NO];
    [self.RTCForbiddenedPoint setHidden:NO];
    
}

- (void)commentsListTabAction:(UIButton*)sender {
    sender.backgroundColor = [UIColor whiteColor];
    self.studentListTab.backgroundColor = [UIColor lightGrayColor];
    [self.studentsListView setHidden:YES];
    [self.commentsListView setHidden:NO];
    [self.chatForbiddenedLabel setHidden:NO];
    [self.chatForbiddenedPoint setHidden:NO];
    [self.RTCForbiddenedLabel setHidden:YES];
    [self.RTCForbiddenedPoint setHidden:YES];
}

- (void)chatForbiddenedButtonAction:(UIButton*)sender {
    self.isChatForbiddened = !self.isChatForbiddened;
    if (!self.isChatForbiddened) {
        self.chatForbiddenedPoint.backgroundColor = [UIColor lightGrayColor];
    } else {
        self.chatForbiddenedPoint.backgroundColor = [UIColor clearColor];
    }
}

- (void)RTCForbiddenedButtonAction:(UIButton*)sender {
    self.isRTCForbiddened = !self.isRTCForbiddened;
    if (!self.isRTCForbiddened) {
        self.RTCForbiddenedPoint.backgroundColor = [UIColor lightGrayColor];
    } else {
        self.RTCForbiddenedPoint.backgroundColor = [UIColor clearColor];
    }
}

- (void)exchangeContentButtonAction:(UIButton*)sender {
    ;
}

- (void)volumeSliderAction:(UISlider*)sender{
    
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
    return 34;
}

#pragma -mark SupportMethods

- (UIImage *)snapshot:(UIView *)view {
    UIGraphicsBeginImageContextWithOptions(view.bounds.size,YES,0);
    [view drawViewHierarchyInRect:view.bounds afterScreenUpdates:YES];
    UIImage *image = UIGraphicsGetImageFromCurrentImageContext();
    UIGraphicsEndImageContext();
    return image;
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
