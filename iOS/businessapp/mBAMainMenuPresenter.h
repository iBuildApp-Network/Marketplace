// IBAHeader

#import <UIKit/UIKit.h>
#import "mBAApplicationModel.h"
#import "RestServiceDelegate.h"
#import "mBASideBarVC.h"
#import "mBASideBarView.h"
#import <MessageUI/MessageUI.h>


/**
* Manages the main application screen
*/
@interface mBAMainMenuPresenter : UIViewController<UIGestureRecognizerDelegate,
                                                    UIActionSheetDelegate,
                                                    RestServiceDelegate,
                                                    MFMailComposeViewControllerDelegate>

/**
* Go to the screen for adding an application
*/
- (void)toPlaceAppView:(UITapGestureRecognizer*)recognizer;

/**
 * Go to the category screen, the category index is passed to the field tag of the view element,
 * associated with recognizer
*/
- (void)toCategoryView:(UITapGestureRecognizer*)recognizer;

/**
* Sending a search request
*/
- (void)search:(NSString *)query;

/**
* Returns a list of attachments
*/
- (NSInteger)searchGrid_applicationCount;

/**
* Returns application data for the specified index
*/
- (mBAApplicationModel *)searchGrid_applicationDataForIndex:(NSInteger)index;

/**
 * Called to go to the specified controller
 */
- (void)toController:(UIViewController *)controller;

/**
 *  Method for showing / hiding the side bar
 */
- (void) toggleSideBar;

/**
 * Go to the category screen, the category index is passed to categoryIndex
 */
- (void)toCategoryWithIndex:(NSUInteger)categoryIndex;

- (void)startApp:(NSInteger)appid withSplashScreen:(UIImage *)spashScreenImage;

- (void)startAppById:(NSInteger)appid;

- (void)appGridCellSelected:(NSInteger)index withSplashScreenImage:(UIImage *)splashScreen;

@property (nonatomic, assign) mBASideBarVC *sideBarController;

- (void)preloadCategories;

@end
