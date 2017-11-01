// IBAHeader

#import "mBAMainMenuView.h"
#import "mBACategoryModel.h"
#import "mBAApplicationModel.h"
#import "mBAMainMenuPresenter.h"
#import "UIImageView+WebCache.h"
#import "NRGridView.h"
#import "customgridcell.h"
#import "UIColor+RGB.h"
#import "NSString+size.h"
#import "mBASideBarView.h"
#import "UIImage+color.h"

#define lightGrayColor 0xEFEFEF
#define mBA_GrayColor 0x666666
#define blackTintColor 0x333333
#define mBA_BlueColor 0x21ADE7
#define blueMenuColor 0x2AA6DA

#define kCategoriesGridViewTag 0xFFFF
#define kSearchResultsGridViewTag 0xFFFE

#define kCellImagePreviewLeftMargin 10.f
#define kCellImagePreviewBottomMargin 10.f
#define kGrigViewLeftMargin 5.f
#define kCellCornerRadius 5.f

#define kApplicationCountLabelPaddingRight 10.0f

#define kSearchResultsGridContentInsetTop 0.0f

#define kAppListBackgroundColor [@"#17161b" asColor]

@interface mBASearchApplicationListCell : NRGridViewCell

@property (nonatomic, retain) UIImageView       *previewImageView;
@property (nonatomic, retain) UILabel           *titleLabel;
@property (nonatomic, retain) UIView            *imagePlaceholderView;
@property (nonatomic, retain) UIView            *containerView;

@property (nonatomic, retain) UIView            *containerViewShadow;
@property (nonatomic, retain) UIView            *titleSeparatorView;

@end

@implementation mBASearchApplicationListCell

- (void) dealloc
{
  self.previewImageView = nil;
  self.titleLabel = nil;
  self.imagePlaceholderView = nil;
  self.containerView = nil;
}


@end

@interface mBAMainMenuView(){
    BOOL newSearchConducted;
  
    BOOL searchInProgress;
    CGRect searchTextFieldCollapsedFrame;
    CGRect searchTextFieldExpandedFrame;
}

@property (nonatomic, retain) mBASearchBarView *toolBar;
@property (nonatomic, retain) NRGridView *categoriesGridView;

@property (nonatomic, retain) UIScrollView *featuredScrollView;

@property (nonatomic, retain) NSArray *defaultCategoriesImages;
@property (nonatomic, retain) NSArray *categories;

@end

@implementation mBAMainMenuView {
    CGFloat addingHeight;
    CGFloat categoryCellHeight;
    CGFloat featuredTitleTop;
    CGFloat featuredCellTop;
    CGFloat featuredCellHeight;
    CGFloat baseLine;
    CGFloat categoryCellText;
    CGFloat picturePaddingTop;
    CGFloat searchBarHeight;
    CGFloat categoryCellWidth;
    CGFloat gridlabelHeight;
    
    UIView *categoryGridVew;
    
    mBAMainMenuPresenter *_presenter;
    UIView *_view;
    
    NRGridView *searchResultsGrid;
    
    CGFloat searchResultsStart;
    CGFloat searchResultsCellHeight;
    CGFloat searchResultsTitleTopMargin;
}

@synthesize messageLabel, defaultCategoriesImages;

#pragma mark -
- (id) init
{
    self = [super init];
    if(self)
    {
        messageLabel = nil;
      defaultCategoriesImages = @[@"mBA_category_shops",
                                   @"mBA_category_beauty_fitness",
                                   @"mBA_category_sports",
                                   @"mBA_category_schools_nonprofit",
                                   @"mBA_category_professional_services",
                                   @"mBA_category_music_entertainment",
                                   @"mBA_category_restaurants",
                                   @"mBA_category_law_finance",
                                   @"mBA_category_automotive",
                                   @"mBA_category_blogs_magazines",
                                   @"mBA_category_real_estate",
                                   @"mBA_category_others"];
        searchInProgress = NO;
      
      _toolBar = nil;
      _featuredScrollView = nil;
      _categoriesGridView = nil;
      categoryGridVew = nil;
      searchResultsGrid = nil;
      newSearchConducted = NO;
    }
    return self;
}

- (void) dealloc
{
  
  self.messageLabel = nil;
  self.categories = nil;
  self.defaultCategoriesImages = nil;

  self.toolBar = nil;
  self.featuredScrollView = nil;
  self.categoriesGridView = nil;
}

