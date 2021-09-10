//
//  AIRBDTeacherViewController.m
//  AliInteractiveRoomDemo
//
//  Created by 麦辣 on 2021/6/9.
//

#import "AIRBDStudentViewController.h"
#import "AIRBDStudentView.h"
@interface AIRBDStudentViewController ()
@property(strong,nonatomic) AIRBDStudentView* studentView;
@end

@implementation AIRBDStudentViewController

- (void)viewDidLoad {
    [super viewDidLoad];
}

- (void)viewWillDisappear:(BOOL)animated {
    [self.studentView stop];
}

- (void)setView:(UIView*)view{
    [super setView:view];
    self.studentView = (AIRBDStudentView*)view;
}

- (void)touchesBegan:(NSSet<UITouch *> *)touches withEvent:(UIEvent *)event{
    [self.view endEditing:YES];
}
@end
