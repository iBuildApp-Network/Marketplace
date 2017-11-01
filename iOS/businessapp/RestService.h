// IBAHeader

#import <Foundation/Foundation.h>
#import "RestServiceDelegate.h"

typedef enum
{rateUp = 1,
rateDown = 0} FavouritedStatus;

@interface RestService : NSObject<NSURLConnectionDelegate>

@property (nonatomic, assign) id<RestServiceDelegate> delegate;
@property (nonatomic, retain) NSArray *categories;
@property (nonatomic, retain) NSArray *categoryTemplates;

- (void)fetchCategories;
- (void)fetchFeaturedApplications;
- (void)fetchApplicationIDs:(NSInteger)categoryId;
- (void)fetchApplicationIDs:(NSInteger)categoryId searchQuery:(NSString *)searchQuery;
- (void)fetchApplicationData:(NSArray *)appId;

- (void)fetchApplicationIDsBySearchQuery:(NSString *)searchQuery;

- (void)performRatingChangeForAppWithId:(NSInteger)appId
                                   uuid:(NSString*)uuid
                                   andStatus:(FavouritedStatus) status;
- (void)fetchCategoryTemplates;

@end