#pragma mark -

- (void)initMetrics
{
    if (_view.frame.size.height == 548.0f || _view.frame.size.height == 460.0f) {
        addingHeight             =  -20.0f;
    } else {
        addingHeight             =  0.0f;
    }
    categoryCellWidth = (int)_view.frame.size.width / 3;
    
    if (_view.frame.size.height >= 548.0f) {
        searchBarHeight       =  42.0f;
        featuredTitleTop      = 48.f + 14;
        featuredCellTop       =  53 + 14;
        categoryCellHeight    =  89.0f;
        featuredCellHeight    =  130.f;
        baseLine              = 170.0f + addingHeight;
        categoryCellText      =  46.0f;
        picturePaddingTop     =  26.0f;
        searchResultsStart            =   78.0f + addingHeight;
        searchResultsCellHeight       =   220.0f;
        searchResultsTitleTopMargin   =    200.f;
        gridlabelHeight      =   27.0f;

    } else {
        categoryCellHeight    =  67.0f;
        featuredTitleTop      =  38.0f + 14;
        featuredCellTop       =  48 + 14;
        featuredCellHeight    = 130;
        baseLine              = 168.0f + addingHeight;
        categoryCellText      =  38.0f;
        picturePaddingTop     =  21.0f;
        searchBarHeight       =  35.0f;
        searchResultsStart            =   63.0f - addingHeight;
        searchResultsCellHeight       =   220.0f;
        searchResultsTitleTopMargin   =    200.f;
        gridlabelHeight      =   27.0f;
    }
}

- (void)hideStatusBar
{
}

- (void)addTapRecoginzer:(SEL)action to:(UIView *)playTapZone parent:(UIView *)parentView
{
    CGRect viewFrame = playTapZone.frame;
    playTapZone.userInteractionEnabled = YES;
    
    UIView *setTapZone = [[UIView alloc] init];
    setTapZone.frame = (CGRect){ viewFrame.origin.x, viewFrame.origin.y, viewFrame.size.width, viewFrame.size.height};
    setTapZone.backgroundColor = [UIColor clearColor];
    setTapZone.tag = playTapZone.tag;
    setTapZone.userInteractionEnabled = YES;
    [parentView addSubview: setTapZone];
    
    UITapGestureRecognizer *playPauseZoneTap = [[UITapGestureRecognizer alloc] initWithTarget: _presenter action: action];
    playPauseZoneTap.delegate = _presenter;
    [setTapZone addGestureRecognizer: playPauseZoneTap];
}

- (void)setupToolBar
{
  self.toolBar = [[mBASearchBarView alloc] initWithApperance:mBASearchBarViewMainScreenAppearance];
  self.toolBar.mBASearchViewDelegate = self;
  self.toolBar.mBASearchViewTextFieldDelegate = self;
  self.toolBar.title = NSLocalizedString(@"masterApp_SelectACategory", @"Select a category");
  
  UIView *statusBar = [[UIView alloc] init];
  statusBar.frame = (CGRect){0.0f, -20.0f, _view.frame.size.width, 20.0f};
  statusBar.backgroundColor = kToolbarColor;
  [self.toolBar addSubview:statusBar];
  
  CGRect shiftedDownSearchBarFrame = self.toolBar.frame;
  shiftedDownSearchBarFrame.origin.y += 20.0f;
  self.toolBar.frame = shiftedDownSearchBarFrame;
  
  [_view addSubview:self.toolBar];
}

- (void)placeAddAppButton
{
    UILabel *label = [[UILabel alloc] init];
    label.frame = (CGRect){218.0f, featuredTitleTop + addingHeight + 3, 120.0f, 30.0f};
    [_view addSubview:label];
    label.font = [UIFont systemFontOfSize: 10.5];
    label.textAlignment = NSTextAlignmentLeft;
    label.adjustsFontSizeToFitWidth = YES;
    label.textColor = [UIColor colorWithRGB: mBA_BlueColor];
    label.text = @"Add Your Business";
    
    UIImageView *uiImageView = [[UIImageView alloc] init];
    uiImageView.frame = (CGRect){188.0f, featuredTitleTop + 6.0f + addingHeight, 24.0f, 24.0f};
    [_view addSubview:uiImageView];
    uiImageView.image = [UIImage imageNamed: @"add_your_business"];
    
    [self addTapRecoginzer:@selector(toPlaceAppView:) to:label parent:_view];
    [self addTapRecoginzer:@selector(toPlaceAppView:) to:uiImageView parent:_view];
}

