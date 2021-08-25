//
//  AIRBDBigClassViewController.m
//  AliInteractiveRoomDemo
//
//  Created by fernando on 2021/5/21.
//

#import "AIRBDBigClassViewController.h"
#import <AliInteractiveRoomBundle/AliInteractiveRoomBundle.h>
#import <Masonry/Masonry.h>

#import "AIRBDStudentViewController.h"
#import "AIRBDStudentView.h"
#import "AIRBDTeacherViewController.h"
#import "AIRBDTeacherView.h"
#import "Utilities/Utility.h"
#import "AIRBDToast.h"

static NSString * const DMClassTableCellIdentifier = @"DMClassTableCellIdentifier";

@interface AIRBDBigClassViewController () <UITableViewDataSource, UITableViewDelegate,UITextFieldDelegate,UIGestureRecognizerDelegate>


@property (copy, nonatomic) NSString* roomID;
@property (copy, nonatomic) NSString* roomOwnerID;
@property (copy, nonatomic) NSString* userNick;
@property (assign, nonatomic) BOOL isTeacher;
@property (strong, nonatomic) AIRBDTeacherView* teacherView;
@property (strong, nonatomic) AIRBDStudentView* studentView;


@property (strong, nonatomic) UILabel* classTitle;
@property (strong, nonatomic) UITextField* classNumberInput;
@property (strong, nonatomic) UITextField* userNickInput;
//@property (strong, nonatomic) UITableView *classTypeTableView;
@property (strong, nonatomic) UIButton* smallClassButton;
@property (strong, nonatomic) UIButton* bigClassButton;
@property (strong, nonatomic) UIButton* teacherSelectedLabel;
@property (strong, nonatomic) UIButton* studentSelectedLabel;
@property (strong, nonatomic) UIButton* teacherSelectedPoint;
@property (strong, nonatomic) UIButton* studentSelectedPoint;
@property (strong, nonatomic) UIButton* enterClassRoomButton;
@property (strong, nonatomic) NSArray<AIRBRoomBasicInfo *> * classRoomList;
@property (strong, nonatomic) UIButton* classRoomListButton;
@property (strong, nonatomic) UITableView * classRoomListTableView;

@end

@implementation AIRBDBigClassViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    // Do any additional setup after loading the view.
    if ([[UIDevice currentDevice].systemVersion floatValue] >= 7.0) {//侧滑退出手势
        if ([self.navigationController respondsToSelector:@selector(interactivePopGestureRecognizer)]) {
            self.navigationController.interactivePopGestureRecognizer.enabled = YES;
            self.navigationController.interactivePopGestureRecognizer.delegate = self;
        }
    }
    [self setupUI];
    [self.view bringSubviewToFront:_teacherView];
    
//    [self.view addSubview:self.loginView];
//    [self.view bringSubviewToFront:self.loginView];
}

- (instancetype) init {
    self = [super init];
    if (self) {
        _isTeacher = YES;
        
        [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(keyBoardWillShow:) name:UIKeyboardWillShowNotification object:nil];
        [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(keyBoardWillHide:) name:UIKeyboardWillHideNotification object:nil];
    }
    return self;
}

- (void)dealloc {
    [[NSNotificationCenter defaultCenter] removeObserver:self];
}

