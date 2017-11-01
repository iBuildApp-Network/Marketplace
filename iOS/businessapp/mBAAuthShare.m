
#import "mBAAuthShare.h"

#import "mBAAnalyticsManager.h"
#import "buisinessapp.h"

#import "mBAAuthShareReplyVC.h"
#import "NSString+colorizer.h"

#define kFacebookReplyVCNavBarColor [@"#314f90" asColor]
#define kTwitterReplyVCNavBarColor  [@"#57adee" asColor]

@interface mBAAuthShare()

-(void)authTwitterWithCompletion:(SEL)completionSelector andData:(NSMutableDictionary *)data;

@end

@implementation mBAAuthShare

-(BOOL)isAuthentificatedWithTwitter {
#ifdef MASTERAPP_STATISTICS
  return NO;
#else
  return [super isAuthentificatedWithTwitter];
#endif
}

-(auth_ShareServiceType)authenticatePersonUsingService:(auth_ShareServiceType)service
                              andCredentials:(NSMutableDictionary *)credentials
                              withCompletion:(SEL)completionSelector
                                     andData:(NSMutableDictionary *)data
               shouldShowLoginRequiredPrompt:(BOOL)showLoginRequired {
#ifdef MASTERAPP_STATISTICS
  if(service == auth_ShareServiceTypeTwitter){
    [self authTwitterWithCompletion:completionSelector andData:data];
    return auth_ShareServiceTypeNone;
  } else {
    return [super authenticatePersonUsingService:service
                                  andCredentials:credentials
                                  withCompletion:completionSelector
                                         andData:data
                   shouldShowLoginRequiredPrompt:showLoginRequired];
  }
#else
  return [super authenticatePersonUsingService:service
                                andCredentials:credentials
                                withCompletion:completionSelector
                                       andData:data
                 shouldShowLoginRequiredPrompt:showLoginRequired];
#endif
}


-(void)typeMessageWithData:(NSMutableDictionary *)data
{
  self.replyVC = [[mBAAuthShareReplyViewController alloc] init];
  
  self.replyVC.data = data;
  
  UIColor *navBarColor = nil;
  NSString *postBtnTitle = nil;
  
  if (self.shareOnTwitterWithCustomWindow)
  {
    navBarColor = kTwitterReplyVCNavBarColor;
    postBtnTitle = NSLocalizedString(@"masterApp_Share_NavBar_Twitter_PostBtnTitle", @"Tweet");
    [((mBAAuthShareReplyViewController *) self.replyVC) setAppearance:mBAAuthShareReplyViewControllerTwitterAppearance];
  }
  else
  {
    navBarColor = kFacebookReplyVCNavBarColor;
    postBtnTitle = NSLocalizedString(@"masterApp_Share_NavBar_Facebook_PostBtnTitle", @"Post");
    [((mBAAuthShareReplyViewController *) self.replyVC) setAppearance:mBAAuthShareReplyViewControllerFacebookAppearance];
  }
  
  BOOL navigationControllerAbscent = self.viewController.navigationController == nil;
  
  UINavigationController *navController = [[UINavigationController alloc] initWithRootViewController:self.replyVC];
  navController.modalPresentationStyle = UIModalPresentationFormSheet;
  
  navController.navigationBar.barStyle = UIBarStyleDefault;
  navController.navigationBar.translucent =  NO;
  navController.navigationBar.tintColor = navBarColor;
  
#ifdef __IPHONE_7_0
  if([[[UIDevice currentDevice] systemVersion] compare:@"7.0" options:NSNumericSearch] != NSOrderedAscending)
    navController.navigationBar.barTintColor = navBarColor;
  
  NSMutableDictionary *titleTextAttributes = [NSMutableDictionary dictionaryWithObjectsAndKeys:[UIColor whiteColor], NSForegroundColorAttributeName,
                                              [UIFont systemFontOfSize:18], NSFontAttributeName,
                                              nil];
  navController.navigationBar.titleTextAttributes = titleTextAttributes;

#endif
  
  UIBarButtonItem *cancelBtn = [[UIBarButtonItem alloc] initWithBarButtonSystemItem:UIBarButtonSystemItemCancel
                                                                              target:self
                                                                              action:@selector(dismissReplyViewController)];
  UIBarButtonItem *sendBtn = [[UIBarButtonItem alloc] init];
  sendBtn.style  = UIBarButtonItemStylePlain;
  sendBtn.target = self;
  
  if(navigationControllerAbscent)
  {
    UIColor *cancelTintColor = nil, *sendTintColor = nil;
    
    if(SYSTEM_VERSION_LESS_THAN(@"7.0"))
    {
      cancelTintColor = [UIColor blackColor];
      sendTintColor = [UIColor colorWithRed:0.0 green:122.0/255.0 blue:1.0 alpha:1.0];
    }
    else
    {
      cancelTintColor = [UIColor whiteColor];
      sendTintColor = [UIColor whiteColor];
    }
    
    cancelBtn.tintColor = cancelTintColor;
    sendBtn.tintColor = sendTintColor;
  }
  
  SEL actionToPerform;
  NSString *screenTitle = NSLocalizedString(@"masterApp_Share_NavBar_ShareTitle", @"Share");
  
  if ([self shareOnTwitterWithCustomWindow])
  {
    actionToPerform = @selector(postTweetWithData:);
  }
  else
  {
    actionToPerform = @selector(shareToFacebookWithData:);
  }
  
  sendBtn.action = actionToPerform;
  sendBtn.title  = postBtnTitle;
  
  self.replyVC.navigationItem.title = screenTitle;
  self.replyVC.navigationItem.leftBarButtonItem  = cancelBtn;
  self.replyVC.navigationItem.rightBarButtonItem = sendBtn;
  
  if (!navigationControllerAbscent)
    [self.viewController.navigationController presentViewController:navController animated:YES completion:nil];
  else
    [self.viewController presentViewController:navController animated:YES completion:nil];
}

-(void)dismissReplyViewController
{
#ifdef MASTERAPP_STATISTICS
  mBAAuthShareReplyViewControllerAppearance appearance = ((mBAAuthShareReplyViewController *)self.replyVC).appearance;
  
  switch (appearance) {
    case mBAAuthShareReplyViewControllerFacebookAppearance:
      [[BuisinessApp analyticsManager] logAppSharingFBResult:NO];
      break;
    case mBAAuthShareReplyViewControllerTwitterAppearance:
      [[BuisinessApp analyticsManager] logAppSharingTwitterResult:NO];
      break;
    default:
      break;
  }
#endif
  [self.viewController dismissViewControllerAnimated:YES completion:nil];
}
@end
