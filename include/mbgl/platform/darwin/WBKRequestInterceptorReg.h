//
//  WBKRequestInterceptorReg.h
//  ios
//
//  Created by William Kamp on 12/19/16.
//

#import <Foundation/Foundation.h>
#import "WBKRequestInterceptor.h"
#include <mbgl/storage/http_file_source.hpp>

@interface WBKRequestInterceptorReg : NSObject

+(void) setInterceptor:(id<WBKInterceptor>)interceptor;
+(void) clearInterceptor;

+(bool) willHandleUrl:(std::string const &)url;
+(std::unique_ptr<mbgl::AsyncRequest>) handleRequest:(std::string const &)url withCallBack:(std::function<void (mbgl::Response)>) callback;

@end