- (void) setupUI {
    self.edgesForExtendedLayout = UIRectEdgeAll;
    self.automaticallyAdjustsScrollViewInsets = YES;
    self.extendedLayoutIncludesOpaqueBars = YES;
    
    self.view.backgroundColor = [UIColor blackColor];
    
    __weak typeof(self) weakSelf = self;
    
    self.classTitle = [[UILabel alloc] init];
    self.classTitle.text = @"在线课堂";
    self.classTitle.textAlignment = NSTextAlignmentCenter;
    self.classTitle.textColor = [UIColor whiteColor];
    [self.classTitle setFont:[UIFont systemFontOfSize:35]];
    [self.view addSubview:self.classTitle];
    [self.classTitle mas_makeConstraints:^(MASConstraintMaker *make) {
        make.top.equalTo(weakSelf.view).with.offset(60);
        make.left.equalTo(weakSelf.view);
        make.right.equalTo(weakSelf.view);
        make.height.mas_equalTo(100);
    }];
    
    self.classNumberInput = [[UITextField alloc] init];
    self.classNumberInput.placeholder = @"输入教室号，空时自动创建";
//    self.classNumberInput.textColor = [UIColor blackColor];
    self.classNumberInput.alpha = 0.8;
//    self.classNumberInput.backgroundColor = [UIColor grayColor];
    self.classNumberInput.textAlignment = NSTextAlignmentLeft;
    self.classNumberInput.keyboardType = UIKeyboardTypeDefault;
    self.classNumberInput.returnKeyType = UIReturnKeySend;
    self.classNumberInput.keyboardAppearance = UIKeyboardAppearanceDefault;
    self.classNumberInput.delegate = self;
    self.classNumberInput.borderStyle = UITextBorderStyleRoundedRect;
//    self.classNumberInput.text = @"a967c72e-f350-4561-8342-8674ff21c593";
    [self.view addSubview:self.classNumberInput];
    [self.classNumberInput mas_makeConstraints:^(MASConstraintMaker *make) {
        make.top.equalTo(weakSelf.classTitle.mas_bottom).with.offset(20);
        make.left.equalTo(weakSelf.view).with.offset(50);
        make.right.equalTo(weakSelf.view).with.offset(-120);
        make.height.mas_equalTo(30);
    }];
    
    self.classRoomListButton = [[UIButton alloc] init];
    [self.classRoomListButton addTarget:self action:@selector(classRoomListButtonAction:) forControlEvents:UIControlEventTouchUpInside];
    self.classRoomListButton.backgroundColor = [UIColor grayColor];
    [self.classRoomListButton setTitle:@"列表" forState:UIControlStateNormal];
    [self.view addSubview:self.classRoomListButton];
    [self.classRoomListButton mas_makeConstraints:^(MASConstraintMaker *make) {
        make.top.equalTo(weakSelf.classNumberInput.mas_top);
        make.left.equalTo(weakSelf.classNumberInput.mas_right).with.offset(10);
        make.right.equalTo(weakSelf.view).with.offset(-50);
        make.height.mas_equalTo(30);
    }];
    
    self.userNickInput = [[UITextField alloc] init];
    self.userNickInput.placeholder = @"输入用户名";
//    self.userNickInput.textColor = [UIColor blackColor];
    self.userNickInput.alpha = 0.8;
//    self.userNickInput.backgroundColor = [UIColor grayColor];
    self.userNickInput.textAlignment = NSTextAlignmentLeft;
    self.userNickInput.keyboardType = UIKeyboardTypeDefault;
    self.userNickInput.returnKeyType = UIReturnKeySend;
    self.userNickInput.keyboardAppearance = UIKeyboardAppearanceDefault;
    self.userNickInput.delegate = self;
    self.userNickInput.borderStyle = UITextBorderStyleRoundedRect;
    [self.view addSubview:self.userNickInput];
    [self.userNickInput mas_makeConstraints:^(MASConstraintMaker *make) {
        make.top.equalTo(weakSelf.classNumberInput.mas_bottom).with.offset(20);
        make.left.equalTo(weakSelf.view).with.offset(50);
        make.right.equalTo(weakSelf.view).with.offset(-50);
        make.height.mas_equalTo(30);
    }];
    
    UIButton* bigClassButton = [[UIButton alloc] init];
    [bigClassButton addTarget:self action:@selector(bigClassButtonAction:) forControlEvents:UIControlEventTouchUpInside];
    bigClassButton.backgroundColor = [UIColor lightGrayColor];
    bigClassButton.layer.borderColor = [[UIColor lightGrayColor] CGColor];
    bigClassButton.layer.borderWidth = 1.0;
    bigClassButton.layer.cornerRadius = 3.0;
    [bigClassButton setTitle:@"大班课" forState:UIControlStateNormal];
    [bigClassButton setTitleColor:[UIColor whiteColor] forState:UIControlStateNormal];
    [self.view addSubview:bigClassButton];
    [bigClassButton mas_makeConstraints:^(MASConstraintMaker *make) {
        make.left.equalTo(weakSelf.view).with.offset(50);
        make.top.equalTo(weakSelf.userNickInput.mas_bottom).with.offset(20);
        make.height.mas_equalTo(30);
        make.width.mas_equalTo((weakSelf.view.bounds.size.width - 100) / 2);
    }];
    self.bigClassButton = bigClassButton;
    
    UIButton* smallClassButton = [[UIButton alloc] init];
    [smallClassButton addTarget:self action:@selector(smallClassButtonAction:) forControlEvents:UIControlEventTouchUpInside];
    smallClassButton.backgroundColor = [UIColor clearColor];
    smallClassButton.layer.borderColor = [[UIColor lightGrayColor] CGColor];
    smallClassButton.layer.borderWidth = 1.0;
    smallClassButton.layer.cornerRadius = 3.0;
    [smallClassButton setTitle:@"小班课" forState:UIControlStateNormal];
    [smallClassButton setTitleColor:[UIColor whiteColor] forState:UIControlStateNormal];
    [self.view addSubview:smallClassButton];
    [smallClassButton mas_makeConstraints:^(MASConstraintMaker *make) {
        make.right.equalTo(weakSelf.view).with.offset(-50);
        make.top.equalTo(weakSelf.userNickInput.mas_bottom).with.offset(20);
        make.height.mas_equalTo(30);
        make.width.mas_equalTo((weakSelf.view.bounds.size.width - 100) / 2);
    }];
    self.smallClassButton = smallClassButton;
    
    
    self.teacherSelectedPoint = [[UIButton alloc] init];
    [self.teacherSelectedPoint addTarget:self action:@selector(teacherButtonAction:) forControlEvents:UIControlEventTouchUpInside];
    self.teacherSelectedPoint.backgroundColor = [UIColor lightGrayColor];
    self.teacherSelectedPoint.layer.borderColor = [UIColor whiteColor].CGColor;
    self.teacherSelectedPoint.layer.borderWidth = 1.0;
    self.teacherSelectedPoint.layer.cornerRadius = 5;
    [self.view addSubview:self.teacherSelectedPoint];
    [self.teacherSelectedPoint mas_makeConstraints:^(MASConstraintMaker *make) {
        make.top.equalTo(weakSelf.smallClassButton.mas_bottom).with.offset(30);
        make.left.equalTo(weakSelf.view).with.offset(70);
        make.size.mas_equalTo(CGSizeMake(10, 10));
    }];
    
    self.teacherSelectedLabel = [[UIButton alloc] init];
    [self.teacherSelectedLabel addTarget:self action:@selector(teacherButtonAction:) forControlEvents:UIControlEventTouchUpInside];
    [self.teacherSelectedLabel setTitle:@"我是老师" forState:UIControlStateNormal];
    [self.teacherSelectedLabel setTitleColor:[UIColor whiteColor] forState:UIControlStateNormal];
    self.teacherSelectedLabel.backgroundColor = [UIColor clearColor];
    [self.view addSubview:self.teacherSelectedLabel];
    [self.teacherSelectedLabel mas_makeConstraints:^(MASConstraintMaker *make) {
        make.centerY.equalTo(weakSelf.teacherSelectedPoint);
        make.left.equalTo(weakSelf.teacherSelectedPoint.mas_right).with.offset(5);
        make.size.mas_equalTo(CGSizeMake(80, 50));
    }];
    
    self.studentSelectedLabel = [[UIButton alloc] init];
    [self.studentSelectedLabel addTarget:self action:@selector(studentButtonAction:) forControlEvents:UIControlEventTouchUpInside];
    [self.studentSelectedLabel setTitle:@"我是学生" forState:UIControlStateNormal];
    [self.studentSelectedLabel setTitleColor:[UIColor whiteColor] forState:UIControlStateNormal];
    self.studentSelectedLabel.backgroundColor = [UIColor clearColor];
    [self.view addSubview:self.studentSelectedLabel];
    [self.studentSelectedLabel mas_makeConstraints:^(MASConstraintMaker *make) {
        make.centerY.equalTo(weakSelf.teacherSelectedPoint);
        make.right.equalTo(weakSelf.view).with.offset(-70);
        make.size.mas_equalTo(CGSizeMake(80, 50));
    }];
    
    self.studentSelectedPoint = [[UIButton alloc] init];
    [self.studentSelectedPoint addTarget:self action:@selector(studentButtonAction:) forControlEvents:UIControlEventTouchUpInside];
    self.studentSelectedPoint.backgroundColor = [UIColor clearColor];
    self.studentSelectedPoint.layer.borderColor = [UIColor whiteColor].CGColor;
    self.studentSelectedPoint.layer.borderWidth = 1.0;
    self.studentSelectedPoint.layer.cornerRadius = 5;
    [self.view addSubview:self.studentSelectedPoint];
    [self.studentSelectedPoint mas_makeConstraints:^(MASConstraintMaker *make) {
        make.centerY.equalTo(weakSelf.teacherSelectedPoint);
        make.right.equalTo(weakSelf.studentSelectedLabel.mas_left).with.offset(-5);
        make.size.mas_equalTo(CGSizeMake(10, 10));
    }];
    
    self.enterClassRoomButton = [[UIButton alloc] init];
    [self.enterClassRoomButton addTarget:self action:@selector(enterClassRoomButtonAction:) forControlEvents:UIControlEventTouchUpInside];
    self.enterClassRoomButton.backgroundColor = [UIColor blueColor];
    [self.enterClassRoomButton setTitle:@"进入教室" forState:UIControlStateNormal];
    [self.view addSubview:self.enterClassRoomButton];
    [self.enterClassRoomButton mas_makeConstraints:^(MASConstraintMaker *make) {
        make.top.equalTo(weakSelf.studentSelectedPoint.mas_bottom).with.offset(40);
        make.left.equalTo(weakSelf.view).with.offset(50);
        make.right.equalTo(weakSelf.view).with.offset(-50);
        make.height.mas_equalTo(30);
    }];
    
    self.classRoomListTableView = [[UITableView alloc] init];
    self.classRoomListTableView.delegate = self;
    self.classRoomListTableView.dataSource = self;
    self.classRoomListTableView.hidden = YES;
    [self.view addSubview:self.classRoomListTableView];
    [self.classRoomListTableView mas_makeConstraints:^(MASConstraintMaker *make) {
        make.top.equalTo(weakSelf.classNumberInput.mas_top);
        make.left.equalTo(weakSelf.classNumberInput.mas_left);
        make.right.equalTo(weakSelf.classNumberInput.mas_right);
        make.height.mas_equalTo(300);
    }];
    
    // 请求房间列表
    if (!self.classRoomList){
        [[AIRBRoomEngine sharedInstance] getRoomListWithPageNum:1 pageSize:20 onSuccess:^(AIRBRoomEngineRoomListResponse * _Nonnull response) {
            self.classRoomList = response.roomBasicInfoList;
        } onFailure:^(NSString * _Nonnull errorMessage) {
            [[AIRBDToast shareInstance]makeToast:[NSString stringWithFormat:@"获取房间列表失败,%@",errorMessage] duration:3.0];
        }];
    }
}

