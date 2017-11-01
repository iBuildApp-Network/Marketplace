// IBAHeader

#import "mBACategoryTableService.h"
#import "mBACategoryModel.h"

static sqlite3 *database = nil;
static sqlite3_stmt *statement = nil;

const char *qCategoryListQuery =
        "Select id, title, picture_url, picture_path, order_id, enable, sorted_apps_list\
         From Category\
         ORDER BY order_id;";

NSString *qSetCategoryListQuery =
        @"\
       Insert Into Category(id, title,\
       picture_url, picture_path, order_id, enable, sorted_apps_list)\
       Values %@;";

NSString *qDeleteCategoryListQuery = @"delete from Category;";

@implementation mBACategoryTableService {
}

- (NSArray *)categoryList
{
    NSMutableArray *result = [[NSMutableArray alloc] init];

    NSFileManager *filemgr = [NSFileManager defaultManager];
    NSString *databasePath = [mBATableService databasePath];
    if ([filemgr fileExistsAtPath: databasePath ] == NO)
    {
        return result;
    }

    const char *dbpath = [databasePath UTF8String];
    if (sqlite3_open(dbpath, &database) == SQLITE_OK)
    {
        const char *query_stmt = qCategoryListQuery;
        int sqlResult = sqlite3_prepare_v2(database, query_stmt, -1, &statement, NULL);
        if (sqlResult == SQLITE_OK) {
            while (sqlite3_step(statement) == SQLITE_ROW) {
                mBACategoryModel *categoryModel   = [[mBACategoryModel alloc] init];
                categoryModel.identifier       = sqlite3_column_int(statement, 0);
                categoryModel.title            = [[NSString alloc] initWithUTF8String:(const char *) sqlite3_column_text(statement, 1)];

                const char *picture_url = (const char *)sqlite3_column_text(statement, 2);
                categoryModel.pictureUrl      = picture_url  != NULL ? [[NSString alloc] initWithUTF8String: picture_url] : nil;
                categoryModel.order            = sqlite3_column_int(statement, 4);
                categoryModel.enable           = sqlite3_column_int(statement, 5) == 1;
                [result addObject: categoryModel];
            }
            sqlite3_reset(statement);
        }
      sqlite3_finalize(statement);
    }
  sqlite3_close(database);
    return result;
}

- (BOOL)deleteCategoryList
{
    Boolean result = YES;
    NSString *databasePath = [mBATableService databasePath];
    const char *dbpath = [databasePath UTF8String];
    if (sqlite3_open(dbpath, &database) == SQLITE_OK)
    {
        const char *insert_stmt = [qDeleteCategoryListQuery UTF8String];
        sqlite3_prepare_v2(database, insert_stmt, -1, &statement, NULL);
        int sqlResult = sqlite3_step(statement);
        if (sqlResult == SQLITE_DONE) {
            NSLog(@"Category deleted");
        } else {
            NSLog(@"\"No category deleted");
            result = NO;
        }
        sqlite3_reset(statement);
    }

    return result;
}

- (void)setCategoryList:(NSArray*)categoryList {
  
    if (categoryList)
      [self deleteCategoryList];

    NSString *listValues = @"";
    for (int i = 0; i < categoryList.count; i++) {
        mBACategoryModel *category = categoryList[i];
        NSString *categoryLine = [NSString stringWithFormat:
                @"( %d, '%@', '%@', '%@', %d, %d, '%@')",
                category.identifier,
                category.title,
                category.pictureUrl, @"",
                category.order, category.enable ? 1 : 0, @""]; // sorted apps list!

        listValues = [listValues stringByAppendingString:categoryLine];
        if (i < categoryList.count - 1) {
            listValues = [listValues stringByAppendingString:@", "];
        }
    }
    NSString *setCategoriesQuery = [NSString stringWithFormat: qSetCategoryListQuery, listValues];
#ifdef MASTERAPP_DEBUG
    NSLog(@"%@", setCategoriesQuery);
#endif
    NSString *databasePath = [mBATableService databasePath];
    const char *dbpath = [databasePath UTF8String];
    if (sqlite3_open(dbpath, &database) == SQLITE_OK)
    {
        NSString *insertSQL = setCategoriesQuery;
        const char *insert_stmt = [insertSQL UTF8String];
        sqlite3_prepare_v2(database, insert_stmt, -1, &statement, NULL);
        int result = sqlite3_step(statement);
        if (result == SQLITE_OK) {
            NSLog(@"Category created");
        } else {
            NSLog(@"No category created");
        }
        sqlite3_reset(statement);
    }
}

@end
