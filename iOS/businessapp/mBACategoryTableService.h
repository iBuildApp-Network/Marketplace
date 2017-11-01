// IBAHeader

#import <Foundation/Foundation.h>
#import "mBATableService.h"

/**
* The service for storing the list of theories in permanent memory
*/
@interface mBACategoryTableService : mBATableService

/**
 * Returns an ordered list of categories
 * @return Ordered categories
*/
- (NSArray *)categoryList;

/**
 * Sets an ordered list of categories
 * @return Ordered categories
*/
- (void) setCategoryList:(NSArray*)categoryList;

@end
