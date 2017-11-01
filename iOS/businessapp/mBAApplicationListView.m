
#import "mBAApplicationListView.h"
#import "mBAApplicationListPresenter.h"
#import "UIColor+RGB.h"
#import "customgridcell.h"
#import "UIImageView+WebCache.h"
#import "UIImage+color.h"

#define kMessageLabelLeftMargin 20.f
#define kMessageLabelOriginY    120.f
#define kMessageLabelHeight     25.f

#define kCellImagePreviewLeftMargin 10.f
#define kCellImagePreviewBottomMargin 10.f
#define kGrigViewLeftMargin 5.f
#define kCellCornerRadius 5.f

#define kApplicationCountLabelPaddingRight 10.0f

@interface mBAApplicationListCell : NRGridViewCell

@property (nonatomic, retain) UIImageView       *previewImageView;
@property (nonatomic, retain) UILabel           *titleLabel;
@property (nonatomic, retain) UIView            *imagePlaceholderView;
@property (nonatomic, retain) UIView            *containerView;
@property (nonatomic, retain) UIView            *containerViewShadow;
@property (nonatomic, retain) UIView            *titleSeparatorView;

@end

@implementation mBAApplicationListCell


- (void) dealloc
{
  self.previewImageView = nil;
  self.titleLabel = nil;
  self.imagePlaceholderView = nil;
  self.containerView = nil;
}

@end

@implementation mBAApplicationListView {
  mBAApplicationListPresenter *_presenter;

  UIView *view;
  UIColor *backgroundColor;
  UIColor *statusBarColor;
  UIColor *labelColor;
  mBASearchBarView *topToolbar;

  float statusBarHeight;
  float searchBarHeight;
  float addingHeight;

  float appGridStart;
  float labelTopMargin;

  float gridlabelHeight;
  float gridViewCellHeight;

  float backButonStart;
  
  int currentIndex;
  BOOL canUpdate;
  int scrollCount;
  NSMutableDictionary *scrollPermissions;
  
  NSInteger currentAppcount;
  
  CGPoint contentOffsetTop;
  
  int deadIndex;
  
  BOOL categorySearch;
}

@synthesize category, searchBar, messageLabel;

#pragma mark -
- (id) init
{
  self = [super init];
  searchBar = nil;
  messageLabel = [[UILabel alloc] init];
  topToolbar = nil;
  _gridView = nil;
  scrollPermissions = [[NSMutableDictionary alloc] init];
  scrollCount = 0;
  categorySearch = NO;
  return self;
}

- (void) dealloc
{
    searchBar = nil;
    topToolbar = nil;
  messageLabel = nil;
  
  self.gridView = nil;
}

#pragma mark -
- (void)initMetrix {
  
  if (SYSTEM_VERSION_GREATER_THAN_OR_EQUAL_TO(@"7.0"))
    addingHeight             =  20.0f;
  else
    addingHeight             =  0.0f;
    statusBarHeight      =    44.0f;
    searchBarHeight      =    44.0f;
    appGridStart         =   addingHeight + 44.0f;
    labelTopMargin       =    200.0f;
    gridlabelHeight      =   27.0f;
    gridViewCellHeight   =   220.0f;
    backButonStart       = (float) (12.0 + addingHeight);
  contentOffsetTop = (CGPoint){0.0f, 10.0f};
}

- (void)initColors
{
  backgroundColor = [UIColor colorWithRGB: 0xefefef];
  statusBarColor  = kToolbarColor;
  labelColor      = [UIColor colorWithRGB: 0x989898];
}

- (void)addTapRecoginzer:(SEL)action to:(UIView *)playTapZone parent:(UIView *)parentView
{
  CGRect viewFrame = playTapZone.frame;
  playTapZone.userInteractionEnabled = YES;

  UIView *setTapZone = [[UIView alloc] init];
  setTapZone.frame = viewFrame;
  setTapZone.backgroundColor = [UIColor clearColor];
  setTapZone.userInteractionEnabled = YES;
  setTapZone.tag = playTapZone.tag;
  [parentView addSubview: setTapZone];

  UITapGestureRecognizer *playPauseZoneTap = [[UITapGestureRecognizer alloc] initWithTarget: _presenter action: action];
  playPauseZoneTap.delegate = _presenter;
  [setTapZone addGestureRecognizer: playPauseZoneTap];
}

