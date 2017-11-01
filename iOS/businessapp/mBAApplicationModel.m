
#import "mBAApplicationModel.h"
#import "NSString+colorizer.h"

@implementation mBAApplicationModel

@synthesize
app_id, category_id,
token, title, pictureUrl, placeholderColor, placeholderColorString;

- init
{
  self = [super init];
  if (self) {
    self.app_id = 0;
    self.category_id = 0;
    self.token = nil;
    self.title = nil;
    self.pictureUrl = nil;
    self.placeholderColor = kDefaultPlaceholderColor;
    self.placeholderColorString = @"#AAAA00";
  }
  return self;
}

- (void)dealloc
{
  token = nil;
  title = nil;
  pictureUrl = nil;
  placeholderColor = nil;
}

@end
