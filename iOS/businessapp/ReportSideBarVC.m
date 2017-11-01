
#import "ReportSideBarVC.h"
#import "buisinessapp.h"
#import "mBASettings.h"
#import "reachability.h"
#import "appdelegate.h"
#import "appconfig.h"
#import "functionLibrary.h"
#import "mBAAuthShare.h"

#import "UIColor+image.h"
#import "NSString+colorizer.h"

#define kIBAMarketLocalizedName NSLocalizedString(@"masterApp_iBuildApp_Market", @"iBuildApp Market")

CGFloat const BUTTON_SIZE = 50.;

@interface ReportSideBarVC () <MFMailComposeViewControllerDelegate, auth_ShareDelegate>

@property (nonatomic, strong) UIButton  *shareFacebookBtn;
@property (nonatomic, strong) UIButton  *shareTwitterBtn;
@property (nonatomic, strong) UIButton  *shareEmailBtn;
@property (nonatomic, strong) UIButton  *shareSMSBtn;
@property (nonatomic, strong) UIButton  *reportButton;
@property (nonatomic, assign) BOOL      isShareIconsVisible;
@property (nonatomic, strong) mBAAuthShare *aSha;
@property (nonatomic, assign) NSInteger curAppId;
@property (nonatomic, copy)   NSString *curAppName;

@end


@implementation ReportSideBarVC


- (instancetype)init
{
  self = [super init];
  if (self) {
    _showSideBarBtnImage = YES;
    _isShareIconsVisible = NO;
    _aSha = [mBAAuthShare new];
    
    NSString* appIdStr = [mBASettings sharedInstance].currentAppId;
    if (appIdStr)
    {
      _curAppId = [appIdStr intValue];
    
      mBAApplicationModel *app = [[BuisinessApp applicationTable] getApplicationData: _curAppId];
      self.curAppName = app.title;
    }

  }
  return self;
}

- (void) dealloc
{
  self.sideBar = nil;
  self.screenSlide = nil;
  self.sideBarFavorite = nil;
  self.screenshotImage = nil;
  self.sideBarBtn = nil;
  self.shareFacebookBtn = nil;
  self.shareTwitterBtn = nil;
  self.shareEmailBtn = nil;
  self.shareSMSBtn = nil;
  self.reportButton = nil;
  self.aSha = nil;
  self.curAppName = nil;
}

- (void)viewDidLoad {
    [super viewDidLoad];
}

- (void)viewDidAppear:(BOOL)animated
{
  [self slideOut];
}