- (void)showMessageLabel
{
  [self.messageLabel removeFromSuperview];
  self.messageLabel.frame = (CGRect){kMessageLabelLeftMargin, kMessageLabelOriginY, view.bounds.size.width - 2*kMessageLabelLeftMargin, kMessageLabelHeight};
  self.messageLabel.numberOfLines = 1;
  self.messageLabel.textColor = [UIColor colorWithWhite:1 alpha:0.8];
  self.messageLabel.textAlignment = NSTextAlignmentCenter;
  self.messageLabel.backgroundColor = [UIColor clearColor];
  
  if (_presenter.favouritesMode && !_presenter.searchMode)
    self.messageLabel.text = kNoFavouritedAppsText;
  else
    self.messageLabel.text = kNoSearchResultText;
  
  [topToolbar clearFoundApplicationsCount];
  [view addSubview:self.messageLabel];
}

- (void)hideMessageLabel
{
  [self.messageLabel removeFromSuperview];
}
- (void)hideStatusBar
{
}

- (void) placeSeparatorOnToolbar:(UIView *)toolbar
{
  CGFloat originY = toolbar.frame.origin.y + toolbar.frame.size.height;
  CGRect separatorFrame = (CGRect){0.0f, originY, toolbar.frame.size.width, kToolbarSeparatorHeight};
  
  UIView *separator = [[UIView alloc] initWithFrame:separatorFrame];
  separator.backgroundColor = kToolbarSeparatorColor;
  toolbar.clipsToBounds = NO;
  
  [view addSubview:separator];
}


- (void)setupTopToolbar
{
  topToolbar = [[mBASearchBarView alloc] initWithApperance:mBASearchBarViewSearchResultsScreenAppearance];
  topToolbar.mBASearchViewDelegate = self;
  topToolbar.mBASearchViewTextFieldDelegate = self;
  
  if (_presenter.favouritesMode){
    topToolbar.title = NSLocalizedString(@"masterApp_Favourites", @"Favourites");
  }
  else {
    topToolbar.title = category.title;
  }
  
  if(addingHeight > 0){
    UIView *statusBar = [[UIView alloc] init];
    statusBar.frame = (CGRect){0.0f, 0.0f, view.frame.size.width, addingHeight};
    statusBar.backgroundColor = kToolbarColor;
    [view addSubview:statusBar];
    
    CGRect shiftedDownSearchBarFrame = topToolbar.frame;
    shiftedDownSearchBarFrame.origin.y += addingHeight;
    topToolbar.frame = shiftedDownSearchBarFrame;
  }
  
  [view addSubview:topToolbar];
}

- (void)setupSearchBar
{
  if(searchBar != nil){
    [searchBar removeFromSuperview];
    searchBar = nil;
  }
  searchBar = [[UISearchBar alloc] init];
  searchBar.frame = (CGRect) {0.0f, addingHeight + statusBarHeight, view.bounds.size.width, searchBarHeight};
  searchBar.translucent = YES;
  searchBar.opaque = NO;
  searchBar.backgroundColor = [UIColor whiteColor];
  searchBar.tintColor = [UIColor whiteColor];
  searchBar.placeholder = NSLocalizedString(@"masterApp_SearchPlaceholder", @"Search");
  searchBar.delegate = nil;
  
  UITextField *txtSearchField = [searchBar valueForKey: @"_searchField"];
  txtSearchField.returnKeyType = UIReturnKeySearch;
  txtSearchField.delegate = self;
  if (SYSTEM_VERSION_GREATER_THAN_OR_EQUAL_TO(@"7.0")) {
    searchBar.barTintColor = [UIColor whiteColor];
  }
  else
  {
    txtSearchField.borderStyle = UITextBorderStyleNone;
    txtSearchField.layer.backgroundColor = [UIColor whiteColor].CGColor;
    txtSearchField.layer.cornerRadius = 15.0f;
    txtSearchField.background = nil;
  }
  [view addSubview: searchBar];
  searchBar.clipsToBounds = NO;

    if(SYSTEM_VERSION_LESS_THAN(@"8.0")) {
        UIView *bottomLine = [[UIView alloc] init];
        bottomLine.frame = (CGRect) {0.0f, searchBar.frame.size.height - 2.0f, searchBar.frame.size.width, 1.0f};
        bottomLine.backgroundColor = [UIColor colorWithWhite:0 alpha:0.2];
        [searchBar addSubview: bottomLine];
    }
}

