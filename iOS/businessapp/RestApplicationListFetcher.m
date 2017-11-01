// IBAHeader

#import "RestApplicationListFetcher.h"

@implementation RestApplicationListFetcher {

}

- (void)processResult:(NSDictionary *)parsedObject
{

  NSMutableArray *result = [[NSMutableArray alloc] init];
  NSArray *appIDs = [parsedObject valueForKey: @"apps"];
  for (int i = 0; i < appIDs.count; i++) {
    NSString* itemStr = appIDs[i];
    [result addObject: @([itemStr integerValue])];
  }
  [self.delegate appIdsLoaded:result withStrRepresentation:[appIDs componentsJoinedByString:@" "]];
}

- (void) cancelOperation
{
  [self.delegate cancelRestOperation];
}

@end
