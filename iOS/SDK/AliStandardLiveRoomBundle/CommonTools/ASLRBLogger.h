//
//  ASLRBLogger.h
//  AliStandardLiveRoomBundle
//
//  Created by fernando on 2021/11/8.
//

#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN


#define LOG(...) internalLog(__VA_ARGS__)
void internalLog(const char* _Nullable format, ...);

@interface ASLRBLogger : NSObject

@property (strong, nonatomic) void(^onLog)(NSString* log);

+ (instancetype)sharedInstance;
- (void) log:(NSString*)log;
@end

NS_ASSUME_NONNULL_END
