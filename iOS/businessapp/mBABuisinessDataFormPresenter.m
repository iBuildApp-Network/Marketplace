
#import "mBABuisinessDataFormPresenter.h"

@implementation mBABuisinessDataFormPresenter

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
  [self.navigationController setNavigationBarHidden: YES];
  [[UIApplication sharedApplication] setStatusBarHidden: NO];
}

- (void)viewDidLoad
{
  [super viewDidLoad];
  self.view.backgroundColor = [UIColor whiteColor];
}

- (BOOL)shouldAutorotate
{
  return NO;
}

@end
