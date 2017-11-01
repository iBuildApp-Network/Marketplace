
#import <Foundation/Foundation.h>
#import <UIKit/UIKit.h>
#import "ViewBase.h"
#import "NRGridView.h"
#import "mBASearchBarView.h"
#import "NSString+colorizer.h"

  // TODO: Localize it!!!
#define kNoFavouritedAppsText NSLocalizedString(@"masterApp_NoFavourites", @"No favourited apps")//@"No featured apps"
#define kNoSearchResultText NSLocalizedString(@"masterApp_NoResultsFound", @"No results found")
#define kAppListBackgroundColor [@"#17161b" asColor]

@class mBACategoryModel;
@class mBAApplicationListPresenter;


@interface mBAApplicationListView : ViewBase <mBASearchViewDelegate, NRGridViewDelegate, NRGridViewDataSource, UITextFieldDelegate>

@property (nonatomic, assign) mBACategoryModel *category;
@property (nonatomic, strong) UISearchBar *searchBar;
@property (nonatomic, strong) UILabel     *messageLabel;
@property (nonatomic, retain) NRGridView *gridView;
@property int currentRow;

- (void)showMessageLabel;
- (void)hideMessageLabel;
- (void)showSearchBar;
- (void)hideSearchBar;

- (void)viewForPresenter:(mBAApplicationListPresenter *)presenter;

- (void)reloadGrid;
- (void)refreshApplicationCount;

- (void)setCurrentIndex:(int)current;
- (int)getCurrentIndex;

@end
