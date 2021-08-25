//
//  AIRBDLiveViewController.m
//  AliInteractiveRoomDemo
//
//  Created by fernando on 2021/5/20.
//

#import "AIRBDLiveViewController.h"

#import <AliInteractiveRoomBundle/AliInteractiveRoomBundle.h>

#import "AIRBDAnchorView.h"
#import "AIRBDAudienceView.h"
#import "AIRBDAnchorViewController.h"
#import "AIRBDAudienceViewController.h"
#import "AIRBDToast.h"
#import "Utilities/Utility.h"

static NSString * const DMLiveTableCellIdentifier = @"DMLiveTableCellIdentifier";

@interface AIRBDLiveViewController () <UITableViewDataSource, UITableViewDelegate, UITextFieldDelegate,UIPickerViewDelegate,UIPickerViewDataSource,UIGestureRecognizerDelegate>
@property (nonatomic, strong) AIRBDAudienceView* audienceView;
@property (nonatomic, strong) AIRBDAnchorView* anchorView;
@property (strong, nonatomic) AIRBRoomEngineConfig* config;
@property (strong, nonatomic) UITableView *myTableView;
@property (strong, nonatomic) UIPickerView *roomIDPickerView;
@property (strong, nonatomic) NSArray *demoList;
@property (strong, nonatomic) NSMutableArray *roomIDList;
@property (strong, nonatomic) NSArray *role;
@property (copy, nonatomic) NSString* userID;
@property (copy, nonatomic) NSString* selectedRoomID;
@property (copy, nonatomic) NSString* selectedRole;
@property (assign, nonatomic) BOOL logined;
@property (assign, nonatomic) BOOL engineStarted;
@property (copy, nonatomic) NSString* plistPath;

@end

@implementation AIRBDLiveViewController


- (void)viewDidLoad {
    [super viewDidLoad];
    
    if ([[UIDevice currentDevice].systemVersion floatValue]>=7.0) {//侧滑退出手势
        if ([self.navigationController respondsToSelector:@selector(interactivePopGestureRecognizer)]) {
            self.navigationController.interactivePopGestureRecognizer.enabled = YES;
            self.navigationController.interactivePopGestureRecognizer.delegate = self;
        }
    }

    self.demoList = @[@"创建房间", @"进入已有房间",@"添加房间号",@"清空房间号"];
    NSArray *paths = NSSearchPathForDirectoriesInDomains(NSDocumentDirectory,NSUserDomainMask,YES);
    _plistPath=[[paths objectAtIndex:0] stringByAppendingPathComponent:@"roomInfo.plist"];
    NSFileManager* fileManager = [NSFileManager defaultManager];
    if(![fileManager fileExistsAtPath:_plistPath]){
        [fileManager createFileAtPath:_plistPath contents:nil attributes:nil];
    }

    //写入内容
    NSMutableDictionary *Dic = [NSMutableDictionary dictionaryWithContentsOfFile:_plistPath];
    if([Dic valueForKey:@"roomID"]!=nil){
        _roomIDList = [Dic valueForKey:@"roomID"];
    }
    if(!_roomIDList){
        _roomIDList = [[NSMutableArray alloc]init];
    }
    _role = @[@"主播",@"观众"];
    _selectedRoomID = @"";
    _selectedRole = _role[0];
    [self setupUI];
    
}


- (void)viewWillAppear:(BOOL)animated{
    [_roomIDPickerView reloadComponent:0];
    if([_roomIDList count]>0){
        _selectedRoomID = _roomIDList[0];
    }
}

#pragma mark - Property
- (UITableView *)myTableView {
    if (!_myTableView) {
        _myTableView = [[UITableView alloc] initWithFrame:CGRectMake(0, self.view.bounds.size.height-400, self.view.bounds.size.width, self.view.bounds.size.height) style:UITableViewStyleGrouped];
        _myTableView.delegate = self;
        _myTableView.dataSource = self;
        [_myTableView setScrollEnabled:NO];
    }
    return _myTableView;
}