- (void) placeSeparator
{
  CGRect separatorFrame = (CGRect){0.0f, kToolbarHeight, _view.bounds.size.width, kToolbarSeparatorHeight};
  
  UIView *separator = [[UIView alloc] initWithFrame:separatorFrame];
  separator.backgroundColor = kToolbarSeparatorColor;
  
  [self.toolBar addSubview:separator];
}

- (void)placeCategoriesGridView
{
  if (self.categoriesGridView != nil)
  {
    [self.categoriesGridView removeFromSuperview];
    self.categoriesGridView = nil;
  }
  CGFloat originY = self.toolBar.frame.origin.y + self.toolBar.frame.size.height;
  
  CGRect categoriesGridViewFrame = (CGRect){0.0f, originY, _view.bounds.size.width, _view.bounds.size.height - originY};
  
  self.categoriesGridView = [[NRGridView alloc] initWithFrame:categoriesGridViewFrame];
  self.categoriesGridView.tag = kCategoriesGridViewTag;
  
  self.categoriesGridView.autoresizesSubviews = YES;
  self.categoriesGridView.autoresizingMask = UIViewAutoresizingFlexibleHeight;
  self.categoriesGridView.layoutStyle = NRGridViewLayoutStyleVertical;
  self.categoriesGridView.cellSize = (CGSize){kCategoryCellWidth, kCategoryCellHeight};
  self.categoriesGridView.delegate = self;
  self.categoriesGridView.dataSource = self;
  self.categoriesGridView.showsHorizontalScrollIndicator = NO;
  self.categoriesGridView.showsVerticalScrollIndicator = NO;
  
  self.categoriesGridView.backgroundColor = [UIColor clearColor];
  [_view insertSubview:self.categoriesGridView belowSubview:self.toolBar];
}

- (void)placeFeaturedScrollView
{
    self.featuredScrollView = [[UIScrollView alloc] init];
    self.featuredScrollView.frame = (CGRect){10.0f, featuredCellTop + addingHeight, 314.0f, featuredCellHeight + 10};
    self.featuredScrollView.backgroundColor = [UIColor whiteColor];
    self.featuredScrollView.indicatorStyle = UIScrollViewIndicatorStyleWhite;
    [self.featuredScrollView setShowsHorizontalScrollIndicator: NO];
    [self.featuredScrollView setShowsVerticalScrollIndicator: NO];
    [_view addSubview:self.featuredScrollView];
}

- (void)placeButtonTitle:(NSString *)title toFrame:(CGRect)frame fontSize:(NSInteger)fontSize
{
    UILabel *label = [[UILabel alloc] init];
    label.frame = frame;
    label.backgroundColor = [UIColor clearColor];
    label.font = [UIFont systemFontOfSize: fontSize];
    label.textAlignment = NSTextAlignmentLeft;
    label.adjustsFontSizeToFitWidth = YES;
    label.textColor = [UIColor whiteColor];
    label.text = title;
    [_view addSubview:label];
}

- (void)placeButtonTitle:(NSString *)title toFrame:(CGRect)frame
{
    [self placeButtonTitle: title toFrame: frame fontSize: 14];
}

- (UIImageView *)placeImageNamed:(NSString *)name toFrame:(CGRect)frame
{
    UIImageView *uiImageView = [[UIImageView alloc] init];
    uiImageView.frame = frame;
    [_view addSubview:uiImageView];
    [uiImageView setImage: [UIImage imageNamed: name]];
    return uiImageView;
}

- (void)placeCategoryRectToRow:(NSInteger)row col:(NSInteger)col
{
    UIView *categoryCellView = [[UILabel alloc] initWithFrame: (CGRect){
        (categoryCellWidth + 1) * col,
        (categoryCellHeight + 1) * row + 41.0f,
        categoryCellWidth,
        categoryCellHeight
    }];
    categoryCellView.userInteractionEnabled = YES;
    categoryCellView.backgroundColor = [UIColor colorWithRGB: blueMenuColor];
    categoryCellView.tag = row * 3 + col + 32;
    [categoryGridVew addSubview: categoryCellView];
    [self addTapRecoginzer: @selector(toCategoryView:) to: categoryCellView parent: categoryGridVew];
}