- (void)viewWillAppear:(BOOL)animated
{
  self.sideBar = [[UIView alloc] init];
  self.sideBar.frame = (CGRect){320, 0, 220, 800};
  self.sideBar.backgroundColor = [UIColor colorWithRed:45.0f/256.0f green:51.0f/256.0f blue:54.0f/256.0f alpha:1.0];
  self.sideBar.layer.masksToBounds = YES;

  float yOffset = 55;
  UIColor *highlightedColor = [@"#A7A7A7" asColor];
  
  UIButton *homeButton = [[UIButton alloc] initWithFrame:CGRectMake(0, yOffset, self.sideBar.bounds.size.width, BUTTON_SIZE)];
  [homeButton setBackgroundImage:[self.sideBar.backgroundColor asImage] forState:UIControlStateNormal];
  [homeButton setBackgroundImage:[highlightedColor asImage] forState:UIControlStateHighlighted];
  [homeButton addTarget:self action:@selector(backFromApp1) forControlEvents:UIControlEventTouchUpInside];
  
  [self.sideBar addSubview:homeButton];
  yOffset += BUTTON_SIZE;
  
  UIButton *favoriteButton = [[UIButton alloc] initWithFrame:CGRectMake(0, yOffset, self.sideBar.bounds.size.width, BUTTON_SIZE)];
  [favoriteButton setBackgroundImage:[self.sideBar.backgroundColor asImage] forState:UIControlStateNormal];
  [favoriteButton setBackgroundImage:[highlightedColor asImage] forState:UIControlStateHighlighted];
  [favoriteButton addTarget:self action:@selector(favApp:) forControlEvents:UIControlEventTouchUpInside];
  
  [self.sideBar addSubview:favoriteButton];
  
   yOffset += BUTTON_SIZE;
  
  UIButton *shareButton = [[UIButton alloc] initWithFrame:CGRectMake(0, yOffset, self.sideBar.bounds.size.width, BUTTON_SIZE)];
  [shareButton setBackgroundImage:[self.sideBar.backgroundColor asImage] forState:UIControlStateNormal];
  [shareButton setBackgroundImage:[highlightedColor asImage] forState:UIControlStateHighlighted];
  [shareButton addTarget:self action:@selector(shareAppButtonTapped) forControlEvents:UIControlEventTouchUpInside];
  
  [self.sideBar addSubview:shareButton];
  yOffset += BUTTON_SIZE;
  
  
  CGFloat buttonsLeftMargin = (kSideBarWidth - (BUTTON_SIZE * 4)) / 2;
  
  _shareFacebookBtn = [[UIButton alloc] initWithFrame:CGRectMake(buttonsLeftMargin, yOffset, BUTTON_SIZE, BUTTON_SIZE)];
  _shareFacebookBtn.contentMode = UIViewContentModeCenter;
  UIImage *facebookImage = [UIImage imageNamed:@"mBA_share_facebook"];
  [_shareFacebookBtn setImage:facebookImage forState:UIControlStateNormal];
  [_shareFacebookBtn addTarget:self action:@selector(shareFacebookButtonTapped) forControlEvents:UIControlEventTouchUpInside];
  [self.sideBar addSubview:_shareFacebookBtn];
  
  _shareTwitterBtn = [[UIButton alloc] initWithFrame:CGRectMake(CGRectGetMaxX(_shareFacebookBtn.frame), yOffset, BUTTON_SIZE, BUTTON_SIZE)];
  _shareTwitterBtn.contentMode = UIViewContentModeCenter;
  UIImage *twitterImage = [UIImage imageNamed:@"mBA_share_twitter"];
  [_shareTwitterBtn setImage:twitterImage forState:UIControlStateNormal];
  [_shareTwitterBtn addTarget:self action:@selector(shareTwitterButtonTapped) forControlEvents:UIControlEventTouchUpInside];
  [self.sideBar addSubview:_shareTwitterBtn];
  
  _shareEmailBtn = [[UIButton alloc] initWithFrame:CGRectMake(CGRectGetMaxX(_shareTwitterBtn.frame), yOffset, BUTTON_SIZE, BUTTON_SIZE)];
  _shareEmailBtn.contentMode = UIViewContentModeCenter;
   UIImage *emailImage = [UIImage imageNamed:@"mBA_share_email"];
  [_shareEmailBtn setImage:emailImage forState:UIControlStateNormal];
  [_shareEmailBtn addTarget:self action:@selector(shareEmailButtonTapped) forControlEvents:UIControlEventTouchUpInside];
  [self.sideBar addSubview:_shareEmailBtn];
  
  _shareSMSBtn = [[UIButton alloc] initWithFrame:CGRectMake(CGRectGetMaxX(_shareEmailBtn.frame), yOffset, BUTTON_SIZE, BUTTON_SIZE)];
  _shareSMSBtn.contentMode = UIViewContentModeCenter;
  UIImage *smsImage = [UIImage imageNamed:@"mBA_share_sms"];
  [_shareSMSBtn setImage:smsImage forState:UIControlStateNormal];
  [_shareSMSBtn addTarget:self action:@selector(shareSmsButtonTapped) forControlEvents:UIControlEventTouchUpInside];
  [self.sideBar addSubview:_shareSMSBtn];
  
  
  
  _reportButton = [[UIButton alloc] initWithFrame:CGRectMake(0, yOffset, self.sideBar.bounds.size.width, BUTTON_SIZE)];
  [_reportButton setBackgroundImage:[self.sideBar.backgroundColor asImage] forState:UIControlStateNormal];
  [_reportButton setBackgroundImage:[highlightedColor asImage] forState:UIControlStateHighlighted];
  [_reportButton addTarget:self action:@selector(reportClick) forControlEvents:UIControlEventTouchUpInside];
  
  [self.sideBar addSubview:_reportButton];
  
  UIImageView *homeImageView = [[UIImageView alloc] init];
  homeImageView.image = [UIImage imageNamed: @"home"];
  homeImageView.frame = (CGRect){ 22, 10, homeImageView.image.size.width, homeImageView.image.size.height };
  
  [homeButton addSubview:homeImageView];
  
  UILabel *homeLabel = [[UILabel alloc] initWithFrame:(CGRect){ 62, 10, 100, 30 }];
  homeLabel.textColor = kSideBarMenuItemsFontColor;
  homeLabel.backgroundColor = [UIColor clearColor];
  homeLabel.font = [UIFont systemFontOfSize:kSideBarMenuItemsFontSize];
  homeLabel.textAlignment = NSTextAlignmentLeft;
  homeLabel.text = NSLocalizedString(@"masterApp_Home", @"Home");
  
  [homeButton addSubview:homeLabel];
  
  self.sideBarFavorite = [[UIImageView alloc] init];
  
  Boolean appIsFav = [[BuisinessApp applicationTable] appInFavourites: _curAppId];
  

  if (!appIsFav)
    self.sideBarFavorite.image = [UIImage imageNamed:@"favorite"];
  else
    self.sideBarFavorite.image = [UIImage imageNamed:@"favorite_on"];

   self.sideBarFavorite.frame = (CGRect){ 22, 10, self.sideBarFavorite.image.size.width, self.sideBarFavorite.image.size.height };
  
  [favoriteButton addSubview:self.sideBarFavorite];

  UILabel *favoriteLabel = [[UILabel alloc] initWithFrame:(CGRect){ 62, 7, 170, 30 }];
  favoriteLabel.textColor = kSideBarMenuItemsFontColor;
  favoriteLabel.backgroundColor = [UIColor clearColor];
  favoriteLabel.font = [UIFont systemFontOfSize:kSideBarMenuItemsFontSize];
  favoriteLabel.textAlignment = NSTextAlignmentLeft;
  favoriteLabel.text = NSLocalizedString(@"masterApp_Favorite", @"Favorite");
  
  [favoriteButton addSubview:favoriteLabel];
  UIImageView *shareImageView = [[UIImageView alloc] init];
  shareImageView.image = [UIImage imageNamed: @"mBA_share"];
  shareImageView.frame = (CGRect){ 22, 10, shareImageView.image.size.width, shareImageView.image.size.height };
  
  [shareButton addSubview:shareImageView];

  
  UILabel *shareLabel = [[UILabel alloc] initWithFrame:(CGRect){ 62, 7, 170, 30 }];
  shareLabel.textColor = kSideBarMenuItemsFontColor;
  shareLabel.backgroundColor = [UIColor clearColor];
  shareLabel.font = [UIFont systemFontOfSize:kSideBarMenuItemsFontSize];
  shareLabel.textAlignment = NSTextAlignmentLeft;
  shareLabel.text = NSLocalizedString(@"masterApp_Share", @"Share");
  
  [shareButton addSubview:shareLabel];

  UIImageView *reportImageView = [[UIImageView alloc] init];
  reportImageView.image = [UIImage imageNamed: @"report"];
  reportImageView.frame = (CGRect){ 22, 10, reportImageView.image.size.width, reportImageView.image.size.height };
  
  [_reportButton addSubview:reportImageView];

  UILabel *reportLabel = [[UILabel alloc] initWithFrame:(CGRect){ 62, 7, 170, 30 }];
  reportLabel.textColor = kSideBarMenuItemsFontColor;
  reportLabel.backgroundColor = [UIColor clearColor];
  reportLabel.font = [UIFont systemFontOfSize:kSideBarMenuItemsFontSize];
  reportLabel.textAlignment = NSTextAlignmentLeft;
  reportLabel.text = NSLocalizedString(@"masterApp_FlagContent", @"Flag Content");

  [_reportButton addSubview:reportLabel];

  [self.sideBar setHidden:YES];

  UISwipeGestureRecognizer *recognizer = [[UISwipeGestureRecognizer alloc] initWithTarget:self action:@selector(slideIn)];
  [recognizer setDirection:(UISwipeGestureRecognizerDirectionRight)];
  [self.sideBar addGestureRecognizer:recognizer];
  [self.sideBar setUserInteractionEnabled:YES];
  
  
  [self.view addSubview:self.sideBar];
  
  
  self.screenSlide = [[UIImageView alloc] initWithImage:self.screenshotImage];
  [self.view addSubview:self.screenSlide];
  
  BOOL iOS6 = floor(NSFoundationVersionNumber) < floor(NSFoundationVersionNumber_iOS_7_0);
  
  if (iOS6 == YES) {
    self.screenSlide.frame = (CGRect){0, -20, self.screenSlide.frame.size.width, self.screenSlide.frame.size.height};
  }else
  {
    
  }

  self.screenSlide.backgroundColor = [UIColor blackColor];
  [self.screenSlide setHidden:NO];
  
  UISwipeGestureRecognizer *screenRecognizer = [[UISwipeGestureRecognizer alloc] initWithTarget:self action:@selector(slideIn)];
  [screenRecognizer setDirection:(UISwipeGestureRecognizerDirectionRight)];
  [self.screenSlide addGestureRecognizer:screenRecognizer];
  [self.screenSlide setUserInteractionEnabled:YES];
  
  
  [self.navigationController setNavigationBarHidden:YES animated:NO];

  [self.sideBar setHidden:NO];
  
  self.sideBarBtn = [[UIImageView alloc] initWithImage:[UIImage imageNamed: @"mBA_hamburger"]];
  
  if (!self.showSideBarBtnImage)
    self.sideBarBtn.image = nil;
  
  if (self.fromMainScreen == YES) {
      self.sideBarBtn.frame = (CGRect){ 64, 39, 20, 16 };
    
  } else
  {
    self.sideBarBtn.frame = (CGRect){ 76, 32, 18, 18 };
  }
  
  if (iOS6 == YES) {
    self.sideBarBtn.frame = (CGRect){ self.sideBarBtn.frame.origin.x, self.sideBarBtn.frame.origin.y - 20, self.sideBarBtn.frame.size.width, self.sideBarBtn.frame.size.height };
    if (self.fromMainScreen == NO) {
      self.sideBarBtn.frame = (CGRect){ self.sideBarBtn.frame.origin.x - 10, self.sideBarBtn.frame.origin.y + 2, self.sideBarBtn.frame.size.width, self.sideBarBtn.frame.size.height - 2 };
    }
  }
  
  self.sideBarBtn.userInteractionEnabled = YES;
  UITapGestureRecognizer *hamTap = [[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(slideIn)];
  [self.sideBarBtn addGestureRecognizer:hamTap];
  [self.sideBarBtn setHidden:YES];
  [self.view addSubview:self.sideBarBtn];
  
}

- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning];
}

