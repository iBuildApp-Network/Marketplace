// IBAHeader

#import "mBADBService.h"
#import "mBAApplicationTableService.h"
#import "mBACategoryTableService.h"
#import "buisinessapp.h"

static sqlite3 *database = nil;
static sqlite3_stmt *statement = nil;
static NSString *databasePath;

const char *qCreateCategoryTable =
        "Create table Category(\
        id integer primary key not null unique,\
        title text not null default '',\
        picture_url text, picture_path text,\
        order_id integer, enable integer,\
        sorted_apps_list text not null default '');";

const char *qCreateApplicationTable =
        "create table if not exists Application (\
        app_id integer primary key not null unique,\
        category_id integer not null,\
        token text, title text,\
        picture_url text, picture_path text);";

const char *qCreateFavouritesTable =
        "create table if not exists Favourites (\
        app_id integer primary key not null unique,\
        category_id integer not null,\
        token text, title text,\
        picture_url text, picture_path text, favourited integer default -1, active integer default -1);";

const char *qCreateFeaturedTable =
        "create table if not exists Featured (\
        app_id integer primary key not null unique,\
        category_id integer not null,\
        token text, title text,\
        picture_url text, picture_path text);";

@implementation mBADBService

#define kApplicationTableName @"Application"
#define kFeaturedTableName    @"Featured"
#define kFavouritesTableName  @"Favourites"

- (BOOL)execSQL:(const char *)sqlQuery failedMessage:(NSString *)failedMessage
{
    BOOL isSuccess = true;

    char *errMsg;
    int sqlresult = sqlite3_exec(database, sqlQuery, NULL, NULL, &errMsg);
    if (sqlresult != SQLITE_OK)
    {
        isSuccess = NO;
        NSLog(@"%s", sqlQuery);
        NSLog(@"%s", errMsg);
        NSLog(@"%@", failedMessage);
    }

    return isSuccess;
}

- (BOOL)createCategoryTable
{
    return [self execSQL: qCreateCategoryTable failedMessage: @"Failed to create category table"];
}

- (BOOL)createApplicationTable
{
    return [self execSQL: qCreateApplicationTable failedMessage: @"Failed to create application table"];
}

- (BOOL)createFeaturedTable
{
    return [self execSQL: qCreateFeaturedTable failedMessage: @"Failed to create featured table"];
}

- (BOOL)createFavouritesTable
{
    return [self execSQL: qCreateFavouritesTable failedMessage: @"Failed to create favourites table"];
}

- (BOOL)createApplicationsTables
{
    return [self createApplicationTable] && [self createFeaturedTable] && [self createFavouritesTable];
}

- (BOOL)createDB
{
  NSString *docsDir;
  NSArray *dirPaths;
  dirPaths = NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES);
  docsDir = dirPaths[0];
  databasePath = [[NSString alloc] initWithString:
                  [docsDir stringByAppendingPathComponent: @"ibaMasterApp.db"]];
  [mBATableService setDatabasePath:databasePath];

  BOOL isSuccess = YES;
  NSError *error;
  
  NSFileManager *filemgr = [NSFileManager defaultManager];
  
  
  if (![filemgr fileExistsAtPath:databasePath]) {
    NSString *defaultDBPath = [[[NSBundle mainBundle] resourcePath] stringByAppendingPathComponent:@"masterapp_default_db.db"];
    
    if ([filemgr fileExistsAtPath:defaultDBPath])
    {
      isSuccess = [filemgr copyItemAtPath:defaultDBPath toPath:databasePath error:&error];
    }
    else
    {
      isSuccess = NO;
    }
    
    if (!isSuccess)
    {

      NSLog(@"create new DB");
      
      const char *dbpath = [databasePath UTF8String];
      if (sqlite3_open(dbpath, &database) == SQLITE_OK)
      {
        if (!([self createCategoryTable] &&
              [self createApplicationsTables]))
        {
          isSuccess = NO;
          NSLog(@"Failed to create tables");
        }
        
        else
        {
          isSuccess = YES;
        }
        sqlite3_close(database);
      }
      else
      {
        isSuccess = NO;
        NSLog(@"Failed to open/create database");
      }
      return isSuccess;
    }
  }
  
  return isSuccess;
}

@end