#pragma -mark UIButton Selector
- (void)smallClassButtonAction:(UIButton*)sender {
//    sender.backgroundColor = [UIColor lightGrayColor];
//    self.bigClassButton.backgroundColor = [UIColor clearColor];
}

- (void)bigClassButtonAction:(UIButton*)sender {
    sender.backgroundColor = [UIColor lightGrayColor];
    self.smallClassButton.backgroundColor = [UIColor clearColor];
}

- (void)teacherButtonAction:(UIButton*)sender {
    self.isTeacher = YES;
    self.teacherSelectedPoint.backgroundColor = [UIColor lightGrayColor];
    self.studentSelectedPoint.backgroundColor = [UIColor clearColor];
}

- (void)studentButtonAction:(UIButton*)sender {
    self.isTeacher = NO;
    self.studentSelectedPoint.backgroundColor = [UIColor lightGrayColor];
    self.teacherSelectedPoint.backgroundColor = [UIColor clearColor];
}

- (void)classRoomListButtonAction:(UIButton*)sender{
    if (self.classRoomListTableView.isHidden){
        dispatch_async(dispatch_get_main_queue(), ^{
            [self.classRoomListTableView reloadData];
            self.classRoomListTableView.hidden = NO;
        });
    } else{
        dispatch_async(dispatch_get_main_queue(), ^{
            self.classRoomListTableView.hidden = YES;
        });
    }
}