- (void)shareAppButtonTapped
{
  [UIView animateWithDuration:0.5f animations:^{
    CGPoint center = _reportButton.center;
    if (_isShareIconsVisible)
    {
      _isShareIconsVisible = NO;
      center.y -= BUTTON_SIZE;
    }
    else
    {
      _isShareIconsVisible = YES;
      center.y += BUTTON_SIZE;
    }
    
    _reportButton.center = center;
  } completion:^(BOOL finished){

  }];
}

- (void)shareFacebookButtonTapped
{
  NSMutableDictionary *data = [NSMutableDictionary dictionary];
  NSString *link = [NSString stringWithFormat:@"http://ibuildapp.com/market/%@/%ld", [_curAppName stringByReplacingOccurrencesOfString:@" " withString:@"-"], (long)_curAppId];
  NSString *message = [NSString stringWithFormat:@"%@: %@ %@", NSLocalizedString(@"masterApp_share_message", @"iBuildApp Market"), _curAppName, link];

  [data setObject:message forKey:@"message"];

  [data setObject:kIBAMarketLocalizedName forKey:@"name"];
  
  _aSha.viewController = self.view.window.rootViewController;
  _aSha.delegate = self;
  
  [_aSha shareContentUsingService:auth_ShareServiceTypeFacebook fromUser:_aSha.user withData:data showLoginRequiredPrompt:NO];

  _isShareIconsVisible = NO;
}

