
#import "auth_ShareReplyVC.h"

typedef enum {
  mBAAuthShareReplyViewControllerFacebookAppearance,
  mBAAuthShareReplyViewControllerTwitterAppearance,
  mBAAuthShareReplyViewControllerAppearanceNone
} mBAAuthShareReplyViewControllerAppearance;

@interface mBAAuthShareReplyViewController : auth_ShareReplyVC

@property (nonatomic) mBAAuthShareReplyViewControllerAppearance appearance;

@end