- (UIPickerView*)roomIDPickerView{
    if(!_roomIDPickerView){
        _roomIDPickerView = [[UIPickerView alloc]initWithFrame:CGRectMake(0, 0, self.view.bounds.size.width, 200)];
        _roomIDPickerView.delegate = self;
        _roomIDPickerView.dataSource = self;
        [_roomIDPickerView setAccessibilityNavigationStyle:UIAccessibilityNavigationStyleSeparate];
    }
    [_roomIDPickerView setBackgroundColor:[UIColor colorWithWhite:0.9 alpha:0.1]];
    return _roomIDPickerView;
}


- (instancetype) initWithUserID:(NSString*)userID config:(nonnull AIRBRoomEngineConfig *)config{
    self = [super init];
    if (self) {
        _userID = userID;
        _config = config;
    }
    return self;
}

- (void) dealloc {
    [self teardownUI];
    
    [[NSNotificationCenter defaultCenter] removeObserver:self];
}

- (void) setupUI {
    self.edgesForExtendedLayout = UIRectEdgeNone;
    self.navigationController.navigationBar.translucent = NO;
    self.automaticallyAdjustsScrollViewInsets = YES;
    self.extendedLayoutIncludesOpaqueBars = YES;
    
    self.view.backgroundColor = [UIColor whiteColor];
    
    [self.view addSubview:self.myTableView];
    [self.view addSubview:self.roomIDPickerView];
    self.myTableView.translatesAutoresizingMaskIntoConstraints = NO;
    
}

- (void) teardownUI {
    [self.myTableView removeFromSuperview];
    [self.anchorView removeFromSuperview];
    [self.audienceView removeFromSuperview];
    
    [self.anchorView leaveRoom];
    [[AIRBRoomEngine sharedInstance] logoutWithUserID:self.userID];
}

- (void)anchroLogin {
        self.anchorView = [[AIRBDAnchorView alloc] initWithFrame:CGRectMake(0, 0, self.view.bounds.size.width, self.view.bounds.size.height)];
        self.anchorView.backgroundColor = [UIColor blackColor];
        AIRBDAnchorViewController* anchorViewController = [[AIRBDAnchorViewController alloc]init];
        anchorViewController.view = self.anchorView;
        self.anchorView.exitActionDelegate = self.navigationController;
        [self.navigationController pushViewController:anchorViewController animated:YES];
        [self.navigationController setNavigationBarHidden:YES];
        [self.anchorView enterRoomWithID:self->_selectedRoomID userID:self.userID];
}

- (void)audienceLogin {
        self.audienceView = [[AIRBDAudienceView alloc] initWithFrame:CGRectMake(0, 0, self.view.bounds.size.width, self.view.bounds.size.height)];
        self.audienceView.backgroundColor = [UIColor blackColor];
        AIRBDAudienceViewController* audienceViewController = [[AIRBDAudienceViewController alloc]init];
        audienceViewController.view = self.audienceView;
        self.audienceView.exitActionDelegate = self.navigationController;
        [self.navigationController setNavigationBarHidden:YES];
        [self.navigationController pushViewController:audienceViewController animated:YES];
        [self.audienceView enterRoomWithID:self->_selectedRoomID userID:self.userID];
}

