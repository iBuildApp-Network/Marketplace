// IBAHeader

#import "mBAMainMenuPresenter.h"
#import "mBACategoryModel.h"
#import "mBAApplicationModel.h"
#import "buisinessapp.h"
#import "mBADealsPresenter.h"
#import "MBANewAppFormPresenter.h"
#import "mBAMainMenuView.h"
#import "iphmainviewcontroller.h"
#import "mBAApplicationTableService.h"
#import "RestService.h"
#import "appconfig.h"
#import "reachability.h"
#import "appdelegate.h"
#import "SDImageCache.h"
#import "functionLibrary.h"

#import <QuartzCore/QuartzCore.h>

#import "FXBlurView.h"

#import <MBProgressHUD/MBProgressHUD.h>
#import "mBAAuthShare.h"

#define GCDBackgroundThread dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0)
#define GCDMainThread dispatch_get_main_queue()

#define kIBAiTunesLink @"https://itunes.apple.com/us/app/ibuildapp-market/id610313033?ls=1&mt=8"
#define kIBAMasterAppMagicLink @"http://ibuildapp.com/getmarket"

#define kIBAMarketLocalizedName NSLocalizedString(@"masterApp_iBuildApp_Market", @"iBuildApp Market")
#define kIBAMarketLocalizedMessage NSLocalizedString(@"masterApp_iBuildApp_MarketMessage", @"Check out this iBuildApp marketplace app")

@interface mBASideBarVC(MFMail)<MFMailComposeViewControllerDelegate>

#pragma mark - MFMailComposeViewControllerDelegate
- (void)mailComposeController:(MFMailComposeViewController *)controller
          didFinishWithResult:(MFMailComposeResult)composeResult
                        error:(NSError *)error;
@end


@implementation mBASideBarVC(MFMail)
- (void)mailComposeController:(MFMailComposeViewController *)controller
          didFinishWithResult:(MFMailComposeResult)composeResult
                        error:(NSError *)error;
{
#ifdef MASTERAPP_STATISTICS
  BOOL succeeded = NO;
  
  switch (composeResult){
    case MFMailComposeResultSent:
      succeeded = YES;
      break;
    default:
      break;
  }

  [[BuisinessApp analyticsManager] logAppSharingEmailResult:succeeded];
#endif
  [self dismissModalViewControllerAnimated:YES];
}
@end

@interface mBAAuthShareDelegate:NSObject<auth_ShareDelegate>

@end

@implementation mBAAuthShareDelegate

#pragma mark - AuthShareDelegate

- (void)didShareDataForService:(auth_ShareServiceType)serviceType error:(NSError *)error
{
#ifdef MASTERAPP_STATISTICS
  
  BOOL succeeded = YES;
  if (error)
    succeeded = NO;
  
  switch (serviceType) {
    case auth_ShareServiceTypeFacebook:
      [[BuisinessApp analyticsManager] logAppSharingFBResult:succeeded];
      break;
    case auth_ShareServiceTypeTwitter:
      [[BuisinessApp analyticsManager] logAppSharingTwitterResult:succeeded];
      break;
      
    default:
      NSLog(@"doneSharingDataForService: service type :%d", serviceType);
      break;
  }
  
#endif
}

@end

@interface mBAMainMenuPresenter()
{
  mBAAuthShare *aSha;
}

@property (nonatomic, retain) mBAAuthShareDelegate *authShareDelegate;
@property (nonatomic, retain) FXBlurView *blurView;
@property (nonatomic, assign) NSInteger     startAppId;

- (void)startAppByModel:(mBAApplicationModel *)app;

@end

@implementation mBAMainMenuPresenter {
  NSArray *categoryArray;
  NSArray *_featuredApplications;
  mBAMainMenuView *viewBuilder;

  NSArray *searchResultIds;
  NSInteger fetchedApplcation;
  
  BOOL keyboardIsShown;
}

