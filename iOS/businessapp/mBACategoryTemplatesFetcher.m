#import "mBACategoryTemplatesFetcher.h"

#define TEMPLATES_KEY @"templates"


mBACategoryTemplatesFetcher *sharedFetcher;

@implementation mBACategoryTemplatesFetcher

+ (mBACategoryTemplatesFetcher *) sharedFetcher{
  if(sharedFetcher == nil){
    sharedFetcher = [[mBACategoryTemplatesFetcher alloc] init];
  }
  return sharedFetcher;
}

- (void)processResult:(NSDictionary *)parsedObject
{
  NSArray *categoryTemplates = [parsedObject objectForKey:TEMPLATES_KEY];
  [self.delegate categoryTemplatesDidFinishLoading:categoryTemplates];
}

- (void) cancelOperation
{
  [self.delegate categoryTemplatesDidFailLoading];
}

@end
