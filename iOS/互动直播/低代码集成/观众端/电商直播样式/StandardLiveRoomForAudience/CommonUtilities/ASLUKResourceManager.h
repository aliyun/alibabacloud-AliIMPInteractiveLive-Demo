//
//  ASLRBResourceManager.h
//  AliStandardLiveRoomBundle
//
//  Created by fernando on 2021/11/16.
//

#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

@interface ASLUKResourceManager : NSObject

@property (strong, nonatomic) NSBundle* resourceBundle;

+ (instancetype)sharedInstance;
@end

NS_ASSUME_NONNULL_END
