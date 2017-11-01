
#import "mBACategoryGridViewCell.h"
#import "NSString+size.h"

@interface mBACategoryGridViewCell()
{
  UIView *titleBackgroundView;
  UILabel *titleLabel;
  CGRect initialFrame;
  CGSize maxTitleLabelSize;
}

@end

@implementation mBACategoryGridViewCell

- (id)initWithFrame:(CGRect)frame andReuseIdentifier:(NSString *)reuseIdentifier
{
    self = [super initWithReuseIdentifier:reuseIdentifier];
  
    if (self) {
        // Initialization code
      initialFrame = frame;
      maxTitleLabelSize = (CGSize){initialFrame.size.width - 2 * kCategoryTitleHorizontalPadding,
        initialFrame.size.height - kCategoryTitlePaddingTop -kCategoryTitlePaddingBottom};
      
      CGRect categoryImageViewFrame = CGRectZero;
      categoryImageViewFrame.size = frame.size;
      _categoryImageView = [[UIImageView alloc] initWithFrame:categoryImageViewFrame];
      _categoryImageView.contentMode = UIViewContentModeScaleAspectFill;
      [self addSubview:_categoryImageView];
      
      CGFloat initialHeight = kCategoryTitlePaddingTop + kCategoryTitlePaddingBottom;
      CGRect titleBackgroundViewRect = (CGRect){0.0f, frame.size.height - initialHeight, frame.size.width, initialHeight};
      
      titleBackgroundView = [[UIView alloc] initWithFrame:titleBackgroundViewRect];
      titleBackgroundView.backgroundColor = kCategoryTitleBackgroundColor;
      
      CGRect titleLabelRect = (CGRect){kCategoryTitleHorizontalPadding, kCategoryTitlePaddingTop, frame.size.width - 2*kCategoryTitleHorizontalPadding, 0.0f};
      
      titleLabel = [[UILabel alloc] initWithFrame:titleLabelRect];
      titleLabel.textColor = kCategoryTitleTextColor;
      titleLabel.font = kCategoryTitleFont;
      titleLabel.lineBreakMode = NSLineBreakByWordWrapping;
      titleLabel.numberOfLines = 0;
      titleLabel.backgroundColor = [UIColor clearColor];
      
      [titleBackgroundView addSubview:titleLabel];
      [self addSubview:titleBackgroundView];
    }
    return self;
}

- (void)setCategoryTitle:(NSString *)title
{
  CGSize currentTitleLabelSize = [_categoryTitle sizeForFont:kCategoryTitleFont
                                      limitSize:maxTitleLabelSize
                                  lineBreakMode:titleLabel.lineBreakMode];
  
  CGSize newTitleLabelSize = [title sizeForFont:kCategoryTitleFont
                                             limitSize:maxTitleLabelSize
                                         lineBreakMode:titleLabel.lineBreakMode];
    if(currentTitleLabelSize.height != newTitleLabelSize.height){
      CGFloat diff = newTitleLabelSize.height - currentTitleLabelSize.height;
      
      CGRect newBackgroundViewFrame = titleBackgroundView.frame;
      newBackgroundViewFrame.origin.y -= diff - 0.5f;
      newBackgroundViewFrame.size.height += diff;
      titleBackgroundView.frame = newBackgroundViewFrame;
      
      CGRect newLabelFrame = titleLabel.frame;
      newLabelFrame.size.height += diff;
      titleLabel.frame = newLabelFrame;
    }
  
  _categoryTitle = title;
  titleLabel.text = _categoryTitle;
}

- (void)dealloc
{
    titleLabel = nil;
    titleBackgroundView = nil;
    _categoryTitle = nil;
}

@end
