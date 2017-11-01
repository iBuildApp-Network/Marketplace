
#import "RestRatingFetcher.h"

#define STATUS_KEY @"status"

@implementation RestRatingFetcher

- (void)processResult:(NSDictionary *)parsedObject
{
  NSString *status = [parsedObject valueForKey:STATUS_KEY];
  NSLog(@"Rating change result: %@", status);
}

@end
