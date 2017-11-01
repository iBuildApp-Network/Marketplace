#import <UIKit/UIKit.h>
#import "mBAMainMenuPresenter.h"

#define kAboutUsPicHeight            128.0f
#define kAboutUsTextParagraphGap    10.0f
#define kAboutUsTextHorizontalMargin 20.0f
#define kAboutUsTextMarginTop        20.0f
#define kAboutUsTextFontSize         16.0f
#define kAboutUsTextColor            [UIColor colorWithWhite:0.5f alpha:1.0]
#define kWebSiteButtonWidth         140.f
#define kWebSiteButtonHeight        25.f


@interface mBAAboutUsViewController : UIViewController

- (id)initWithPresenter:(mBAMainMenuPresenter *) presenter;

@end
