// IBAHeader

#import <Foundation/Foundation.h>
#import <UIKit/UIKit.h>
#import "RestServiceDelegate.h"



/**
* Screen controller for creating a new application
*/
@interface mBANewAppFormPresenter : UIViewController<RestServiceDelegate>

//Numbering starts from 1
@property (nonatomic, strong, retain) NSString *selectedCategoryId;

/**
* When you press the back button, go to the main screen
*/
- (void)back;

- (void)showCategoriesPicker;

- (void)setCategoryNameLabelText:(NSString *)text;

@end