- (void)placeCategoriesGrid
{
    _view.backgroundColor = kSideBarBackgroundColor;
  
    if (categoryGridVew != nil) {
        [categoryGridVew removeFromSuperview];
    }
    
    categoryGridVew = [[UILabel alloc] init];
    categoryGridVew.frame = (CGRect){0, baseLine, 320, 480};
    categoryGridVew.backgroundColor = [UIColor clearColor];
    categoryGridVew.userInteractionEnabled = YES;
  
    [_view addSubview:categoryGridVew];
    
    for (int row = 0; row < 4; row++) {
        for (int col = 0; col < 3; col++) {
            [self placeCategoryRectToRow: row col: col];
        }
    }
}

- (void)placeSearchResultsGrid
{
    searchResultsStart = self.toolBar.frame.origin.y + self.toolBar.frame.size.height;
  
    searchResultsGrid = [[NRGridView alloc] initWithFrame:
                         (CGRect){kGrigViewLeftMargin,
                           searchResultsStart,
                           _view.bounds.size.width - 2*kGrigViewLeftMargin,
                           _view.bounds.size.height - searchResultsStart - 10
                         }];
 
    searchResultsGrid.contentInset = UIEdgeInsetsMake(kSearchResultsGridContentInsetTop,
                                                      0.f,
                                                      0.f,
                                                      0.f);
  
    searchResultsGrid.tag = kSearchResultsGridViewTag;
    searchResultsGrid.delegate = self;
    searchResultsGrid.dataSource = self;
    searchResultsGrid.autoresizesSubviews = YES;
    searchResultsGrid.backgroundColor = kAppListBackgroundColor;
  
    searchResultsGrid.autoresizingMask = UIViewAutoresizingFlexibleHeight;
    searchResultsGrid.layoutStyle = NRGridViewLayoutStyleVertical;
    searchResultsGrid.cellSize = (CGSize){(int)_view.bounds.size.width / 2 - kGrigViewLeftMargin, searchResultsCellHeight};
    searchResultsGrid.showsHorizontalScrollIndicator = NO;
    searchResultsGrid.showsVerticalScrollIndicator = NO;
    searchResultsGrid.opaque = YES;
  
    [_view insertSubview:searchResultsGrid belowSubview:self.toolBar];
}

- (void)placeOriginalViewImage
{
    UIImageView *uiImageView = [[UIImageView alloc] initWithFrame: (CGRect){0.0f, 0.0f + addingHeight, 320.0f, _view.frame.size.height}];
    uiImageView.alpha = 0.5;
    [_view addSubview:uiImageView];
    [uiImageView setImage: [UIImage imageNamed: @"mainscreen"]];
}

- (void)viewForPresenter:(mBAMainMenuPresenter *)presenter
{
  _presenter = presenter;
  _view = presenter.view;
  [[UIApplication sharedApplication] setStatusBarStyle:UIStatusBarStyleLightContent];
  
  [self initMetrics];
  
  _view.backgroundColor = kSideBarBackgroundColor;
  _view.userInteractionEnabled = YES;

  if(self.toolBar == nil)
  {
    [self setupToolBar];
  }

  if(self.categoriesGridView == nil)
  {
    [self placeCategoriesGridView];
  }

  if(searchResultsGrid == nil)
  {
    [self placeSearchResultsGrid];
    searchResultsGrid.hidden = YES;
  }
  if(SYSTEM_VERSION_LESS_THAN(@"7.0")){
    [self pullUpViewForiOS6];
  }

}

- (void) pullUpViewForiOS6
{
  if(_view.frame.size.height == 548.0f || _view.frame.size.height == 460.0f){
    CGRect newFrame = _view.frame;
    newFrame.origin.y = addingHeight;
    newFrame.size.height = [UIScreen mainScreen].bounds.size.height;
    _view.frame = newFrame;
  }
}

- (void)removeAllFromSuperView:(UIView *)superView withTag:(NSInteger)tag
{
    for (UIView *view in [superView subviews])
    {
        if (view.tag == tag)
        {
            [view removeFromSuperview];
        }
    }
}

