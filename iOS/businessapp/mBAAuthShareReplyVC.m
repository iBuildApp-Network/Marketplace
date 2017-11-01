#import "mBAAuthShareReplyVC.h"
#import "buisinessapp.h"
#import "mBAAnalyticsManager.h"

#define kTextViewLeftMargin 15.f

@interface mBAAuthShareReplyViewController ()

@end

@implementation mBAAuthShareReplyViewController
@synthesize textView = _textView;
@synthesize data = _data;

- (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil {
  self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil];
  if (self) {
    self.appearance = mBAAuthShareReplyViewControllerAppearanceNone;
  }
  return self;
}

- (void)viewDidLoad {
  [super viewDidLoad];
}

#pragma mark - UI
- (void)drawInterface
{
  self.view.backgroundColor = [UIColor whiteColor];
  
  _textView = [[UITextView alloc] init];
  _textView.frame = CGRectMake(kTextViewLeftMargin, 0, self.view.bounds.size.width - 2*kTextViewLeftMargin, self.view.bounds.size.height - 220); 
  _textView.autoresizesSubviews = YES;
  _textView.autoresizingMask    = UIViewAutoresizingFlexibleWidth | UIViewAutoresizingFlexibleHeight;
  _textView.backgroundColor = [UIColor clearColor];
  _textView.font = [UIFont systemFontOfSize:17.0f];
  _textView.textColor = [UIColor colorWithWhite:0 alpha:0.5];
  _textView.layer.masksToBounds = YES;
  
  if (_data && [_data objectForKey:@"message"])
    _textView.text = [_data objectForKey:@"message"];
  
  [self.view addSubview:self.textView];
  
#ifdef MASTERAPP_STATISTICS
  switch(_appearance){
    case mBAAuthShareReplyViewControllerFacebookAppearance:
      [[BuisinessApp analyticsManager] logAppSharingFBAttempt];
      break;
    case mBAAuthShareReplyViewControllerTwitterAppearance:
      [[BuisinessApp analyticsManager] logAppSharingTwitterAttempt];
      break;
    default:
      break;
  }
#endif
  
  [self.textView becomeFirstResponder];
}

-(void)viewWillAppear:(BOOL)animated
{
  [super viewWillAppear:animated];
  self.view.backgroundColor = [UIColor whiteColor];
}

- (void)viewWillDisappear:(BOOL)animated
{
  if (_textView)
    [_textView resignFirstResponder];
  
  [super viewWillDisappear:animated];
}

@end
