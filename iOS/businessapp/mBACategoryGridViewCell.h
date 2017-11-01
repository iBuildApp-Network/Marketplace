#import "NRGridViewCell.h"

/*
 * NSString + size returns slightly bigger size that 
 * needed to fit the very string.
 * This constants define this difference.
 */
#define kCalculatedLabelSizeTopOverhead 4.0f
#define kCalculatedLabelSizeBottomOverhead 3.0f
#define kCalculatedLabelSizeHorizontalOverhead 0.0f

#define kCategoryCellWidth 160.0f
#define kCategoryCellHeight 128.0f
#define kCategoryTitleFontSize 14.0f
#define kCategoryTitleTextColor [UIColor whiteColor]
#define kCategoryTitleFont [UIFont boldSystemFontOfSize:kCategoryTitleFontSize]
#define kCategoryTitleBackgroundColor [UIColor colorWithWhite:0.0f alpha:0.5f]

#define kCategoryTitlePaddingTop 7.0f - kCalculatedLabelSizeTopOverhead
#define kCategoryTitlePaddingBottom 5.0f - kCalculatedLabelSizeBottomOverhead
#define kCategoryTitleHorizontalPadding 10.0f - kCalculatedLabelSizeHorizontalOverhead

@interface mBACategoryGridViewCell : NRGridViewCell

@property (nonatomic, retain) UIImageView *categoryImageView;
@property (nonatomic, retain) NSString *categoryTitle;

/**
 * Preferred way to init mBACategoryGridViewCell
 */
- (id)initWithFrame:(CGRect)frame andReuseIdentifier:(NSString *)reuseIdentifier;

@end
