//
//  Utility.m
//  AliInteractiveRoomBundle
//
//  Created by fernando on 2021/5/18.
//  Copyright © 2021 AliYun. All rights reserved.
//

#import "AIRBUtility.h"
#import "AIRBHMACSHA1.h"
#import "sys/utsname.h"

/*
 * 签名公共参数HeaderKey
 */
const NSString* APP_ID_KEY = @"a-app-id";
const NSString* SIGNATURE_KEY = @"a-signature"; // 签名结果串
const NSString* SIGNATURE_METHOD_KEY = @"a-signature-method";
const NSString* TIMESTAMP_KEY = @"a-timestamp";
const NSString* SIGNATURE_VERSION_KEY = @"a-signature-version";
const NSString* SIGNATURE_NONCE_KEY = @"a-signature-nonce";

const NSString* SIGNATURE_METHOD_VALUE = @"HMAC-SHA1";
const NSString* SIGNATURE_VERSION_VALUE = @"1.0";

@implementation AIRBUtility

+ (NSString*)currentDateString {
    NSDateFormatter *dateFormatter = [[NSDateFormatter alloc] init];
    NSLocale *enUSPOSIXLocale = [NSLocale localeWithLocaleIdentifier:@"en_US_POSIX"];
    [dateFormatter setLocale:enUSPOSIXLocale];
    [dateFormatter setDateFormat:@"yyyy-MM-dd'T'HH:mm:ss'Z'"];

    NSDate *now = [NSDate date];
    return [dateFormatter stringFromDate:now];
}

+ (NSString*)randomNumString {
    int x = arc4random() % 100;
    return [NSString stringWithFormat:@"%d", x];
}

+ (NSString*) getSignedRequestStringWithSecret:(NSString *)secret method:(NSString *)method path:(NSString *)path parameters:(NSDictionary *)params headers:(NSDictionary *)headers {
    
    NSParameterAssert([method isEqualToString:@"POST"] || [method isEqualToString:@"GET"] || [method isEqualToString:@"DELETE"]);
    NSParameterAssert(secret != nil);
    NSParameterAssert(method != nil);
    NSParameterAssert(path != nil);
    NSParameterAssert(params != nil && [params count] > 0);
    NSParameterAssert(headers != nil && [headers count] > 0);
    
    NSParameterAssert([SIGNATURE_METHOD_VALUE isEqualToString:[headers valueForKey:@"a-signature-method"]]);
    NSParameterAssert([SIGNATURE_VERSION_VALUE isEqualToString:[headers valueForKey:@"a-signature-version"]]);
    
    NSMutableArray* PROCESSED_HEADERS = [[NSMutableArray alloc] initWithArray:@[APP_ID_KEY,SIGNATURE_KEY,SIGNATURE_METHOD_KEY,TIMESTAMP_KEY,SIGNATURE_VERSION_KEY,SIGNATURE_NONCE_KEY]];
    
    NSMutableDictionary* signedHeaders = [[NSMutableDictionary alloc] init];
    for (NSString* str in PROCESSED_HEADERS) {
        if ([headers valueForKey:str]) {
            [signedHeaders setObject:[headers valueForKey:str] forKey:str];
        }
    }
    NSLog(@"signedHeaders:%@", signedHeaders);
    
    NSString* headerString = [AIRBUtility canonicalizedStringWithDictionary:signedHeaders];
    NSLog(@"headerString:%@", headerString);
    
    NSString* queryString = [AIRBUtility canonicalizedStringWithDictionary:params];
    NSLog(@"queryString:%@", queryString);
    
    NSString* stringToSign = [AIRBUtility buildSignString:method path:path queryString:queryString headerString:headerString];
    NSLog(@"stringToSign:%@", stringToSign);
    
    return [AIRBUtility encodeToPercentEscapeString:[AIRBUtility signWithString:stringToSign secret:[secret stringByAppendingString:@"&"]]];
    
}