- (void)shareTwitterButtonTapped
{
  NSString *link = [NSString stringWithFormat:@"http://ibuildapp.com/market/%@/%ld", [_curAppName stringByReplacingOccurrencesOfString:@" " withString:@"-"], (long)_curAppId];
  NSString *message = [NSString stringWithFormat:@"%@: %@ %@", NSLocalizedString(@"masterApp_share_message", @"iBuildApp Market"), _curAppName, link];
  NSMutableDictionary *data = [NSMutableDictionary dictionary];
  [data setObject:message forKey:@"additionalText"];
  
  _aSha.viewController = self.view.window.rootViewController;
  _aSha.delegate = self;
  
  [_aSha shareContentUsingService:auth_ShareServiceTypeTwitter fromUser:_aSha.user withData:data];
  
  _isShareIconsVisible = NO;
}

- (void)shareEmailButtonTapped
{
  NSString *htmlConten = @"";
  
  if (_curAppId && _curAppName)
  {
    NSString *link = [NSString stringWithFormat:@"http://ibuildapp.com/market/%@/%ld", [_curAppName stringByReplacingOccurrencesOfString:@" " withString:@"-"], (long)_curAppId];
    htmlConten = [NSString stringWithFormat:@"%@: %@ %@", NSLocalizedString(@"masterApp_share_message", @"iBuildApp Market"), _curAppName, link];
  }
 
  
  [functionLibrary  callMailComposerWithRecipients:nil
                                        andSubject:kIBAMarketLocalizedName
                                           andBody:htmlConten
                                            asHTML:YES
                                    withAttachment:nil
                                          mimeType:@""
                                          fileName:@""
                                    fromController:self
                                          showLink:NO];
    _isShareIconsVisible = NO;
}

