//
//  WBKRequestInterceptor.h
//  ios
//
//  Created by William Kamp on 12/19/16.
//

#import <Foundation/Foundation.h>
#import "WBKResponse.h"

@protocol WBKInterceptor <NSObject>

-(NSString *)host;
-(BOOL)handleRequest:(id<WBKResponse>)callback;

@end
