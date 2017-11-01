// IBAHeader

#import <Foundation/Foundation.h>

@interface mBASettings : NSObject

+ (mBASettings *)sharedInstance;

@property (nonatomic, strong) NSString *currentAppId;
@property (nonatomic, strong) NSString *currentAppToken;

@end
