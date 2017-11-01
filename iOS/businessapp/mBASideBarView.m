#import "mBASideBarView.h"
#import "buisinessapp.h"

@interface mBASideBarView()

@property(nonatomic, strong) UIButton *shareFacebookBtn;
@property(nonatomic, strong) UIButton *shareTwitterBtn;
@property(nonatomic, strong) UIButton *shareEmailBtn;
@property(nonatomic, strong) UIButton *shareSMSBtn;
@property(nonatomic, strong) UIView *shareButtonsContentView;
@property(nonatomic, strong) UIView *aboutUsBckgrdView;

@end

@implementation mBASideBarView

@synthesize shareFacebookBtn, shareTwitterBtn, shareEmailBtn, shareSMSBtn, shareButtonsContentView, aboutUsBckgrdView;

- (id)initWithFrame:(CGRect)frame
{
    self = [super initWithFrame:frame];
    if (self) {
      
      _favouritesLabel = nil;
        _facebookLabel = nil;
        _twitterLabel = nil;
      _inviteLabel = nil;
      _aboutUsLabel = nil;
      _sharingMode = NO;
      
      shareFacebookBtn = nil;
      shareTwitterBtn  = nil;
      shareEmailBtn    = nil;
      shareSMSBtn      = nil;
      shareButtonsContentView = nil;
      aboutUsBckgrdView = nil;
      
      self.backgroundColor = kSideBarBackgroundColor;
      [self setupSideBar];
    }
    return self;
}

- (void)setupSharingButtons
{
  if (!self.shareButtonsContentView)
  {
    self.shareButtonsContentView = [[UIView alloc] init];
    self.shareButtonsContentView.backgroundColor = [UIColor clearColor];
    self.shareButtonsContentView.frame = CGRectZero;

  }
    
    
  if (!self.shareFacebookBtn)
  {
    self.shareFacebookBtn = [UIButton buttonWithType:UIButtonTypeCustom];
    UIImage *facebookImage = [UIImage imageNamed:@"mBA_share_facebook"];
    [self.shareFacebookBtn setImage:facebookImage forState:UIControlStateNormal];
    self.shareFacebookBtn.frame = CGRectMake(15, 0, facebookImage.size.width, facebookImage.size.height);
    [self.shareFacebookBtn addTarget:self action:@selector(shareFacebookBtnClicked) forControlEvents:UIControlEventTouchUpInside];
  }
  
  if (!self.shareTwitterBtn)
  {
    self.shareTwitterBtn = [UIButton buttonWithType:UIButtonTypeCustom];
    UIImage *twitterImage = [UIImage imageNamed:@"mBA_share_twitter"];
    [self.shareTwitterBtn setImage:twitterImage forState:UIControlStateNormal];
    self.shareTwitterBtn.frame = CGRectMake(65, 0, twitterImage.size.width, twitterImage.size.height);
    [self.shareTwitterBtn addTarget:self action:@selector(shareTwitterBtnClicked) forControlEvents:UIControlEventTouchUpInside];
  }
  
  if (!self.shareEmailBtn)
  {
    self.shareEmailBtn = [UIButton buttonWithType:UIButtonTypeCustom];
    UIImage *emailImage = [UIImage imageNamed:@"mBA_share_email"];
    [self.shareEmailBtn setImage:emailImage forState:UIControlStateNormal];
    self.shareEmailBtn.frame = CGRectMake(115, 0, emailImage.size.width, emailImage.size.height);
    [self.shareEmailBtn addTarget:self action:@selector(shareEmailBtnClicked) forControlEvents:UIControlEventTouchUpInside];
  }
  
  if (!self.shareSMSBtn)
  {
    self.shareSMSBtn = [UIButton buttonWithType:UIButtonTypeCustom];
    UIImage *smsImage = [UIImage imageNamed:@"mBA_share_sms"];
    [self.shareSMSBtn setImage:smsImage forState:UIControlStateNormal];
    self.shareSMSBtn.frame = CGRectMake(165, 0, smsImage.size.width, smsImage.size.height);
    [self.shareSMSBtn addTarget:self action:@selector(shareSMSBtnClicked) forControlEvents:UIControlEventTouchUpInside];
  }
  
  [self.shareButtonsContentView addSubview:self.shareFacebookBtn];
  [self.shareButtonsContentView addSubview:self.shareTwitterBtn];
  [self.shareButtonsContentView addSubview:self.shareEmailBtn];
  [self.shareButtonsContentView addSubview:self.shareSMSBtn];
}

