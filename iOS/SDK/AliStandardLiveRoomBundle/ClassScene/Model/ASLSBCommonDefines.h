//
//  ASLSBCommonDefines.h
//  AliInteractiveRoomDemo
//
//  Created by fernando on 2021/7/16.
//

#import <Foundation/Foundation.h>

// 代理的action
#define DELEGATE_ACTION(func) if ([self.delegate respondsToSelector:@selector(func)]) {    \
[self.delegate func];  \
}

#define DELEGATE_ACTION_1ARG(func, arg) if ([self.delegate respondsToSelector:@selector(func:)]) {    \
[self.delegate func:arg];  \
}

#define DELEGATE_ACTION_2ARG(func1, arg1, func2, arg2) if ([self.delegate respondsToSelector:@selector(func1:func2:)]) {    \
[self.delegate func1:arg1 func2:arg2];  \
}

#define DELEGATE_ACTION_3ARG(func1, arg1, func2, arg2, func3, arg3) if ([self.delegate respondsToSelector:@selector(func1:func2:func3:)]) {    \
[self.delegate func1:arg1 func2:arg2 func3:arg3];  \
}

// 代理的代理的action
#define DELEGATE_DELEGATE_ACTION(func) if ([self.delegate.delegate respondsToSelector:@selector(func)]) {    \
[self.delegate.delegate func];  \
}

#define DELEGATE_DELEGATE_ACTION_1ARG(func, arg) if ([self.delegate.delegate respondsToSelector:@selector(func:)]) {    \
[self.delegate.delegate func:arg];  \
}

#define DELEGATE_DELEGATE_ACTION_2ARG(func1, arg1, func2, arg2) if ([self.delegate.delegate respondsToSelector:@selector(func1:func2:)]) {    \
[self.delegate.delegate func1:arg1 func2:arg2];  \
}

#define DELEGATE_DELEGATE_ACTION_3ARG(func1, arg1, func2, arg2, func3, arg3) if ([self.delegate.delegate respondsToSelector:@selector(func1:func2:func3:)]) {    \
[self.delegate.delegate func1:arg1 func2:arg2 func3:arg3];  \
}

typedef NS_ENUM(NSInteger, ASCRBClassroomRole)
{
    ASCRBClassroomRoleTeacher = 0,
    ASCRBClassroomRoleStudent
};

typedef NS_ENUM(NSInteger, ASCRBClassroomType)
{
    ASCRBClassroomTypeLargeScale = 0,
    ASCRBClassroomTypeSmallScale
};

typedef NS_ENUM(NSInteger, ASCRBClassroomToastType)
{
    ASCRBClassroomToastTypeCommon = 0,
    ASCRBClassroomToastTypeWarning,
    ASCRBClassroomToastTypeFailure,
    ASCRBClassroomToastTypeError,
};

typedef NS_ENUM(NSInteger, ASLSBPusherEngineEvent)
{
    ASLSBPusherEngineEventPreviewStarted = 0,
    ASLSBPusherEngineEventStreamStarted,
    ASLSBPusherEngineEventStopped,
    ASLSBPusherEngineEventStreamResumed,
};
