// IBAHeader

#import <Foundation/Foundation.h>
#import "RestServiceDelegate.h"

@interface RestFetcher : NSObject

@property (nonatomic, assign) NSURLRequest *request;
@property (nonatomic, retain) id<RestServiceDelegate> delegate;

- (void)start;
- (void)processResult: (NSDictionary *)parsedObject;
- (void)cancelOperation;

@end
