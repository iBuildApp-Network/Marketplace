
#import <Foundation/Foundation.h>
#import "RestFetcher.h"
#import "RestServiceDelegate.h"

#define CATEGORY_TEMPLATES_FILENAME @"CategoryTemplates.plist"

@interface mBACategoryTemplatesFetcher : RestFetcher
+ (mBACategoryTemplatesFetcher *) sharedFetcher;
@end
