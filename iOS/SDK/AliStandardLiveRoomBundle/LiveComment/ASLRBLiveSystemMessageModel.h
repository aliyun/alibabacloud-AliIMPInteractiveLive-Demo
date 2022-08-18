//
//  ASLRBLiveSystemMessageModel.h
//  AliStandardLiveRoomBundle
//
//  Created by fernando on 2021/10/15.
//

#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

@interface ASLRBLiveSystemMessageModel : NSObject
@property (copy, nonatomic) NSString* rawMessage;
@property (copy, nonatomic) NSDictionary* extension;
@end

NS_ASSUME_NONNULL_END
