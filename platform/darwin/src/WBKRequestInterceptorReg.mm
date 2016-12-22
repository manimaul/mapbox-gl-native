//
//  WBKRequestInterceptorReg.m
//  ios
//
//  Created by William Kamp on 12/19/16.
//

#import <Foundation/Foundation.h>
#import <mbgl/platform/darwin/WBKRequestInterceptorReg.h>

#include <mbgl/storage/resource.hpp>
#include <mbgl/storage/response.hpp>
#include <mbgl/util/http_header.hpp>
#include <mbgl/util/async_task.hpp>
#include <memory>
#include <mutex>
#include <string>

using namespace mbgl;

class InterceptRequestShared {
public:
    InterceptRequestShared(Response& response_,
                      util::AsyncTask& async_) :
    response(response_),
    async(async_) {}
    
    void notify(const Response& response_) {
        std::lock_guard<std::mutex> lock(mutex);
        if (!cancelled) {
            response = response_;
            async.send();
        }
    }
    
    void cancel() {
        std::lock_guard<std::mutex> lock(mutex);
        cancelled = true;
    }
    
private:
    std::mutex mutex;
    bool cancelled = false;
    
    Response& response;
    util::AsyncTask& async;
};

class InterceptRequest : public AsyncRequest {
public:
    InterceptRequest(FileSource::Callback callback_) :
    shared(std::make_shared<InterceptRequestShared>(response, async)),
    callback(callback_) {}
    
    ~InterceptRequest() override {
        shared->cancel();
    }
    
    std::shared_ptr<InterceptRequestShared> shared;
    
private:
    FileSource::Callback callback;
    Response response;
    
    util::AsyncTask async { [this] {
        // Calling `callback` may result in deleting `this`. Copy data to temporaries first.
        auto callback_ = callback;
        auto response_ = response;
        callback_(response_);
    } };
};

@interface WBKResponseImpl : NSObject <WBKResponse>

@property NSString* url;

@end

@implementation WBKResponseImpl {
    std::unique_ptr<InterceptRequest> request;
    std::shared_ptr<InterceptRequestShared> sharedRequest;
}

-(id) initWithUrl:(std::string const &)url withCallBack:(std::function<void (mbgl::Response)>) callback {
    if (self = [super init]) {
        _url = [NSString stringWithUTF8String:url.c_str()];
        request = std::make_unique<InterceptRequest>(callback);
        sharedRequest = request->shared;
        return self;
    }
    return nil;
}

-(std::unique_ptr<mbgl::AsyncRequest>) asyncRequest {
    return std::move(request);
}

- (void) success:(NSData *)data {
    Response response;
    response.data = std::make_shared<std::string>((const char*)[data bytes], [data length]);
    response.expires = util::now() + Seconds(10);
    response.modified = util::now();
    //response.etag = std::string("some_etag");
    sharedRequest->notify(response);
}

- (void) failure:(NSString *)reason {
    Response response;
    using Error = Response::Error;
    response.error = std::make_unique<Error>(Error::Reason::Server, [reason UTF8String]);
    sharedRequest->notify(response);
}

@end

@interface WBKRequestInterceptorReg()

@property id<WBKInterceptor> interceptor;

+(id) instance;

@end

@implementation WBKRequestInterceptorReg

+(id) instance {
    static WBKRequestInterceptorReg *sharedInstance = nil;
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        sharedInstance = [[self alloc] init];
    });
    return sharedInstance;
}

+(void) setInterceptor:(id<WBKInterceptor>)interceptor {
    [[WBKRequestInterceptorReg instance] setInterceptor:interceptor];
}

+(void) clearInterceptor {
    [[WBKRequestInterceptorReg instance] setInterceptor:nil];
}

+(bool) willHandleUrl:(std::string const &)url {
    id<WBKInterceptor> interceptor = [[WBKRequestInterceptorReg instance] interceptor];
    if (interceptor) {
        if (url.find([[interceptor host] UTF8String]) == 0) {
            return true;
        }
    }
    return false;
}

+(std::unique_ptr<mbgl::AsyncRequest>) handleRequest:(std::string const &)url withCallBack:(std::function<void (mbgl::Response)>) callback {
    id<WBKInterceptor> interceptor = [[WBKRequestInterceptorReg instance] interceptor];
    WBKResponseImpl *callbackWrapper = [[WBKResponseImpl alloc] initWithUrl:url withCallBack:callback];
    [interceptor handleRequest:[NSString stringWithUTF8String:url.c_str()] callback:callbackWrapper];
    return [callbackWrapper asyncRequest];
}

@end
