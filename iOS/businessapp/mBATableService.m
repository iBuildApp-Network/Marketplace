// IBAHeader

#import "mBATableService.h"

static NSString *_databasePath;

@implementation mBATableService

+ (void)setDatabasePath:(NSString *)path
{
    _databasePath = path;
}

+ (NSString *)databasePath
{
    return _databasePath;
}

@end