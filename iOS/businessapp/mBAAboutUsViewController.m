
#import "mBAAboutUsViewController.h"
#import "buisinessapp.h"
#import "NSString+size.h"
#import "mBASearchBarView.h"

@interface mBAAboutUsViewController () {
  
  UIToolbar *myTopToolbar;
  
  UIWebView *aboutUsWebView;
  
  mBAMainMenuPresenter *_presenter;
  
  float toolbarHeight;
  float addingHeight;
  float backButonStart;
}

@end

@implementation mBAAboutUsViewController

- (id)initWithPresenter:(mBAMainMenuPresenter *) presenter
{
    self = [super init];
    if (self) {
        myTopToolbar = nil;
      aboutUsWebView = nil;
      _presenter = presenter;
      
      if (SYSTEM_VERSION_GREATER_THAN_OR_EQUAL_TO(@"7.0"))
        addingHeight             =  20.0f;
      else
        addingHeight             =  0.0f;
      
      toolbarHeight      =    44.0f;
      backButonStart       = (float) (12.0 + addingHeight);
    }
    return self;
}

- (BOOL) shouldAutorotate
{
  return NO;
}

- (void)viewDidLoad
{
  [super viewDidLoad];
  self.view.backgroundColor = [UIColor whiteColor];
  [self setupToolbar];
  [self placeAboutUsImage];
  [self placeAboutUsText];
  [self placeSeparatorOnToolbar];
}

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
}

- (void)viewWillAppear:(BOOL)animated
{
  [super viewWillAppear:animated];
  [aboutUsWebView.scrollView setContentOffset:CGPointZero animated:YES];
}

- (void)placeAboutUsImage
{
  CGRect aboutUsPicFrame = (CGRect){0.0f,
    myTopToolbar.frame.origin.y + myTopToolbar.frame.size.height,
    self.view.frame.size.width,
    kAboutUsPicHeight
  };
  
  UIImageView *aboutUsPicImageView = [[UIImageView alloc] initWithFrame:aboutUsPicFrame];
  UIImage *aboutUsPic = [UIImage imageNamed:@"about_us_pic"];
  aboutUsPicImageView.image = aboutUsPic;
  aboutUsPicImageView.contentMode = UIViewContentModeScaleAspectFit;
  
  [self.view addSubview:aboutUsPicImageView];
}

