//
//  AIRBRoomEngineReloginUtility.m
//  AliInteractiveRoomBundle
//
//  Created by fernando on 2022/3/15.
//  Copyright © 2022 AliYun. All rights reserved.
//

namespace gaea {
    namespace lwp {
        extern void NotifyNetworkChange(bool network_is_available);
    };
};

#import "AIRBRoomEngineReloginUtility.h"

@implementation AIRBRoomEngineReloginUtility
+ (void) notifyNetworkAvailable:(BOOL)available {
    gaea::lwp::NotifyNetworkChange(available);
}
@end
