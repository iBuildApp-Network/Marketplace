// IBAHeader

#import "mBAApplicationListPresenter.h"
#import "mBAApplicationListView.h"
#import "mBAMainMenuPresenter.h"
#import "buisinessapp.h"
#import "mBAApplicationModel.h"
#import "mBACategoryModel.h"
#import "iphmainviewcontroller.h"

#import "reachability.h"
#import "appdelegate.h"
#import "SDImageCache.h"
#import <SDWebImage/SDWebImageManager.h>

#import <MBProgressHUD/MBProgressHUD.h>

#import "FXBlurView.h"

@interface mBAApplicationListPresenter()

@property (nonatomic, retain) NSArray *applicatonIds;
@property (nonatomic, retain) NSArray *allApplicationIds;
@property (nonatomic, retain) FXBlurView *blurView;
@property (nonatomic, retain) UIImageView *fakeImageView;
@property (nonatomic, retain) UIView *titleSeparatorView;
@property (nonatomic, retain) NSDate *startTime;

@end

@implementation mBAApplicationListPresenter {
  mBAApplicationListView *viewBuilder;
  NSInteger fetchedApplcation;
  NSInteger oldCategoryId;
  int currentOffset;
  int maxIndex;
  BOOL firstStart;
  BOOL fromSearch;
  long lastCount;
}

@synthesize category, favouritesMode, searchMode;

#pragma mark -
- (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil
{
  self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil];
  if (self) {
    viewBuilder = [[mBAApplicationListView alloc] init];
    fetchedApplcation = 0;
    oldCategoryId = -1;
    searchMode = NO;
    currentOffset = 19;
    maxIndex = 4;
    firstStart = YES;
    _startTime = [NSDate date];
    fromSearch = NO;
    lastCount = 0;
  }
  return self;
}

- (void)dealloc
{
  self.applicatonIds = nil;
  self.allApplicationIds = nil;
}

- (BOOL)checkNetworkStatusWithAlert:(BOOL)showAlert
{
  NetworkStatus internetStatus = [((TAppDelegate *)[[UIApplication sharedApplication] delegate])
                                  .internetReachable currentReachabilityStatus];
  NetworkStatus hostStatus     = [((TAppDelegate *)[[UIApplication sharedApplication] delegate])
                                  .hostReachable currentReachabilityStatus];
  
  BOOL reacheable = (internetStatus != NotReachable && hostStatus != NotReachable);
  
  if (!reacheable && showAlert)
  {
    
    UIAlertView *msg = [[UIAlertView alloc] initWithTitle:NSLocalizedString(@"general_cellularDataTurnedOff",@"Cellular Data is Turned off")
                                                   message:NSLocalizedString(@"general_cellularDataTurnOnMessage",@"Turn on cellular data or use Wi-Fi to access data")
                                                  delegate:nil
                                         cancelButtonTitle:NSLocalizedString(@"general_defaultButtonTitleOK",@"OK")
                                         otherButtonTitles:nil];
    [msg show];
    
    return NO;
  }
  else
    return YES;
}