- (void)placeCategoryTitle:(NSString *)title toFrame:(CGRect)frame
{
    UILabel *label = [[UILabel alloc] init];
    label.frame = frame;
    [categoryGridVew addSubview: label];
    label.backgroundColor = [UIColor clearColor];
    label.font = [UIFont systemFontOfSize: 10.5];
    label.textAlignment = NSTextAlignmentCenter;
    label.adjustsFontSizeToFitWidth = YES;
    label.textColor = [UIColor whiteColor];
    label.text = title;
    label.tag = 665;
}

- (void)displayCategories:(NSArray *) categories
{
    self.categories = categories;
}

- (UIImage *)imageForCategoryAtIndex:(NSInteger)categoryIndex
{
    NSString *imageName = nil;
    
    if (self.defaultCategoriesImages && categoryIndex < self.defaultCategoriesImages.count)
        imageName = self.defaultCategoriesImages[categoryIndex];
    
    if (imageName)
        return [UIImage imageNamed:imageName];
    else
        return nil;
}


- (void)placeImageWithUri:(NSString *)uri toFrame:(CGRect)frame tag:(NSInteger)tag
{
    UIImageView *uiImageView = [[UIImageView alloc] init];
    uiImageView.frame = frame;
    [self.featuredScrollView addSubview: uiImageView];
    uiImageView.contentMode = UIViewContentModeScaleAspectFill;
    uiImageView.clipsToBounds = YES;
    uiImageView.layer.contentsRect = CGRectMake(0, 0.13, 1, 0.74);
    [uiImageView setImageWithURL: [NSURL URLWithString: uri]
                placeholderImage: [UIImage imageNamed: @"placeholder"]
                         success: ^(UIImage *image, BOOL cached){
                             uiImageView.layer.contentsRect = CGRectMake(0, 0, 1, 1);
                         }
                         failure: ^(NSError * error){}];
    
    uiImageView.tag = tag;
    [self addTapRecoginzer:@selector(appClick:) to: uiImageView parent: self.featuredScrollView];
}

- (void)placeApplicationTitle:(NSString *)title toFrame:(CGRect)frame tag:(NSInteger)tag
{
    UILabel *label = [[UILabel alloc] init];
    label.frame = frame;
    [self.featuredScrollView addSubview: label];
    label.font = [UIFont systemFontOfSize: 11];
    label.textAlignment = NSTextAlignmentLeft;
    label.adjustsFontSizeToFitWidth = NO;
    label.textColor = [UIColor colorWithRGB: mBA_GrayColor];
    label.text = title;
    label.tag = tag;
    [self addTapRecoginzer: @selector(appClick:) to: label parent: self.featuredScrollView];
}

- (void)removeAllFromSuperView:(UIView *)superView
{
    for (UIView *subView in [superView subviews]) {
        [subView removeFromSuperview];
    }
}

- (void)displayFeatured:(NSArray *) featuredApplications
{
    [self removeAllFromSuperView: self.featuredScrollView];
    self.featuredScrollView.contentSize = CGSizeMake(featuredApplications.count * 120, featuredCellHeight + 10);

    for (int i = 0; i < featuredApplications.count; i++) {
        mBAApplicationModel *featured = featuredApplications[i];
        int x = (i % 3) * 120 + ( i / 3) * 360;
        [self placeImageWithUri: featured.pictureUrl toFrame: (CGRect){x, 5, 110, featuredCellHeight - 20} tag: i + 32];
        [self placeApplicationTitle: featured.title toFrame: (CGRect){x, 5 + featuredCellHeight - 16, 91, 20}tag: i + 32];
    }
}

- (void)showSearchResultsGrid
{
  if (searchInProgress)
  {
    if(searchResultsGrid.hidden)
    {
      [self toggleSearchResultsGridVisibility];
    }
  }
}

- (void)hideSearchResultsGrid
{
  if (!searchInProgress)
  {
    if(!searchResultsGrid.hidden)
    {
      [self toggleSearchResultsGridVisibility];
    }
  }
}

