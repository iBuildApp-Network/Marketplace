// IBAHeader

#import "RestCategoryFetcher.h"
#import "mBACategoryModel.h"
#import "buisinessapp.h"


@implementation RestCategoryFetcher

- (void)processResult:(NSDictionary *)parsedObject
{
  NSMutableArray *result = [[NSMutableArray alloc] init];

  NSArray *categories = [parsedObject valueForKey: @"categories"];
  for (int i = 0; i < categories.count; i++) {
    NSDictionary *categoryData = categories[i];
    mBACategoryModel *categoryModel = [self mapCategory: categoryData];
    [result addObject: categoryModel];
  }
  [BuisinessApp rest].categories = categories;
  [self.delegate CategoryDataReceiver:result];
}

- (mBACategoryModel *)mapCategory:(NSDictionary *)categoryData
{
  mBACategoryModel * result = [[mBACategoryModel alloc] init];
  result.enable = [[categoryData valueForKey: @"enable"] integerValue];
  result.identifier = [[categoryData valueForKey: @"id"] integerValue];
  result.order = [[categoryData valueForKey: @"order"] integerValue];
  result.pictureUrl = [categoryData valueForKey: @"pictureUrl"];
  result.title = [categoryData valueForKey: @"title"];
  return result;
}

- (void) cancelOperation
{
  [self.delegate cancelRestOperation];
}

@end
