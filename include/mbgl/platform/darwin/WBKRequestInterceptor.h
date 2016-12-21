//
//  WBKRequestInterceptor.h
//  ios
//
//  Created by William Kamp on 12/19/16.
//

#import <Foundation/Foundation.h>
#import "WBKResponse.h"

@protocol WBKInterceptor

-(NSString *)host;
-(void)handleRequest:(NSString*)url callback:(id<WBKResponse>)cb;

@end
