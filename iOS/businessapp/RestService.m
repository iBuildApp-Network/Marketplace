// IBAHeader

#import "RestService.h"
#import "RestRatingFetcher.h"
#import "RestCategoryFetcher.h"
#import "RestFeaturedFetcher.h"
#import "RestApplicationListFetcher.h"
#import "RestApplcationDataFetcher.h"
#import "functionLibrary.h"
#import "appconfig.h"
#import "mBACategoryTemplatesFetcher.h"

#define qCategoryList [NSURL URLWithString:[[@"http://" stringByAppendingString:appIBuildAppHostName()] stringByAppendingString: @"/masterapp/category_list"]]
#define qFeaturedApplications [NSURL URLWithString:[[@"http://" stringByAppendingString:appIBuildAppHostName()] stringByAppendingString: @"/masterapp/featured_apps_list"]]
#define qApplicationIDsBase [[@"http://" stringByAppendingString:appIBuildAppHostName()] stringByAppendingString: @"/masterapp/sorted_app_list?category_id=%d"]
#define qSearch [[@"http://" stringByAppendingString:appIBuildAppHostName()] stringByAppendingString: @"/masterapp/find?search=%@"]
#define qSearchInCategory [[@"http://" stringByAppendingString:appIBuildAppHostName()] stringByAppendingString: @"/masterapp/find?category_id=%d&search=%@"]
#define qApplcationData [NSURL URLWithString:[[@"http://" stringByAppendingString:appIBuildAppHostName()] stringByAppendingString: @"/masterapp/app_list"]]
#define qRateApplicationUrlString [[@"http://" stringByAppendingString:appIBuildAppHostName()] stringByAppendingString: @"/masterapp/rate_app"]
#define qRateApplicationPostDataString @"appid=%d&uuid=%@&rate=%d"

#define qCategoryTemplatesUrl [NSURL URLWithString:[[@"http://" stringByAppendingString:appIBuildAppHostName()] stringByAppendingString: @"/masterapp/category_template"]]

@implementation RestService{
}

@synthesize delegate;

- init
{
  self = [super init];
  if (self) {
  }
  return self;
}

- (void)fetchCategories
{
  NSMutableURLRequest *theRequest = [NSMutableURLRequest requestWithURL: qCategoryList];
  
  RestCategoryFetcher *fetcher = [[RestCategoryFetcher alloc] init];
  fetcher.request = theRequest;
  fetcher.delegate = self.delegate;
  
  [fetcher start];
}

- (void)fetchFeaturedApplications
{
  NSMutableURLRequest *theRequest = [NSMutableURLRequest requestWithURL: qFeaturedApplications];
  
  RestFeaturedFetcher *fetcher = [[RestFeaturedFetcher alloc] init];
  fetcher.request = theRequest;
  fetcher.delegate = self.delegate;
  
  [fetcher start];
}

- (void)fetchApplicationIDs:(NSInteger)categoryId
{
  NSURL *url = [NSURL URLWithString: [NSString stringWithFormat: qApplicationIDsBase, categoryId]];
  NSMutableURLRequest *theRequest = [NSMutableURLRequest requestWithURL: url];
  [theRequest setHTTPMethod:@"POST"];

  RestApplicationListFetcher *fetcher = [[RestApplicationListFetcher alloc] init];
  fetcher.request = theRequest;
  fetcher.delegate = self.delegate;

  [fetcher start];
}

- (void)fetchApplicationIDsBySearchQuery:(NSString *)searchQuery
{
  NSString* qSearchUrl = [NSString stringWithFormat: qSearch, [searchQuery stringByAddingPercentEscapesUsingEncoding:NSUTF8StringEncoding]];
  NSURL *url = [NSURL URLWithString: qSearchUrl];
  NSMutableURLRequest *theRequest = [NSMutableURLRequest requestWithURL: url];

  RestApplicationListFetcher *fetcher = [[RestApplicationListFetcher alloc] init];
  fetcher.request = theRequest;
  fetcher.delegate = self.delegate;

  [fetcher start];
}