- (void)enterClassRoomButtonAction:(UIButton*)sender {
    if (self.classNumberInput.text.length > 0 && self.isTeacher && ![self.userID isEqualToString:self.roomOwnerID]){
        dispatch_async(dispatch_get_main_queue(), ^{
            UIAlertController *alertController = [UIAlertController alertControllerWithTitle:@"您不是该教室的老师" message:@"" preferredStyle:UIAlertControllerStyleAlert];
            [alertController addAction:[UIAlertAction actionWithTitle:@"确定" style:UIAlertActionStyleCancel handler:^(UIAlertAction * _Nonnull action) {
                ;
            }]];
            [self presentViewController:alertController animated:YES completion:nil];
        });
        return;
    }
    
    if (self.userNickInput.text.length > 0){
        self.userNick = self.userNickInput.text;
    }
    
    [self.classNumberInput resignFirstResponder];
    [self.userNickInput resignFirstResponder];

    if (self.isTeacher) {
        if (self.classNumberInput.text.length > 0) {
            self.roomID = self.classNumberInput.text;
            self.teacherView = [[AIRBDTeacherView alloc] initWithFrame:CGRectMake(0, 0, self.view.bounds.size.height, self.view.bounds.size.width)];
            [self.teacherView startWithRoomID:self.roomID userID:self.userID];
            AIRBDTeacherViewController* teacherViewController = [[AIRBDTeacherViewController alloc]init];
            teacherViewController.view = self.teacherView;
            [self.navigationController pushViewController:teacherViewController animated:YES];
        } else {
            self.roomOwnerID = self.userID;
            self.teacherView = [[AIRBDTeacherView alloc]  initWithFrame:CGRectMake(0, 0, self.view.bounds.size.height, self.view.bounds.size.width)];
            AIRBDTeacherViewController* teacherViewController = [[AIRBDTeacherViewController alloc]init];
            teacherViewController.view = self.teacherView;
            [self.navigationController pushViewController:teacherViewController animated:YES];
            [self.teacherView createRoomWithConfig:self.config userID:self.userID completion:^(NSString * _Nonnull roomID) {
                if (roomID.length > 0) {
                    dispatch_async(dispatch_get_main_queue(), ^{

                        [self.teacherView startWithRoomID:roomID userID:self.userID];

                        UIAlertController *alert = [UIAlertController alertControllerWithTitle:@"房间ID" message:roomID preferredStyle:UIAlertControllerStyleAlert];
                        [alert addAction:[UIAlertAction actionWithTitle:@"拷贝" style:UIAlertActionStyleDefault handler:^(UIAlertAction * _Nonnull action) {
                            UIPasteboard *pboard = [UIPasteboard generalPasteboard];
                            pboard.string = roomID;
                        }]];
                        [self presentViewController:alert animated:true completion:nil];
                    });
                    
                } else {
                    [[AIRBDToast shareInstance] makeToast:@"创建房间失败" duration:2.0];
                    [self.navigationController popViewControllerAnimated:YES];
                }
            }];
        }
    } else {
        if (self.classNumberInput.text.length > 0) {
            self.roomID = self.classNumberInput.text;
            self.studentView = [[AIRBDStudentView alloc] initWithFrame:CGRectMake(0, 0, self.view.bounds.size.width, self.view.bounds.size.height)];
            [self.studentView startWithRoomID:self.roomID userID:self.userID roomOwnerID:self.roomOwnerID];
            AIRBDStudentViewController* studentViewController = [[AIRBDStudentViewController alloc]init];
            studentViewController.view = self.studentView;
            [self.navigationController pushViewController:studentViewController animated:YES];
        } else {
            [[AIRBDToast shareInstance] makeToast:@"请先输入有效的房间ID" duration:2.0];
        }
    }
}