#pragma mark - View Lifecycle
-(void)viewWillAppear:(BOOL)animated
{
  fromSearch = NO;
  NSLog(@"mBAApplicationListPresenter viewWillAppear");
  [super viewWillAppear: animated];
  [[UIApplication sharedApplication] setStatusBarStyle:UIStatusBarStyleLightContent];
  
  _startTime = [NSDate date];
  
#ifdef SDWEBIMAGE_LOG
  SDWebImageManager *manager = [SDWebImageManager sharedManager];
  manager.loggingStarted = YES;
#endif
  
  [viewBuilder showSearchBar];
  
  [viewBuilder hideMessageLabel];
  
  self.searchMode = NO;
  
  if (self.favouritesMode)
  {
    [MBProgressHUD showHUDAddedTo:self.view animated:NO];
    self.applicatonIds = [[BuisinessApp applicationTable] getFavouritesIds];
    
    if (_applicatonIds && _applicatonIds.count)
    {
      [self appIdsLoaded: _applicatonIds];
      [viewBuilder viewForPresenter: self];
    }
    else
    {
      [viewBuilder viewForPresenter: self];
      [viewBuilder hideSearchBar];
      [viewBuilder showMessageLabel];
      [MBProgressHUD hideAllHUDsForView:self.view animated:NO];
      self.view.userInteractionEnabled = YES;
    }
    oldCategoryId = -1;
  }
  else if (oldCategoryId != category.identifier || !_applicatonIds)
  {
    [MBProgressHUD showHUDAddedTo:self.view animated:NO];
    self.applicatonIds = nil;
    [viewBuilder reloadGrid];
    viewBuilder.category = self.category;

    if([self checkNetworkStatus])
    {
      [BuisinessApp rest].delegate = self;
      [[BuisinessApp rest] fetchApplicationIDs:category.identifier];
    }
    else
    {
      self.applicatonIds = [[BuisinessApp applicationTable] getApplicationCachedIds:category.identifier];
    }
    
    
    [viewBuilder viewForPresenter: self];
  }
  else
  {
    NSLog(@"OTHER");
  }
  
  if (oldCategoryId != category.identifier && favouritesMode != YES) {
  self.blurView = [[FXBlurView alloc] initWithFrame:CGRectMake(0, 0, self.view.frame.size.width, self.view.frame.size.height)];
  
  self.fakeImageView = [[UIImageView alloc] initWithFrame:CGRectMake(0, 0, self.view.frame.size.width, self.view.frame.size.height)];
      NSLog(@"category id %ld", (long)self.category.identifier);
    
    self.fakeImageView.image = self.screenshotImage;
  self.fakeImageView.contentMode = UIViewContentModeScaleAspectFill;

  [self.view addSubview:self.fakeImageView];
  [self.blurView setDynamic:YES];
  [self.blurView setBlurEnabled:YES];
  [self.blurView setIterations:5];
  [self.blurView setTintColor:[UIColor clearColor]];
  [self.blurView setBlurRadius:40.0];
  
  [self.view addSubview:self.blurView];
    
  self.blurView.alpha = 0.98;
    
  [UIView animateWithDuration:1.5 animations:^{
      self.blurView.alpha = 1.0;
      
    }completion:^(BOOL finished) {
//      mainWindow.rootViewController = controller;
//      [self.blurView removeFromSuperview];
    }];
  }
  
}

-(void)viewDidAppear:(BOOL)animated
{
  [super viewDidAppear:animated];
  
  if (oldCategoryId == category.identifier) {
      //    firstStart = NO;
    self.blurView.alpha = 0.0;
    self.fakeImageView.alpha = 0.0;
    self.titleSeparatorView.alpha = 0.0;
    [self.titleSeparatorView removeFromSuperview];
    [self.blurView removeFromSuperview];
    [self.fakeImageView removeFromSuperview];
  }
  else
  {

  }
    NSLog(@"old category id %ld", (long)oldCategoryId);
  if (favouritesMode != YES) {
    oldCategoryId = category.identifier;
  }
    NSLog(@"new category id %ld", (long)oldCategoryId);

  NSLog(@"mBAApplicationListPresenter viewDidAppear");
}

- (void)viewDidLoad
{
  [super viewDidLoad];
  self.view.backgroundColor = kAppListBackgroundColor;
}

- (void)viewWillDisappear:(BOOL)animated
{
#ifdef SDWEBIMAGE_LOG
  SDWebImageManager *manager = [SDWebImageManager sharedManager];
  manager.loggingStarted = NO;
  [manager saveLog];
#endif
  
  [super viewWillDisappear:animated];
}

#pragma mark - Navigation & actions
- (void)toController:(UIViewController *)controller
{
  UIWindow *mainWindow = [UIApplication sharedApplication].windows[0];
  CGRect frame = mainWindow.rootViewController.view.frame;
  CGAffineTransform transform = [mainWindow.rootViewController.view transform];
  controller.view.transform = CGAffineTransformIdentity;
  controller.view.frame = CGRectApplyAffineTransform(frame, transform);
  controller.view.transform = mainWindow.rootViewController.view.transform;

  [controller.view.layer removeAllAnimations];
  [UIView transitionWithView: mainWindow
                    duration: 0.5f
                     options: UIViewAnimationOptionTransitionCrossDissolve
                  animations: ^{
                                  BOOL oldState = [UIView areAnimationsEnabled];
                                  [UIView setAnimationsEnabled: NO];
                                  mainWindow.rootViewController = controller;
                                  [UIView setAnimationsEnabled: oldState];
                               } completion: nil];
}

- (void)back
{
  _startTime = [NSDate date];
  NSTimeInterval timeInterval = [_startTime timeIntervalSinceNow];
  
  [[BuisinessApp analyticsManager] logCategorySessionTime:timeInterval andAppsCount:10];
  [self toController: [BuisinessApp mainMenuPresenter]];
}

