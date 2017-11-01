
#import "mBASearchBarView.h"
#import "NSString+size.h"

@interface mBASearchBarView()
{
    CGRect searchTextFieldCollapsedFrame;
    CGRect searchTextFieldExpandedFrame;
}
    @property (nonatomic, assign, readwrite) mBASearchBarViewAppearance appearance;
    @property (nonatomic, retain) UILabel *cancelSearchLabel;
    @property (nonatomic, retain) UIView *searchIconView;
    @property (nonatomic, retain) UIView *hamburgerView;
    @property (nonatomic, retain) UIView *backLabelView;
    @property (nonatomic, retain) UILabel *titleLabel;
    @property (nonatomic, retain) UILabel *foundApplicationsCountLabel;

@end

@implementation mBASearchBarView{
  /**
   * Width for hamburger imageview
   * or back item depending on appearance
   */
  CGFloat leftItemWith;
}

@synthesize searchInProgress = searchInProgress;


- (id) initWithFrame:(CGRect)frame {
    self = [super initWithFrame:frame];
    if(self){
      self.appearance = mBASearchBarViewMainScreenAppearance;
      NSLog(@"mBASearchBarView: WARNING initWithFrame: constructor invoked, assuming mBASearchBarViewMainScreenAppearance");
      [self setupSelf];
    }
    return self;
}

- (id)initWithFrame:(CGRect)frame apperance:(mBASearchBarViewAppearance) appearance
{
  self = [super initWithFrame:frame];
  
  if(self){
    self.appearance = appearance;
    [self setupSelf];
  }
  return self;
}

- (id)initWithApperance:(mBASearchBarViewAppearance) appearance
{
  CGRect screenRect = [[UIScreen mainScreen] bounds];
  CGFloat screenWidth = screenRect.size.width;
  CGRect searchBarRect = (CGRect){0.0f, 0.0f, screenWidth, kToolbarHeight};
  
  self = [super initWithFrame:searchBarRect];
  
  if(self){
    self.appearance = appearance;
    [self setupSelf];
  }
  return self;
}

- (void)setupSelf
{
  searchInProgress = NO;
  _titleLabel = nil;
  _searchTextField = nil;
  _cancelSearchLabel = nil;
  _searchIconView = nil;
  _hamburgerView = nil;
  _foundApplicationsCountLabel = nil;
  
  switch(self.appearance){
      //assume we need mainscreen appearance by deafult
    default:
      NSLog(@"mBASearchBarView: WARNING unrecognized appearance, assumed mBASearchBarViewMainScreenAppearance");
    case mBASearchBarViewMainScreenAppearance:
      [self placeHamburgerImageView];
      break;
    case mBASearchBarViewSearchResultsScreenAppearance:
      [self placeNavigationBackView];
      break;
  }
  
  [self placeSearchIconImageView];
  
  [self placeTitleLabel];
  
  [self placeCancelSearchLabel];
  self.cancelSearchLabel.hidden = YES;
  
  [self placeSearchTextField];
  self.searchTextField.hidden = YES;
  
  [self placeSeparator];
  
  self.backgroundColor = [UIColor blackColor];
}

- (void) centerView:(UIView *)view
{
  CGFloat centerY = self.frame.size.height / 2;
  
  CGPoint viewCenter = view.center;
  viewCenter.y = centerY;
  view.center = viewCenter;
}

