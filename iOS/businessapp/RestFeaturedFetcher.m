// IBAHeader

#import "RestFeaturedFetcher.h"
#import "mBAApplicationModel.h"

@implementation RestFeaturedFetcher

- (Boolean)appExists:(mBAApplicationModel *)application in:(NSArray *)applications
{
  for (int i = 0; i < applications.count; i++)
  {
    mBAApplicationModel *app = applications[i];
    if (app.app_id == application.app_id)
      return YES;
  }
  return NO;
}

- (void)processResult:(NSDictionary *)parsedObject
{
  NSMutableArray *result = [[NSMutableArray alloc] init];

  NSArray *applications = [parsedObject valueForKey: @"apps"];
  for (int i = 0; i < applications.count; i++) {
    NSDictionary *applicationData = applications[i];
    mBAApplicationModel *applicationModel = [self mapApplication: applicationData];

    if (![self appExists: applicationModel in: result]) {
      [result addObject: applicationModel];
    }
  }
  [self.delegate FeaturedApplications: result];
}

- (mBAApplicationModel *)mapApplication:(NSDictionary *)applicationData
{
  mBAApplicationModel * result = [[mBAApplicationModel alloc] init];
  result.app_id = [[applicationData valueForKey: @"appid"] integerValue];
  result.category_id = [[applicationData valueForKey: @"categoryid"] integerValue];
  result.pictureUrl = [applicationData valueForKey: @"pictureUrl"];
  result.title = [applicationData valueForKey: @"title"];
  result.token = [applicationData valueForKey: @"token"];
  return result;
}

@end