#pragma mark -
- (id)init
{
  self = [super init];
  if (self) {
    viewBuilder = [[mBAMainMenuView alloc] init];
    
    categoryArray = nil;
    _featuredApplications = nil;
    searchResultIds = nil;
    fetchedApplcation = -1;
    keyboardIsShown = NO;
    _authShareDelegate = [[mBAAuthShareDelegate alloc] init];
    aSha = [mBAAuthShare new];
  }
  return self;
}

- (void)dealloc
{
  self.authShareDelegate = nil;
  
  if(aSha){
    aSha.delegate = nil;
    aSha = nil;
  }
  
  [self removeKeyboardObservers];
}

#pragma mark - View Lifecycle
-(void)viewWillAppear:(BOOL)animated
{
  [super viewWillAppear: animated];
  self.navigationController.navigationBarHidden = YES;
  [UIApplication sharedApplication].statusBarHidden = NO;
  [UIApplication sharedApplication].statusBarStyle = UIStatusBarStyleDefault;

  [viewBuilder viewForPresenter: self];
  [viewBuilder displayCategories: categoryArray];
  
  [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(auth_Share_AuthentificationSucceeded:) name:k_auth_Share_LoginState object:nil];
}

#pragma mark - View Lifecycle
-(void)viewWillDisappear:(BOOL)animated
{
  [super viewWillDisappear: animated];
}

-(void) viewDidAppear:(BOOL)animated{
  [super viewDidAppear:animated];
  
  NetworkStatus internetStatus = [((TAppDelegate *)[[UIApplication sharedApplication] delegate])
                                  .internetReachable currentReachabilityStatus];
  NetworkStatus hostStatus     = [((TAppDelegate *)[[UIApplication sharedApplication] delegate])
                                  .hostReachable currentReachabilityStatus];
  
  if(internetStatus != NotReachable && hostStatus != NotReachable){
    dispatch_async(GCDBackgroundThread,
                   ^{
                     [self processPendingRateRequests];
                   });
  }
}

- (void)viewDidLoad
{
  [super viewDidLoad];
  [self registerKeyboardObservers];
  [self loadData];
  
#ifdef MASTERAPP_STATISTICS
  [[BuisinessApp analyticsManager] logMasterAppLaunching];
#endif
  
}

#pragma mark -

- (void)loadData
{
  if ([[BuisinessApp database] createDB])
  {
 
    categoryArray = [[BuisinessApp categoryTable] categoryList];
    [viewBuilder displayCategories: categoryArray];
    
    _featuredApplications = [[BuisinessApp applicationTable] featuredList];
    [viewBuilder displayFeatured: _featuredApplications];
  }
  [BuisinessApp rest].delegate = self;
  [[BuisinessApp rest] fetchCategories];
  [[BuisinessApp rest] fetchFeaturedApplications];
}

/**
 * The method checks for the presence of applications that have not been made rate / derate
 * due to lack of Internet, and, if such applications exist, performs appropriate requests to the server
 */
-(void)processPendingRateRequests{
  NSDictionary *pendingApps = [[BuisinessApp applicationTable] getPendingApplications];
  
  for(NSNumber *appId in pendingApps.allKeys){
    
    NSString *decision = [pendingApps objectForKey:appId];
    NSInteger appIdInteger = [appId integerValue];
    
    if([decision isEqualToString:PENDING_RATE_DECISION]){
      
      [BuisinessApp rest].delegate = self;
      [[BuisinessApp rest] performRatingChangeForAppWithId:appIdInteger uuid:appGetUID() andStatus:rateUp];
      [[BuisinessApp applicationTable] setFavouritedState:FAVOURITE_DEFAULT forAppWithId:appIdInteger];
      
      //In the case of a successful raid, we reset the field values
      //[[BuisinessApp applicationTable] resetActiveAndFavouriteStatesForAppId:appIdInteger];
      
    } else if([decision isEqualToString:PENDING_DERATE_DECISION]){
      
      [BuisinessApp rest].delegate = self;
      [[BuisinessApp rest] performRatingChangeForAppWithId:appIdInteger uuid:appGetUID() andStatus:rateDown];
      [[BuisinessApp applicationTable] removeFromFavourites: appIdInteger];
      
    }
  }
}

