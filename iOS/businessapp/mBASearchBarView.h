
#import <UIKit/UIKit.h>

#define kToolbarColor [UIColor blackColor]
#define kToolbarHeight 44.0f
#define kToolbarSeparatorHeight 1.0f
#define kToolbarSeparatorColor [UIColor colorWithWhite:1.0f alpha:0.2f]

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

#define kAppCountTextColor [UIColor colorWithWhite:0.5f alpha:1.0]

typedef enum {
  mBASearchBarViewMainScreenAppearance = 1,
  mBASearchBarViewSearchResultsScreenAppearance = 2
} mBASearchBarViewAppearance;

@protocol mBASearchViewDelegate

@optional
-(void)mBASearchViewDidShowSearchField;
-(void)mBASearchViewDidCancelSearch;

-(void)mBASearchViewLeftItemPressed;

@end

@interface mBASearchBarView : UIView {
    BOOL searchInProgress;
}

- (id) initWithFrame:(CGRect)frame apperance:(mBASearchBarViewAppearance) appearance;
- (id) initWithApperance:(mBASearchBarViewAppearance) appearance;

- (void) refreshFoundApplicationsCount:(NSUInteger)newValue;
- (void) clearFoundApplicationsCount;

@property (nonatomic, assign) id<NSObject, mBASearchViewDelegate> mBASearchViewDelegate;
@property (nonatomic, assign) id<NSObject, UITextFieldDelegate> mBASearchViewTextFieldDelegate;

@property (nonatomic, retain) UITextField *searchTextField;
@property (nonatomic, assign) BOOL searchInProgress;
@property (nonatomic, retain) NSString *title;
@property (nonatomic, assign, readonly) mBASearchBarViewAppearance appearance;

@end
