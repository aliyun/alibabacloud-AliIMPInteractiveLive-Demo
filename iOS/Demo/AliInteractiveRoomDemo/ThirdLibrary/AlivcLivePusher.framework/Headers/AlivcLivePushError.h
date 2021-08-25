//
//  AlivcLivePushError.h
//  AlivcLiveCaptureLib
//
//  Created by TripleL on 2017/9/25.
//  Copyright © 2017年 Alibaba. All rights reserved.
//

#import <Foundation/Foundation.h>


@interface AlivcLivePushError : NSObject


/**
 错误描述
 */
@property (nonatomic, strong) NSString* errorDescription;


/**
 错误码
 */
@property (nonatomic, assign) NSInteger errorCode;

@end