- (void)toController:(UIViewController *)controller
{
  UIWindow *mainWindow = [UIApplication sharedApplication].windows[0];
  CGRect frame = mainWindow.rootViewController.view.frame;
  CGSize screenSize = [[UIScreen mainScreen] applicationFrame].size;
  CGColorSpaceRef colorSpaceRef = CGColorSpaceCreateDeviceRGB();
  CGContextRef ctx = CGBitmapContextCreate(nil, screenSize.width, screenSize.height, 8, 4*(int)screenSize.width, colorSpaceRef, kCGImageAlphaPremultipliedLast);
  CGContextTranslateCTM(ctx, 0.0, screenSize.height);
  CGContextScaleCTM(ctx, 1.0, -1.0);
  
  [(CALayer*)self.view.layer renderInContext:ctx];
  
  CGImageRef cgImage = CGBitmapContextCreateImage(ctx);
  UIImage *copied = [UIImage imageWithCGImage:cgImage];
  CGImageRelease(cgImage);
  CGContextRelease(ctx);
  
  
  
  
  [BuisinessApp applicationListPresenter].screenshotImage = copied;
  
  if ([BuisinessApp applicationListPresenter].favouritesMode != YES) {
    self.blurView = [[FXBlurView alloc] initWithFrame:CGRectMake(0, 0, self.view.frame.size.width, self.view.frame.size.height)];
    
    [self.blurView setDynamic:YES];
    [self.blurView setBlurEnabled:YES];
    [self.blurView setIterations:5];
    [self.blurView setTintColor:[UIColor clearColor]];
    [self.blurView setBlurRadius:50.0];
    
    [self.view addSubview:self.blurView];
    self.blurView.alpha = 0.0;
    
    [UIView animateWithDuration:1.5 animations:^{
      self.blurView.alpha = 0.97;
      
    }completion:^(BOOL finished) {
      mainWindow.rootViewController = controller;
      [self.blurView removeFromSuperview];
    }];

  }
  else
  {
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
                          } completion:nil];
  }

}

#pragma mark - mBASideBarViewDelegate
- (void)favouritesMenuItemSelected
{
  [BuisinessApp applicationListPresenter].favouritesMode = YES;
  [self toController: [BuisinessApp applicationListPresenter]];
  [self.sideBarController hideSideBarAnimated:NO];
}

- (void)aboutUsMenuItemSelected
{
  [self toController:[BuisinessApp aboutUsController]];
  [self.sideBarController hideSideBarAnimated:NO];
}

- (void)inviteViaEmail
{
  if([self checkNetworkStatusWithAlert:YES]){ //Anyway, emails get queued and sent
  //once the internet is available. So you may want to remove this check
  NSString *htmlContent = [NSString stringWithFormat:@"<br>%@<br><a href=\"%@\">%@</a>", kIBAMarketLocalizedMessage, kIBAMasterAppMagicLink, kIBAMasterAppMagicLink];
  
  [functionLibrary  callMailComposerWithRecipients:nil
                                        andSubject:kIBAMarketLocalizedName
                                           andBody:htmlContent
                                            asHTML:YES
                                    withAttachment:nil
                                          mimeType:@""
                                          fileName:@""
                                    fromController:self.sideBarController
                                          showLink:NO];
    
  #ifdef MASTERAPP_STATISTICS
    [[BuisinessApp analyticsManager] logAppSharingEmailAttempt];
  #endif
    
  }
}

- (void)inviteViaFacebook
{
  if([self checkNetworkStatusWithAlert:YES]){
    NSMutableDictionary *data = [NSMutableDictionary dictionary];
    [data setObject:kIBAMarketLocalizedMessage forKey:@"message"];
    [data setObject:kIBAMasterAppMagicLink forKey:@"link"];
    [data setObject:kIBAMarketLocalizedName forKey:@"name"];
    
    aSha.viewController = self.view.window.rootViewController;
    aSha.delegate = self.authShareDelegate;

    [aSha shareContentUsingService:auth_ShareServiceTypeFacebook fromUser:nil withData:data showLoginRequiredPrompt:NO];
  }
}