#pragma -mark UITextFieldDelegate
- (BOOL)textFieldShouldReturn:(UITextField *)textField {
    if (self.classNumberInput == textField) {
        [self.classNumberInput resignFirstResponder];    //主要是[receiver resignFirstResponder]在哪调用就能把receiver对应的键盘往下收
        if (textField.text.length > 0) {
            self.roomID = textField.text;
        }
//        self.messageInputField.text = nil;
    } else if (self.userNickInput == textField){
        [self.userNickInput resignFirstResponder];    //主要是[receiver resignFirstResponder]在哪调用就能把receiver对应的键盘往下收
        if (textField.text.length > 0) {
            self.userNick = textField.text;
        }
    }
    return YES;
}

- (BOOL)textFieldShouldClear:(UITextField *)textField{
     return YES;
}

#pragma -mark NSNotification
- (void)keyBoardWillShow:(NSNotification *) note {
    // 获取用户信息
    NSDictionary *userInfo = [NSDictionary dictionaryWithDictionary:note.userInfo];
    // 获取键盘高度
    CGRect keyBoardBounds  = [[userInfo objectForKey:UIKeyboardFrameEndUserInfoKey] CGRectValue];
    CGFloat keyBoardHeight = keyBoardBounds.size.height;
    // 获取键盘动画时间
    CGFloat animationTime  = [[userInfo objectForKey:UIKeyboardAnimationDurationUserInfoKey] floatValue];
    
    // 定义好动作
    void (^animation)(void) = ^void(void) {
//        self.messageInputField.transform = CGAffineTransformMakeTranslation(0, -keyBoardHeight);
    };
    
    if (animationTime > 0) {
        [UIView animateWithDuration:animationTime animations:animation];
    } else {
        animation();
    }
}

