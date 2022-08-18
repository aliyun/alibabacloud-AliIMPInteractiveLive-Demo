//
//  ASLRBLogger.m
//  AliStandardLiveRoomBundle
//
//  Created by fernando on 2021/11/8.
//

#import "ASLRBLogger.h"

void internalLog(const char* _Nullable format, ...) {
    @autoreleasepool {
        va_list args;
        va_start(args, format);
        NSString *message = [[NSString alloc] initWithFormat:[NSString stringWithCString:format encoding:NSUTF8StringEncoding] arguments:args];
        [[ASLRBLogger sharedInstance] log:message];
        va_end(args);
    }
}

@implementation ASLRBLogger
+ (ASLRBLogger*) sharedInstance {
    static ASLRBLogger *sharedInstance = nil;
    static dispatch_once_t token;
    dispatch_once(&token, ^{
        sharedInstance = [[ASLRBLogger alloc] init];
    });
    return sharedInstance;
}

- (void) log:(NSString *)log {
    if (self.onLog) {
        self.onLog(log);
    } else {
        NSLog(@"%@", log);
    }
}
@end
