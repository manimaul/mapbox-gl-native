//
//  WBKResponse.h
//  ios
//
//  Created by William Kamp on 12/20/16.
//

#import <Foundation/Foundation.h>


@protocol WBKResponse

- (void) success:(NSData *)data;
- (void) failure:(NSString *)reason;

@end
