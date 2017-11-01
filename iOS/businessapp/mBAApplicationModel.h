
#import <Foundation/Foundation.h>
#import <UIKit/UIKit.h>

#define kDefaultPlaceholderColor [@"#CCCCCC" asColor]

/**
* Application data model
*/
@interface mBAApplicationModel : NSObject

/**
* Application ID
*/
@property NSInteger app_id;

/**
* Category ID
*/
@property NSInteger category_id;

/**
* Security Token Application
*/
@property (nonatomic, copy) NSString *token;

/**
* Application title
*/
@property (nonatomic, copy) NSString *title;

/**
* URL application splashscreen images
*/
@property (nonatomic, copy) NSString *pictureUrl;


/**
 * URL application splashscreen images
 */
@property (nonatomic, copy) UIColor *placeholderColor;

/**
 * URL application splashscreen images
 */
@property (nonatomic, copy) NSString *placeholderColorString;

/**
* The constructor fills the fields with zero default values
*/
- init;

@end