- (void)showSearchBar
{
  searchBar.hidden = NO;
}

- (void)hideSearchBar
{
  searchBar.hidden = YES;
}


- (CGRect)frameTop:(float)height in:(CGRect)frame
{
  return (CGRect){frame.origin.x, frame.origin.y, frame.size.width, height};
}

- (CGRect)frameBottom:(float)height in:(CGRect)frame
{
  return (CGRect){ frame.origin.x, frame.origin.y  +frame.size.height - height, frame.size.width, height};
}

- (void)placeAppImageViewStub:(NSString *)title imageName:(NSString *)imageName frame:(CGRect)frame
{
  CGRect imageFrame = [self frameTop:    162.0f in: frame];
  CGRect labelFrame = [self frameBottom:  20.0f in: frame];

  UIImageView *uiImageView = [[UIImageView alloc] initWithFrame: imageFrame];
  [view addSubview: uiImageView];
  uiImageView.image = [UIImage imageNamed: imageName];

  UILabel *titleLabel = [[UILabel alloc] initWithFrame: labelFrame];
  [view addSubview: titleLabel];
  titleLabel.text = title;
  titleLabel.textColor = labelColor;
  titleLabel.backgroundColor = [UIColor clearColor];
  titleLabel.font = [UIFont systemFontOfSize: 11.0f];
}

- (void)placeGridView
{
  if (_gridView != nil)
  {
    [_gridView removeFromSuperview];
    self.gridView = nil;
  }

  self.gridView = [[NRGridView alloc] initWithFrame: (CGRect){kGrigViewLeftMargin, appGridStart, view.bounds.size.width - 2*kGrigViewLeftMargin, view.bounds.size.height - appGridStart}];
  _gridView.autoresizesSubviews = YES;
  _gridView.autoresizingMask = UIViewAutoresizingFlexibleHeight;
  _gridView.layoutStyle = NRGridViewLayoutStyleVertical;
  _gridView.cellSize = (CGSize){(int)view.bounds.size.width / 2 - kGrigViewLeftMargin, gridViewCellHeight};
  _gridView.delegate = self;
  _gridView.dataSource = self;
  _gridView.showsHorizontalScrollIndicator = NO;
  _gridView.showsVerticalScrollIndicator = NO;

  _gridView.backgroundColor = kAppListBackgroundColor;
  [view addSubview:_gridView];
  
  view.backgroundColor = kAppListBackgroundColor;
  currentIndex = 4;
  self.currentRow = 4;
}

- (void)placeOriginalViewImage
{
  UIImageView *uiImageView = [[UIImageView alloc] initWithFrame: CGRectMake(0.0f, 0.0f, view.bounds.size.width, view.frame.size.height)];
  uiImageView.alpha = 0.4f;
  [view addSubview: uiImageView];
  uiImageView.image = [UIImage imageNamed: @"directory_app_list"];
}

- (void)viewForPresenter:(mBAApplicationListPresenter *)presenter
{
    NSLog(@"applist");
  _presenter = (mBAApplicationListPresenter *)presenter;
  view = presenter.view;
  view.backgroundColor = [UIColor whiteColor];
  [[view subviews] makeObjectsPerformSelector:@selector(removeFromSuperview)];

  [self initMetrix];
  [self initColors];

  [self hideStatusBar];
  [self setupTopToolbar];
  [self placeGridView];
  [self placeSeparatorOnToolbar:topToolbar];
  
  NSLog(@"viewForPresenter");
  
}

/**
 * Fix for ios 8. Otherwise crashes with unrecognizedSelector sent...
 */
- (UISearchController *)_searchController
{
  return nil;
}

