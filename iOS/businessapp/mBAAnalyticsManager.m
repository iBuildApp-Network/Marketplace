#import "mBAAnalyticsManager.h"
#import <Flurry-iOS-SDK/Flurry.h>


#define kMASTER_APP_APP_STARTED       @"MarketPlace App Started"
#define kMASTER_APP_FAVOURITE_PRESSED @"MarketPlace Favourite pressed"
#define kMASTER_APP_FAVOURITE_ADDED @"MarketPlace Favourite added"
#define kMASTER_APP_CATEGORY_OPENED   @"MarketPlace category"
#define kMASTER_APP_APP_OPENED_COUNT  @"MarketPlace opened app count"
#define kMASTER_APP_LAUCHED_FROM_URL  @"MarketPlace App opened from url"

#define kMASTER_APP_SHARING_TWITTER @"MarketPlace sharing twitter"
#define kMASTER_APP_SHARING_TWITTER_ATTEMPT @"MarketPlace sharing twitter attempt"
#define kMASTER_APP_SHARING_TWITTER_RESULT @"MarketPlace sharing twitter result"

#define kMASTER_APP_SHARING_FB @"MarketPlace sharing facebook"
#define kMASTER_APP_SHARING_FB_ATTEMPT @"MarketPlace sharing facebook attempt"
#define kMASTER_APP_SHARING_FB_RESULT @"MarketPlace sharing facebook result"

#define kMASTER_APP_SHARING_EMAIL @"MarketPlace sharing email"
#define kMASTER_APP_SHARING_EMAIL_ATTEMPT @"MarketPlace sharing email attempt"
#define kMASTER_APP_SHARING_EMAIL_RESULT @"MarketPlace sharing email result"

#define kMASTER_APP_SHARING_SMS @"MarketPlace sharing sms"
#define kMASTER_APP_SHARING_SMS_ATTEMPT @"MarketPlace sharing sms attempt"
#define kMASTER_APP_SHARING_SMS_RESULT @"MarketPlace sharing sms result"

#define kMasterAppDefaultsKeyLaunchCounter   @"MasterApp_LaunchCounter"
#define kMasterAppDefaultsKeyFavoritePressed @"MasterApp_FavoritePressed"

#define kMASTER_APP_CATEGORY_SESSION_TIME  @"Marketplace category session time"
#define kMASTER_APP_APPLICATION_SESSION_TIME @"Marketplace app session time"
#define kMASTER_APP_SESSION_APPLICATION_COUNT  @"Marketplace session application count"

#define kMASTER_APP_SESSION_TIME @"session_time"
#define kMASTER_APP_LESS_THEN_MINUTE @"<1"
#define kMASTER_APP_ONE_FIVE_MINUTES @"1-5"
#define kMASTER_APP_FIVE_FIFTEEN_MINUTE @"5-15"
#define kMASTER_APP_FIFTEEN_MINUTE @"15>"

@implementation mBAAnalyticsManager

- (id)init
{
  if (self = [super init])
  {
    _appsLaunchCounter = 0;
    _favoritePressed = NO;
    
    NSUserDefaults *uDefaults = [NSUserDefaults standardUserDefaults];
    
    NSNumber *favoritePressedNumber = [uDefaults objectForKey:@"MasterApp_FavoritePressed"];
    
    if (favoritePressedNumber && [favoritePressedNumber isKindOfClass:[NSNumber class]])
      _favoritePressed = [favoritePressedNumber boolValue];
  }
  
  return self;
}

- (void)logMasterAppLaunching
{
  NSLog(@"logAppLaunching");
  
  [Flurry logEvent:kMASTER_APP_APP_STARTED
    withParameters:nil
             timed:NO];
  
  NSUserDefaults *uDefaults = [NSUserDefaults standardUserDefaults];
  
  NSNumber *numberLaunchCounter = [uDefaults objectForKey:kMasterAppDefaultsKeyLaunchCounter];
  
  if (numberLaunchCounter && [numberLaunchCounter isKindOfClass:[NSNumber class]])
    [Flurry logEvent:kMASTER_APP_APP_OPENED_COUNT
      withParameters:@{ @"count":numberLaunchCounter }
               timed:NO];
  
  [uDefaults setObject:[NSNumber numberWithInt:0] forKey:kMasterAppDefaultsKeyLaunchCounter];
    
    
}

- (void)logCategoryOpened:(NSString *)categoryName
{
  if (!categoryName)
    return;
  
  NSLog(@"logCategoryOpened");
  
  [Flurry logEvent:kMASTER_APP_CATEGORY_OPENED
    withParameters:@{ @"category":categoryName }
             timed:NO];
}


- (void)logAppFavoritesPressed
{
  NSLog(@"logAppFavoritesPressed");

  if (_favoritePressed)
    return;
  
  [Flurry logEvent:kMASTER_APP_APP_STARTED
    withParameters:nil
             timed:NO];
  
  _favoritePressed = YES;
  NSUserDefaults *uDefaults = [NSUserDefaults standardUserDefaults];
  [uDefaults setObject:[NSNumber numberWithInt:1] forKey:kMasterAppDefaultsKeyFavoritePressed];
}


