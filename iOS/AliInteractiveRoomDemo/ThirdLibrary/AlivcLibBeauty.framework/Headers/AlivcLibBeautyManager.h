//
//  AlivcLibBeauty.h
//  AlivcLibBeauty
//
//  Created by pengshuang on 25/12/2017.
//  Copyright © 2017 pengshuang. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface AlivcLibBeautyManager : NSObject

+ (AlivcLibBeautyManager *)shareManager;

- (int)create:(void*)context;

- (void)setParam:(float)buffing whiten:(float)whiten pink:(float)pink cheekpink:(float)cheekpink thinface:(float)thinface shortenface:(float)shortenface bigeye:(float)bigeye;

- (void)switchOn:(bool)on;

- (int)process:(int)tex width:(int)width height:(int)height extra:(long)extra;

- (void)destroy;


@end