#pragma mark - UITextField delegate
-(BOOL) textFieldShouldBeginEditing:(UITextField *)textField
{
  textField.returnKeyType = UIReturnKeySearch;
  return YES;
}

-(void) textFieldDidEndEditing:(UITextField *)textField
{
  categorySearch = YES;
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
  
  if (! (textField.text && textField.text.length)) {
    _presenter.searchMode = NO;
    [topToolbar clearFoundApplicationsCount];
    [self scrollGridViewToTop];
  }
  view.userInteractionEnabled = NO;
  [_presenter search:textField.text];
  return YES;
}

- (BOOL)textFieldShouldClear:(UITextField *)textField
{
  textField.text = @"";
  _presenter.searchMode = NO;
  [topToolbar clearFoundApplicationsCount];
  [self scrollGridViewToTop];
  view.userInteractionEnabled = YES;
  [_presenter search:textField.text];
  [textField resignFirstResponder];
  return NO;
}

#pragma mark - NRGridView Data Source
- (CGFloat)gridView:(NRGridView*)gridView heightForHeaderInSection:(NSInteger)section
{
  return 0.0f;
}

- (NSInteger)gridView:(NRGridView *)gridView numberOfItemsInSection:(NSInteger)section
{
  return _presenter.applicationCount;
}

- (NRGridViewCell*)gridView:(NRGridView *)gridView_ cellForItemAtIndexPath:(NSIndexPath *)indexPath
{
  
  canUpdate = NO;
  [scrollPermissions setObject:@NO forKey:indexPath];

  NSLog(@"cell created %ld", (long)indexPath.row);
  mBAApplicationModel *applicationModel = [_presenter applicationDataForIndex: indexPath.row];
  
  NSString *MyCellIdentifier = @"mBAApplicationListCell";

  mBAApplicationListCell* cell = (mBAApplicationListCell *)[gridView_ dequeueReusableCellWithIdentifier: MyCellIdentifier];
  if (cell == nil)
  {
    cell = [[mBAApplicationListCell alloc] initWithReuseIdentifier: MyCellIdentifier];
    [cell.contentView setHidden:YES];
    
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
    [cell.contentView addSubview:cell.containerViewShadow];
    [cell.containerViewShadow addSubview:cell.containerView];
    cell.previewImageView = [[UIImageView alloc] init];
    cell.previewImageView.frame =  (CGRect){0, 0, cell.containerView.bounds.size.width, cell.containerView.bounds.size.height - gridlabelHeight - 0.5};
    cell.previewImageView.contentMode = UIViewContentModeScaleAspectFill;
    cell.previewImageView.clipsToBounds = YES;

    
      // do rounding only the upper corners of the picture
    CAShapeLayer * maskLayer = [CAShapeLayer layer];
    maskLayer.path = [UIBezierPath bezierPathWithRoundedRect: cell.previewImageView.bounds byRoundingCorners: UIRectCornerTopLeft | UIRectCornerTopRight cornerRadii: (CGSize){kCellCornerRadius, kCellCornerRadius}].CGPath;
    cell.previewImageView.layer.mask = maskLayer;
    cell.imagePlaceholderView = [[UIView alloc] init];
    cell.imagePlaceholderView.frame = cell.previewImageView.frame;
    
    NSLog(@"placeholder color %@", applicationModel.placeholderColor);
    cell.imagePlaceholderView.backgroundColor = [applicationModel.placeholderColorString asColor];
    
      // We only round the top corners of the placeholder
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
  
  [cell.contentView setHidden:NO];
  cell.imagePlaceholderView.backgroundColor = [applicationModel.placeholderColorString asColor];
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
                                   
                                   canUpdate = YES;
                                   [scrollPermissions setObject:@YES forKey:indexPath];
                                 }
                                 failure:^(NSError *error) {  } ];
  
  
  cell.previewImageView.tag = indexPath.row + 32;
  
  cell.titleLabel.text = applicationModel.title;
  NSLog(@"appp label %@", applicationModel.title);
    NSLog(@"appp id %ld", (long)applicationModel.app_id);
    NSLog(@"category id %ld", (long)applicationModel.category_id);
  return cell;
}


#pragma mark - NRGridView Delegate

