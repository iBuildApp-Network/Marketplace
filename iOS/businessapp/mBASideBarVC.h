
#import <UIKit/UIKit.h>

@interface mBASideBarVC : UIViewController

- (id) initWithMainViewController:(UIView *)main hiddenViewController:(UIView *)hidden;
- (void) moveMainViewHorizontally:(CGFloat)shift;

/**
 * Method for any external controller to open or hide side bar
 */
- (void) toggleHiddenView;

- (void) hideSideBarAnimated:(BOOL)animated;
- (void) showSideBarAnimated:(BOOL)animated;

/**
 * View on top.
 */
@property (nonatomic, retain) UIView *mainView;

/**
 * Hidden view below the main. Currently appears from LEFT.
 */
@property (nonatomic, retain) UIView *hiddenView;

@property (nonatomic, getter=isShifted) BOOL shifted;

@end
