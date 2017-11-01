

#import "mBAApplicationTableService.h"
#import "mBAApplicationModel.h"
#import "mBASettings.h"
#import "NSString+colorizer.h"

static sqlite3 *database = nil;
static sqlite3_stmt *statement = nil;

const char *qFeaturedListQuery =
        "Select app_id, category_id,            \
        token, title, picture_url, picture_path \
         From Featured;";

const char *qDeleteFeaturedListQuery = "delete from Featured;";

NSString *qSetFeaturedListQuery =
        @"Insert Into Featured(app_id, category_id, \
token, title, picture_url, picture_path)            \
Values %@;";

NSString *qApplicationCachedList = @"Select app_id From Application where category_id == %d";
NSString *qApplicationList = @"SELECT sorted_apps_list FROM Category \
                              WHERE id = %d;";

NSString *qInsertApps = @"insert into Application(app_id, category_id) values %@;";
NSString *qDeleteApp = @"delete from Application where app_id = %@;";

NSString *qApplicationData = @"SELECT app_id, category_id,             \
                               token, title, picture_url, picture_path \
                               FROM %@                        \
                               WHERE app_id = %d;";

NSString *qUpdateApplicationData = @"insert or replace into Application( \
                                     app_id, category_id, token, title,   \
                                     picture_url, picture_path)         \
                                     values %@;";

NSString *qUpdateCategoryAppIDs = @"UPDATE Category \
SET sorted_apps_list = '%@'   \
WHERE id = %d;";


NSString *qAppIdInFavourites = @"select app_id From Favourites where app_id = %d;";
NSString *qFavouritesIds = @"select app_id From Favourites where active != 0 AND favourited != 0 order by app_id desc;";
NSString *qAddToFavourtes = @"insert or replace into Favourites(app_id, category_id) values (%d, 0);";
NSString *qRemoveFromFavourtes = @"delete from Favourites where app_id = %d;";

NSString *qChangeActiveState = @"update Favourites set active = %d where app_id = %d;";
NSString *qChangeFavouritedState = @"update Favourites set favourited = %d where app_id = %d;";

NSString *qAppFavouritesStatus = @"Select favourited, active \
                               From Favourites           \
                               where app_id = %d;";

NSString *qAppPending = @"Select app_id, favourited, active \
                          From Favourites where (active = %d and favourited = %d) OR \
                          (active = %d and favourited = %d);";

NSString *qResetActiveAndFavouritedStates = @"update Favourites set active = %d, favourited = %d where app_id = %d;";

@implementation mBAApplicationTableService {
}

- (BOOL)deleteFeaturedList
{
  NSString *databasePath = [mBATableService databasePath];

  Boolean result = YES;
  const char *dbpath = [databasePath UTF8String];
  if (sqlite3_open(dbpath, &database) == SQLITE_OK)
  {
    const char *delete_stmt = qDeleteFeaturedListQuery ;
    sqlite3_prepare_v2(database, delete_stmt, -1, &statement, NULL);
    int sqlResult = sqlite3_step(statement);
    if (sqlResult == SQLITE_DONE) {
      NSLog(@"Clear Features Table");
    } else {
      NSLog(@"\"Failed to clear features table");
      result = NO;
    }
    sqlite3_reset(statement);
  }

  return result;
}

- (void)setFeaturedList:(NSArray*)featuredList
{
  [self deleteFeaturedList];

  NSString* listValues = @"";
  for (int i = 0; i < featuredList.count; i++) {
    mBAApplicationModel *application = featuredList[i];

      NSString *applicationLine = [NSString stringWithFormat: @"(%ld, %ld, '%@', '%@', '%@', '%@')",
                                   (long)application.app_id, (long)application.category_id,
      application.token, application.title,
      application.pictureUrl, @""];

    listValues = [listValues stringByAppendingString: applicationLine];
    if (i < featuredList.count - 1) {
      listValues = [listValues stringByAppendingString: @", "];
    }
  }

  NSString *setFeaturedQuery = [NSString stringWithFormat: qSetFeaturedListQuery, listValues];
  NSString *databasePath = [mBATableService databasePath];
  const char *dbpath = [databasePath UTF8String];
  if (sqlite3_open(dbpath, &database) == SQLITE_OK)
  {
    const char *insert_stmt = [setFeaturedQuery UTF8String];
    sqlite3_prepare_v2(database, insert_stmt, -1, &statement, NULL);
    int result = sqlite3_step(statement);
    if (result == SQLITE_DONE) {
      NSLog(@"Category created");
    } else {
      NSLog(@"No category created");
    }
    sqlite3_reset(statement);
  }
}

