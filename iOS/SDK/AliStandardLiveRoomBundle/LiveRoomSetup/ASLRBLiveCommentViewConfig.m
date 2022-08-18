//
//  ASLRBLiveCommentViewConfig.m
//  AliStandardLiveRoomBundle
//
//  Created by fernando on 2022/3/31.
//

#import "ASLRBLiveCommentViewConfig.h"
#import "../CommonTools/UIColor+ColorWithHexString.h"

@implementation ASLRBLiveCommentViewConfig

- (instancetype) init {
    self = [super init];
    if (self) {
        _liveCommentLabelCornerRadius = 12.0;
    }
    return self;
}


-(UIFont*) liveCommentLabelTextFont {
    if (!_liveCommentLabelTextFont) {
        _liveCommentLabelTextFont = [UIFont fontWithName:@"PingFangSC-Regular" size:14];
    }
    return _liveCommentLabelTextFont;
}

- (UIColor*)liveCommentLabelBackgroundColor {
    if (!_liveCommentLabelBackgroundColor) {
        _liveCommentLabelBackgroundColor = [UIColor colorWithHexString:@"#000000" alpha:0.3];
    }
    return _liveCommentLabelBackgroundColor;
}

- (UIColor*)liveUnreadCommentCountLabelTextColor {
    if (!_liveUnreadCommentCountLabelTextColor) {
        _liveUnreadCommentCountLabelTextColor = [UIColor colorWithRed:255/255.0 green:68/255.0 blue:44/255.0 alpha:1/1.0];
    }
    return _liveUnreadCommentCountLabelTextColor;
}

- (NSString*)liveCommentWarningNotice {
    if (!_liveCommentWarningNotice) {
        _liveCommentWarningNotice = @"欢迎大家来到直播间！直播间内严禁出现违法违规、低俗色情、吸烟酗酒等内容，若有违规行为请及时举报。";
    }
    return _liveCommentWarningNotice;
}

- (UIColor*)liveCommentWarningNoticeTextColor {
    if (!_liveCommentWarningNoticeTextColor) {
        _liveCommentWarningNoticeTextColor = [UIColor colorWithHexString:@"#12DBE6" alpha:1.0];
    }
    return _liveCommentWarningNoticeTextColor;
}

@end