- (void)inviteViaTwitter
{
  if([self checkNetworkStatusWithAlert:YES]){
  NSString *message = [NSString stringWithFormat:@"%@: \r\n%@", kIBAMarketLocalizedMessage, kIBAMasterAppMagicLink];
  NSMutableDictionary *data = [NSMutableDictionary dictionary];
  [data setObject:message forKey:@"additionalText"];
  
  aSha.viewController = self.view.window.rootViewController;
  aSha.viewController = self.view.window.rootViewController;
  aSha.delegate = self.authShareDelegate;

  [aSha shareContentUsingService:auth_ShareServiceTypeTwitter fromUser:aSha.user withData:data];
  }
}

- (void)inviteViaSMS
{
  NSString *message = [NSString stringWithFormat:@"%@: \r\n%@", kIBAMarketLocalizedMessage, kIBAMasterAppMagicLink];
  NSMutableDictionary *data = [NSMutableDictionary dictionary];
  [data setObject:message forKey:@"message"];
  
  aSha.viewController = self.view.window.rootViewController;
  
  [aSha shareContentUsingService:auth_ShareServiceTypeSMS fromUser:aSha.user withData:data];
  
  NSLog(@"invite via sms");
  #ifdef MASTERAPP_STATISTICS
    [[BuisinessApp analyticsManager] logAppSharingSmsAttempt];
  #endif
}


#pragma mark -
- (void)toPlaceAppView:(UITapGestureRecognizer*)recognizer
{
  NSLog(@"fufu");
  [self toController:[[mBANewAppFormPresenter alloc] initWithNibName:nil bundle:nil]];
}

- (void)toDealsView:(UITapGestureRecognizer*)recognizer
{
  [self toController:[[mBADealsPresenter alloc] initWithNibName:nil bundle:nil]];;
}

- (void)toCategoryView:(UITapGestureRecognizer*)recognizer
{
  if ([self checkNetworkStatusWithAlert:YES]) {
    if (categoryArray == nil)
      return;
    NSInteger tag = recognizer.view.tag - 32;
    if (tag >= categoryArray.count)
      return;
    
    mBAApplicationListPresenter *controller = [BuisinessApp applicationListPresenter];
    controller.favouritesMode = NO;
    controller.category = categoryArray[tag];
    
    [self toController: controller];
  };
  

}


- (void)toCategoryWithIndex:(NSUInteger)categoryIndex
{
  if ([self checkNetworkStatusWithAlert:YES]) {
  if (categoryArray == nil)
    return;
  
  if (categoryIndex >= categoryArray.count)
    return;
  
  mBACategoryModel *category = categoryArray[categoryIndex];
  
#ifdef MASTERAPP_STATISTICS
  [[BuisinessApp analyticsManager] logCategoryOpened:category.title];
#endif
  
  mBAApplicationListPresenter *controller = [BuisinessApp applicationListPresenter];
  controller.favouritesMode = NO;
  controller.category = category;
    [self toController: controller];
  }
}

- (void)preloadCategories
{
  if (categoryArray == nil)
    return;
  
  for (int x = 0; x < categoryArray.count; x++) {
    mBACategoryModel *category = categoryArray[x];
    mBAApplicationListPresenter *controller = [BuisinessApp applicationListPresenter];
    controller.favouritesMode = NO;
    controller.category = category;
    NSArray *applicatonIds = [[BuisinessApp applicationTable] getApplicationIds:x];
    NSMutableArray *appIds = [[NSMutableArray alloc] init];
    for (int i = 0; i < 6; i++) {
      [appIds addObject: applicatonIds[i]];
    }
    
    [BuisinessApp rest].delegate = self;
    [[BuisinessApp rest] fetchApplicationData: appIds];
  }
}


- (void)CategoryDataReceiver:(NSArray *)categories
{
  [[BuisinessApp categoryTable] setCategoryList: categories];
  categoryArray = [[BuisinessApp categoryTable] categoryList];
  [viewBuilder displayCategories: categoryArray];
}

