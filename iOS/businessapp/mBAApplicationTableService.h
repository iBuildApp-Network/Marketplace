// IBAHeader

#import <Foundation/Foundation.h>
#import "mBATableService.h"
#import "mBAApplicationModel.h"


#define FAV_AND_ACTIVE_DEAFULT_STATE -1

#define FAV_STATE_ACTIVE 1
#define FAV_STATE_INACTIVE 0

#define FAV_STATE_FAVED 1
#define FAV_STATE_UNFAVED 0

#define PENDING_DERATE_DECISION  @"derate"
#define PENDING_RATE_DECISION @"rate"

//States for the case when we had favourited or unfavourited an app,
//but at that time there were no connection to the server and now
//we have to resend rating/derating request
#define APP_STATUS_FAVOURITED_REQUEST_PENDING 1

//resembles "active" field in Favourites table
typedef enum{
  ACTIVE,
  INACTIVE,
  ACTIVE_DEFAULT = -1
} AppActiveState;

//resembles "favourited" field in Favourites table
typedef enum{
  FAVOURITED,
  UNFAVOURITED,
  FAVOURITE_DEFAULT = -1
} AppFavouritedState;

/**
* Service for storing data about downloaded applications,
* as well as those selected by the user and marked as "best" by the service iBuildApp
*/
@interface mBAApplicationTableService : mBATableService

/**
* Returns the best apps
* @return List of the best apps
*/
- (NSArray *)featuredList;

/**
* Sets the list of the best applications
* @param featuredList List of the best apps
*/
- (void)setFeaturedList:(NSArray* )featuredList;

/**
* Sets the application identifier list for the category
* @param ids List of referers
* @param categoryId Category ID
*/
- (void)updateApplicationIds:(NSArray *)ids forCategoryId:(NSInteger)categoryId;

/**
* Gets a list of identifiers for the category
* @param categoryId Category ID
* @return ids List of referers
*/
- (NSArray*)getApplicationIds:(NSInteger)categoryId;


/**
 * Gets a list of IDs for the category, if there is no network access
 * @param categoryId Category ID
 * @return ids List of referers
 */
- (NSArray*)getApplicationCachedIds:(NSInteger)categoryId;

/**
* Updates the data for a set of applications with the specified identifiers
* @param data Array of application models, identifier - model field
*/
- (void)updateApplicationData:(NSArray *)data;

/**
* Gets the data for the application
* @param app_id Array of applications
* @return Application model
*/
- (mBAApplicationModel *)getApplicationData:(NSInteger)app_id;

/**
 *  Updates the sorted_apps_list field in the Category table at the specified category_id
 *
 *  @param appIDs     A serialized array of identifiers (JSON string)
 *  @param categoryId Category ID
 */
- (void)updateSortedAppIDs:(NSString *)appIDs forCategoryId:(NSInteger)categoryId;

/**
* Adds an application with a given id to favorites
* @param app_id Application ID
*/
- (void)addToFavourites:(NSInteger)app_id;

/**
* Deletes the application with the specified identifier from the favorites
* @param app_id Application ID
*/
- (void)removeFromFavourites:(NSInteger)app_id;

/**
* Gets a list of the identifiers of the selected applications
* @return Array of identifiers for selected applications
*/
- (NSArray *)getFavouritesIds;

/**
 * Returns whether the application is included in the favorites
 * @param app_id Application ID
 * @return A Boolean value that indicates whether the application is selected
*/
- (Boolean)appInFavourites:(NSInteger)app_id;

/**
 * List of identifiers, applications from favorites,
 * headers that match the search query
 * @param query Search query
 * @return An array of identifiers for selected applications that satisfy a certain query
*/
- (NSArray *)getFavouritesIdsLike:(NSString *)query;

/**
 * Fills the data in the singleton mBASettings for the application with the specified appId
 *
 * @param appId Application ID
 */
- (void)fillSettingsForAppId:(NSInteger)appId;

/**
 * Changes the active field for the specified application
 *
 * @param appId Application ID
 * @param state new value active
 */
- (void)setActiveState:(AppActiveState)state forAppWithId:(NSInteger)appId;

/**
 * Modifies the favourited field for the specified application
 *
 * @param appId Application ID
 * @param state new favourited value
 */
- (void)setFavouritedState:(AppFavouritedState)state forAppWithId:(NSInteger)appId;

/**
 *  Returns the app_id array for raw applications
 *
 */
-(NSDictionary *)getPendingApplications;

@end