- (void)shareSmsButtonTapped
{

  NSMutableDictionary *data = [NSMutableDictionary dictionary];
  NSString *link = [NSString stringWithFormat:@"http://ibuildapp.com/market/%@/%ld", [_curAppName stringByReplacingOccurrencesOfString:@" " withString:@"-"], (long)_curAppId];
  NSString *message = [NSString stringWithFormat:@"%@: %@ %@", NSLocalizedString(@"masterApp_share_message", @"iBuildApp Market"), _curAppName, link];

  [data setObject:message forKey:@"message"];
  
  _aSha.viewController = self.view.window.rootViewController;
  
  [_aSha shareContentUsingService:auth_ShareServiceTypeSMS fromUser:_aSha.user withData:data];

  _isShareIconsVisible = NO;
}

-(void) favApp: (UITapGestureRecognizer *)rec
{
  NSString* appIdStr = [mBASettings sharedInstance].currentAppId;
  NSInteger app_id = 0;
  if (appIdStr)
    app_id = [appIdStr intValue];
  
  Boolean appIsFav = [[BuisinessApp applicationTable] appInFavourites: app_id];
  
  UIImage*  favouriteStatusImage;
  FavouritedStatus status;
  
#ifdef MASTERAPP_STATISTICS
  [[BuisinessApp analyticsManager] logAppFavoritesPressed];
#endif
  
  if (appIsFav)
  {
    status = rateDown;
    favouriteStatusImage = [UIImage imageNamed:@"favorite"];
  }
  else
  {
    [[BuisinessApp applicationTable] addToFavourites: app_id];
    [[BuisinessApp applicationTable] setActiveState:ACTIVE forAppWithId:app_id];
    status = rateUp;
    favouriteStatusImage = [UIImage imageNamed:@"favorite_on"];
  }
  self.sideBarFavorite.image = favouriteStatusImage;
  
  NetworkStatus internetStatus = [((TAppDelegate *)[[UIApplication sharedApplication] delegate])
                                  .internetReachable currentReachabilityStatus];
  NetworkStatus hostStatus     = [((TAppDelegate *)[[UIApplication sharedApplication] delegate])
                                  .hostReachable currentReachabilityStatus];
  
  if(internetStatus == NotReachable || hostStatus == NotReachable)
  {
    NSLog(@"No internet connection!");
    
    if(appIsFav)
    {
      [[BuisinessApp applicationTable] setActiveState:INACTIVE forAppWithId:app_id];
      [[BuisinessApp applicationTable] setFavouritedState:UNFAVOURITED forAppWithId:app_id];
    }
    else
    {
      [[BuisinessApp applicationTable] setFavouritedState:FAVOURITED forAppWithId:app_id];
    }
  }
  else
  {
    [[BuisinessApp rest] performRatingChangeForAppWithId:app_id uuid:appGetUID() andStatus:status];
    if(appIsFav)
    {
      [[BuisinessApp applicationTable] removeFromFavourites: app_id];
    }
  }
}

