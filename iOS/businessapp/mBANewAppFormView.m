// IBAHeader

#import "mBANewAppFormView.h"
#import "MBANewAppFormPresenter.h"
#import "UIColor+RGB.h"
#import "widgetborder.h"
#import "UIView+corners.h"
#import "buisinessapp.h"
#import "mBACategoryTemplatesFetcher.h"

#define navBarHeight 44.f
#define addingHeight 20.f
#define backButtonY  30.f
#define leftMargin   20.f
#define buttonHeight 40.f
#define backLabelHeight 40.f
#define categoryHeight  49.f
#define inputURLViewHeight 39.f

#define marginTop (navBarHeight+addingHeight)

#define SMALL_HORIZONTAL_MARGIN 10.0f
#define BIG_HORIZONTAL_MARGIN 20.0f

typedef enum {
  FieldIsValid,
  FacebookTextFieldInvalid,
  WebSiteTextFieldInvalid,
  UnknownTextField
}FieldValidityStatus;

@interface mBANewAppFormView()

@property (nonatomic, strong) UITextField *facebookURLTxtField;
@property (nonatomic, strong) UITextField *webSiteURLTxtField;

@end

@implementation mBANewAppFormView {
  UIColor *backgroundColor;
  UIColor *statusBarColor;
  UIColor *orColor;
  UIColor *labelColor;
  UIColor *linkColor;
  UIToolbar *myTopToolbar;

  mBANewAppFormPresenter *_presenter;
  UIView *view;
  
  float screenWidth;

}

@synthesize scrollView;
@synthesize facebookURLTxtField, webSiteURLTxtField;


#pragma mark -
- (id) init
{
  self = [super init];
  if (self)
  {
    _categoryNameLabel =   [[UILabel alloc] init];
    facebookURLTxtField = [[UITextField alloc] init];
    webSiteURLTxtField =  [[UITextField alloc] init];
    myTopToolbar = nil;

    scrollView = nil;
  }
  return self;
}

- (void)dealloc
{
    _categoryNameLabel = nil;
    myTopToolbar = nil;
    facebookURLTxtField = nil;
    webSiteURLTxtField = nil;
}


#pragma mark - 
- (void)initMetrix
{

}

- (void)initColors
{
  backgroundColor = [UIColor colorWithRGB: 0xefefef];
  statusBarColor  = [UIColor blackColor];
  orColor         = [UIColor colorWithRGB: 0x444444];
  labelColor      = [UIColor colorWithRGB: 0x989898];
  linkColor       = [UIColor colorWithRGB: 0x21ade7];
}

- (void) placeSeparatorOnToolbar:(UIView *)toolbar
{
  CGFloat originY = toolbar.frame.origin.y + toolbar.frame.size.height;
  CGRect separatorFrame = (CGRect){0.0f, originY, toolbar.frame.size.width, 1.0f};
  
  UIView *separator = [[UIView alloc] initWithFrame:separatorFrame];
  separator.backgroundColor = [UIColor blackColor];
  toolbar.clipsToBounds = NO;
  
  [view addSubview:separator];
}

- (void)addTapRecoginzer:(SEL)action to:(UIView *)playTapZone
{
  UITapGestureRecognizer *playPauseZoneTap = [[UITapGestureRecognizer alloc] initWithTarget: _presenter action: action];
  playPauseZoneTap.delegate = _presenter;
  playTapZone.userInteractionEnabled = YES;
  [playTapZone addGestureRecognizer: playPauseZoneTap];
}

- (void)hideStatusBar
{
  [[UIApplication sharedApplication] setStatusBarHidden: YES
                                          withAnimation: UIStatusBarAnimationNone];
}

