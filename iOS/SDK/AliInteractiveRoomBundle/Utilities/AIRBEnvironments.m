//
//  AIRBEnvironments.m
//  AliInteractiveRoomBundle
//
//  Created by fernando on 2021/5/8.
//  Copyright © 2021 AliYun. All rights reserved.
//

#import "AIRBEnvironments.h"

#import <UIKit/UIKit.h>
#import <sys/utsname.h>
#import <CoreTelephony/CTTelephonyNetworkInfo.h>
#import <CoreTelephony/CTCarrier.h>

#import "../MonitorHub/AIRBMonitorHubManager.h"

const NSString* kVPaasSDKVersion = @"2.0.0.20220812001";

@implementation AIRBEnvironments

+ (AIRBEnvironments*) shareInstance {
    static AIRBEnvironments *sharedInstance = nil;
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        sharedInstance = [[AIRBEnvironments alloc] init];
    });
    return sharedInstance;
}

- (instancetype) init {
    self = [super init];
    if (self) {
        _usePrereleaseEnvironment = NO;
    }
    return self;
}

- (NSString *)appLocale {
    return [NSString stringWithFormat:@"%@", NSLocale.preferredLanguages.firstObject];
}

- (NSString*)longLinkAddr {
    if (self.usePrereleaseEnvironment) {
        [AIRBMonitorHubManager sharedInstance].configModel.prereleaseEnvironment = YES;
        return [NSString stringWithFormat:@"%@", @"tls://pre-tls.imp.aliyuncs.com:443"];
    } else {
        [AIRBMonitorHubManager sharedInstance].configModel.prereleaseEnvironment = NO;
        return [NSString stringWithFormat:@"%@", @"tls://tls.imp.aliyuncs.com:443"];
    }
}

- (NSString *)dataPath {
    static NSString *dataDirectory = nil;
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        @autoreleasepool {
            NSArray *paths = NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES);
            if (paths != nil) {
                NSString *documentsDirectory = [paths objectAtIndex:0];
                dataDirectory = [documentsDirectory stringByAppendingPathComponent:@"AIRBData"];
                
                // if path doesn't exist, then create the default path
                if (![[NSFileManager defaultManager] fileExistsAtPath:dataDirectory]) {
                    [[NSFileManager defaultManager] createDirectoryAtPath:dataDirectory withIntermediateDirectories:YES attributes:nil error:nil];
                }
            }
        }
    });
    return dataDirectory;
}

- (NSString *)logPath {
    NSArray *paths = NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES);
    
    NSString *documentsDirectory = [paths firstObject];
    
    if (documentsDirectory == nil) {
        NSLog(@"日志文件夹为空");
        return nil;
    }
    
    NSString *filePath = [NSString stringWithFormat:@"%@/log.txt", documentsDirectory];
    
    if (![[NSFileManager defaultManager] fileExistsAtPath:filePath]) {
        [[NSFileManager defaultManager] createFileAtPath:filePath contents:nil attributes:nil];
    }
    
    return filePath;
}

- (NSString *)sdkVersion {
    return kVPaasSDKVersion;
}

- (NSString *)osName {
    return [UIDevice currentDevice].systemName;
}

- (NSString *)osVersion {
    return [UIDevice currentDevice].systemVersion;
}

- (NSString *)deviceName {
    struct utsname systemInfo;
    uname(&systemInfo);
    return [NSString stringWithCString:systemInfo.machine encoding:NSUTF8StringEncoding];
}

- (NSString *)deviceType {
    return [UIDevice currentDevice].model;
}

- (NSString *)deviceLocale {
    return NSLocale.preferredLanguages.firstObject;
}

- (NSString *)timeZoneName {
    //use system timezone as default
    return [NSTimeZone systemTimeZone].name;
}

- (void)setNetowrkType:(NSString *)netowrkType {
    _netowrkType = netowrkType;
    if ([_netowrkType isEqualToString:@"Cellular"]) {
        CTTelephonyNetworkInfo *info = [[CTTelephonyNetworkInfo alloc] init];
        NSString *currentStatus = info.currentRadioAccessTechnology;
        if ([currentStatus isEqualToString:@"CTRadioAccessTechnologyCDMA1x"]){
            _netowrkType = @"2G";
        }else if ([currentStatus isEqualToString:@"CTRadioAccessTechnologyCDMAEVDORev0"]){
            _netowrkType = @"3G";
        }else if ([currentStatus isEqualToString:@"CTRadioAccessTechnologyCDMAEVDORevA"]){
            _netowrkType = @"3G";
        }else if ([currentStatus isEqualToString:@"CTRadioAccessTechnologyCDMAEVDORevB"]){
            _netowrkType = @"3G";
        }else if ([currentStatus isEqualToString:@"CTRadioAccessTechnologyeHRPD"]){
            _netowrkType = @"HRPD";
        }else if ([currentStatus isEqualToString:@"CTRadioAccessTechnologyLTE"]){
            _netowrkType = @"4G";
        }else if ([currentStatus isEqualToString:@"CTRadioAccessTechnologyNRNSA"]){
            _netowrkType = @"5G";
        }else if ([currentStatus isEqualToString:@"CTRadioAccessTechnologyNR"]){
            _netowrkType = @"5G";
        }
    }
}

@end
