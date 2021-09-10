//
//  AIRBDocumentProtocol.h
//  AliInteractiveRoomBundle
//
//  Created by fernando on 2021/9/3.
//  Copyright © 2021 AliYun. All rights reserved.
//

#ifndef AIRBDocumentProtocol_h
#define AIRBDocumentProtocol_h

#import <Foundation/Foundation.h>

@protocol AIRBDocumentProtocol <NSObject>

- (void) uploadDocument:(NSString*)docName
              onSuccess:(void(^)(NSString* documentID))onSuccess
              onFailure:(void(^)(NSString* errorMessage))onFailure;

- (void) startConvertDocument:(NSString*)documentID
                       toType:(NSString*)targetType
                    onSuccess:(void(^)(NSString* documentID))onSuccess
                    onFailure:(void(^)(NSString* errorMessage))onFailure;

- (void) getDocument:(NSString*)documentID
           onSuccess:(void(^)(NSArray<NSString*>* images))onSuccess
           onFailure:(void(^)(NSString* errorMessage))onFailure;
                       
                   
@end


#endif /* AIRBDocumentProtocol_h */
