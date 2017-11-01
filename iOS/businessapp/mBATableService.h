// IBAHeader

#import <Foundation/Foundation.h>
#import <sqlite3.h>

/**
* Database for the services of working with tables
*/
@interface mBATableService : NSObject

/**
* Sets the database path common to all table services
*/
+ (void)setDatabasePath:(NSString *)path;

/**
* Gets the path to the database, common to all table services
*/
+ (NSString *)databasePath;

@end
