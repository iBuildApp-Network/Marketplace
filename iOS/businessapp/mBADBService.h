// IBAHeader

#import <Foundation/Foundation.h>


/**
* Service for working with the database
*/
@interface mBADBService : NSObject

/**
 * Creates a database if it was not created
 * @return YES - was created, NO - was not created
*/
- (BOOL)createDB;

@end