- (void)showMessageLabel
{
  if(self.messageLabel == nil){
    self.messageLabel = [[UILabel alloc] init];
    self.messageLabel.frame = (CGRect){20, 70, 280, 60};
    self.messageLabel.numberOfLines = 3;
    self.messageLabel.textColor = [UIColor colorWithWhite:1 alpha:0.8];
    self.messageLabel.textAlignment = NSTextAlignmentCenter;
    self.messageLabel.text = @"No results found";
    [_view addSubview:self.messageLabel];
  } else {
    self.messageLabel.hidden = NO;
  }
}

- (void)hideMessageLabel
{
  self.messageLabel.hidden = YES;
}

#pragma mark - UITextField delegate
-(BOOL) textFieldShouldBeginEditing:(UITextField *)textField
{
    return YES;
}

-(void) textFieldDidEndEditing:(UITextField *)textField
{
    [textField resignFirstResponder];
}

- (BOOL)textFieldShouldEndEditing:(UITextField *)textField
{
    [textField resignFirstResponder];
    return YES;
}

- (BOOL)textField:(UITextField *)textField shouldChangeCharactersInRange:(NSRange)range replacementString:(NSString *)string
{
    return YES;
}

- (BOOL)textFieldShouldReturn:(UITextField *)textField
{
    [textField resignFirstResponder];
    _view.userInteractionEnabled = NO;
    [_presenter search:textField.text];
    newSearchConducted = YES;
    return YES;
}

- (BOOL)textFieldShouldClear:(UITextField *)textField
{
    textField.text = @"";
    [self.toolBar clearFoundApplicationsCount];
    _view.userInteractionEnabled = NO;
    [_presenter search:textField.text];
    newSearchConducted = YES;
    [self hideMessageLabel];
    return NO; // clear text manually and return NO! for correct resigning first responder
}

#pragma mark - NRGridView Data Source
- (CGFloat)gridView:(NRGridView*)gridView heightForHeaderInSection:(NSInteger)section
{
    return 0.0f;
}

- (NSInteger)gridView:(NRGridView *)gridView numberOfItemsInSection:(NSInteger)section
{
  if(gridView.tag == kSearchResultsGridViewTag){
    return _presenter.searchGrid_applicationCount;
  } else if(gridView.tag == kCategoriesGridViewTag){
    return _categories.count ? _categories.count : defaultCategoriesImages.count;
  } else return 0;
}

