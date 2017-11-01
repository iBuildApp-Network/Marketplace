// IBAHeader

#import <Foundation/Foundation.h>

@protocol RestServiceDelegate
@optional
- (void)CategoryDataReceiver:(NSArray *)categories;
- (void)FeaturedApplications:(NSArray *)featured;
- (void)appIdsLoaded:(NSArray *)applicationIDs;
- (void)appIdsLoaded:(NSArray *)applicationIDs withStrRepresentation:(NSString *)appIDsStr;
- (void)appDataLoaded:(NSArray *)applicationData;
- (void)cancelRestOperation;

- (void) categoryTemplatesDidFinishLoading:(NSArray*)templates;
- (void) categoryTemplatesDidFailLoading;
@end
