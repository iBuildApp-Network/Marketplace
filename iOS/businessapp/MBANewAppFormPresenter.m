// IBAHeader

#import "MBANewAppFormPresenter.h"
#import "mBANewAppFormView.h"
#import "mBAMainMenuView.h"
#import "mBAMainMenuPresenter.h"
#import "TPKeyboardAvoidingScrollView.h"
#import "RestService.h"
#import "buisinessapp.h"
#import "mBACategoriesTableViewController.h"
#import "UIColor+RGB.h"

#import "reachability.h"
#import "appdelegate.h"

#import "mBACategoryTemplatesFetcher.h"
#import "mBANewBusinessDetailsVC.h"

#import <UIKit/UIKit.h>

#define NAV_BAR_COLOR [UIColor colorWithRGB:0x0c9ad5]

#define GCDBackgroundThread dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0)

@interface mBAUnrotatableNavigationController : UINavigationController
@end

@implementation mBAUnrotatableNavigationController

-(BOOL) shouldAutorotate{
  return NO;
}
@end

@interface mBANewAppFormPresenter()
 @property (nonatomic, strong, retain) NSArray *categoryTemplates; // еще проблема в этой проперти
@end

@implementation mBANewAppFormPresenter{
  mBANewAppFormView *viewBuilder;
  mBACategoriesTableViewController *categoryPickerController;
  
  UINavigationController *categoriesNavigationController;
  mBANewBusinessDetailsViewController *newBusinessDetailsViewController;
  
  NSArray *categories;
  
  UIColor *navigationColor;
}

- (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil
{
  self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil];
  if (self) {
    viewBuilder = [[mBANewAppFormView alloc] init];
    
    if(categoryPickerController == nil){
       categoryPickerController = [[mBACategoriesTableViewController alloc] initWithStyle:UITableViewStylePlain];
    }
    
    if(categoriesNavigationController == nil){
      
      categoriesNavigationController = [[mBAUnrotatableNavigationController alloc] initWithRootViewController:categoryPickerController];

      if(SYSTEM_VERSION_GREATER_THAN_OR_EQUAL_TO(@"7.0")){
        
        NSMutableDictionary *titleTextAttributes = [NSMutableDictionary dictionaryWithObject:[UIFont fontWithName:@"Arial" size:0.0] forKey: UITextAttributeFont];
        
        [titleTextAttributes setObject:[UIColor whiteColor] forKey: NSForegroundColorAttributeName];
        
        categoriesNavigationController.navigationBar.titleTextAttributes = titleTextAttributes;
      } else {
        categoriesNavigationController.navigationBar.tintColor = NAV_BAR_COLOR;
      }
    }
    
    if(newBusinessDetailsViewController == nil){
      newBusinessDetailsViewController = [[mBANewBusinessDetailsViewController alloc] init];
    }
  }
  return self;
}

- (NSArray *)getProperCategoryTemplate{
  for(NSDictionary *template in self.categoryTemplates){
    NSString *categoryId = [template objectForKey:@"categoryid"];
    if([categoryId isEqualToString:self.selectedCategoryId]){
      return [template objectForKey:@"template"];
    }
  }
  return nil;
}

- (void)dealloc
{
     categoriesNavigationController = nil;
    newBusinessDetailsViewController = nil;
    self.categoryTemplates = nil;
}

-(void)viewWillAppear:(BOOL)animated
{
  [super viewWillAppear: animated];
  [self.navigationController setNavigationBarHidden: YES];
  [[UIApplication sharedApplication] setStatusBarHidden: NO];
}

- (void)viewDidLoad
{
  [super viewDidLoad];
  NSLog(@"%f", self.view.frame.size.height);
  [self setupCategoriesList];
  [viewBuilder viewForPresenter: self];
  
  [self fetchCategoryTemplatesAsync];
  NSLog(@"%f", self.view.frame.size.height);
}

-(void)setupCategoriesList{
  if([BuisinessApp rest].categories == nil){
    if([self isNetworking]){
      [[BuisinessApp rest] fetchCategories];
      if([BuisinessApp rest].categories != nil){
        categories = [BuisinessApp rest].categories;
      } else {
        categories = [[BuisinessApp categoryTable] categoryList];
      }
    } else{
      categories = [[BuisinessApp categoryTable] categoryList];
    }
  } else {
    categories = [BuisinessApp rest].categories;
  }
  viewBuilder.categories = categories;
  categoryPickerController.categories = categories;
}

- (BOOL) isNetworking {
  NetworkStatus internetStatus = [((TAppDelegate *)[[UIApplication sharedApplication] delegate])
                                  .internetReachable currentReachabilityStatus];
  NetworkStatus hostStatus     = [((TAppDelegate *)[[UIApplication sharedApplication] delegate])
                                  .hostReachable currentReachabilityStatus];
  
  return (internetStatus != NotReachable) && (hostStatus != NotReachable);
}

- (void) fetchCategoryTemplatesAsync{

}

- (void)toController:(UIViewController *)controller
{
  UIWindow *mainWindow;
  mainWindow = [UIApplication sharedApplication].windows[0];
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
                               } completion:nil];
}

- (void) categoryTemplatesDidFinishLoading:(NSArray*)templates{
  self.categoryTemplates = templates;
  [self.categoryTemplates writeToFile:[self getPathToCategoryTemplatesList] atomically:YES];
}

- (void) categoryTemplatesDidFailLoading{
  [self readCategoryTemplatesFromPlist];
}

- (void) readCategoryTemplatesFromPlist{
  NSString *pathToCategoryTemplatesList = [self getPathToCategoryTemplatesList];
  
  if([[NSFileManager defaultManager] fileExistsAtPath:pathToCategoryTemplatesList]){
   self.categoryTemplates = [NSArray arrayWithContentsOfFile:pathToCategoryTemplatesList];
  }
}

- (NSString *)getPathToCategoryTemplatesList{
  NSArray *paths = NSSearchPathForDirectoriesInDomains(NSCachesDirectory, NSUserDomainMask, YES);
  NSString *cachesDir = paths[0];
  
  return [cachesDir stringByAppendingPathComponent:CATEGORY_TEMPLATES_FILENAME];
}

- (void)back
{
  NSLog(@"back from app");
  [self toController: [BuisinessApp mainMenuPresenter]];
}

- (BOOL)shouldAutorotate
{
  return NO;
}

- (void)showCategoriesPicker{
  [self presentModalViewController:categoriesNavigationController animated:YES];
}

- (void) showBusinessDetailsController{

}

- (void) setCategoryNameLabelText:(NSString *)text{
  if(viewBuilder != nil){
    viewBuilder.categoryNameLabel.text = text;
  }
}

@end
