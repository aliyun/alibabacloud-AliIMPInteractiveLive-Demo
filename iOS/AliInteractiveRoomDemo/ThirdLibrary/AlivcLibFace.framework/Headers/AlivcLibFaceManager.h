//
//  AlivcLibFace.h
//  AlivcLibFace
//
//  Created by pengshuang on 25/12/2017.
//  Copyright © 2017 pengshuang. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface AlivcLibFaceManager : NSObject

+ (AlivcLibFaceManager *)shareManager;

- (int)create;

- (long)process:(long)data width:(int)width height:(int)height rotation:(int)rotation format:(int)format extra:(long)extra;

- (void)destroy;


@end

