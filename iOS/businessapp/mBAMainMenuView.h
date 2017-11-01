// IBAHeader

#import <Foundation/Foundation.h>
#import <UIKit/UIKit.h>
#import "ViewBase.h"
#import "NRGridViewDelegate.h"
#import "NRGridViewDataSource.h"
#import "mBASideBarVC.h"
#import "mBACategoryGridViewCell.h"
#import "mBASearchBarView.h"
#import "NSString+colorizer.h"

#define kHamburgerPadding 14.0f
#define kSearchTextFieldHeight 30.0f
#define kSearchIconWidth 22.0f
#define kSearchIconPaddingRight 16.0f
#define kSearchIconVerticalPadding 11.0f

#define kCancelLabelFontSize 18.0f
#define kCancelLabelHorizontalPadding 10.0f
#define kCancelLabelFontColor [UIColor whiteColor]
#define kMainPageTitleFontSize 18.0f
#define kMainPageTitleFontColor [UIColor whiteColor]
#define kSearchBarTextViewFontSize 14.0f

#define kSearchBarTextFieldCornerRadius 3.5f

#define kAppListBackgroundColor [@"#17161b" asColor]

@class mBAMainMenuPresenter;

@interface mBAMainMenuView : ViewBase<NRGridViewDelegate, NRGridViewDataSource, UIGestureRecognizerDelegate, UITextFieldDelegate, mBASearchViewDelegate>

@property (nonatomic, strong) UILabel *messageLabel;

- (void)displayCategories:(NSArray *)categories;
- (void)displayFeatured:(NSArray *)featuredApplications;

- (void)showSearchResultsGrid;
- (void)hideSearchResultsGrid;

- (void)showMessageLabel;
- (void)hideMessageLabel;

- (void)reloadGrid;

- (void)viewForPresenter:(mBAMainMenuPresenter *)presenter;

- (void) adjustPresentationForKeyboardHeight:(CGFloat)height;

@end