- (void)setupStatusBar
{
  [[UIApplication sharedApplication] setStatusBarStyle:UIStatusBarStyleLightContent];

  UIBarButtonItem *mySpacer = [[UIBarButtonItem alloc]
    initWithBarButtonSystemItem: UIBarButtonSystemItemFlexibleSpace
                         target: nil action: nil];

  myTopToolbar = [[UIToolbar alloc]init];
  myTopToolbar.frame = (CGRect){0, 0, view.bounds.size.width, navBarHeight + addingHeight};
  myTopToolbar.tintColor = statusBarColor;
  myTopToolbar.backgroundColor = statusBarColor;
  
  UIImage *toolBarBckgrndImage = [UIImage imageNamed:(@"blue-statusbar-background.png")];
  float w = toolBarBckgrndImage.size.width  / 2,
  h = toolBarBckgrndImage.size.height / 2;
  UIImage *stretchedImage = [toolBarBckgrndImage stretchableImageWithLeftCapWidth:w topCapHeight:h];
  
  UIImageView *toolBarBckgrndView = [[UIImageView alloc] initWithImage:stretchedImage];
  toolBarBckgrndView.frame = myTopToolbar.bounds;

  UILabel *titleLabel = [[UILabel alloc] init];
  
  float margin = BIG_HORIZONTAL_MARGIN;
  float width = screenWidth - margin * 2;
  
  titleLabel.frame = (CGRect) {margin, addingHeight, width, buttonHeight};
  
  titleLabel.textAlignment = NSTextAlignmentCenter;
  titleLabel.text = @"Add New Business";
  
  titleLabel.textColor = [UIColor whiteColor];
  titleLabel.backgroundColor = [UIColor clearColor];

  UIImage *backImg = [UIImage imageNamed:@"back.png"];
  UIImageView *backImgView = [[UIImageView alloc] initWithImage:backImg];
  backImgView.frame = (CGRect){7.0f, backButtonY, backImg.size.width, backImg.size.height};
  [myTopToolbar addSubview:backImgView];

  UILabel *backLabel = [[UILabel alloc] init];
  backLabel.frame = (CGRect){23, addingHeight , 50, backLabelHeight};

  backLabel.textAlignment = NSTextAlignmentLeft;
  backLabel.text = NSLocalizedString(@"masterApp_Back", @"Back");
  backLabel.textColor = [UIColor whiteColor];
  backLabel.font = [UIFont systemFontOfSize: 17];
  backLabel.backgroundColor = [UIColor clearColor];

  [myTopToolbar setItems:@[mySpacer] animated:NO];
  
  [self placeSeparatorOnToolbar:myTopToolbar];
  
  [view addSubview: myTopToolbar];
  [view addSubview: titleLabel];
  [view addSubview: backLabel];

  [self addTapRecoginzer: @selector(back) to: backLabel];
  [self addTapRecoginzer: @selector(back) to: backImgView];
}

- (UILabel *)placeOrTo:(float)yOrigin
{
  UILabel *orLabel = [[UILabel alloc] init];
  orLabel.frame = (CGRect){0.0f, yOrigin, view.bounds.size.width, 20.0f};
  orLabel.backgroundColor = [UIColor clearColor];
  orLabel.text = @"OR";
  orLabel.textAlignment = NSTextAlignmentCenter;
  orLabel.textColor = orColor;
  orLabel.font = [UIFont systemFontOfSize: 16];
  
  return orLabel;
}