- (void)fetchApplicationIDs:(NSInteger)categoryId searchQuery:(NSString *)searchQuery
{
  NSString* qSearchUrl = [NSString stringWithFormat: qSearchInCategory, categoryId, [searchQuery stringByAddingPercentEscapesUsingEncoding:NSUTF8StringEncoding]];
  NSURL *url = [NSURL URLWithString: qSearchUrl];
  NSMutableURLRequest *theRequest = [NSMutableURLRequest requestWithURL: url];

  RestApplicationListFetcher *fetcher = [[RestApplicationListFetcher alloc] init];
  fetcher.request = theRequest;
  fetcher.delegate = self.delegate;

  [fetcher start];
}

- (void)fetchApplicationData:(NSArray *)appIds
{
  NSMutableArray *appIdStrings = [[NSMutableArray alloc] init];

  for(int i = 0; i < appIds.count; i++)
  {
    NSObject *appId = appIds[i];
    
    if ([appId isKindOfClass:[NSNumber class]])
      [appIdStrings addObject: [appIds[i] stringValue]];
    else if ([appId isKindOfClass:[NSString class]])
      [appIdStrings addObject:appIds[i]];
    else
      NSLog(@"incorrect type for appId");
      
  }

  NSMutableURLRequest *theRequest = [NSMutableURLRequest requestWithURL: qApplcationData];
  NSData* jsonData = (NSData*)[NSJSONSerialization dataWithJSONObject: appIdStrings options: NSJSONWritingPrettyPrinted error: nil];
  NSString *strData = [[NSString alloc] initWithData:jsonData encoding: NSUTF8StringEncoding];
  strData = [@"appid=" stringByAppendingString:strData];

  NSData *postData = [strData dataUsingEncoding:NSUTF8StringEncoding];

  [theRequest setHTTPMethod: @"POST"];
  [theRequest setValue: [@(postData.length) stringValue]      forHTTPHeaderField: @"Content-Length"];
  [theRequest setValue:  @"application/x-www-form-urlencoded" forHTTPHeaderField: @"Content-Type"];
  [theRequest setHTTPBody: postData];

  RestApplcationDataFetcher *fetcher = [[RestApplcationDataFetcher alloc] init];
  fetcher.request = theRequest;
  fetcher.delegate = self.delegate;

  [fetcher start];
}

- (void)performRatingChangeForAppWithId:(NSInteger)appId uuid:(NSString*)uuid andStatus:(FavouritedStatus) status{
  
  int statusAsInt;
  
  switch(status){
    case rateUp:
      statusAsInt = 1;
      break;
    case rateDown:
      statusAsInt = 0;
      break;
    default:
      return;
  }
  
  NSURL *url = [NSURL URLWithString: qRateApplicationUrlString];
  
  NSMutableURLRequest *ratingChangeRequest = [NSMutableURLRequest requestWithURL: url];
  
  NSString *postDataString = [NSString stringWithFormat:qRateApplicationPostDataString, appId, uuid, statusAsInt];
  
  NSData *postData = [postDataString dataUsingEncoding:NSASCIIStringEncoding allowLossyConversion:YES];
  NSString *postDataLength = [NSString stringWithFormat:@"%d",[postDataString length]];
  
  [ratingChangeRequest setHTTPMethod:@"POST"];
  [ratingChangeRequest setValue:@"application/x-www-form-urlencoded" forHTTPHeaderField:@"Content-Type"];
  [ratingChangeRequest setValue:postDataLength forHTTPHeaderField: @"Content-Length"];
  [ratingChangeRequest setHTTPBody:postData];
  
  
  RestRatingFetcher *fetcher = [[RestRatingFetcher alloc] init];
  fetcher.request = ratingChangeRequest;
  fetcher.delegate = self.delegate;
  
  [fetcher start];
}

- (void)fetchCategoryTemplates{
  NSMutableURLRequest *theRequest = [NSMutableURLRequest requestWithURL: qCategoryTemplatesUrl];

  mBACategoryTemplatesFetcher *fetcher = [mBACategoryTemplatesFetcher sharedFetcher];
  fetcher.request = theRequest;
  fetcher.delegate = self.delegate; 
  
  [fetcher start];
}

@end