- (void)keyBoardWillHide:(NSNotification *) note {
    // 获取用户信息
    NSDictionary *userInfo = [NSDictionary dictionaryWithDictionary:note.userInfo];
    // 获取键盘动画时间
    CGFloat animationTime  = [[userInfo objectForKey:UIKeyboardAnimationDurationUserInfoKey] floatValue];
    
    // 定义好动作
    void (^animation)(void) = ^void(void) {
//        self.messageInputField.transform = CGAffineTransformIdentity;
    };
    
    if (animationTime > 0) {
        [UIView animateWithDuration:animationTime animations:animation];
    } else {
        animation();
    }
}

- (void)touchesBegan:(NSSet<UITouch *> *)touches withEvent:(UIEvent *)event{
    [self.view endEditing:YES];
}

#pragma mark  - UIGestureRecognizerDelegate
- (BOOL)gestureRecognizerShouldBegin:(UIGestureRecognizer *)gestureRecognizer{
    if ([self.navigationController.viewControllers count] == 1) {
        return NO;
    }else {
        return YES;
    }
}

#pragma mark - UITableViewDelegate
- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath {
    self.classNumberInput.text = self.classRoomList[indexPath.row].roomID;
    self.roomOwnerID = self.classRoomList[indexPath.row].ownerID;
    [tableView deselectRowAtIndexPath:indexPath animated:YES];
    self.classRoomListTableView.hidden = YES;
}

- (CGFloat)tableView:(UITableView *)tableView heightForRowAtIndexPath:(NSIndexPath *)indexPath {
    return 40;
}

#pragma mark - UITableViewDataSource
- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView {
    return 1;
}

- (NSString *) tableView:(UITableView *)tableView titleForHeaderInSection:(NSInteger)section {
    return @"选择教室";
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section {
    return self.classRoomList.count;
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath {
    UITableViewCell *cell = [tableView dequeueReusableCellWithIdentifier:@"indexpath"];
    if (!cell) {
        cell = [[UITableViewCell alloc] initWithStyle:UITableViewCellStyleSubtitle reuseIdentifier:@"indexpath"];
    }
    
    cell.textLabel.text = self.classRoomList[indexPath.row].roomID;
    
    return cell;
}


@end