- (void)createRoomAction {
    self.anchorView = [[AIRBDAnchorView alloc] initWithFrame:CGRectMake(0, 0, self.view.bounds.size.width, self.view.bounds.size.height)];
    self.anchorView.backgroundColor = [UIColor blackColor];
    self.anchorView.exitActionDelegate = self.navigationController;
    AIRBDAnchorViewController* anchorViewController = [[AIRBDAnchorViewController alloc]init];
    anchorViewController.view = self.anchorView;
    [self.navigationController pushViewController:anchorViewController animated:YES];
    [self.navigationController setNavigationBarHidden:YES];
    [self.anchorView createRoomWithConfig:self.config userID:self.userID completion:^(NSString * _Nonnull roomID) {
        dispatch_async(dispatch_get_main_queue(), ^{
            [self.anchorView enterRoomWithID:roomID userID:self.userID];
            UIAlertController *alert = [UIAlertController alertControllerWithTitle:@"房间ID" message:roomID preferredStyle:UIAlertControllerStyleAlert];
            [alert addAction:[UIAlertAction actionWithTitle:@"拷贝" style:UIAlertActionStyleDefault handler:^(UIAlertAction * _Nonnull action) {
                UIPasteboard *pboard = [UIPasteboard generalPasteboard];
                pboard.string = roomID;
                
                if(![self->_roomIDList containsObject:roomID]){
                    [self->_roomIDList addObject:roomID];
                }
                NSMutableDictionary *Dic = [NSMutableDictionary dictionaryWithContentsOfFile:self->_plistPath];
                if(!Dic){
                    Dic = [[NSMutableDictionary alloc]init];
                }
                [Dic setObject:self->_roomIDList forKey:@"roomID"];
                [Dic writeToFile:self->_plistPath atomically:YES];
                
            }]];
            [self presentViewController:alert animated:true completion:nil];
        });
    }];
}

-(void) addRoomID{
        UIAlertController *alertController = [UIAlertController alertControllerWithTitle:nil
                                                                                 message:@"请输入房间ID"
                                                                          preferredStyle:UIAlertControllerStyleAlert];
        [alertController addAction:[UIAlertAction actionWithTitle:@"取消" style:UIAlertActionStyleCancel handler:nil]];
        [alertController addAction:[UIAlertAction actionWithTitle:@"确定"
                                                            style:UIAlertActionStyleDefault
                                                          handler:^(UIAlertAction * _Nonnull action) {
            UITextField* roomIDTextField = alertController.textFields.firstObject;
            if(![self->_roomIDList containsObject:roomIDTextField.text]){
                [self->_roomIDList addObject:roomIDTextField.text];
            }
            NSMutableDictionary *Dic = [NSMutableDictionary dictionaryWithContentsOfFile:self->_plistPath];
            if(!Dic){
                Dic = [[NSMutableDictionary alloc]init];
            }
            [Dic setObject:self->_roomIDList forKey:@"roomID"];
            [Dic writeToFile:self->_plistPath atomically:YES];
            [self->_roomIDPickerView reloadAllComponents];
        }]];
    
        [alertController addTextFieldWithConfigurationHandler:^(UITextField*_Nonnull textField) {
            textField.placeholder = @"请输入房间ID";
        }];
        [self presentViewController:alertController animated:YES completion:nil];
}


-(void) clearRoomID{
        UIAlertController *alertController = [UIAlertController alertControllerWithTitle:nil
                                                                                 message:@"确认删除所有房间号？"
                                                                          preferredStyle:UIAlertControllerStyleAlert];
        [alertController addAction:[UIAlertAction actionWithTitle:@"取消" style:UIAlertActionStyleCancel handler:nil]];
        [alertController addAction:[UIAlertAction actionWithTitle:@"确定"
                                                            style:UIAlertActionStyleDefault
                                                          handler:^(UIAlertAction * _Nonnull action) {
            [self->_roomIDList removeAllObjects];
            NSMutableDictionary *Dic = [NSMutableDictionary dictionaryWithContentsOfFile:self->_plistPath];
            if(!Dic){
                Dic = [[NSMutableDictionary alloc]init];
            }
            [Dic setObject:self->_roomIDList forKey:@"roomID"];
            [Dic writeToFile:self->_plistPath atomically:YES];
            [self->_roomIDPickerView reloadAllComponents];
        }]];

        [self presentViewController:alertController animated:YES completion:nil];
}

