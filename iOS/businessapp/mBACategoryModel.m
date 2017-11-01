// IBAHeader

#import "mBACategoryModel.h"

@implementation mBACategoryModel

@synthesize
identifier,
title, order,
pictureUrl, enable;

- init
{
  self = [super init];
  if (self) {
    self.identifier = 0;
    self.title = nil;
    self.order = 0;
    self.pictureUrl = nil;
    self.enable = true; // categories enable by default
  }
  return self;
}

- (void)dealloc
{
    title = nil;
    pictureUrl= nil;
}

@end