- (void) appClick:(UITapGestureRecognizer *)recognizer
{
  [self appGridSelected: recognizer.view.tag - 32];
}

- (void) startApp:(NSInteger)appid
{
  CIphoneMainViewController *mainViewController = [[CIphoneMainViewController alloc] init];
  [self toController: mainViewController];
}
- (void)appGridSelected:(NSInteger)index
{
  NSInteger appid = [_applicatonIds[index] integerValue];
  [[BuisinessApp applicationTable] fillSettingsForAppId:appid];
  [self startApp: appid];
}

- (void) startApp:(NSInteger)appid withSplashScreenImage:(UIImage *)splashScreen
{
  if ([self checkNetworkStatusWithAlert:YES]) {

#ifdef MASTERAPP_STATISTICS
  [[BuisinessApp analyticsManager] increaseAppsLaunchCounter];
#endif
  
  CIphoneMainViewController *mainViewController = [[CIphoneMainViewController alloc] init];
  [mainViewController startLoadingWithAppID: [@(appid) stringValue]
                                cachePolicy: NSURLRequestReloadIgnoringLocalCacheData
                          splashScreenImage: splashScreen];
  [self toController: mainViewController];
  }
}
- (void) appGridCellSelected:(NSInteger)index withSplashScreenImage:(UIImage *)splashScreen
{
  NSInteger appid = [_applicatonIds[index] integerValue];
  [[BuisinessApp applicationTable] fillSettingsForAppId:appid];
  [self startApp: appid withSplashScreenImage:splashScreen];
}


#pragma mark -

- (void)fetchAppsFrom:(NSInteger)startIndex to:(NSInteger)endIndex
{
  if (![self checkNetworkStatus] && !self.favouritesMode)
  {
    NSLog(@"Connection unavailable, fetching terminated");
    [MBProgressHUD hideAllHUDsForView:self.view animated:NO];
    self.view.userInteractionEnabled = YES;
    return;
  }
  
  if (!_applicatonIds || !_applicatonIds.count)
  {
    NSLog(@"app IDs array is empty, fetching terminated");
    [MBProgressHUD hideAllHUDsForView:self.view animated:NO];
    self.view.userInteractionEnabled = YES;
    return;
  }
  
  if (self.favouritesMode)
  {
    [MBProgressHUD hideAllHUDsForView:self.view animated:NO];
    self.view.userInteractionEnabled = YES;
    [viewBuilder refreshApplicationCount];
    [viewBuilder reloadGrid];
  }
  else
  {
      NSLog(@"Load apps from: %ld to: %ld",(long)startIndex, (long)endIndex);
    NSMutableArray *appIds = [[NSMutableArray alloc] init];
    for (int i = startIndex; i < endIndex && i < _applicatonIds.count; i++) {
        [appIds addObject: _applicatonIds[i]];
    }
    fetchedApplcation = endIndex;

#ifdef MASTERAPP_DEBUG
    NSLog(@"appIds: %@", appIds);
#endif
    
    [BuisinessApp rest].delegate = self;
    [[BuisinessApp rest] fetchApplicationData: appIds];
  }
}

- (void) preloadApps
{
  NSLog(@"preload apps");
  [self fetchAppsFrom:0 to:6];
}

- (void)search:(NSString *)query
{
  [viewBuilder hideMessageLabel];
  
  [MBProgressHUD showHUDAddedTo:self.view animated:NO];
  
  if(self.allApplicationIds == nil)
  {
    self.allApplicationIds = _applicatonIds;
  }
  
  if (query == nil || [query isEqualToString: @""])
  {
    if(self.favouritesMode){
      self.applicatonIds = self.applicatonIds = [[BuisinessApp applicationTable] getFavouritesIds];
    } else {
      self.applicatonIds = _allApplicationIds;
    }
    self.allApplicationIds = nil;
    [self fetchAppsFrom: 0 to: 19];
    return;
  }
  
  
  self.searchMode = YES;
  
  if (self.favouritesMode)
  {
    NSArray *favouritesIds = [[BuisinessApp applicationTable] getFavouritesIdsLike: query];
    [self appIdsLoaded: favouritesIds];
  }
  else
  {
    self.applicatonIds = nil;
    
    [BuisinessApp rest].delegate = self;
    [[BuisinessApp rest] fetchApplicationIDs:category.identifier searchQuery: query];
  }
}
#pragma mark - RestServiceDelegate
- (void)appIdsLoaded:(NSArray *)applicationIds
{
  self.applicatonIds = applicationIds;
  
  if (!_applicatonIds || !_applicatonIds.count)
  {
    [viewBuilder showMessageLabel];
    [viewBuilder reloadGrid];
    [MBProgressHUD hideAllHUDsForView:self.view animated:NO];
    self.view.userInteractionEnabled = YES;
    return;
  }
  
  [[BuisinessApp applicationTable] updateApplicationIds: applicationIds forCategoryId: category.identifier];
  
  
  [self fetchAppsFrom: 0 to: 19];
  
}

