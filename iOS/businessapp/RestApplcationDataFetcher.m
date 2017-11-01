// IBAHeader

#import "RestApplcationDataFetcher.h"
#import "mBAApplicationModel.h"
#import "NSString+colorizer.h"

@implementation RestApplcationDataFetcher {
}

- (void)processResult:(NSDictionary *)parsedObject
{
  NSMutableArray *result = [[NSMutableArray alloc] init];

  NSArray *categories = parsedObject[@"apps"];
  for (int i = 0; i < categories.count; i++) {
    NSDictionary *applicationData = categories[i];
    mBAApplicationModel *applicatonModel = [self mapApplication: applicationData];
    [result addObject: applicatonModel];
  }
  if (self.delegate !=nil) {
    [self.delegate appDataLoaded:result];
  }
}

- (mBAApplicationModel *)mapApplication:(NSDictionary *)applicationData
{
  mBAApplicationModel * result = [[mBAApplicationModel alloc] init];
  result.app_id = [applicationData[@"appid"] integerValue];
  result.category_id = [applicationData[@"categoryid"] integerValue];
  result.pictureUrl = applicationData[@"pictureUrl"];
  result.title = applicationData[@"title"];
  result.token = applicationData[@"token"];
  result.placeholderColor = [(NSString *) applicationData[@"background"] asColor];
  result.placeholderColorString = applicationData[@"background"];
  NSLog(@"placeholder background %@", applicationData[@"background"]);
  return result;
}

- (void) cancelOperation
{
  [self.delegate cancelRestOperation];
}

@end
