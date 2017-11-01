// IBAHeader

#import <Foundation/Foundation.h>
#import <UIKit/UIKit.h>
#import "viewbase.h"
#import "TPKeyboardAvoidingScrollView.h"

@class mBANewAppFormPresenter;

@interface mBANewAppFormView : ViewBase<UIGestureRecognizerDelegate, UITextFieldDelegate>

@property (nonatomic, strong) TPKeyboardAvoidingScrollView *scrollView;
@property (nonatomic, strong) UILabel *categoryNameLabel;
@property (nonatomic, retain) NSArray *categories;

- (void)viewForPresenter:(mBANewAppFormPresenter *)presenter;

@end