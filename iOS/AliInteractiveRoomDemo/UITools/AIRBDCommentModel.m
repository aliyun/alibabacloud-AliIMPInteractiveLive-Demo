//
//  AIRBDCommentModel.m
//  AliInteractiveLiveDemo
//
//  Created by 麦辣 on 2021/6/8.
//

#import "AIRBDCommentModel.h"

@implementation AIRBDCommentModel

- (instancetype)init
{
    self = [super init];
    if (self) {
        
    }
    return self;
}

- (void)setContent:(NSString *)string{
    //去除空格
    string = [string stringByTrimmingCharactersInSet:[NSCharacterSet whitespaceCharacterSet]];
    
    //如果含有: 执行如下逻辑
    if([string containsString:@":"]){
        //截取sender
        _senderName = [string componentsSeparatedByString:@":"][0];
        //截取content
        _content = [string substringFromIndex:([_senderName length]+1)];
    }else if([string containsString:@"："]){
        //截取sender
        _senderName = [string componentsSeparatedByString:@"："][0];
        //截取content
        _content = [string substringFromIndex:([_senderName length]+1)];
    }else{
        _content = string;
    }
    
}

- (UIColor *)color{
    if(_senderName!=nil){
        NSUInteger senderHash = [_senderName hash];
        CGFloat hue = ( senderHash% 256 / 256.0 );
        CGFloat saturation = ( senderHash% 128 / 256.0 ) + 0.5;
        CGFloat brightness = ( senderHash% 128 / 256.0 ) + 0.5;
        _color = [UIColor colorWithHue:hue saturation:saturation brightness:brightness alpha:1];;
    }else{
        CGFloat hue = ( arc4random() % 256 / 256.0 );
        CGFloat saturation = ( arc4random() % 128 / 256.0 ) + 0.5;
        CGFloat brightness = ( arc4random() % 128 / 256.0 ) + 0.5;
        _color = [UIColor colorWithHue:hue saturation:saturation brightness:brightness alpha:1];;
    }
    return _color;
}

- (NSString *)description
{
    if(!_senderName){
        return [NSString stringWithFormat:@"%@", _content];
    }else if(!_content){
        return @"";
    }else{
        return [NSString stringWithFormat:@"%@: %@",_senderName, _content];
    }
}
@end