- (void)FeaturedApplications:(NSArray *)featuredApplications
{
  _featuredApplications = featuredApplications;
  [[BuisinessApp applicationTable] setFeaturedList: featuredApplications];
  _featuredApplications = [[BuisinessApp applicationTable] featuredList];
  [viewBuilder displayFeatured: _featuredApplications];
}

- (void)startApp:(NSInteger)appid withSplashScreen:(UIImage *)spashScreenImage
{
#ifdef MASTERAPP_STATISTICS
  [[BuisinessApp analyticsManager] increaseAppsLaunchCounter];
#endif
  
  CIphoneMainViewController *mainViewController = [[CIphoneMainViewController alloc] init];
  [BuisinessApp fromMainMenu:YES];
  [self toController: mainViewController];
  
  [mainViewController startLoadingWithAppID:[@(appid) stringValue] cachePolicy:NSURLRequestReloadIgnoringLocalCacheData splashScreenImage:spashScreenImage];
}

- (void)startAppById:(NSInteger)appid
{
   mBAApplicationModel *app = [[BuisinessApp applicationTable] getApplicationData: appid];
  if (app)
  {
    _startAppId = 0;
    [self startAppByModel:app];
  }
  else
  {
     _startAppId = appid;
    [BuisinessApp rest].delegate = self;
    [[BuisinessApp rest] fetchApplicationData:@[@(appid)]];
  }
  
}

- (void)startAppByModel:(mBAApplicationModel *)app
{
  [BuisinessApp fromMainMenu:YES];
  [[BuisinessApp applicationTable] fillSettingsForAppId:app.app_id];
  
  NSURLRequest *rq = [[NSURLRequest alloc] initWithURL:[NSURL URLWithString:app.pictureUrl] cachePolicy:NSURLRequestUseProtocolCachePolicy timeoutInterval:5.0];
  NSData *responseData = [NSURLConnection sendSynchronousRequest:rq returningResponse:nil error:nil];
  
  UIImage *croppedImg = nil;
  if (responseData && responseData.length > 0)
  {
    UIImage *img = [UIImage imageWithData:responseData];
    CGRect rect = (CGRect){0, 10, img.size.width, img.size.height - 10 - 30};
    CGImageRef imageRef = CGImageCreateWithImageInRect(img.CGImage, rect);
    croppedImg = [UIImage imageWithCGImage:imageRef];
    CGImageRelease(imageRef);
  }
  
  CIphoneMainViewController *mainViewController = [[CIphoneMainViewController alloc] init];
  [mainViewController startLoadingWithAppID: [@(app.app_id) stringValue]
                                cachePolicy: NSURLRequestReloadIgnoringLocalCacheData
                          splashScreenImage: croppedImg];
  
  UIWindow *mainWindow = [UIApplication sharedApplication].windows[0];
  CGRect frame = mainWindow.rootViewController.view.frame;
  CGAffineTransform transform = [mainWindow.rootViewController.view transform];
  mainViewController.view.transform = CGAffineTransformIdentity;
  mainViewController.view.frame = CGRectApplyAffineTransform(frame, transform);
  mainViewController.view.transform = mainWindow.rootViewController.view.transform;
  
  [mainViewController.view.layer removeAllAnimations];
  
  [UIView transitionWithView: mainWindow
                    duration: 0.5f
                     options: UIViewAnimationOptionTransitionCrossDissolve
                  animations: ^{
                    BOOL oldState = [UIView areAnimationsEnabled];
                    [UIView setAnimationsEnabled: NO];
                    mainWindow.rootViewController = mainViewController;
                    [UIView setAnimationsEnabled: oldState];
                  } completion: nil];

}

- (BOOL)shouldAutorotate
{
  return NO;
}

- (void)search:(NSString *)query
{
  if (query == nil || [query isEqualToString: @""]) {
    [viewBuilder hideSearchResultsGrid];
    self.view.userInteractionEnabled = YES;
    return;
  }
  
  searchResultIds = nil; //to clear grid data before starting the next search

  [viewBuilder hideMessageLabel];
  
  NSLog(@"show HUD...");
  [MBProgressHUD showHUDAddedTo:self.view animated:NO];
  
  [BuisinessApp rest].delegate = self;
  [[BuisinessApp rest] fetchApplicationIDsBySearchQuery: query];
}