- (NSArray *)featuredList
{
  NSMutableArray *result = [[NSMutableArray alloc] init];
  
  NSFileManager *filemgr = [NSFileManager defaultManager];
  NSString *databasePath = [mBATableService databasePath];
  if ([filemgr fileExistsAtPath: databasePath ] == NO)
    return result;

  const char *dbpath = [databasePath UTF8String];
  if (sqlite3_open(dbpath, &database) == SQLITE_OK) {
    const char *query_stmt = qFeaturedListQuery;
    int sqlResult = sqlite3_prepare_v2(database, query_stmt, -1, &statement, NULL);
    if (sqlResult == SQLITE_OK) {
      while (sqlite3_step(statement) == SQLITE_ROW) {
        mBAApplicationModel *applicationModel = [[mBAApplicationModel alloc] init];
        applicationModel.app_id = sqlite3_column_int(statement, 0);
        applicationModel.category_id = sqlite3_column_int(statement, 1);

        const char *stringField = (const char *) sqlite3_column_text(statement, 2);
        if (stringField != nil)
          applicationModel.token = [NSString stringWithUTF8String:stringField];
        
        stringField = (const char *) sqlite3_column_text(statement, 3);
        if (stringField != nil)
          applicationModel.title = [NSString stringWithUTF8String:stringField];
        stringField = (const char *) sqlite3_column_text(statement, 4);
        if (stringField != nil)
          applicationModel.pictureUrl = [NSString stringWithUTF8String:stringField];
        
        [result addObject:applicationModel];
      }
      sqlite3_reset(statement);
    }
  }
  return result;
}

- (NSArray*)getApplicationCachedIds:(NSInteger)categoryId
{
  NSMutableArray *result = [[NSMutableArray alloc] init];

  NSFileManager *filemgr = [NSFileManager defaultManager];
  NSString *databasePath = [mBATableService databasePath];
  if ([filemgr fileExistsAtPath: databasePath ] == NO)
    return result;

  const char *dbpath = [databasePath UTF8String];
  if (sqlite3_open(dbpath, &database) == SQLITE_OK) {
    NSString *query = [NSString stringWithFormat: qApplicationCachedList, categoryId];
    const char *query_stmt = [query UTF8String];
    int sqlResult = sqlite3_prepare_v2(database, query_stmt, -1, &statement, NULL);
    if (sqlResult == SQLITE_OK) {
      while (sqlite3_step(statement) == SQLITE_ROW) {
        NSInteger applicationId = sqlite3_column_int(statement, 0);
        [result addObject: @(applicationId)];
      }
      sqlite3_reset(statement);
    }
  }
  return result;
}

- (NSArray*)getApplicationIds:(NSInteger)categoryId
{
  NSString *appIDsStr = nil;
  
  NSFileManager *filemgr = [NSFileManager defaultManager];
  NSString *databasePath = [mBATableService databasePath];
  if ([filemgr fileExistsAtPath: databasePath ] == NO)
    return nil;
  
  const char *dbpath = [databasePath UTF8String];
  if (sqlite3_open(dbpath, &database) == SQLITE_OK) {
    NSString *query = [NSString stringWithFormat: qApplicationList, categoryId];
    const char *query_stmt = [query UTF8String];
    int sqlResult = sqlite3_prepare_v2(database, query_stmt, -1, &statement, NULL);
    if (sqlResult == SQLITE_OK) {
      while (sqlite3_step(statement) == SQLITE_ROW) {
        
        const char *stringField = (const char *) sqlite3_column_text(statement, 0);
        if (stringField != nil)
          appIDsStr = [NSString stringWithUTF8String:stringField];
      }
      sqlite3_reset(statement);
      
      if (appIDsStr && appIDsStr.length)
        return [appIDsStr componentsSeparatedByString:@" "];
      else
        return [NSArray array];
    }
  }
  
  return [NSArray array];
}