+ (NSData *)postToUrl:(NSURL*)url form:(NSDictionary*)form andImage:(UIImage*)img imageKey:(NSString*)imageKey error:(NSError**)error returningResponse:(NSURLResponse**)response
{
  NSLog(@"postToUrl:%@ Form:%@ andImage:%@ imageKey:%@",url,form,img,imageKey);
  NSString *boundary = @"------WebKitFormBoundaryO74I2GrJDX6YLZB8";
  NSString *contentType = [NSString stringWithFormat:@"multipart/form-data; boundary=%@", boundary];
  NSMutableURLRequest *request = [NSMutableURLRequest requestWithURL:url];
  [request setHTTPMethod:@"POST"];
  [request setTimeoutInterval:15.0];
  [request addValue:contentType forHTTPHeaderField:@"Content-Type"];
  
  NSMutableData *body = [NSMutableData data];
  
  [body appendData:[[NSString stringWithFormat:@"\r\n--%@\r\n", boundary] dataUsingEncoding:NSUTF8StringEncoding]];
  [body appendData:[[NSString stringWithFormat:@"Content-Disposition: form-data; name=\"%@\"; filename=\"screenshot.jpg\"\r\n", imageKey] dataUsingEncoding:NSUTF8StringEncoding]];
  [body appendData:[@"Content-Type: application/octet-stream\r\n\r\n" dataUsingEncoding:NSUTF8StringEncoding]];
  [body appendData:[NSData dataWithData:UIImageJPEGRepresentation(img, 1)]];
  
  for (NSString *key in [form allKeys])
  {
    NSString *value = [form objectForKey:key];
    [body appendData:[[NSString stringWithFormat:@"\r\n--%@\r\n", boundary] dataUsingEncoding:NSUTF8StringEncoding]];
    [body appendData:[[NSString stringWithFormat:@"Content-Disposition: form-data; name=\"%@\"\r\n\r\n%@",key, value] dataUsingEncoding:NSUTF8StringEncoding]];
  }
  
  [body appendData:[[NSString stringWithFormat:@"\r\n--%@\r\n", boundary] dataUsingEncoding:NSUTF8StringEncoding]];
  
  [request setHTTPBody:body];
  return [NSURLConnection sendSynchronousRequest:request returningResponse:response error:error];
}

- (BOOL)shouldAutorotateToInterfaceOrientation:(UIInterfaceOrientation)interfaceOrientation
{
  return UIInterfaceOrientationIsPortrait(interfaceOrientation); // YES
}

- (BOOL)shouldAutorotate
{
  return YES;
}

- (NSUInteger)supportedInterfaceOrientations
{
  return UIInterfaceOrientationMaskPortrait | UIInterfaceOrientationMaskPortraitUpsideDown;
}

- (UIInterfaceOrientation)preferredInterfaceOrientationForPresentation
{
  return UIInterfaceOrientationPortrait;
}