- (void)gridView:(NRGridView*)gridView didLongPressCellAtIndexPath:(NSIndexPath*)indexPath
{
  mBAApplicationListCell* cell =  (mBAApplicationListCell *) [gridView cellAtIndexPath:indexPath];
  if ([cell.titleLabel.text length] > 0) {
    [gridView deselectCellAtIndexPath: indexPath animated: NO];
    [self startAppFromGridView:gridView atIndexPath:indexPath];
  }
}

- (void)gridView:(NRGridView *)gridView didSelectCellAtIndexPath:(NSIndexPath *)indexPath
{
  mBAApplicationListCell* cell =  (mBAApplicationListCell *) [gridView cellAtIndexPath:indexPath];
  
  if ([cell.titleLabel.text length] > 0) {
    [gridView deselectCellAtIndexPath: indexPath animated: NO];
    [self startAppFromGridView:gridView atIndexPath:indexPath];
  }
}

- (void)startAppFromGridView:(NRGridView *)gridView atIndexPath:(NSIndexPath *)indexPath
{
  UIImage *splashScreen = ((mBAApplicationListCell*) [gridView cellAtIndexPath:indexPath]).previewImageView.image;
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
  if(_presenter.searchMode){
    [self scrollGridViewToTop];
    _presenter.searchMode = NO;
  }
  [_gridView reloadData];
}

- (void)setCurrentIndex:(int)current
{
  NSLog(@"setter");
  self.currentRow = current;
}

- (int)getCurrentIndex
{
  NSLog(@"getter");
  return self.currentRow;
}


- (void)scrollViewWillBeginDragging:(UIScrollView *)scrollView
{
  NSLog(@"scroll");
}

-(void)scrollViewDidScroll: (UIScrollView*)scrollView
{
  float scrollViewHeight = scrollView.frame.size.height;
  float scrollContentSizeHeight = scrollView.contentSize.height;
  float scrollOffset = scrollView.contentOffset.y;
  
  if (scrollOffset == 0)
  {
      // then we are at the top
  }
  else if (scrollOffset + scrollViewHeight == scrollContentSizeHeight)
  {
      // then we are at the end
    if (canUpdate == YES && !_presenter.favouritesMode) {
      self.currentRow += 8;
      canUpdate = NO;
    } else if (canUpdate == YES && _presenter.favouritesMode) {
      self.currentRow += 8;
      canUpdate = NO;
    }
  }
}

-(void)scrollViewDidEndDragging:(UIScrollView *)scrollView willDecelerate:(BOOL)decelerate
{
  NSLog(@"current index %d", self.currentRow);
  if (_presenter.favouritesMode || categorySearch) {
    NSLog(@"short drug");
    NSLog(@"app count = %ld", (_presenter.searchApplicationCount + 1)/2);
    if ((_presenter.searchApplicationCount + 1)/2 == (_presenter.searchApplicationCount)/2) {
      self.gridView.contentSize =  CGSizeMake(self.gridView.contentSize.width, 220 * (_presenter.searchApplicationCount + 1)/2 - 100);
    }
    else{
      self.gridView.contentSize =  CGSizeMake(self.gridView.contentSize.width, 220 * (_presenter.searchApplicationCount + 1)/2 + 10);}
  }
  else
  {
    NSLog(@"long drug");
    self.gridView.contentSize =  CGSizeMake(self.gridView.contentSize.width, 220 * self.currentRow + 10);
  }
}

- (void)refreshApplicationCount
{
  if(_presenter.searchMode) {
    [topToolbar refreshFoundApplicationsCount:_presenter.searchApplicationCount];
  }
}

-(void)mBASearchViewDidCancelSearch
{
  NSLog(@"cancel search");
  categorySearch = NO;
  [self textFieldShouldClear:topToolbar.searchTextField];
}
/**
 * Method for hadling taps on Hamburger (if on main screen)
 * or on "<Back" navigation item on search results screen
 */
-(void)mBASearchViewLeftItemPressed
{
  [_presenter back];
}

-(void) scrollGridViewToTop
{
  _gridView.contentOffset = (CGPoint){0.0f, -10.0f};
}

@end
