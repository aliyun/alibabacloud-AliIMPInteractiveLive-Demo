//
//  AIRBLoggerManager.m
//  AliInteractiveRoomBundle
//
//  Created by fernando on 2021/5/11.
//  Copyright © 2021 AliYun. All rights reserved.
//

#import "AIRBLoggerManager.h"
#import "AliInteractiveRoomLogger.h"

void AIRBLogVerbose(const char* _Nullable format, ...) {
    @autoreleasepool {
        va_list args;
        va_start(args, format);
        NSString *message = [[NSString alloc] initWithFormat:[NSString stringWithCString:format encoding:NSUTF8StringEncoding] arguments:args];
        [[AIRBLoggerManager sharedInstance] log:message withLevel:AIRBLoggerLevelVerbose];
        va_end(args);
    }
}

void AIRBLogDebug(const char* _Nullable format, ...) {
    @autoreleasepool {
        va_list args;
        va_start(args, format);
        NSString *message = [[NSString alloc] initWithFormat:[NSString stringWithCString:format encoding:NSUTF8StringEncoding] arguments:args];
        [[AIRBLoggerManager sharedInstance] log:message withLevel:AIRBLoggerLevelDebug];
        va_end(args);
    }
}

void AIRBLogInfo(const char* _Nullable format, ...) {
    @autoreleasepool {
        va_list args;
        va_start(args, format);
        NSString *message = [[NSString alloc] initWithFormat:[NSString stringWithCString:format encoding:NSUTF8StringEncoding] arguments:args];
        [[AIRBLoggerManager sharedInstance] log:message withLevel:AIRBLoggerLevelInfo];
        va_end(args);
    }
}

void AIRBLogWarning(const char* _Nullable format, ...) {
    @autoreleasepool {
        va_list args;
        va_start(args, format);
        NSString *message = [[NSString alloc] initWithFormat:[NSString stringWithCString:format encoding:NSUTF8StringEncoding] arguments:args];
        [[AIRBLoggerManager sharedInstance] log:message withLevel:AIRBLoggerLevelWarning];
        va_end(args);
    }
}

void AIRBLogError(const char* _Nullable format, ...) {
    @autoreleasepool {
        va_list args;
        va_start(args, format);
        NSString *message = [[NSString alloc] initWithFormat:[NSString stringWithCString:format encoding:NSUTF8StringEncoding] arguments:args];
        [[AIRBLoggerManager sharedInstance] log:message withLevel:AIRBLoggerLevelError];
        va_end(args);
    }
}

@interface AIRBLoggerManager()
@property (strong, nonatomic) NSDateFormatter *formatter;

@end

@implementation AIRBLoggerManager

+ (AIRBLoggerManager*) sharedInstance {
    static AIRBLoggerManager *sharedInstance = nil;
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        sharedInstance = [[AIRBLoggerManager alloc] init];
    });
    return sharedInstance;
}

- (instancetype) init {
    self = [super init];
    if (self) {
//#ifdef DEBUG
        _loggerLevel = AIRBLoggerLevelInfo;
//#else
//        _loggerLevel = AIRBLoggerLevelE;
//#endif
        _formatter = [[NSDateFormatter alloc] init];
        [_formatter setDateFormat:@"yyyy-MM-dd HH:mm:ss.SSS"];
    }
    return self;
}

- (void) log:(NSString *)message withLevel:(AIRBLoggerLevel)level {
    if (level >= self.loggerLevel) {
        NSString* bundlLogMessasge = [NSString stringWithFormat:@"[AliInteractiveRoomBundle] %@", message];
        if ([self.delegate respondsToSelector:@selector(onLogMessage:)]) {
            [self.delegate onLogMessage:[NSString stringWithFormat:@"%@ %@", [self.formatter stringFromDate:[NSDate date]], bundlLogMessasge]];
        }
    }
}

@end