- (void)placeAboutUsText
{
  CGFloat maxTextWidth = self.view.bounds.size.width - 2 * kAboutUsTextHorizontalMargin;
  
  NSLineBreakMode aboutUsBreakMode = NSLineBreakByWordWrapping;
  UIFont *aboutUsFont = [UIFont systemFontOfSize:kAboutUsTextFontSize];
  
  NSString *aboutUsText1 = NSLocalizedString(@"masterApp_AboutUsText_paragraph1", @"Organize your life or find what you are looking for.");
  
  CGRect aboutUsTextFrame = (CGRect){kAboutUsTextHorizontalMargin,
    myTopToolbar.frame.origin.x + myTopToolbar.frame.size.height + kAboutUsPicHeight + kAboutUsTextMarginTop,
    maxTextWidth, 0.0f};
  
  UILabel *aboutUsTextLabelFirstParagraph = [[UILabel alloc] initWithFrame:aboutUsTextFrame];
  aboutUsTextLabelFirstParagraph.text = aboutUsText1;
  aboutUsTextLabelFirstParagraph.numberOfLines = 0;
  aboutUsTextLabelFirstParagraph.textColor = kAboutUsTextColor;
  aboutUsTextLabelFirstParagraph.lineBreakMode = aboutUsBreakMode;
  aboutUsTextLabelFirstParagraph.font = aboutUsFont;
  [aboutUsTextLabelFirstParagraph sizeToFit];
  
  [self.view addSubview:aboutUsTextLabelFirstParagraph];
  
  NSString *aboutUsText2 = NSLocalizedString(@"masterApp_AboutUsText_paragraph2", @"Organize your life or find what you are looking for.");
  
  CGRect secondParagraphFrame = aboutUsTextFrame;
  secondParagraphFrame.origin.y = aboutUsTextLabelFirstParagraph.frame.origin.y + (int)aboutUsTextLabelFirstParagraph.frame.size.height + kAboutUsTextParagraphGap;
  secondParagraphFrame.size.height = 0.0f;
  
  UILabel *aboutUsTextLabelSecondParagraph = [[UILabel alloc] initWithFrame:secondParagraphFrame];
  aboutUsTextLabelSecondParagraph.text = aboutUsText2;
  aboutUsTextLabelSecondParagraph.numberOfLines = 0;
  aboutUsTextLabelSecondParagraph.textColor = kAboutUsTextColor;
  aboutUsTextLabelSecondParagraph.lineBreakMode = aboutUsBreakMode;
  aboutUsTextLabelSecondParagraph.font = aboutUsFont;
  [aboutUsTextLabelSecondParagraph sizeToFit];
  
  [self.view addSubview:aboutUsTextLabelSecondParagraph];
  
  
  NSString *aboutUsText3 = NSLocalizedString(@"masterApp_AboutUsText_paragraph3", @"Create your own app with");
  
  CGRect thirdParagraphFrame = aboutUsTextFrame;
  thirdParagraphFrame.origin.y = aboutUsTextLabelSecondParagraph.frame.origin.y + (int)aboutUsTextLabelSecondParagraph.frame.size.height + kAboutUsTextParagraphGap;
  thirdParagraphFrame.size.height = 0.0f;
  
  
  UILabel *aboutUsTextLabelThirdParagraph = [[UILabel alloc] initWithFrame:thirdParagraphFrame];
  aboutUsTextLabelThirdParagraph.text = aboutUsText3;
  aboutUsTextLabelThirdParagraph.numberOfLines = 0;
  aboutUsTextLabelThirdParagraph.textColor = kAboutUsTextColor;
  aboutUsTextLabelThirdParagraph.lineBreakMode = aboutUsBreakMode;
  aboutUsTextLabelThirdParagraph.font = aboutUsFont;
  [aboutUsTextLabelThirdParagraph sizeToFit];
  
  [self.view addSubview:aboutUsTextLabelThirdParagraph];
  
  UIButton *webSiteButton;
  if(SYSTEM_VERSION_GREATER_THAN_OR_EQUAL_TO(@"7.0")){
    webSiteButton = [UIButton buttonWithType:UIButtonTypeSystem];
  } else {
    webSiteButton = [UIButton buttonWithType:UIButtonTypeCustom];
    webSiteButton.titleLabel.font = [UIFont systemFontOfSize:kAboutUsTextFontSize];
  }
  webSiteButton.frame = CGRectMake(0, aboutUsTextLabelThirdParagraph.frame.origin.y + aboutUsTextLabelThirdParagraph.frame.size.height, kWebSiteButtonWidth, kWebSiteButtonHeight);
  [webSiteButton addTarget:self action:@selector(openWebSite) forControlEvents:UIControlEventTouchUpInside];
  
  webSiteButton.layer.masksToBounds = YES;
  [webSiteButton setBackgroundColor:[UIColor clearColor]];
  
  webSiteButton.titleLabel.textAlignment = NSTextAlignmentLeft;
  [webSiteButton setTitle:@"ibuildapp.com" forState:UIControlStateNormal];
  
  if(SYSTEM_VERSION_GREATER_THAN_OR_EQUAL_TO(@"7.0")){
    webSiteButton.contentEdgeInsets = UIEdgeInsetsMake(-5.0, 0, 0.0, 0.0);
  } else {
    webSiteButton.contentEdgeInsets = UIEdgeInsetsMake(-5.0, 2.0, 0.0, 0.0);
  }
  
  [webSiteButton setTitleColor:[UIColor blueColor] forState:UIControlStateNormal];
  
  webSiteButton.titleLabel.hidden = NO;
  
  [self.view addSubview:webSiteButton];
}

