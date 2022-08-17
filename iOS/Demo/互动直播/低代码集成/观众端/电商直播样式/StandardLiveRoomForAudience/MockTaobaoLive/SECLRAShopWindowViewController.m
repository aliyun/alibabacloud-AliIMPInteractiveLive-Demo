//
//  AIRBDShopWindowViewController.m
//  AliInteractiveRoomDemo
//
//  Created by fernando on 2021/10/9.
//

#import "SECLRAShopWindowViewController.h"
#import "ASLUKResourceManager.h"

@interface SECLRAShopWindowViewController ()
@property (strong, nonatomic) UIImageView* image;
@end

@implementation SECLRAShopWindowViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    // Do any additional setup after loading the view.
    
    _image = [[UIImageView alloc] initWithImage:[UIImage imageNamed:@"直播-商品橱窗" inBundle:[ASLUKResourceManager sharedInstance].resourceBundle compatibleWithTraitCollection:nil]];
    _image.frame = self.view.bounds;
    [self.view addSubview:_image];
}

- (void) viewWillLayoutSubviews {
    _image.frame = self.view.bounds;
}

- (void) touchesBegan:(NSSet<UITouch *> *)touches withEvent:(UIEvent *)event {
    if (self.onTapped) {
        self.onTapped();
    }
}

/*
#pragma mark - Navigation

// In a storyboard-based application, you will often want to do a little preparation before navigation
- (void)prepareForSegue:(UIStoryboardSegue *)segue sender:(id)sender {
    // Get the new view controller using [segue destinationViewController].
    // Pass the selected object to the new view controller.
}
*/

@end