- (void) setupSideBar
{
  [self setupSharingButtons];
  
  CGRect menuItemFrame = (CGRect){kSideBarMenuItemsHorizontalPadding,
    kSideBarMenuPaddingTop,
    kSideBarMenuItemsSeparatorWidth,
    kSideBarMenuItemsTextLabelHeight};
  
  _favouritesLabel = [[UILabel alloc] initWithFrame:menuItemFrame];
  _favouritesLabel = [self setupMenuItemLabel:_favouritesLabel];
  _favouritesLabel.text = kSideBarFavouritesLabelText;
  _favouritesLabel.textAlignment = NSTextAlignmentLeft;
  _favouritesLabel.clipsToBounds = NO;
  
  [self addSubview:_favouritesLabel];
  
  menuItemFrame.origin.y += kSideBarMenuItemsTextLabelHeight + kSideBarMenuItemsSeparatorHeight;
  
  _inviteLabel = [[UILabel alloc] initWithFrame:menuItemFrame];
  _inviteLabel = [self setupMenuItemLabel:_inviteLabel];
  _inviteLabel.text = kSideBarInviteLabelText;
  _inviteLabel.textAlignment = NSTextAlignmentLeft;
  
  [_inviteLabel addSubview:[self setupSeparator]];
  [self addSubview:_inviteLabel];
  
  menuItemFrame.origin.y += kSideBarMenuItemsTextLabelHeight + kSideBarMenuItemsSeparatorHeight;

  self.shareButtonsContentView.frame = CGRectMake(0, menuItemFrame.origin.y, kSideBarWidthForSharingMode, kSideBarSharingButtonsHeight);
  self.shareButtonsContentView.hidden = YES;
  [self addSubview:self.shareButtonsContentView];

  self.aboutUsBckgrdView = [[UIView alloc] init];
  self.aboutUsBckgrdView.frame = CGRectMake(0, menuItemFrame.origin.y, kSideBarWidthForSharingMode, kSideBarSharingButtonsHeight);
  self.aboutUsBckgrdView.backgroundColor = self.backgroundColor;
  [self addSubview:self.aboutUsBckgrdView];
  
  
  _aboutUsLabel = [[UILabel alloc] initWithFrame:menuItemFrame];
  _aboutUsLabel = [self setupMenuItemLabel:_aboutUsLabel];
  _aboutUsLabel.text = kSideBarAboutUsLabelText;
  _aboutUsLabel.textAlignment = NSTextAlignmentLeft;
  
  [_aboutUsLabel addSubview:[self setupSeparator]];
  [self addSubview:_aboutUsLabel];
  
  
  UITapGestureRecognizer *favouritesTapRecognizer = [[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(favouritesMenuItemSelected)];
  
  UITapGestureRecognizer *inviteTapRecognizer = [[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(inviteMenuItemSelected)];
  
  UITapGestureRecognizer *aboutUsTapRecognizer = [[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(aboutUsMenuItemSelected)];
  
  _favouritesLabel.userInteractionEnabled = YES;
  _inviteLabel.userInteractionEnabled  = YES;
  _aboutUsLabel.userInteractionEnabled = YES;
  
  [_favouritesLabel addGestureRecognizer:favouritesTapRecognizer];
  [_inviteLabel addGestureRecognizer:inviteTapRecognizer];
  [_aboutUsLabel addGestureRecognizer:aboutUsTapRecognizer];
  
}

- (void)favouritesMenuItemSelected
{
  [self.sideBarDelegate favouritesMenuItemSelected];
}

- (void)aboutUsMenuItemSelected
{
   [self.sideBarDelegate aboutUsMenuItemSelected];
}

- (void)inviteMenuItemSelected
{
  NSLog(@"sharingNode: %d", _sharingMode);
  
  if (_sharingMode == YES) {
    [self hideSharingButtons];
  }
  else {
    [self showSharingButtons];
  }
}



- (void)shareFacebookBtnClicked
{
  NSLog(@"shareFacebookBtnClicked");
  
  [self.sideBarDelegate inviteViaFacebook];
}


- (void)shareTwitterBtnClicked
{
  NSLog(@"shareTwitterBtnClicked");
  
  [self.sideBarDelegate inviteViaTwitter];
}


- (void)shareEmailBtnClicked
{
  NSLog(@"shareEmailBtnClicked");
  
  [self.sideBarDelegate inviteViaEmail];
}

- (void)shareSMSBtnClicked
{
  NSLog(@"shareSMSBtnClicked");
//  
  [self.sideBarDelegate inviteViaSMS];
}

- (void)showSharingButtons
{
  _sharingMode = YES;
  
  
  [UIView animateWithDuration:0.5f animations:^{
    [_inviteLabel setUserInteractionEnabled:NO];
  
    CGRect aboutUsLabelFrame = self.aboutUsLabel.frame;
    aboutUsLabelFrame.origin.y += kSideBarSharingButtonsHeight;
    self.aboutUsLabel.frame = aboutUsLabelFrame;
  
    CGRect aboutUsBckgrdViewFrame = self.aboutUsBckgrdView.frame;
    aboutUsBckgrdViewFrame.origin.y += kSideBarSharingButtonsHeight;
    self.aboutUsBckgrdView.frame = aboutUsBckgrdViewFrame;
  
  
    self.shareButtonsContentView.hidden = NO;
    self.shareButtonsContentView.alpha = 1;
    
  } completion:^(BOOL finished){

    [_inviteLabel setUserInteractionEnabled:YES];
  }];
}

- (void)hideSharingButtons
{
  _sharingMode = NO;
    
    [UIView animateWithDuration:0.5f animations:^{
        [_inviteLabel setUserInteractionEnabled:NO];
      
        CGRect aboutUsLabelFrame = self.aboutUsLabel.frame;
        aboutUsLabelFrame.origin.y -= kSideBarSharingButtonsHeight;
        self.aboutUsLabel.frame = aboutUsLabelFrame;
        
        CGRect aboutUsBckgrdViewFrame = self.aboutUsBckgrdView.frame;
        aboutUsBckgrdViewFrame.origin.y -= kSideBarSharingButtonsHeight;
        self.aboutUsBckgrdView.frame = aboutUsBckgrdViewFrame;

    } completion:^(BOOL finished){
       self.shareButtonsContentView.hidden = YES;
      [_inviteLabel setUserInteractionEnabled:YES];
    }];
}

- (UIView *) setupSeparator
{
  CGRect separatorFrame = (CGRect){0.0f,
    0.f,
    kSideBarMenuItemsSeparatorWidth,
    kSideBarMenuItemsSeparatorHeight};
  
  UIView *separator = [[UIView alloc] initWithFrame:separatorFrame];
  separator.backgroundColor = kSideBarSeparatorColor;
  
  return separator;
}

- (UILabel *) setupMenuItemLabel:(UILabel *)label
{
  label.textColor = kSideBarMenuItemsFontColor;
  label.backgroundColor = [UIColor clearColor];
  label.font = [UIFont systemFontOfSize:kSideBarMenuItemsFontSize];
  label.textAlignment = NSTextAlignmentLeft;
  return label;
}

- (void) dealloc
{
  _favouritesLabel = nil;
    _facebookLabel = nil;
    _twitterLabel = nil;
  _inviteLabel = nil;
  _aboutUsLabel = nil;
  shareFacebookBtn = nil;
  shareTwitterBtn = nil;
  shareEmailBtn = nil;
  shareButtonsContentView = nil;
  aboutUsBckgrdView = nil;
}

@end
