
#import <UIKit/UIKit.h>

#define kSideBarBackgroundColor [UIColor colorWithRed:45.0f/256.0f green:51.0f/256.0f blue:54.0f/256.0f alpha:1.0]
#define kSideBarSeparatorColor [UIColor colorWithWhite:1.0f alpha:0.2f]

#define kSideBarWidth 220.0f
#define kSideBarWidthForSharingMode 220.f

#define kSideBarMenuPaddingTop 51.0f
#define kSideBarMenuItemsHorizontalPadding 20.0f

#define kSideBarMenuItemsSeparatorWidth (kSideBarWidth - 2 * kSideBarMenuItemsHorizontalPadding)
#define kSideBarMenuItemsSeparatorHeight 1.0f
#define kSideBarSharingButtonsHeight 60.f

#define kSideBarMenuItemsTextLabelHeight 50.0f
#define kSideBarMenuItemsFontSize 23.0f
#define kSideBarMenuItemsFontColor [UIColor whiteColor]
#define kSideBarMenuItemsSelectedFontColor [UIColor colorWithRed:0.7 green:0.7 blue:1 alpha:1]


#define SYSTEM_VERSION_EQUAL_TO(v)                  ([[[UIDevice currentDevice] systemVersion] compare:v options:NSNumericSearch] == NSOrderedSame)
#define SYSTEM_VERSION_GREATER_THAN(v)              ([[[UIDevice currentDevice] systemVersion] compare:v options:NSNumericSearch] == NSOrderedDescending)
#define SYSTEM_VERSION_GREATER_THAN_OR_EQUAL_TO(v)  ([[[UIDevice currentDevice] systemVersion] compare:v options:NSNumericSearch] != NSOrderedAscending)
#define SYSTEM_VERSION_LESS_THAN(v)                 ([[[UIDevice currentDevice] systemVersion] compare:v options:NSNumericSearch] == NSOrderedAscending)
#define SYSTEM_VERSION_LESS_THAN_OR_EQUAL_TO(v)     ([[[UIDevice currentDevice] systemVersion] compare:v options:NSNumericSearch] != NSOrderedDescending)

@interface ReportSideBarVC : UIViewController

@property (nonatomic, strong) UIView *sideBar;
@property (nonatomic, strong) UIImageView *screenSlide;
@property (nonatomic, strong) UIImageView *sideBarFavorite;
@property (nonatomic, retain) UIImage *screenshotImage;
@property (nonatomic, retain) UIImageView *sideBarBtn;
@property (nonatomic, assign) BOOL        showSideBarBtnImage;
@property BOOL fromMainScreen;

- (void) slideOut;
- (void) slideIn;
- (void) getScreenshot;

@end