- (void) placeNavigationBackView
{
  self.backLabelView = [[UIView alloc] initWithFrame:(CGRect){0.0f, 0.0f, 82.0f, self.frame.size.height}];
  UIImage *backImg = [UIImage imageNamed:@"back.png"];
  UIImageView *backImgView = [[UIImageView alloc] initWithImage:backImg];
  backImgView.frame = (CGRect){7.0f, 0.0f, 13.0f, 20.0f};
  [self centerView:backImgView];
  [self.backLabelView addSubview:backImgView];
  
  UILabel *backLabel = [[UILabel alloc] init];
  backLabel.frame = (CGRect){23.0f, 0.0f, 64.0f, self.frame.size.height};
  backLabel.textAlignment = NSTextAlignmentLeft;
  backLabel.text = NSLocalizedString(@"masterApp_Back", @"Back");
  backLabel.textColor = [UIColor whiteColor];
  backLabel.font = [UIFont systemFontOfSize: 16];
  backLabel.backgroundColor = [UIColor clearColor];
  [self centerView:backLabel];
  [self.backLabelView addSubview:backLabel];

  UITapGestureRecognizer *backTapRecognizer = [[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(leftItemTapped)];
  [self.backLabelView addGestureRecognizer:backTapRecognizer];
  [self addSubview:self.backLabelView];
}

- (void) placeHamburgerImageView
{
    self.hamburgerView = [[UIView alloc] initWithFrame:(CGRect){0.0f, 0.0f, kToolbarHeight, kToolbarHeight}];
    self.hamburgerView.userInteractionEnabled = YES;
    [self addSubview:self.hamburgerView];
  
    UITapGestureRecognizer *hamburgerTapRecognizer = [[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(leftItemTapped)];
    [self.hamburgerView addGestureRecognizer:hamburgerTapRecognizer];
    
    UIImage *hamburger = [UIImage imageNamed:@"mBA_hamburger"];
    UIImageView *hamburgerImageView = [[UIImageView alloc] initWithImage:hamburger];
    CGRect hamburgerFrame = hamburgerImageView.frame;
    hamburgerFrame.origin.x = kHamburgerPadding;
    hamburgerFrame.origin.y = 0.0f;
    
    hamburgerImageView.frame = hamburgerFrame;
    hamburgerImageView.center =  (CGPoint){hamburgerImageView.center.x, self.hamburgerView.center.y};
    
    [self.hamburgerView addSubview:hamburgerImageView];
}

- (void) placeSearchIconImageView
{
    CGFloat tapAreaOriginX = self.frame.size.width - kToolbarHeight;
    
    self.searchIconView = [[UIView alloc] initWithFrame:(CGRect){tapAreaOriginX, 0.0f, kToolbarHeight, kToolbarHeight}];
    self.searchIconView.backgroundColor = [UIColor clearColor];
    self.searchIconView.userInteractionEnabled = YES;
    
    UITapGestureRecognizer *searchIconTapRecognizer = [[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(searchIconTapped)];
    [self.searchIconView addGestureRecognizer:searchIconTapRecognizer];
    [self addSubview:self.searchIconView];
    
    CGFloat searchIconOriginX = kToolbarHeight - kSearchIconWidth - kSearchIconPaddingRight;
    
    UIImage *searchIcon = [UIImage imageNamed:@"mBA_search"];
    UIImageView *searchIconImageView = [[UIImageView alloc] initWithImage:searchIcon];
    
    CGRect searchIconFrame = (CGRect){searchIconOriginX, 0.0f, kSearchIconWidth, kSearchIconWidth};
    searchIconImageView.frame = searchIconFrame;
    searchIconImageView.center = (CGPoint){searchIconImageView.center.x, kToolbarHeight/2};
    
    [self.searchIconView addSubview:searchIconImageView];
}

- (void) placeTitleLabel
{
  self.titleLabel = [[UILabel alloc] init];
  
  switch(self.appearance){
    case mBASearchBarViewMainScreenAppearance:
      _titleLabel.text = NSLocalizedString(@"masterApp_SelectACategory", @"Select a category");
      break;
    case mBASearchBarViewSearchResultsScreenAppearance:
      break;
  }
  
  _titleLabel.font = [UIFont systemFontOfSize:kMainPageTitleFontSize];
  _titleLabel.textColor = kMainPageTitleFontColor;
  _titleLabel.backgroundColor = [UIColor clearColor];
  _titleLabel.textAlignment = NSTextAlignmentCenter;
  
  CGRect titleLabelFrame = (CGRect){0.0f, 0.0f, self.bounds.size.width / 2, kToolbarHeight / 2};
  [self addSubview:_titleLabel];
}

- (void) placeCancelSearchLabel
{
    self.cancelSearchLabel = [[UILabel alloc] init];
    self.cancelSearchLabel.font = [UIFont systemFontOfSize:kCancelLabelFontSize];
    
    NSString *labelText = NSLocalizedString(@"masterApp_CancelSearch", @"Cancel");
    
    CGSize cancelSearchLabelSize = [labelText sizeForFont:self.cancelSearchLabel.font limitSize:self.frame.size];
    CGFloat cancelSearchLabelOriginX = self.bounds.size.width - kCancelLabelHorizontalPadding - cancelSearchLabelSize.width;
    CGFloat cancelSearchLabelOriginY = 0.0f;
    
    CGRect cancelLabelFrame = (CGRect){cancelSearchLabelOriginX, cancelSearchLabelOriginY, cancelSearchLabelSize.width, kToolbarHeight};
    
    self.cancelSearchLabel.frame = cancelLabelFrame;
    self.cancelSearchLabel.backgroundColor = [UIColor clearColor];
    self.cancelSearchLabel.text = labelText;
    self.cancelSearchLabel.textColor = kCancelLabelFontColor;
    self.cancelSearchLabel.userInteractionEnabled = YES;
    
    UITapGestureRecognizer *cancelSearchTapRecognizer = [[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(cancelSearch)];
    
    [self.cancelSearchLabel addGestureRecognizer:cancelSearchTapRecognizer];
    
    [self addSubview:self.cancelSearchLabel];
}

- (void) placeSearchTextField
{
    self.searchTextField = [[UITextField alloc] init];
    
    if(SYSTEM_VERSION_LESS_THAN(@"7.0")){
        self.searchTextField.contentVerticalAlignment = UIControlContentVerticalAlignmentCenter;
    }
    
    searchTextFieldCollapsedFrame = (CGRect){self.cancelSearchLabel.frame.origin.x - kCancelLabelHorizontalPadding,
        (self.frame.size.height - kSearchTextFieldHeight) / 2,
        0.0f,
        kSearchTextFieldHeight};
    
    CGFloat searchTextFieldWidth = self.frame.size.width - (kCancelLabelHorizontalPadding + 2 * kCancelLabelHorizontalPadding + self.cancelSearchLabel.frame.size.width);
    
    searchTextFieldExpandedFrame = (CGRect){kCancelLabelHorizontalPadding, searchTextFieldCollapsedFrame.origin.y, searchTextFieldWidth, kSearchTextFieldHeight};
    
    self.searchTextField.returnKeyType = UIReturnKeySearch;
    self.searchTextField.delegate = self.mBASearchViewTextFieldDelegate;
    self.searchTextField.borderStyle = UITextBorderStyleNone;
    self.searchTextField.layer.backgroundColor = [UIColor whiteColor].CGColor;
    
    self.searchTextField.layer.cornerRadius = kSearchBarTextFieldCornerRadius;
    self.searchTextField.font = [UIFont systemFontOfSize:kSearchBarTextViewFontSize];
    self.searchTextField.textAlignment = NSTextAlignmentLeft;
    
    UIView *spacerView = [[UIView alloc] initWithFrame:CGRectMake(0.0f, 0.0f, 10.0f, kSearchTextFieldHeight)];
    [self.searchTextField setLeftViewMode:UITextFieldViewModeAlways];
    [self.searchTextField setLeftView:spacerView];
    
    self.searchTextField.clearButtonMode = UITextFieldViewModeWhileEditing;
    self.searchTextField.placeholder = NSLocalizedString(@"masterApp_SearchPlaceholder", @"Search");
    self.searchTextField.autoresizingMask = UIViewAutoresizingFlexibleTopMargin | UIViewAutoresizingFlexibleBottomMargin;
    self.searchTextField.frame = searchTextFieldCollapsedFrame;
    
    [self addSubview:self.searchTextField];
  
    [self placeFoundApplicationsCountRightView];
}

- (void) placeSeparator
{
    CGRect separatorFrame = (CGRect){0.0f, kToolbarHeight, self.frame.size.width, kToolbarSeparatorHeight};
    
    UIView *separator = [[UIView alloc] initWithFrame:separatorFrame];
    separator.backgroundColor = kToolbarSeparatorColor;
    
    [self addSubview:separator];
}

- (void) leftItemTapped
{
    if([self.mBASearchViewDelegate respondsToSelector:@selector(mBASearchViewLeftItemPressed)]){
        [self.mBASearchViewDelegate mBASearchViewLeftItemPressed];
    }
}

- (void) searchIconTapped
{
  searchInProgress = !searchInProgress;
  
  switch(self.appearance){
    case mBASearchBarViewMainScreenAppearance:
      self.hamburgerView.hidden = YES;
      break;
    case mBASearchBarViewSearchResultsScreenAppearance:
      self.backLabelView.hidden = YES;
      break;
  }
  
  self.titleLabel.hidden = YES;
  self.searchIconView.hidden = YES;
  
  self.cancelSearchLabel.alpha = 0.0f;
  self.cancelSearchLabel.hidden = NO;
  self.searchTextField.hidden = NO;
  
  [self.searchTextField becomeFirstResponder];
  
  [UIView animateWithDuration:0.2f animations:^{
    self.cancelSearchLabel.alpha = 1.0f;
  }];
  
  [UIView animateWithDuration:0.3f animations:^{
    self.searchTextField.frame = searchTextFieldExpandedFrame;
  } completion:^(BOOL completed){
    if([self.mBASearchViewDelegate respondsToSelector:@selector(mBASearchViewDidShowSearchField)]){
      [self.mBASearchViewDelegate mBASearchViewDidShowSearchField];
    }
  }];
}

- (void) cancelSearch
{
  searchInProgress = !searchInProgress;
  
  self.searchTextField.text = @"";
  self.titleLabel.alpha = 0.0f;
  self.titleLabel.hidden = NO;
  
  self.searchIconView.alpha = 0.0f;
  self.searchIconView.hidden = NO;
  
  switch(self.appearance){
    case mBASearchBarViewMainScreenAppearance:
      self.hamburgerView.alpha = 0.0f;
      self.hamburgerView.hidden = NO;
      break;
    case mBASearchBarViewSearchResultsScreenAppearance:
      self.backLabelView.alpha = 0.0f;
      self.backLabelView.hidden = NO;
      break;
  }
  
  if([self.mBASearchViewDelegate respondsToSelector:@selector(mBASearchViewDidCancelSearch)]){
    [self.mBASearchViewDelegate mBASearchViewDidCancelSearch];
  }
  
  [UIView animateWithDuration:0.3f animations:^{
    self.searchTextField.frame = searchTextFieldCollapsedFrame;
    [self.searchTextField resignFirstResponder];
  } completion:^(BOOL finished){
    self.cancelSearchLabel.hidden = YES;
    self.searchTextField.hidden = YES;
    
    [UIView animateWithDuration:0.1f animations:^{
      self.titleLabel.alpha = 1.0f;
      self.searchIconView.alpha = 1.0f;
      
      switch(self.appearance){
        case mBASearchBarViewMainScreenAppearance:
          self.hamburgerView.alpha = 1.0f;
          break;
        case mBASearchBarViewSearchResultsScreenAppearance:
          self.backLabelView.alpha = 1.0f;
          break;
      }
    }];
    
  }];
}


- (void) setMBASearchViewTextFieldDelegate:(id<NSObject,UITextFieldDelegate>)mBASearchViewTextFieldDelegate {
    if(_mBASearchViewTextFieldDelegate != mBASearchViewTextFieldDelegate){
        _mBASearchViewTextFieldDelegate = mBASearchViewTextFieldDelegate;
        self.searchTextField.delegate = _mBASearchViewTextFieldDelegate;
    }
}

- (void) setTitle:(NSString *)title
{
  if(_title != title){
    _title = title;
    self.titleLabel.text = _title;
    [self.titleLabel sizeToFit];
    self.titleLabel.center = (CGPoint){self.bounds.size.width / 2, kToolbarHeight / 2};
    
    if(self.titleLabel.frame.origin.x <= (self.backLabelView.frame.origin.x + self.backLabelView.frame.size.width + 6.0f)){
      CGRect shiftedTitleFrame = self.titleLabel.frame;
      shiftedTitleFrame.origin.x += 6.0f;
      self.titleLabel.frame = shiftedTitleFrame;
    }
  }
}

-(void)placeFoundApplicationsCountRightView
{
  self.foundApplicationsCountLabel = [[UILabel alloc] init];
  self.foundApplicationsCountLabel.textColor = kAppCountTextColor;
  self.foundApplicationsCountLabel.backgroundColor = [UIColor clearColor];
  self.foundApplicationsCountLabel.font = [UIFont systemFontOfSize:12.0f];
  
  [self.searchTextField setRightViewMode:UITextFieldViewModeUnlessEditing];
  [self.searchTextField setClearButtonMode:UITextFieldViewModeWhileEditing];
  [self.searchTextField setRightView:self.foundApplicationsCountLabel];
}

- (void)clearFoundApplicationsCount
{
  self.foundApplicationsCountLabel.text = @"";
}

- (void)refreshFoundApplicationsCount:(NSUInteger)newValue
{
  NSString *countAsString = [NSString stringWithFormat:@"%d", newValue];
  
  self.foundApplicationsCountLabel.text = countAsString;
  self.foundApplicationsCountLabel.textColor = kAppCountTextColor;
  [self.foundApplicationsCountLabel sizeToFit];
  
  CGRect newFrame = self.foundApplicationsCountLabel.frame;
  newFrame.size.width += 5.0f;
  self.foundApplicationsCountLabel.frame = newFrame;
}

- (void) dealloc
{
    self.titleLabel = nil;
    self.searchTextField = nil;
    self.cancelSearchLabel = nil;
    self.searchIconView = nil;
    self.hamburgerView = nil;
    self.foundApplicationsCountLabel = nil;
}

@end