- (NRGridViewCell*)gridView:(NRGridView *)gridView cellForItemAtIndexPath:(NSIndexPath *)indexPath
{
  if(gridView.tag == kSearchResultsGridViewTag)
  {
    static NSString *MyCellIdentifier = @"mBAApplicationListCell";
    
    mBAApplicationModel *applicationModel = [_presenter searchGrid_applicationDataForIndex: indexPath.row];
    
    mBASearchApplicationListCell* cell = (mBASearchApplicationListCell *)[gridView dequeueReusableCellWithIdentifier: MyCellIdentifier];
    if (cell == nil)
    {
      cell = [[mBASearchApplicationListCell alloc] initWithReuseIdentifier: MyCellIdentifier];
      cell.selectionBackgroundView = [[UIView alloc] init];
      
      cell.containerViewShadow = [[UIView alloc] init];
      cell.containerViewShadow.frame = CGRectMake(0, 5, 145, 211);
      cell.containerViewShadow.backgroundColor = [@"#2e175d" asColor];
      cell.containerViewShadow.layer.cornerRadius = kCellCornerRadius;
      
      cell.containerView = [[UIView alloc] init];
      cell.containerView.frame = CGRectMake(0, 5, 145, 210);
      cell.containerView.backgroundColor = [@"#2e175d" asColor];
      cell.containerView.layer.cornerRadius = kCellCornerRadius;
      
      cell.containerView.frame = CGRectOffset( cell.containerView.frame, 0, -4.5 );
      [cell.containerViewShadow addSubview:cell.containerView];
      [cell.contentView addSubview:cell.containerViewShadow];
      
      
      cell.previewImageView = [[UIImageView alloc] init];
      cell.previewImageView.frame =  (CGRect){0, 0, cell.containerView.bounds.size.width, cell.containerView.bounds.size.height - gridlabelHeight};
      cell.previewImageView.contentMode = UIViewContentModeScaleAspectFill;
      cell.previewImageView.clipsToBounds = YES;

      CAShapeLayer * maskLayer = [CAShapeLayer layer];
      maskLayer.path = [UIBezierPath bezierPathWithRoundedRect: cell.previewImageView.bounds byRoundingCorners: UIRectCornerTopLeft | UIRectCornerTopRight cornerRadii: (CGSize){kCellCornerRadius, kCellCornerRadius}].CGPath;
      cell.previewImageView.layer.mask = maskLayer;
      
      cell.imagePlaceholderView = [[UIView alloc] init];
      cell.imagePlaceholderView.frame = cell.previewImageView.frame;
      cell.imagePlaceholderView.backgroundColor = kDefaultPlaceholderColor;
      
      maskLayer = [CAShapeLayer layer];
      maskLayer.path = [UIBezierPath bezierPathWithRoundedRect: cell.imagePlaceholderView.bounds byRoundingCorners: UIRectCornerTopLeft | UIRectCornerTopRight cornerRadii: (CGSize){kCellCornerRadius, kCellCornerRadius}].CGPath;
      
      cell.imagePlaceholderView.layer.mask = maskLayer;
      [cell.containerView addSubview: cell.imagePlaceholderView];
      
      [cell.containerView addSubview: cell.previewImageView];
      
      cell.titleSeparatorView = [[UIView alloc] init];
      cell.titleSeparatorView.frame = CGRectMake(0, cell.containerView.bounds.size.height - gridlabelHeight - 0.5, 145, 0.5);
      cell.titleSeparatorView.backgroundColor = [@"#2e175d" asColor];
      
      [cell.containerView addSubview:cell.titleSeparatorView];
      
      
      cell.titleLabel = [[UILabel alloc] init];
      cell.titleLabel.frame = (CGRect){9, cell.containerView.bounds.size.height - gridlabelHeight, 130, gridlabelHeight};
      cell.titleLabel.font = [UIFont systemFontOfSize: 11.0f];
      cell.titleLabel.textColor = [@"#d4d4e3" asColor];
      cell.titleLabel.textAlignment = NSTextAlignmentLeft;
      cell.titleLabel.backgroundColor = [@"#2e175d" asColor];
      cell.titleLabel.layer.masksToBounds = YES;
      cell.titleLabel.tag = indexPath.row + 32;

      [cell.containerView addSubview: cell.titleLabel];
     
    }

    cell.imagePlaceholderView.backgroundColor = applicationModel.placeholderColor;
    
    [cell.previewImageView setImageWithURL: [NSURL URLWithString: applicationModel.pictureUrl]
                          placeholderImage: nil
                                   success:^(UIImage *image, BOOL dummy) {
                                     CGRect rect = (CGRect){0, 10, image.size.width, image.size.height - 10 - 30};
                                     CGImageRef imageRef = CGImageCreateWithImageInRect(image.CGImage, rect);
                                     cell.previewImageView.image = [UIImage imageWithCGImage:imageRef];
                                     CGImageRelease(imageRef);
                                     
                                     cell.previewImageView.alpha = 0.0f;
                                     [UIView animateWithDuration:0.5f animations:^{
                                       cell.previewImageView.alpha = 1.0f;
                                     }];

                                     
                                   }
                                   failure:^(NSError *error) {  } ];
    
    
    cell.previewImageView.tag = indexPath.row + 32;
    
    cell.titleLabel.text = applicationModel.title;
    
    
    return cell;
    
  }
  else if(gridView.tag == kCategoriesGridViewTag)
  {
    static NSString *cellIdentifier = @"mBA_mainViewCategoryCell";
    mBACategoryModel *category = _categories[indexPath.row];
    
    mBACategoryGridViewCell *cell = (mBACategoryGridViewCell *)[gridView dequeueReusableCellWithIdentifier: cellIdentifier];
    if (cell == nil)
    {
      CGRect cellFrame = CGRectZero;
      cellFrame.size = gridView.cellSize;
      
      cell = [[mBACategoryGridViewCell alloc] initWithFrame:cellFrame andReuseIdentifier:cellIdentifier];
    }
    
    [cell.categoryImageView setImage:[UIImage imageNamed: defaultCategoriesImages[indexPath.row]]];
    cell.categoryTitle = [category.title uppercaseString];
    
    return cell;
  }
  else
    return nil;
}

#pragma mark - NRGridView Delegate

