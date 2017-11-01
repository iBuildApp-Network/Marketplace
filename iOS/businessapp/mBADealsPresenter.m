// IBAHeader

#import "mBADealsPresenter.h"

@implementation mBADealsPresenter

- (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil
{
  self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil];
  if (self) {
  }
  return self;
}

-(void)viewWillAppear:(BOOL)animated
{
  [super viewWillAppear: animated];

  [self.navigationController setNavigationBarHidden: NO animated: NO];
  [self.navigationItem setHidesBackButton: NO animated: NO];
  self.navigationItem.title = @"Deals";
  self.navigationController.navigationBar.barStyle = UIBarStyleBlack;
  self.tabBarController.tabBar.hidden = NO;
  self.view.backgroundColor = [UIColor whiteColor];
}

- (void)viewDidLoad {
  [super viewDidLoad];
}

- (BOOL)shouldAutorotate
{
  return NO;
}

@end
