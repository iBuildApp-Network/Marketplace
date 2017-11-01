// IBAHeader

#import "mBASettings.h"


@implementation mBASettings

@synthesize currentAppId, currentAppToken;

static mBASettings *settingsSingleton = nil;

+(mBASettings *)sharedInstance
{
  @synchronized(self)
  {
    if (settingsSingleton == nil)
    {
      settingsSingleton = [[mBASettings alloc] init];
    }
  }
  return settingsSingleton;
}

- (id) copyWithZone:(NSZone*)zone
{
  return self;
}

@end