- (void) placeSeparatorOnToolbar
{
  CGFloat originY = myTopToolbar.frame.origin.y + myTopToolbar.frame.size.height;
  CGRect separatorFrame = (CGRect){0.0f, originY, myTopToolbar.frame.size.width, kToolbarSeparatorHeight};
  
  UIView *separator = [[UIView alloc] initWithFrame:separatorFrame];
  separator.backgroundColor = kToolbarSeparatorColor;
  myTopToolbar.clipsToBounds = NO;
  
  [self.view addSubview:separator];
}


- (void)setupToolbar
{
  UIBarButtonItem *mySpacer = [[UIBarButtonItem alloc]
                                initWithBarButtonSystemItem: UIBarButtonSystemItemFlexibleSpace
                                target: nil action: nil];
  
  myTopToolbar = [[UIToolbar alloc]init];
  myTopToolbar.frame = (CGRect){0.0f, 0.0f, self.view.bounds.size.width, toolbarHeight + addingHeight};
  myTopToolbar.backgroundColor = kToolbarColor;
  [self.view addSubview:myTopToolbar];
  
  if (SYSTEM_VERSION_GREATER_THAN_OR_EQUAL_TO(@"7.0")) {
    myTopToolbar.barTintColor = kToolbarColor;
  }
  
  myTopToolbar.backgroundColor = kToolbarColor;
  myTopToolbar.tintColor = kToolbarColor;
  
  UILabel *titleLabel = [[UILabel alloc] init];
  titleLabel.frame = (CGRect){20.0f, addingHeight, 280.0f, toolbarHeight};
  titleLabel.textAlignment = NSTextAlignmentCenter;
  titleLabel.text = NSLocalizedString(@"masterApp_AboutUs", @"About us");
  titleLabel.textColor = [UIColor whiteColor];
  titleLabel.backgroundColor = [UIColor clearColor];
  [self.view addSubview: titleLabel];
  
  UIImage *backImg = [UIImage imageNamed:@"back.png"];
  UIImageView *backImgView = [[UIImageView alloc] initWithImage:backImg];
  backImgView.frame = (CGRect){7.0f, backButonStart, 13.0f, 20.0f};
  [myTopToolbar addSubview:backImgView];

  UILabel *backLabel = [[UILabel alloc] init];
  backLabel.frame = (CGRect){23.0f, addingHeight, 64.0f, toolbarHeight};
  backLabel.textAlignment = NSTextAlignmentLeft;
  backLabel.text = NSLocalizedString(@"masterApp_Back", @"Back");
  backLabel.textColor = [UIColor whiteColor];
  backLabel.font = [UIFont systemFontOfSize: 16];
  backLabel.backgroundColor = [UIColor clearColor];
  [self.view addSubview: backLabel];
  
  UIView *backButtonTapView = [[UIView alloc] init];
  backButtonTapView.frame = (CGRect){7.0f, addingHeight, 80.0f, toolbarHeight};
  [self.view addSubview:backButtonTapView];
  
  [myTopToolbar setItems:@[mySpacer] animated:NO];
  
  UITapGestureRecognizer *backRecognizer = [[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(back)];
  [backButtonTapView addGestureRecognizer:backRecognizer];
}

- (void) back
{
  [_presenter performSelector:@selector(toController:) withObject:[BuisinessApp mainMenuPresenter]];
}

- (void) setupAboutUsWebView
{
  CGRect aboutUsFrame = (CGRect){
    0.0f,
    myTopToolbar.bounds.size.height,
    self.view.bounds.size.width,
    self.view.bounds.size.height - toolbarHeight
  };
  aboutUsWebView = [[UIWebView alloc] initWithFrame:aboutUsFrame];
  
  NSString *aboutUsContentFile = [[NSBundle mainBundle] pathForResource:@"aboutus" ofType:@"html"];
  
  [aboutUsWebView loadRequest:[NSURLRequest requestWithURL:
                               [NSURL fileURLWithPath:aboutUsContentFile]]];
  
  [self.view addSubview:aboutUsWebView];
}

- (void) dealloc
{
    myTopToolbar = nil;
    aboutUsWebView = nil;
}


- (void)openWebSite
{
  [[UIApplication sharedApplication] openURL:[NSURL URLWithString:@"http://ibuildapp.com/"]];
}

@end
