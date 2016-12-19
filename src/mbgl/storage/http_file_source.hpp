#pragma once

#include <mbgl/storage/file_source.hpp>

namespace mbgl {
    
/**
 * Protocol for intercepting requests.
 */
class HttpInterceptor {
public:
    
    /**
     * Asks you to handle a request for a url. If you return true, then be sure to invoke
     * callback.
     */
    std::unique_ptr<AsyncRequest> handleRequest(std::string const &url, std::function<void (mbgl::Response)> callback);
    
    /**
     * The host name you would like to handle requests for.
     */
    std::string const &host();
};


class HttpIntercepReg {
friend class HTTPFileSource;
   
private: // Constructor
    HttpIntercepReg() {};
    
public: // Constructor
    static HttpIntercepReg& sharedInstance() {
        static HttpIntercepReg shared;
        return shared;
    }
    HttpIntercepReg(HttpIntercepReg const &other) = delete;
    HttpIntercepReg(HttpIntercepReg const &&other) = delete;
    void operator = (HttpIntercepReg const &other) = delete;
    
public: // Methods
    void setIntecepter(std::unique_ptr<HttpInterceptor> interceptor);
    void clearInterceptor();

    
private: // Methods
    bool willHandleRequest(std::string const &url);
    std::unique_ptr<AsyncRequest> handleRequest(std::string const &url, std::function<void (mbgl::Response)> callback);
    std::unique_ptr<HttpInterceptor> interceptor;
};


class HTTPFileSource : public FileSource {
public:
    HTTPFileSource();
    ~HTTPFileSource() override;

    std::unique_ptr<AsyncRequest> request(const Resource&, Callback) override;

    static uint32_t maximumConcurrentRequests();

    class Impl;

private:
    std::unique_ptr<Impl> impl;
};

} // namespace mbgl
