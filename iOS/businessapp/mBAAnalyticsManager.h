
#import <Foundation/Foundation.h>

@interface mBAAnalyticsManager : NSObject

@property (nonatomic, assign, readonly) NSInteger appsLaunchCounter;
@property (nonatomic, assign, readonly) BOOL favoritePressed;

- (void)logMasterAppLaunching;
- (void)logCategoryOpened:(NSString *)categoryName;
- (void)logAppFavoritesPressed;
- (void)increaseAppsLaunchCounter;
- (void)logLaunchFromUrlWithApp:(NSInteger) appId;

- (void)logAppSharingFBAttempt;
- (void)logAppSharingTwitterAttempt;
- (void)logAppSharingEmailAttempt;
- (void)logAppSharingSmsAttempt;

- (void)logAppSharingFBResult:(BOOL)succeeded;
- (void)logAppSharingTwitterResult:(BOOL)succeeded;
- (void)logAppSharingEmailResult:(BOOL)succeeded;
- (void)logAppSharingSmsResult:(BOOL)succeeded;

- (void)logSharingByTwitterWithAppId:(NSInteger) appId;
- (void)logSharingByFacebookWithAppId:(NSInteger) appId;
- (void)logSharingByEmailWithAppId:(NSInteger) appId;
- (void)logSharingBySmsWithAppId:(NSInteger) appId;


- (void)logFavouritesAdded;
- (void)logCategorySessionTime:(NSTimeInterval)time andAppsCount:(int)apps;

@end