- (void)addAppWithIds:(NSArray *)ids categoryId:(NSInteger)categoryId
{
  NSString* listValues = @"";
  for (int i = 0; i < ids.count; i++) {
    NSInteger applicationId = [ids[i] integerValue];

      NSString *applicationLine = [NSString stringWithFormat: @"(%ld, %ld)", (long)applicationId, (long)categoryId, nil];
    listValues = [listValues stringByAppendingString: applicationLine];
    if (i < ids.count - 1) {
      listValues = [listValues stringByAppendingString: @", "];
    }
  }

  NSString *setFeaturedQuery = [NSString stringWithFormat: qInsertApps, listValues];
  NSString *databasePath = [mBATableService databasePath];
  const char *dbpath = [databasePath UTF8String];
  if (sqlite3_open(dbpath, &database) == SQLITE_OK)
  {
    const char *insert_stmt = [setFeaturedQuery UTF8String];
    sqlite3_prepare_v2(database, insert_stmt, -1, &statement, NULL);
    int result = sqlite3_step(statement);
    if (result == SQLITE_DONE) {
      NSLog(@"Category created");
    } else {
      NSLog(@"No category created");
    }
  sqlite3_reset(statement);
  }
}

- (void)removeAppsWithIds:(NSArray *)ids
{
  NSString *databasePath = [mBATableService databasePath];

  const char *dbpath = [databasePath UTF8String];
  if (sqlite3_open(dbpath, &database) == SQLITE_OK)
  {
    for(int i = 0; i < ids.count; i++) {
      NSString *deleteAppQuery = [NSString stringWithFormat: qDeleteApp, ids[i]];
      const char *delete_stmt = [deleteAppQuery cStringUsingEncoding:NSASCIIStringEncoding];
      sqlite3_prepare_v2(database, delete_stmt, -1, &statement, NULL);
      int sqlResult = sqlite3_step(statement);
      if (sqlResult == SQLITE_DONE) {
        NSLog(@"Clear Features Table");
      } else {
        NSLog(@"\"Failed to remove app wth id");
      }
      sqlite3_reset(statement);
    }
  }
}

- (void)updateApplicationIds:(NSArray *)newIds forCategoryId:(NSInteger)categoryId
{

}

- (mBAApplicationModel *)getApplicationData:(NSInteger)app_id
{
  mBAApplicationModel *result = [self getApplicationData:app_id fromTable:@"Application"];
  if (!result)
    result = [self getApplicationData:app_id fromTable:@"Featured"];

  return result;
}


- (mBAApplicationModel *)getApplicationData:(NSInteger)app_id fromTable:(NSString*)tableName;
{
  mBAApplicationModel *result = nil;
  
  NSFileManager *filemgr = [NSFileManager defaultManager];
  NSString *databasePath = [mBATableService databasePath];
  if ([filemgr fileExistsAtPath: databasePath ] == NO)
    return result;
  
  const char *dbpath = [databasePath UTF8String];
  if (sqlite3_open(dbpath, &database) == SQLITE_OK) {
    NSLog(@"select data with placeholder");
    NSString *query = [NSString stringWithFormat: qApplicationData, tableName, app_id];
    const char *query_stmt = [query UTF8String];
    int sqlResult = sqlite3_prepare_v2(database, query_stmt, -1, &statement, NULL);
    NSLog(@"sqlite result %d", sqlResult);
    if (sqlResult == SQLITE_OK) {
      while (sqlite3_step(statement) == SQLITE_ROW) {
        mBAApplicationModel *applicationModel   = [[mBAApplicationModel alloc] init];
        applicationModel.app_id              = sqlite3_column_int(statement, 0);
        applicationModel.category_id         = sqlite3_column_int(statement, 1);
        
        const char *stringField = (const char *)sqlite3_column_text(statement, 2);
        if (stringField != nil)
          applicationModel.token           = [NSString stringWithUTF8String: stringField];
        stringField = (const char *)sqlite3_column_text(statement, 3);
        if (stringField != nil)
          applicationModel.title           = [NSString stringWithUTF8String: stringField];
        stringField = (const char *)sqlite3_column_text(statement, 4);
        if (stringField != nil)
          applicationModel.pictureUrl     = [NSString stringWithUTF8String: stringField];
        stringField = (const char *)sqlite3_column_text(statement, 5);
        if (stringField != nil)
          applicationModel.placeholderColorString = [NSString stringWithUTF8String: stringField];
        NSLog(@"select placeholder color %@", applicationModel.placeholderColorString);
        result = applicationModel;
        break;
      }
      sqlite3_reset(statement);
    }
  sqlite3_finalize(statement);
  }
  sqlite3_close(database);
  return result;
}

