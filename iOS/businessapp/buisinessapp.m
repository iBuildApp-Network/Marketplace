
#import "buisinessapp.h"
#import "mBAApplicationListPresenter.h"
#import "mBASideBarView.h"


@implementation BuisinessApp

static mBADBService *databaseService = nil;
static mBAAnalyticsManager *analyticsManager = nil;
static RestService *restService = nil;
static mBAApplicationTableService *applicationTableService = nil;
static mBACategoryTableService *categoryTableService = nil;
static mBAApplicationListPresenter * _applicationListPresenter = nil;
static mBASideBarVC *_mainMenuSlidablePresenter = nil;
static mBAAboutUsViewController *aboutUsController = nil;
static mBAMainMenuPresenter *mainMenuController = nil;
static UIViewController * _lastPresenter = nil;
static BOOL fromMainMenu = NO;

+ (mBADBService *)database
{
  if (databaseService != nil)
    return databaseService;
  databaseService = [[mBADBService alloc] init];
  return databaseService;
}

+ (mBAAnalyticsManager *) analyticsManager
{
  if (analyticsManager != nil)
    return analyticsManager;
  analyticsManager = [[mBAAnalyticsManager alloc] init];
  return analyticsManager;
}

+ (RestService *)rest
{
  if (restService != nil)
    return restService;
  restService = [[RestService alloc] init];
  return restService;
}

+ (mBAApplicationTableService *)applicationTable
{
  if (applicationTableService != nil)
    return applicationTableService;
  applicationTableService = [[mBAApplicationTableService alloc] init];
  return applicationTableService;
}

+ (mBACategoryTableService *)categoryTable
{
  if (categoryTableService != nil)
    return categoryTableService;
  categoryTableService = [[mBACategoryTableService alloc] init];
  return categoryTableService;
}

+ (mBAApplicationListPresenter *) applicationListPresenter
{
  if (_applicationListPresenter != nil) {
    _lastPresenter = _applicationListPresenter;
    return _applicationListPresenter;
  }
  _applicationListPresenter = [[mBAApplicationListPresenter alloc] initWithNibName: nil bundle: nil];
  NSLog(@"ba applist presenter %@", _applicationListPresenter.title);
  _lastPresenter = _applicationListPresenter;
  return _applicationListPresenter;
}

+ (mBASideBarVC *) mainMenuPresenter
{
  if (_mainMenuSlidablePresenter != nil) {
    _lastPresenter = _mainMenuSlidablePresenter;
    return _mainMenuSlidablePresenter;
  }
  
  if(mainMenuController == nil){
    mainMenuController = [[mBAMainMenuPresenter alloc] init];
  }
  
    UIView *mainView = mainMenuController.view;
    
    mBASideBarView *hiddenView = [[mBASideBarView alloc] initWithFrame:
                          (CGRect){0.0f, 0.0f, mainView.bounds.size.width, mainView.bounds.size.height}];
    
    _mainMenuSlidablePresenter = [[mBASideBarVC alloc] initWithMainViewController:mainView hiddenViewController:hiddenView];
    
    mainMenuController.sideBarController = _mainMenuSlidablePresenter;
    hiddenView.sideBarDelegate = mainMenuController;
  NSLog(@"ba mainmenu presenter %@", _mainMenuSlidablePresenter.title);
  _lastPresenter = _mainMenuSlidablePresenter;
  return _mainMenuSlidablePresenter;
}

+ (mBAAboutUsViewController *)aboutUsController
{
  if (aboutUsController != nil){
    return aboutUsController;
  }
  
  if(mainMenuController == nil){
    mainMenuController = [[mBAMainMenuPresenter alloc] init];
  }
  
  aboutUsController = [[mBAAboutUsViewController alloc] initWithPresenter:mainMenuController];
  NSLog(@"ba about presenter %@", _mainMenuSlidablePresenter.title);
  _lastPresenter = _mainMenuSlidablePresenter;
  return aboutUsController;
}

+ (mBAMainMenuPresenter *) lastPresenter
{
  NSLog(@"ba last presenter %@", _lastPresenter.title);
  if (fromMainMenu) {
    fromMainMenu = NO;
    return (mBAMainMenuPresenter *) _mainMenuSlidablePresenter;
  }
  return (mBAMainMenuPresenter *)_lastPresenter;
}

+ (mBAMainMenuPresenter *) mainMenuController
{
  return mainMenuController;
}

+ (void) fromMainMenu:(BOOL)result
{
  fromMainMenu = result;
}

+ (int) getFromMainMenu
{
  if (fromMainMenu) {
    return 1;
  }
  return 0;
}


@end