#pragma mark - UITableViewDelegate
- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath {
    if (indexPath.row == 0) {
        [self createRoomAction];
    } else if(indexPath.row == 2){
        [self addRoomID];
    } else if(indexPath.row == 3){
        [self clearRoomID];
    }else if ([_selectedRole isEqualToString:@"主播"]) {
        if([_selectedRoomID length]>0&&[_roomIDList count]>0){
            [self anchroLogin];
        }else{
            UIAlertController *alertController = [UIAlertController alertControllerWithTitle:nil
                                                                                     message:@"请选择正确的房间号"
                                                                              preferredStyle:UIAlertControllerStyleAlert];
            [alertController addAction:[UIAlertAction actionWithTitle:@"确认" style:UIAlertActionStyleCancel handler:nil]];
            [self presentViewController:alertController animated:YES completion:nil];
        }
    } else if ([_selectedRole isEqualToString:@"观众"]) {
        if([_selectedRoomID length]>0&&[_roomIDList count]>0){
            [self audienceLogin];
        }else{
            UIAlertController *alertController = [UIAlertController alertControllerWithTitle:nil
                                                                                     message:@"请选择正确的房间号"
                                                                              preferredStyle:UIAlertControllerStyleAlert];
            [alertController addAction:[UIAlertAction actionWithTitle:@"确认" style:UIAlertActionStyleCancel handler:nil]];
            [self presentViewController:alertController animated:YES completion:nil];
        }
        
    }
    [tableView deselectRowAtIndexPath:indexPath animated:YES];
}



- (CGFloat)tableView:(UITableView *)tableView heightForRowAtIndexPath:(NSIndexPath *)indexPath {
    return 50;
}

#pragma mark - UITableViewDataSource
- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView {
    return 1;
}

- (NSString *) tableView:(UITableView *)tableView titleForHeaderInSection:(NSInteger)section {
    return @"直播场景";
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section {
    return self.demoList.count;
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath {
    UITableViewCell *cell = [tableView dequeueReusableCellWithIdentifier:DMLiveTableCellIdentifier];
    if (!cell) {
        cell = [[UITableViewCell alloc] initWithStyle:UITableViewCellStyleSubtitle reuseIdentifier:DMLiveTableCellIdentifier];
    }
    
    NSString *demoTitle = self.demoList[indexPath.row];
    cell.textLabel.text = demoTitle;
    
    return cell;
}

#pragma mark - UIPickerViewDataSource
//设置列数
- (NSInteger)numberOfComponentsInPickerView:(UIPickerView *)pickerView{
    return 2;
}

//设置指定列包含的项数
- (NSInteger)pickerView:(UIPickerView *)pickerView numberOfRowsInComponent:(NSInteger)component{
    if (component == 0) {
        return self.roomIDList.count;
    }
    return self.role.count;
}

//设置每个选项显示的内容
- (NSString *)pickerView:(UIPickerView *)pickerView titleForRow:(NSInteger)row forComponent:(NSInteger)component{
    if (component == 0) {
        return self.roomIDList[row];
    }
    return self.role[row];
}

//用户进行选择
- (void)pickerView:(UIPickerView *)pickerView didSelectRow:(NSInteger)row inComponent:(NSInteger)component{
    if (component == 0) {
        if([_roomIDList count]!=0){
            self.selectedRoomID = self.roomIDList[row];
        }
    }else{
        self.selectedRole = self.role[row];
    }
}

- (UIView *)pickerView:(UIPickerView *)pickerView viewForRow:(NSInteger)row forComponent:(NSInteger)component reusingView:(UIView *)view{
    UILabel *label = (UILabel *)view;
    if (label == nil) {
        label = [[UILabel alloc]init];
        if(component==0){
            label.font = [UIFont systemFontOfSize:12];
        }else{
            label.font = [UIFont systemFontOfSize:20];
        }
        
        label.textColor = [UIColor brownColor];
        [label setTextAlignment:NSTextAlignmentCenter];
        [label setBackgroundColor:[UIColor clearColor]];
    }
    label.text = [self pickerView:pickerView titleForRow:row forComponent:component];
    return label;
}

- (CGFloat)pickerView:(UIPickerView *)pickerView widthForComponent:(NSInteger)component {
    if(component==0){
        return pickerView.bounds.size.width*0.75;
    }else{
        return pickerView.bounds.size.width*0.25;
    }
}

#pragma mark  - UIGestureRecognizerDelegate
- (BOOL)gestureRecognizerShouldBegin:(UIGestureRecognizer *)gestureRecognizer{
    if ([self.navigationController.viewControllers count] == 1) {
        return NO;
    }else{
        return YES;
    }
}
@end
