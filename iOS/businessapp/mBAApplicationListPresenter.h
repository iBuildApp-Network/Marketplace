
#import <Foundation/Foundation.h>
#import <UIKit/UIKit.h>
#import "RestServiceDelegate.h"
#import "mBAApplicationModel.h"
#import "mBACategoryModel.h"

/**
* Manages the application list screen
*/
@interface mBAApplicationListPresenter : UIViewController<UIGestureRecognizerDelegate, RestServiceDelegate>

/**
* Model category. information about which is displayed when not in Favorites Favorites
*/
@property (nonatomic, assign) mBACategoryModel *category;

/**
* If YES, show favorites
*/
@property Boolean favouritesMode;

/**
 * YES if we are in search mode
 */
@property Boolean searchMode;


@property (nonatomic, retain) UIImage* screenshotImage;

/**
* When you click on the "back" button, we return to the main screen
*/
- (void)back;

/**
* Returns the number of applications displayed
*/
- (NSInteger)applicationCount;

- (NSInteger)searchApplicationCount;

/**
* The data for the index, for a given index
*/
- (mBAApplicationModel *)applicationDataForIndex:(NSInteger)index;

/**
* Performed when a cell is selected
*/
- (void)appGridSelected:(NSInteger)index;

/**
* Search for an application
*/
- (void)search:(NSString *)query;

/**
 * Running an application with the specified splash screen
 */
- (void) appGridCellSelected:(NSInteger)index withSplashScreenImage:(UIImage *)splashScreen;

- (void) preloadApps;

@end
