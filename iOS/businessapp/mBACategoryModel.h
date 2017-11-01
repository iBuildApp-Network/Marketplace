// IBAHeader

#import <Foundation/Foundation.h>

/**
* Category data model
*/
@interface mBACategoryModel : NSObject

/**
* Category ID
*/
@property NSInteger identifier;

/**
* Category Title
*/
@property (nonatomic, copy) NSString *title;

/**
* Order of the category in the list
*/
@property NSInteger order;

/**
 * The category is switched on, or disabled. can I click on it on the main application screen
 * Not currently used
*/
@property NSInteger enable;

/**
* App home screen image URL
*/
@property (nonatomic, copy) NSString *pictureUrl;

/**
* The constructor fills the fields with zero default values
*/
- init;

@end