- (void)increaseAppsLaunchCounter
{
  _appsLaunchCounter++;
  NSUserDefaults *uDefaults = [NSUserDefaults standardUserDefaults];
  [uDefaults setObject:[NSNumber numberWithInt:_appsLaunchCounter] forKey:kMasterAppDefaultsKeyLaunchCounter];
}

#pragma mark - Sharing logs

- (void)logAppSharingFBAttempt
{
  NSLog(@"logAppSharingFBAttempt");
  
  [Flurry logEvent:kMASTER_APP_SHARING_FB_ATTEMPT
    withParameters:nil
             timed:NO];
}

- (void)logAppSharingTwitterAttempt
{
  NSLog(@"logAppSharingTwitterAttempt");
  
  [Flurry logEvent:kMASTER_APP_SHARING_TWITTER_ATTEMPT
    withParameters:nil
             timed:NO];
}

- (void)logAppSharingEmailAttempt
{
  NSLog(@"logAppSharingEmailAttempt");
  
  [Flurry logEvent:kMASTER_APP_SHARING_EMAIL_ATTEMPT
    withParameters:nil
             timed:NO];
}


- (void)logAppSharingSmsAttempt
{
  NSLog(@"logAppSharingSMSAttempt");
  
  [Flurry logEvent:kMASTER_APP_SHARING_SMS_ATTEMPT
    withParameters:nil
             timed:NO];
}

- (void)logAppSharingFBResult:(BOOL)succeeded
{
  NSString *resultValue = (succeeded ? @"ok" : @"cancel");
  NSLog(@"sharing fb result %@", resultValue);
  [Flurry logEvent:kMASTER_APP_SHARING_FB_RESULT
    withParameters:@{ @"result_facebook":resultValue }
             timed:NO];
}

- (void)logAppSharingTwitterResult:(BOOL)succeeded
{
  NSString *resultValue = (succeeded ? @"ok" : @"cancel");
  NSLog(@"sharing twitter result %@", resultValue);
  [Flurry logEvent:kMASTER_APP_SHARING_TWITTER_RESULT
    withParameters:@{ @"result_twitter":resultValue }
             timed:NO];
}

- (void)logAppSharingEmailResult:(BOOL)succeeded
{
  NSString *resultValue = (succeeded ? @"ok" : @"cancel");
  NSLog(@"sharing email result %@", resultValue);
  [Flurry logEvent:kMASTER_APP_SHARING_EMAIL_RESULT
    withParameters:@{ @"result_email":resultValue }
             timed:NO];
}

- (void)logAppSharingSmsResult:(BOOL)succeeded
{
  NSString *resultValue = (succeeded ? @"ok" : @"cancel");
  NSLog(@"sharing sms result %@", resultValue);
  [Flurry logEvent:kMASTER_APP_SHARING_SMS_RESULT
    withParameters:@{ @"result_sms":resultValue }
             timed:NO];
}

- (void)logFavouritesAdded
{
  
  NSLog(@"logFavouriteAdded");
  
  [Flurry logEvent:kMASTER_APP_FAVOURITE_ADDED
    withParameters:nil
             timed:NO];
}

- (void)logCategorySessionTime:(NSTimeInterval)time andAppsCount:(int)apps
{
  
  NSLog(@"logCategoryTime"); 

  [Flurry logEvent:kMASTER_APP_CATEGORY_SESSION_TIME
    withParameters:@{ @"session_time": @"<1"}
             timed:NO];
}

- (void)logAppSessionTime:(NSTimeInterval)time
{
  
}

- (void)logLaunchFromUrlWithApp:(NSInteger) appId
{
  [Flurry logEvent:kMASTER_APP_LAUCHED_FROM_URL
    withParameters:@{ @"app_id" : @(appId) }
             timed:NO];
}

- (void)logSharingByTwitterWithAppId:(NSInteger) appId
{
  [Flurry logEvent:kMASTER_APP_SHARING_TWITTER
    withParameters:@{ @"app_id" : @(appId) }
             timed:NO];
}

- (void)logSharingByFacebookWithAppId:(NSInteger) appId
{
  [Flurry logEvent:kMASTER_APP_SHARING_FB
    withParameters:@{ @"app_id" : @(appId) }
             timed:NO];
}

- (void)logSharingByEmailWithAppId:(NSInteger) appId
{
  [Flurry logEvent:kMASTER_APP_SHARING_EMAIL
    withParameters:@{ @"app_id" : @(appId) }
             timed:NO];
}

- (void)logSharingBySmsWithAppId:(NSInteger) appId
{
  [Flurry logEvent:kMASTER_APP_SHARING_SMS
    withParameters:@{ @"app_id" : @(appId) }
             timed:NO];
}

@end