- (void) reportClick
{
  [[UIApplication sharedApplication] setNetworkActivityIndicatorVisible:YES];
  
  NSMutableDictionary * formData = [NSMutableDictionary dictionary];
  [formData setObject:appProjectID() forKey:@"app_id"];
  [formData setObject:@"rep_forbidden_content" forKey:@"action"];
  
  [ReportSideBarVC postToUrl:[NSURL URLWithString:@"http://ibuildapp.com/endpoint/masterapp.php"]
                        form:formData
                    andImage:self.screenshotImage
                    imageKey:@"screenshot"
                       error:nil
           returningResponse:nil];
  
  [[UIApplication sharedApplication] setNetworkActivityIndicatorVisible:NO];
  
  [UIView animateWithDuration:2.5f
                        delay:2.5f
                      options:UIViewAnimationOptionCurveEaseOut
                   animations:^{
                   } completion:^(BOOL finished) {
                     NSString *msg = NSLocalizedString(@"masterApp_ComolainSended", @"Your complain has been sent and will be consider in short");
                     
                     UIAlertView *alert = [[UIAlertView alloc] initWithTitle:@""
                                                                     message:msg
                                                                    delegate:nil
                                                           cancelButtonTitle:nil
                                                           otherButtonTitles:@"OK", nil];
                     [alert show];
                   }];
}


-(void) backFromApp1
{
  [self toController:[BuisinessApp lastPresenter]];
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


- (void) slideOut
{
  NSLog(@"slide out");
  [UIView animateWithDuration:0.5f
                        delay:0.0f
                      options:UIViewAnimationOptionCurveEaseOut
                   animations:^{
                     self.screenSlide.frame = (CGRect){self.screenSlide.frame.origin.x - 220,self.screenSlide.frame.origin.y ,self.screenSlide.frame.size.width,self.screenSlide.frame.size.height};
                     self.sideBar.frame = (CGRect){100, 0, 220, 800};
                   } completion:^(BOOL finished) {
                     [self.sideBarBtn setHidden:NO];
                   }];
  
}

- (void) slideIn
{
  NSLog(@"slide in");
  [self.sideBarBtn setHidden:YES];
  [UIView animateWithDuration:0.5f
                        delay:0.0f
                      options:UIViewAnimationOptionCurveEaseOut
                   animations:^{
                     self.screenSlide.frame = (CGRect){self.screenSlide.frame.origin.x + 220,self.screenSlide.frame.origin.y ,self.screenSlide.frame.size.width,self.screenSlide.frame.size.height};
                     self.sideBar.frame = (CGRect){320, 0, 220, 800};

                   } completion:^(BOOL finished) {
                     [self.navigationController popViewControllerAnimated:NO];  
                   }];
}

- (void) getScreenshot
{
  CALayer *layer = [[UIApplication sharedApplication] keyWindow].layer;
  CGFloat scale = [UIScreen mainScreen].scale;
  UIGraphicsBeginImageContextWithOptions(layer.frame.size, NO, scale);
  
  [layer renderInContext:UIGraphicsGetCurrentContext()];
  self.screenshotImage = UIGraphicsGetImageFromCurrentImageContext();
  
  UIGraphicsEndImageContext();
}

- (void)didShareDataForService:(auth_ShareServiceType)serviceType error:(NSError *)error
{
#ifdef MASTERAPP_STATISTICS
  if (!error)
  {
    switch (serviceType) {
      case auth_ShareServiceTypeFacebook:
        [[BuisinessApp analyticsManager] logSharingByFacebookWithAppId:_curAppId];
        break;
      case auth_ShareServiceTypeTwitter:
        [[BuisinessApp analyticsManager] logSharingByTwitterWithAppId:_curAppId];
        break;
      case auth_ShareServiceTypeSMS:
        [[BuisinessApp analyticsManager] logSharingBySmsWithAppId:_curAppId];
        break;
      default:
        NSLog(@"doneSharingDataForService: service type :%d", serviceType);
        break;
    }
  }
#endif
}

- (void)mailComposeController:(MFMailComposeViewController *)controller
          didFinishWithResult:(MFMailComposeResult)composeResult
                        error:(NSError *)error;
{
#ifdef MASTERAPP_STATISTICS
  if (composeResult == MFMailComposeResultSent)
    [[BuisinessApp analyticsManager] logSharingByEmailWithAppId:_curAppId];
#endif
  [self dismissModalViewControllerAnimated:YES];
}


@end
