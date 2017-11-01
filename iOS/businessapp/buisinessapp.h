// IBAHeader

#import <Foundation/Foundation.h>
#import "mBADBService.h"
#import "RestService.h"
#import "mBAApplicationTableService.h"
#import "mBACategoryTableService.h"
#import "mBAApplicationListPresenter.h"
#import "mBAMainMenuPresenter.h"
#import "mBASideBarVC.h"
#import "mBAAboutUsViewController.h"
#import "mBAAnalyticsManager.h"

#define MASTERAPP_STATISTICS

/**
* Catalog of services and application screens
*/
@interface BuisinessApp : NSObject

/**
* Database creation service
*/
+ (mBADBService *) database;

/**
 * Manager for Flurry analytics by MasterApp
 */
+ (mBAAnalyticsManager *) analyticsManager;

/**
* Service access to RestApi
*/
+ (RestService *) rest;

/**
* Service with the application table
*/
+ (mBAApplicationTableService *) applicationTable;

/**
* Table service
*/
+ (mBACategoryTableService *) categoryTable;

+ (mBAApplicationListPresenter *) applicationListPresenter;
+ (mBASideBarVC *) mainMenuPresenter;

+ (mBAAboutUsViewController *)aboutUsController;

+ (mBAMainMenuPresenter *) mainMenuController;
/**
* The last open screen before starting the application
*/
+ (mBAMainMenuPresenter *) lastPresenter;

+ (void) fromMainMenu:(BOOL)result ;

+ (int) getFromMainMenu;

@end