- (void)fetchAppsFrom:(NSInteger)startIndex to:(NSInteger)endIndex
{
  
  if (![self checkNetworkStatusWithAlert:NO])
  {
    NSLog(@"Connection unavailable, fetching terminated");
    self.view.userInteractionEnabled = YES;
    return;
  }
  
  if (!searchResultIds || !searchResultIds.count)
  {
    NSLog(@"app IDs array is empty, fetching terminated");
    [MBProgressHUD hideAllHUDsForView:self.view animated:NO];
    self.view.userInteractionEnabled = YES;
    return;
  }
  
  if (![MBProgressHUD HUDForView:self.view])
    [MBProgressHUD showHUDAddedTo:self.view animated:NO];
  
  self.view.userInteractionEnabled = NO;
  
  NSLog(@"Load apps from: %d to: %d",startIndex, endIndex);
  NSMutableArray *appIds = [[NSMutableArray alloc] init];
  for (int i = startIndex; i < endIndex && i < searchResultIds.count; i++) {
    [appIds addObject: searchResultIds[i]];
  }
  fetchedApplcation = endIndex;
  
  [BuisinessApp rest].delegate = self;
  [[BuisinessApp rest] fetchApplicationData: appIds];
}

#pragma mark - RestServiceDelegate

- (void)appIdsLoaded:(NSArray *)applicationIds
{
  
  searchResultIds = applicationIds;
  fetchedApplcation = 0;
  
  if (searchResultIds && searchResultIds.count)
  {
    [self fetchAppsFrom: 0 to: 19];
  }
  else
  {
    [viewBuilder reloadGrid];
    [MBProgressHUD hideAllHUDsForView:self.view animated:NO];
    self.view.userInteractionEnabled = YES;
  }
}

- (void)appIdsLoaded:(NSArray *)applicationIDs withStrRepresentation:(NSString *)appIDsStr
{
  [self appIdsLoaded:applicationIDs];
  
  if (searchResultIds && searchResultIds.count)
    [viewBuilder hideMessageLabel];
  else
    [viewBuilder showMessageLabel];
  
}

- (void)appDataLoaded:(NSArray *)applicationData
{
  [[BuisinessApp applicationTable] updateApplicationData: applicationData];
  
  if (_startAppId)
  {
    mBAApplicationModel *app = [[BuisinessApp applicationTable] getApplicationData: _startAppId];
    _startAppId = 0;
    if (app)
      [self startAppByModel:app];
  }
  else
  {
    NSLog(@"Grid reloaded");
    [MBProgressHUD hideAllHUDsForView:self.view animated:NO];
    self.view.userInteractionEnabled = YES;
    NSLog(@"hide HUD...");
    [viewBuilder reloadGrid];
  }
}

- (void) cancelRestOperation
{
  NSLog(@"CANCEL REST OPERATION!!!");
  [MBProgressHUD hideAllHUDsForView:self.view animated:NO];
  self.view.userInteractionEnabled = YES;
  UIAlertView *alert = [[UIAlertView alloc] initWithTitle:NSLocalizedString(@"general_cellularDataTurnedOff",@"Cellular Data is Turned off")
                                                   message:NSLocalizedString(@"general_cellularDataTurnOnMessage",@"Turn on cellular data or use Wi-Fi to access data")
                                                  delegate:nil
                                         cancelButtonTitle:NSLocalizedString(@"general_defaultButtonTitleOK",@"OK")
                                         otherButtonTitles:nil];
  [alert show];
}

#pragma mark -

- (void)hideAllHUDs
{
  [MBProgressHUD hideAllHUDsForView:self.view animated:NO];
  NSLog(@"hideAllHUDs");
}


- (NSInteger)searchGrid_applicationCount
{
  if (searchResultIds == nil)
    return 0;
  return searchResultIds.count;
}