- (void)updateApplicationData:(NSArray *)data
{
    NSString* listValues = @"";
    for (int i = 0; i < data.count; i++) {
        mBAApplicationModel *application = data[i];
        NSLog(@"update data with placeholder %@", (NSString *) application.placeholderColorString);
        NSString *applicationLine = [NSString stringWithFormat: @"(%ld, %ld, '%@', '%@', '%@', '%@')",
                                     (long)application.app_id, (long)application.category_id,
                                                                application.token, application.title,
                                                                application.pictureUrl, application.placeholderColorString
                                                                ];

        listValues = [listValues stringByAppendingString: applicationLine];
        if (i < data.count - 1) {
            listValues = [listValues stringByAppendingString: @", "];
        }
    }

    NSString *setFeaturedQuery = [NSString stringWithFormat: qUpdateApplicationData, listValues];
    NSString *databasePath = [mBATableService databasePath];
    const char *dbpath = [databasePath UTF8String];
    if (sqlite3_open(dbpath, &database) == SQLITE_OK)
    {
        const char *insert_stmt = [setFeaturedQuery UTF8String];
        sqlite3_prepare_v2(database, insert_stmt, -1, &statement, NULL);
        char *err;
        int result = sqlite3_exec(database, insert_stmt, NULL, NULL, &err);
        if (result == SQLITE_DONE) {
            NSLog(@"Application data updated");
        } else {
            NSLog(@"No applicaton data updated");
        }
        sqlite3_reset(statement);
        sqlite3_finalize(statement);
    }
    sqlite3_close(database);
}

- (void)updateSortedAppIDs:(NSString *)appIDs forCategoryId:(NSInteger)categoryId
{
  NSString *setFeaturedQuery = [NSString stringWithFormat: qUpdateCategoryAppIDs, appIDs, categoryId];
  NSString *databasePath = [mBATableService databasePath];
  const char *dbpath = [databasePath UTF8String];
  if (sqlite3_open(dbpath, &database) == SQLITE_OK)
  {
    const char *insert_stmt = [setFeaturedQuery UTF8String];
    char *err;
    int result = sqlite3_exec(database, insert_stmt, NULL, NULL, &err);
    if (result == SQLITE_DONE) {
      NSLog(@"Category was updated with sorted app ids");
    } else {
      NSLog(@"No category data updated");
    }
  }
}

- (void)addToFavourites:(NSInteger)app_id
{
  NSString *addToFavouritesQuery = [NSString stringWithFormat: qAddToFavourtes, app_id];
  NSString *databasePath = [mBATableService databasePath];
  const char *dbpath = [databasePath UTF8String];
  if (sqlite3_open(dbpath, &database) == SQLITE_OK)
  {
    const char *insert_stmt = [addToFavouritesQuery UTF8String];
    char *err;
    int result = sqlite3_exec(database, insert_stmt, NULL, NULL, &err);
    if (result == SQLITE_OK) {
      NSLog(@"Application data updated");
    } else {
      NSLog(@"No applicaton data updated");
    }
    sqlite3_reset(statement);
  }
}

- (void)removeFromFavourites:(NSInteger)app_id
{
  NSString *databasePath = [mBATableService databasePath];

  const char *dbpath = [databasePath UTF8String];
  if (sqlite3_open(dbpath, &database) == SQLITE_OK)
  {
    NSString *deleteAppQuery = [NSString stringWithFormat: qRemoveFromFavourtes, app_id];
    const char *delete_stmt = [deleteAppQuery UTF8String];
    char *err;
    int sqlResult = sqlite3_exec(database, delete_stmt, NULL, NULL, &err);
    if (sqlResult == SQLITE_OK) {
      NSLog(@"Remove from favourtes table");
    } else {
      NSLog(@"Failed remove from favourites table");
    }
    sqlite3_reset(statement);
  }
}