- (void)appIdsLoaded:(NSArray *)applicationIDs withStrRepresentation:(NSString *)appIDsStr
{
  [[BuisinessApp applicationTable] updateSortedAppIDs:appIDsStr forCategoryId:category.identifier];
  
  [self appIdsLoaded:applicationIDs];
}

- (void)appDataLoaded:(NSArray *)applicationData
{
  [[BuisinessApp applicationTable] updateApplicationData: applicationData];
  NSLog(@"Grid reloaded");
  [MBProgressHUD hideAllHUDsForView:self.view animated:NO];
  [viewBuilder refreshApplicationCount];
  [viewBuilder reloadGrid];
  self.view.userInteractionEnabled = YES;
  self.fakeImageView.alpha = 0.0;
  [UIView animateWithDuration:1.5 animations:^{
    self.blurView.alpha = 0.0;
  
  }completion:^(BOOL finished) {
    [self.titleSeparatorView removeFromSuperview];
    [self.fakeImageView removeFromSuperview];
    [self.blurView removeFromSuperview];
    
  }];
}

- (void) cancelRestOperation
{
  NSLog(@"CANCEL REST OPERATION!!!");
  [MBProgressHUD hideAllHUDsForView:self.view animated:NO];
  self.view.userInteractionEnabled = YES;
}

#pragma mark - DataSource

- (NSInteger)applicationCount
{
  if (_applicatonIds == nil)
    return 0;

  NSLog(@"app count from back %lu", (unsigned long)_applicatonIds.count);
  long lastLast = lastCount;
  lastCount = _applicatonIds.count;
  return lastLast;
}

- (NSInteger)searchApplicationCount
{
  if (_applicatonIds == nil)
    return 0;

  return _applicatonIds.count;
}


- (mBAApplicationModel *)applicationDataForIndex:(NSInteger)index
{
  if (_applicatonIds == nil)
    return nil;

  NSInteger appId = [_applicatonIds[index] integerValue];
  mBAApplicationModel *app = [[BuisinessApp applicationTable] getApplicationData: appId];

    NSLog(@"Load data for: %ld", (long)index);
  NSLog(@"  %@", app.title);
  if (index > maxIndex) {
    maxIndex = index;
  }
  
  NSLog(@"fetchApplication %ld", (long)fetchedApplcation);
  NSLog(@"fetch index %ld", (long)index);
  
  if (app.title == nil && fetchedApplcation <= index) {
    [self fetchAppsFrom: fetchedApplcation to: (index + 19)];
  }
  return app;
}

#pragma mark - UIResponder
- (void)touchesBegan:(NSSet *)touches withEvent:(UIEvent *)event
{
  UITextField *txtSearchField = nil;
  
  if (viewBuilder && viewBuilder.searchBar)
  {
    txtSearchField = [viewBuilder.searchBar valueForKey:@"_searchField"];
  }

  if (txtSearchField)
  {
    UITouch *touch = [[event allTouches] anyObject];
    if ([txtSearchField isFirstResponder] && [touch view] != txtSearchField)
    {
      [txtSearchField resignFirstResponder];
    }
  }  
  
  [super touchesBegan:touches withEvent:event];
}

#pragma mark -


- (BOOL)needToShowMessageLabel
{
  if (!_applicatonIds || !_applicatonIds.count)
    return YES;
  
  return NO;
}

- (BOOL)checkNetworkStatus
{
  NetworkStatus internetStatus = [((TAppDelegate *)[[UIApplication sharedApplication] delegate])
                                  .internetReachable currentReachabilityStatus];
  NetworkStatus hostStatus     = [((TAppDelegate *)[[UIApplication sharedApplication] delegate])
                                  .hostReachable currentReachabilityStatus];
  
  return (internetStatus != NotReachable && hostStatus != NotReachable);
}

- (BOOL)shouldAutorotate
{
  return NO;
}

- (void) didReceiveMemoryWarning
{
    SDImageCache *imageCache = [SDImageCache sharedImageCache];
    [imageCache clearMemory];
    [imageCache clearDisk];
}

@end