- (UIButton *)placeCategoryButton
{
  UIButton *categoryBtn = [UIButton buttonWithType:UIButtonTypeCustom];
  categoryBtn.frame = CGRectMake(-5, 84 - marginTop, view.bounds.size.width + 10, categoryHeight);
  categoryBtn.layer.masksToBounds = YES;
  categoryBtn.backgroundColor = [UIColor whiteColor];
  categoryBtn.layer.borderWidth = 1;
  categoryBtn.layer.borderColor = [UIColor lightGrayColor].CGColor;

  UILabel *categoryLabel = [[UILabel alloc] init];
  categoryLabel.frame = (CGRect){15, 14, 100, 24.0f};
  categoryLabel.backgroundColor = [UIColor whiteColor];
  categoryLabel.textColor = orColor;
  categoryLabel.font = [UIFont systemFontOfSize: 15];
  categoryLabel.text = @"Category";
  
  [categoryBtn addSubview: categoryLabel];
  
  self.categoryNameLabel.frame = (CGRect){140.0f, 14.0f, 150.0f, 24.0f};
  self.categoryNameLabel.backgroundColor = [UIColor whiteColor];
  self.categoryNameLabel.textColor = labelColor;
  self.categoryNameLabel.font = [UIFont systemFontOfSize: 15];
  
  if(self.categories != nil){
    id category = self.categories[0];
    NSString *labelText = nil;
    if([category isKindOfClass:[mBACategoryModel class]]){
      labelText = ((mBACategoryModel*)category).title;
    } else if([category isKindOfClass:[NSDictionary class]]){
      labelText = [category objectForKey:@"title"];
    }
    self.categoryNameLabel.text = labelText;
    _presenter.selectedCategoryId = @"1";
  }
  
  self.categoryNameLabel.textAlignment = UITextAlignmentCenter;
  
  self.categoryNameLabel.userInteractionEnabled = YES;
  
  UITapGestureRecognizer *categoryNameLabelTapGesture = [[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(showPicker)];
  
  [self.categoryNameLabel addGestureRecognizer:categoryNameLabelTapGesture];

  [categoryBtn addSubview: self.categoryNameLabel];
  
  return categoryBtn;
}

-(void) showPicker{
  [webSiteURLTxtField resignFirstResponder];
  [facebookURLTxtField resignFirstResponder];
  
  [_presenter showCategoriesPicker];
}

- (UIView *)placeFacebookButton
{
  UIView *baseView = [[UIView alloc] init];
  
  float margin = SMALL_HORIZONTAL_MARGIN;
  float width = screenWidth - margin * 2;
  
  baseView.frame = (CGRect){margin, 162.0f-marginTop, width, 40.0f};
  baseView.backgroundColor = [UIColor whiteColor];
  baseView.layer.cornerRadius = 3;
  baseView.layer.borderWidth = 1;
  baseView.layer.borderColor = [UIColor lightGrayColor].CGColor;
  
  if(SYSTEM_VERSION_GREATER_THAN_OR_EQUAL_TO(@"7.0")){
    facebookURLTxtField.frame = (CGRect){0, 0, width, 40.0f};
  } else {
    facebookURLTxtField.frame = (CGRect){0, 10, width, 20.0f};
  }
  
  facebookURLTxtField.backgroundColor = [UIColor clearColor];

  facebookURLTxtField.placeholder = @"Enter Facebook Page";
  facebookURLTxtField.textAlignment = NSTextAlignmentCenter;
  facebookURLTxtField.textColor = labelColor;
  facebookURLTxtField.font = [UIFont systemFontOfSize: 15];
  facebookURLTxtField.autocapitalizationType = UITextAutocapitalizationTypeNone;
  facebookURLTxtField.delegate = self;
  [baseView addSubview: facebookURLTxtField];
  
  UIImageView *facebookImageView = [[UIImageView alloc] initWithImage:[UIImage imageNamed:(@"business_app_facebook")]];
  facebookImageView.frame = (CGRect){10, 7, facebookImageView.image.size.width, facebookImageView.image.size.height};
  [baseView addSubview:facebookImageView];
  
  return baseView;
}

- (UIView *)placeWebSiteButton
{
  float margin = SMALL_HORIZONTAL_MARGIN;
  float width = screenWidth - margin * 2;
  
  UIView *baseView = [[UIView alloc] init];
  baseView.frame = (CGRect){margin, 260.0f-marginTop, width, 40.0f};
  baseView.backgroundColor = [UIColor whiteColor];
  baseView.layer.cornerRadius = 3;
  baseView.layer.borderWidth = 1;
  baseView.layer.borderColor = [UIColor lightGrayColor].CGColor;

  if(SYSTEM_VERSION_GREATER_THAN_OR_EQUAL_TO(@"7.0")){
    webSiteURLTxtField.frame = (CGRect){0, 0, width, 40.0f};
  } else {
    webSiteURLTxtField.frame = (CGRect){0, 10, width, 20.0f};
  }
  
  webSiteURLTxtField.backgroundColor = [UIColor clearColor];
  webSiteURLTxtField.placeholder = @"Enter website URL";
  webSiteURLTxtField.textAlignment = NSTextAlignmentCenter;
  webSiteURLTxtField.textColor = labelColor;
  webSiteURLTxtField.font = [UIFont systemFontOfSize: 15];
  webSiteURLTxtField.autocapitalizationType = UITextAutocapitalizationTypeNone;
  webSiteURLTxtField.delegate = self;
  [baseView addSubview: webSiteURLTxtField];
  
  UIImageView *urlImageView = [[UIImageView alloc] initWithImage:[UIImage imageNamed:(@"business_app_url")]];
  urlImageView.frame = (CGRect){10, 7, urlImageView.image.size.width, urlImageView.image.size.height};
  [baseView addSubview:urlImageView];
  return baseView;
}

- (UILabel *)placeBuisinessLink
{
  UILabel *businessLinkLabel = [[UILabel alloc] init];
  businessLinkLabel.frame = (CGRect){0.0f, 353.0f-marginTop, view.bounds.size.width, 20.0f};
  businessLinkLabel.backgroundColor = [UIColor clearColor];
  businessLinkLabel.text = @"Enter Business Information";
  businessLinkLabel.textAlignment = NSTextAlignmentCenter;
  businessLinkLabel.textColor = linkColor;
  businessLinkLabel.font = [UIFont systemFontOfSize: 14.5];
  businessLinkLabel.userInteractionEnabled = YES;
  UITapGestureRecognizer *businessLinkTapGesture = [[UITapGestureRecognizer alloc] initWithTarget:_presenter action:@selector(showBusinessDetailsController)];
  
  [businessLinkLabel addGestureRecognizer:businessLinkTapGesture];
  
  return businessLinkLabel;
}

/**
 * To put as a background a picture and to adjust positions
 */
- (UIImageView *)placeOriginalViewImage
{
  UIImageView *uiImageView = [[UIImageView alloc] initWithFrame: CGRectMake(0.0f, 0.0f, view.bounds.size.width, view.frame.size.height)];
  uiImageView.alpha = 0.3;
  [uiImageView setImage: [UIImage imageNamed: @"newappscreen"]];
  [view addSubview: uiImageView];
  
  return uiImageView;
}

-(void) placeScrollView{
   CGRect accurateFrame = (CGRect){0, marginTop, view.frame.size.width, view.frame.size.height - marginTop};

   scrollView = [[TPKeyboardAvoidingScrollView alloc] initWithFrame:accurateFrame];
   scrollView.autoresizesSubviews = YES;
   scrollView.autoresizingMask    = UIViewAutoresizingFlexibleHeight | UIViewAutoresizingFlexibleWidth;
   scrollView.opaque              = YES;
  
   [view addSubview:scrollView];
  
  int f = view.frame.size.height;
  int s = scrollView.frame.size.height;
  
  NSLog(@"Frame height: %d scroll height: %d", f, s);
}

- (void)viewForPresenter:(mBANewAppFormPresenter *)presenter
{
  _presenter = (mBANewAppFormPresenter *)presenter;
  view = presenter.view;
  
  screenWidth = view.frame.size.width;
  
  NSLog(@"MARGIN TOP %f", marginTop);
  
  [self initMetrix];
  [self initColors];
  view.backgroundColor = backgroundColor;

  [self hideStatusBar];
  [self setupStatusBar];
  
  [self placeScrollView];
  
  [scrollView addSubview:[self placeCategoryButton]];
  [scrollView addSubview:[self placeFacebookButton]];
  [scrollView addSubview:[self placeOrTo: 224.0f - marginTop]];
  [scrollView addSubview:[self placeWebSiteButton]];
  [scrollView addSubview:[self placeOrTo: 316.0f - marginTop]];
  [scrollView addSubview:[self placeBuisinessLink]];
  
  [self placeSeparatorOnToolbar:myTopToolbar];
}


#pragma mark - UITextField delegate

- (BOOL)textFieldShouldBeginEditing:(UITextField *)textField
{
  BOOL shouldBeginEditing = true;
  
  if(textField == facebookURLTxtField){
    if(webSiteURLTxtField.text.length > 0){
      shouldBeginEditing = false;
    }
  } else if(textField == webSiteURLTxtField){
    if(facebookURLTxtField.text.length > 0){
      shouldBeginEditing = false;
    }
  }
  return shouldBeginEditing;
}

- (BOOL)textFieldShouldEndEditing:(UITextField *)textField
{
  if(textField.text.length > 0){
    FieldValidityStatus status = [self validateTextField:textField];
    
    switch(status){
      case FacebookTextFieldInvalid:{
        UIAlertView *fbInvalidAlertView = [[UIAlertView alloc] initWithTitle:@"Facebook" message:@"Username invalid" delegate:nil cancelButtonTitle:@"OK" otherButtonTitles:nil];
        [fbInvalidAlertView show];
        return NO;
      }
      case WebSiteTextFieldInvalid:{
        UIAlertView *urlInvalidAlertView = [[UIAlertView alloc] initWithTitle:@"Website" message:@"Invalid address" delegate:nil cancelButtonTitle:@"OK" otherButtonTitles:nil];
        [urlInvalidAlertView show];
        return NO;
      }
      default: {
        NSLog(@"FieldValidityStatus was not processed");
      }
        
    }
  }
  [textField resignFirstResponder];
  return YES;
}

- (BOOL)textFieldShouldReturn:(UITextField *)textField
{
  [textField resignFirstResponder];
  return YES;
}

- (FieldValidityStatus) validateTextField:(UITextField *)textField{
  if(textField == facebookURLTxtField){
    if(![self isStringValidFacebook:textField.text]){
      return FacebookTextFieldInvalid;
    }
    return FieldIsValid;
  }
  else if(textField == webSiteURLTxtField){
    if(![self isStringValidUrl:textField.text]){
      return WebSiteTextFieldInvalid;
    }
    return FieldIsValid;
  }
  return UnknownTextField;
}


- (BOOL) isStringValidFacebook:(NSString *) checkString {
  /*
   Facebook has requirements for the user name, it is possible that the code will have to be passed through:
        The user name can only contain letters and numbers (A-Z, 0-9), as well as points ("."). We are working
        Ability to support non-Latin characters in the future.
    https://www.facebook.com/help/329992603752372
   */
   
static NSString *const fbRegeExp = @"[a-zA-Z0-9\\.]{5,}";
  NSPredicate *fbTest = [NSPredicate predicateWithFormat:@"SELF MATCHES %@", fbRegeExp];
  NSLog(@"%@", [[NSURL URLWithString:checkString] description]);
  
  return [fbTest evaluateWithObject:checkString];
}

- (BOOL) isStringValidUrl:(NSString *)checkString {
  static NSString *const urlRegeExp = @"(\b(https?|ftp|file)://)?[-A-Za-zА-Яа-я0-9+&@#/%?=~_|!:,.;]+[-A-Za-zА-Яа-я0-9+&@#/%=~_|]\\.[A-Za-zА-Яа-я]{2,10}";
  NSPredicate *urlTest = [NSPredicate predicateWithFormat:@"SELF MATCHES %@", urlRegeExp];
  return [urlTest evaluateWithObject:checkString];
}


@end