- (NSArray *)getFavouritesIds
{
  NSMutableArray *result = [[NSMutableArray alloc] init];

  NSFileManager *filemgr = [NSFileManager defaultManager];
  NSString *databasePath = [mBATableService databasePath];
  if ([filemgr fileExistsAtPath: databasePath ] == NO)
    return result;

  const char *dbpath = [databasePath UTF8String];
  if (sqlite3_open(dbpath, &database) == SQLITE_OK) {
    const char *query_stmt = [qFavouritesIds UTF8String];
    int sqlResult = sqlite3_prepare_v2(database, query_stmt, -1, &statement, NULL);
    if (sqlResult == SQLITE_OK) {
      while (sqlite3_step(statement) == SQLITE_ROW) {
        NSInteger applicationId = sqlite3_column_int(statement, 0);
        [result addObject: @(applicationId)];
      }
      sqlite3_reset(statement);
    }
  }
  return result;
}

-(Boolean)appInFavouritesTable:(NSInteger)app_id{
  NSMutableArray *result = [[NSMutableArray alloc] init];
  
  NSFileManager *filemgr = [NSFileManager defaultManager];
  NSString *databasePath = [mBATableService databasePath];
  if ([filemgr fileExistsAtPath: databasePath ] == NO)
  {
    return NO;
  }
  
  NSString *query = [NSString stringWithFormat: qAppIdInFavourites, app_id];
  const char *dbpath = [databasePath UTF8String];
  if (sqlite3_open(dbpath, &database) == SQLITE_OK) {
    const char *query_stmt = [query UTF8String];
    int sqlResult = sqlite3_prepare_v2(database, query_stmt, -1, &statement, NULL);
    if (sqlResult == SQLITE_OK) {
      while (sqlite3_step(statement) == SQLITE_ROW) {
        NSInteger applicationId = sqlite3_column_int(statement, 0);
        [result addObject: @(applicationId)];
      }
      sqlite3_reset(statement);
    }
  }
  Boolean appInFav = result.count > 0;
  return appInFav;
}

-(Boolean)appIsPendingDerate:(NSInteger)app_id{
  Boolean result = false;
  
  NSFileManager *filemgr = [NSFileManager defaultManager];
  NSString *databasePath = [mBATableService databasePath];
  
  if ([filemgr fileExistsAtPath: databasePath ] == NO){
    return result;
  }
  
  const char *dbpath = [databasePath UTF8String];
  if (sqlite3_open(dbpath, &database) == SQLITE_OK) {
    NSString *query = [NSString stringWithFormat: qAppFavouritesStatus, app_id];
    const char *query_stmt = [query UTF8String];
    int sqlResult = sqlite3_prepare_v2(database, query_stmt, -1, &statement, NULL);
    if (sqlResult == SQLITE_OK) {
      while (sqlite3_step(statement) == SQLITE_ROW) {
        if(sqlite3_column_int(statement, 0) == FAV_STATE_UNFAVED &&
           sqlite3_column_int(statement, 1) == FAV_STATE_INACTIVE){
          
          result = true;
        }
      }
      sqlite3_reset(statement);
    }
  }
  return result;
}


- (Boolean)appInFavourites:(NSInteger)app_id{
  if([self appIsPendingDerate:app_id]){
    return false;
  }
  return [self appInFavouritesTable:app_id];
}

- (NSArray *)getFavouritesIdsLike:(NSString *)query
{
  NSMutableArray *result = [[NSMutableArray alloc] init];
  NSArray *favIds = [self getFavouritesIds];

  query =[query stringByTrimmingCharactersInSet: [NSCharacterSet whitespaceCharacterSet]];

  for(int i = 0; i < favIds.count; i++)
  {
    mBAApplicationModel *favApp = [self getApplicationData:[favIds[i] integerValue]];
    if ([[favApp.title lowercaseString] rangeOfString: [query lowercaseString]].location != NSNotFound)
    {
      [result addObject: favIds[i]];
    }
  }

  return result;
}