- (void)gridView:(NRGridView*)gridView didLongPressCellAtIndexPath:(NSIndexPath*)indexPath
{
  if(gridView.tag == kSearchResultsGridViewTag){
    [searchResultsGrid deselectCellAtIndexPath: indexPath animated: NO];
    
    [self startAppFromGridView:gridView atIndexPath:indexPath];
    
  } else if(gridView.tag == kCategoriesGridViewTag){
    [searchResultsGrid deselectCellAtIndexPath: indexPath animated: NO];
  }
}

- (void)gridView:(NRGridView *)gridView didSelectCellAtIndexPath:(NSIndexPath *)indexPath
{
  if(gridView.tag == kSearchResultsGridViewTag){
    [searchResultsGrid deselectCellAtIndexPath: indexPath animated: NO];
    [self startAppFromGridView:gridView atIndexPath:indexPath];

  } else if(gridView.tag == kCategoriesGridViewTag){
    [searchResultsGrid deselectCellAtIndexPath: indexPath animated: NO];
    [_presenter toCategoryWithIndex:indexPath.row];
  }
}

- (void)startAppFromGridView:(NRGridView *)gridView atIndexPath:(NSIndexPath *)indexPath
{
  UIImage *splashScreen = ((mBASearchApplicationListCell*) [gridView cellAtIndexPath:indexPath]).previewImageView.image;
  [_presenter appGridCellSelected:indexPath.row withSplashScreenImage:splashScreen];
}

#pragma mark - NRGridView cell Selection
/**
 *  Custom animated cell selection (cell.selectionBackgroundView animation is switched off)
 *
 *  @param cell Current cell
 */
- (void)selectCell:(TCustomGridCell*)cell
{
    UIView *selectionView = [cell viewWithTag: 1111];
    if (!selectionView)
        return;
    
    [cell bringSubviewToFront: selectionView];
    
    [UIView beginAnimations: nil context: nil];
    [UIView setAnimationDuration: 0.5f];
    
    [selectionView setAlpha: 1.0];
    [UIView commitAnimations];
    
    [UIView beginAnimations: nil context: nil];
    [UIView setAnimationDuration: 0.5f];
    
    [selectionView setAlpha: 0.0];
    [UIView commitAnimations];
}

- (void)reloadGrid
{

  if(searchResultsGrid.hidden)
  {
    [self toggleSearchResultsGridVisibility];
  }

  [searchResultsGrid reloadData];
  
  if(newSearchConducted)
  {
    CGPoint contentOffset = searchResultsGrid.contentOffset;
    contentOffset.y = -searchResultsGrid.contentInset.top;
    [searchResultsGrid setContentOffset:contentOffset animated:NO];

    NSUInteger applicationCount = _presenter.searchGrid_applicationCount;
    if(applicationCount){
      [self.toolBar refreshFoundApplicationsCount:applicationCount];
    } else {
      [self.toolBar clearFoundApplicationsCount];
    }
    newSearchConducted = NO;
  }
}


- (void)toggleSearchResultsGridVisibility
{
  BOOL hidden = searchResultsGrid.hidden;
  
  self.categoriesGridView.hidden = hidden;
  searchResultsGrid.hidden = !hidden;
  if(searchResultsGrid.hidden)
  {
    _view.backgroundColor = kSideBarBackgroundColor;
    [self hideMessageLabel];
  }
  else
    _view.backgroundColor = kAppListBackgroundColor;
  
}

- (void) adjustPresentationForKeyboardHeight:(CGFloat)height
{
  [self adjustGridView:self.categoriesGridView forKeyboardHeight:height];
  [self adjustGridView:searchResultsGrid forKeyboardHeight:height];
}

- (void) adjustGridView:(NRGridView *)gridView forKeyboardHeight:(CGFloat)height
{
  UIEdgeInsets contentInset = gridView.contentInset;
  contentInset.bottom = height;
  gridView.contentInset = contentInset;
}

#pragma mark mBASearchViewDelegate methods
-(void)mBASearchViewDidCancelSearch
{
  if(!searchResultsGrid.hidden){
    [self toggleSearchResultsGridVisibility];
  }
  [self textFieldShouldClear:self.toolBar.searchTextField];
}
/**
 * Method for hadling taps on Hamburger (if on main screen)
 * or on "<Back" navigation item on search results screen
 */
-(void)mBASearchViewLeftItemPressed
{
  [_presenter toggleSideBar];
}

@end