- (mBAApplicationModel *)searchGrid_applicationDataForIndex:(NSInteger)index
{
  if (searchResultIds == nil)
    return nil;

  NSInteger appId = [searchResultIds[index] integerValue];
  mBAApplicationModel *app = [[BuisinessApp applicationTable] getApplicationData: appId];
  
  NSLog(@"Load data for: %d", index);
  NSLog(@"  %@", app.title);
  
  if (app.title == nil && fetchedApplcation <= index) {
    [self fetchAppsFrom: fetchedApplcation to: (index + 20)];
  }

  return app;
}

- (void) startApp:(NSInteger)appid withSplashScreenImage:(UIImage *)splashScreen
{

#ifdef MASTERAPP_STATISTICS
  [[BuisinessApp analyticsManager] increaseAppsLaunchCounter];
#endif
  
  CIphoneMainViewController *mainViewController = [[CIphoneMainViewController alloc] init];
  [mainViewController startLoadingWithAppID: [@(appid) stringValue]
                                cachePolicy: NSURLRequestReloadIgnoringLocalCacheData
                          splashScreenImage: splashScreen];
  [self toController: mainViewController];
}
- (void) appGridCellSelected:(NSInteger)index withSplashScreenImage:(UIImage *)splashScreen
{
  [BuisinessApp fromMainMenu:YES];
  NSInteger appid = [searchResultIds[index] integerValue];
  [[BuisinessApp applicationTable] fillSettingsForAppId:appid];
  [self startApp: appid withSplashScreenImage:splashScreen];
}

#pragma mark - UIResponder
- (void)touchesBegan:(NSSet *)touches withEvent:(UIEvent *)event
{
  [super touchesBegan:touches withEvent:event];
}

#pragma mark -

- (void)toggleSideBar
{
    NSLog(@"toggle sidebar");
    [self.sideBarController toggleHiddenView];
}

#pragma mark - Keyboard notifications
- (void)keyboardWillShow:(NSNotification *)notification
{
  [self performViewAdjustmentBasedOnKeyboardNotification:notification];
}

- (void)keyboardWillHide:(NSNotification *)notification
{
  [self performViewAdjustmentBasedOnKeyboardNotification:notification];
}

- (void) performViewAdjustmentBasedOnKeyboardNotification:(NSNotification *)notification
{
  CGFloat keyboardHeight = -1.0f;
  
  if(keyboardIsShown){
    if([notification.name isEqualToString:UIKeyboardWillHideNotification]){
      keyboardHeight = 0.0f;
      keyboardIsShown = NO;
    } else {
      return;
    }
  } else {
    if([notification.name isEqualToString:UIKeyboardWillShowNotification]){
      CGRect keyboardFrame = [[[notification userInfo] objectForKey:UIKeyboardFrameBeginUserInfoKey] CGRectValue];
      keyboardHeight = keyboardFrame.size.height;
      keyboardIsShown = YES;
    } else {
      return;
    }
  }
  
  NSUInteger animationCurve = [[[notification userInfo] objectForKey:UIKeyboardAnimationCurveUserInfoKey] unsignedIntegerValue];
  NSTimeInterval animationDuration = [[[notification userInfo] objectForKey:UIKeyboardAnimationDurationUserInfoKey] doubleValue];
  
  [UIView animateWithDuration:animationDuration delay:0.0f options:animationCurve animations:^{
    [viewBuilder adjustPresentationForKeyboardHeight:keyboardHeight];
  } completion:nil];
}

- (void) didReceiveMemoryWarning
{
  SDImageCache *imageCache = [SDImageCache sharedImageCache];
  [imageCache clearMemory];
  [imageCache clearDisk];
  
  [super didReceiveMemoryWarning];
}

- (void)auth_Share_AuthentificationSucceeded:(NSNotification*)notification
{
}

-(void)registerKeyboardObservers
{
  [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(keyboardWillShow:) name:UIKeyboardWillShowNotification object:nil];
  [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(keyboardWillHide:) name:UIKeyboardWillHideNotification object:nil];
}

-(void)removeKeyboardObservers
{
  [[NSNotificationCenter defaultCenter] removeObserver:self name:UIKeyboardWillShowNotification object:nil];
  [[NSNotificationCenter defaultCenter] removeObserver:self name:UIKeyboardWillHideNotification object:nil];
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

@end