- (void)fillSettingsForAppId:(NSInteger)appId
{
  mBAApplicationModel *app = [self getApplicationData:appId];
  
  if (app)
  {
    mBASettings *settings = [mBASettings sharedInstance];
    settings.currentAppId = [@(appId) stringValue];
    settings.currentAppToken = [app.token copy];
    NSLog(@"settings.currentAppToken :%@", settings.currentAppToken);
  }
  else
  {
      NSLog(@"Can not get data for app with id = %ld", (long)appId);
  }
  
  
}

- (void)setActiveState:(AppActiveState)state forAppWithId:(NSInteger)appId{
  
  NSInteger newState = -1;
  
  switch(state){
    case ACTIVE:
      newState = 1;
      break;
    case INACTIVE:
      newState = 0;
      break;
    case ACTIVE_DEFAULT:
      break;
  }
  
  NSString *query = [NSString stringWithFormat:qChangeActiveState, newState, appId];
  [self executeUpdateQuery:query];
}

- (void)setFavouritedState:(AppFavouritedState)state forAppWithId:(NSInteger)appId{
  
  NSInteger newState = -1;
  
  switch(state){
    case FAVOURITED:
      newState = 1;
      break;
    case UNFAVOURITED:
      newState = 0;
      break;
    case FAVOURITE_DEFAULT:
      break;
  }
  
  NSString *query = [NSString stringWithFormat:qChangeFavouritedState, newState, appId];
  [self executeUpdateQuery:query];
}

-(void) executeUpdateQuery:(NSString*)query{
  NSString *databasePath = [mBATableService databasePath];
  const char *dbpath = [databasePath UTF8String];
  sqlite3_stmt *updateStmt = nil;
  if (sqlite3_open(dbpath, &database) == SQLITE_OK)
  {
    const char *update_sql_text = [query UTF8String];
    
    int preparingResult = sqlite3_prepare_v2(database, update_sql_text, -1, &updateStmt, NULL);
    if(preparingResult != SQLITE_OK)
      NSLog(@"Error while creating update statement. %s", sqlite3_errmsg(database));
    
    if (sqlite3_step(updateStmt) != SQLITE_DONE)
      NSLog(@"Error during step. %s", sqlite3_errmsg(database));
    
    sqlite3_finalize(updateStmt);
    sqlite3_close(database);
  }else{
    NSLog(@"Error while opening database");
  }
}

-(NSDictionary *)getPendingApplications{
  NSMutableDictionary *result = [[NSMutableDictionary alloc] init];
  
  NSString *pendingApplicationsQuery = [NSString stringWithFormat:qAppPending,
                                        FAV_STATE_INACTIVE, FAV_STATE_UNFAVED,
                                        FAV_STATE_ACTIVE, FAV_STATE_FAVED];
  
  NSFileManager *filemgr = [NSFileManager defaultManager];
  NSString *databasePath = [mBATableService databasePath];
  if ([filemgr fileExistsAtPath: databasePath ] == NO){
    return result;
  }
  
  const char *dbpath = [databasePath UTF8String];
  if (sqlite3_open(dbpath, &database) == SQLITE_OK) {
    const char *query_stmt = [pendingApplicationsQuery UTF8String];
    int sqlResult = sqlite3_prepare_v2(database, query_stmt, -1, &statement, NULL);
    if (sqlResult == SQLITE_OK) {
      while (sqlite3_step(statement) == SQLITE_ROW) {
        NSInteger applicationId = sqlite3_column_int(statement, 0);
        
        NSInteger favourited = sqlite3_column_int(statement, 1);
        NSInteger active = sqlite3_column_int(statement, 2);
        
        if(favourited == FAV_STATE_UNFAVED && active == FAV_STATE_INACTIVE){
          [result setObject:PENDING_DERATE_DECISION forKey:[NSNumber numberWithInteger:applicationId]];
        } else {
          [result setObject:PENDING_RATE_DECISION forKey:[NSNumber numberWithInteger:applicationId]];
        }
      }
      sqlite3_reset(statement);
    }
  }
  return result;
}

@end