+ (NSString*) canonicalizedStringWithDictionary:(NSDictionary*)dic {
    NSArray* keys = [[dic allKeys] sortedArrayUsingComparator:^NSComparisonResult(id  _Nonnull obj1, id  _Nonnull obj2) {
        return [(NSString*)obj1 compare:(NSString*)obj2 options:NSLiteralSearch];
    }];
    NSString* result = [[NSString alloc] init];
    for (id key in keys) {
        if (result.length == 0) {
            result = [result stringByAppendingFormat:@"%@=%@", [AIRBUtility encodeToPercentEscapeString:key], [AIRBUtility encodeToPercentEscapeString:dic[key]]];
        } else {
            result = [result stringByAppendingFormat:@"&%@=%@", [AIRBUtility encodeToPercentEscapeString:key], [AIRBUtility encodeToPercentEscapeString:dic[key]]];
        }
    }
    return result;
}

+ (NSString*) buildSignString:(NSString*)requestMethod path:(NSString*)path queryString:(NSString*)queryString headerString:(NSString*)headerString {
    NSString* result = [[NSString alloc] init];
    result = [result stringByAppendingString:requestMethod];
    result = [result stringByAppendingString:@"+"];
    result = [result stringByAppendingString:[AIRBUtility encodeToPercentEscapeString:path]];
    result = [result stringByAppendingString:@"+"];
    result = [result stringByAppendingString:[AIRBUtility encodeToPercentEscapeString:queryString]];
    result = [result stringByAppendingString:@"+"];
    result = [result stringByAppendingString:[AIRBUtility encodeToPercentEscapeString:headerString]];
    return result;
}

+ (NSString*)signWithString:(NSString*)stringToSign secret:(NSString*)secret {
    return [AIRBHMACSHA1 hmacsha1:stringToSign key:secret];
}

+ (NSString *)encodeToPercentEscapeString:(NSString *)input
{
    NSString *charactersToEscape = @"?!@#$^&%*+,:;='\"`<>()[]{}/\\| ";
    NSCharacterSet *allowedCharacters = [[NSCharacterSet characterSetWithCharactersInString:charactersToEscape] invertedSet];
    return [input stringByAddingPercentEncodingWithAllowedCharacters:allowedCharacters];
}

+ (BOOL) currentDeviceiPad {
    struct utsname systemInfo;
    uname(&systemInfo);
    NSString* systemInfoString =  [NSString stringWithCString:systemInfo.machine encoding:NSUTF8StringEncoding];
    NSUInteger positionOfComma = [systemInfoString rangeOfString:@"iPad"].location;
    if (positionOfComma != NSNotFound) {
        return YES;
    }
    return NO;
}

+ (BOOL) currentDeviceiPhone {
    struct utsname systemInfo;
    uname(&systemInfo);
    NSString* systemInfoString =  [NSString stringWithCString:systemInfo.machine encoding:NSUTF8StringEncoding];
    NSUInteger positionOfComma = [systemInfoString rangeOfString:@"iPhone"].location;
    if (positionOfComma != NSNotFound) {
        return YES;
    }
    return NO;
}

+ (BOOL)isNum:(NSString *)checkedString {
    NSRegularExpression* tNumRegularExpression = [NSRegularExpression regularExpressionWithPattern:@"[0-9]" options:NSRegularExpressionCaseInsensitive error:nil];
    NSUInteger numMatchCount = [tNumRegularExpression numberOfMatchesInString:checkedString options:NSMatchingReportProgress range:NSMakeRange(0, checkedString.length)];
    if (numMatchCount == checkedString.length) {
        return YES;
    }
    return NO;
}

+ (BOOL) isNumOrCharacter:(NSString *)checkedString {
    //数字条件
    NSRegularExpression* tNumRegularExpression = [NSRegularExpression regularExpressionWithPattern:@"[0-9]" options:NSRegularExpressionCaseInsensitive error:nil];
    NSUInteger numMatchCount = [tNumRegularExpression numberOfMatchesInString:checkedString options:NSMatchingReportProgress range:NSMakeRange(0, checkedString.length)];

    //英文字条件
    NSRegularExpression* tLetterRegularExpression = [NSRegularExpression regularExpressionWithPattern:@"[A-Za-z]" options:NSRegularExpressionCaseInsensitive error:nil];
    NSUInteger letterMatchCount = [tLetterRegularExpression numberOfMatchesInString:checkedString options:NSMatchingReportProgress range:NSMakeRange(0, checkedString.length)];
    
    if (numMatchCount + letterMatchCount == checkedString.length) {
        return YES;
    }
    
    return NO;
}

@end
