
#import <UIKit/UIKit.h>

@protocol mBASideBarViewDelegate <NSObject>

@required
- (void)favouritesMenuItemSelected;
- (void)aboutUsMenuItemSelected;
- (void)inviteViaEmail;
- (void)inviteViaFacebook;
- (void)inviteViaTwitter;
- (void)inviteViaSMS;

@end

#define kSideBarBackgroundColor [UIColor colorWithRed:45.0f/256.0f green:51.0f/256.0f blue:54.0f/256.0f alpha:1.0]
#define kSideBarSeparatorColor [UIColor colorWithWhite:1.0f alpha:0.2f]

#define kSideBarWidth 220.0f
#define kSideBarWidthForSharingMode 220.f

#define kSideBarMenuPaddingTop 51.0f
#define kSideBarMenuItemsHorizontalPadding 20.0f
#define kSideBarMenuItemsSeparatorWidth (kSideBarWidth - 2 * kSideBarMenuItemsHorizontalPadding)
#define kSideBarMenuItemsSeparatorHeight 1.0f
#define kSideBarSharingButtonsHeight 60.f

#define kSideBarMenuItemsTextLabelHeight 50.0f
#define kSideBarMenuItemsFontSize 23.0f
#define kSideBarMenuItemsFontColor [UIColor whiteColor]
#define kSideBarMenuItemsSelectedFontColor [UIColor colorWithRed:0.7 green:0.7 blue:1 alpha:1]

#define kSideBarFavouritesLabelText NSLocalizedString(@"masterApp_Favourites", @"Favourites")
#define kSideBarAboutUsLabelText NSLocalizedString(@"masterApp_AboutUs", @"About us")
#define kSideBarInviteLabelText  NSLocalizedString(@"masterApp_Invite", @"Invite")

@interface mBASideBarView : UIView

@property(nonatomic, assign) id<mBASideBarViewDelegate> sideBarDelegate;

@property(nonatomic, strong) UILabel *favouritesLabel;
@property(nonatomic, strong) UILabel *facebookLabel;
@property(nonatomic, strong) UILabel *twitterLabel;
@property(nonatomic, strong) UILabel *inviteLabel;
@property(nonatomic, strong) UILabel *aboutUsLabel;

@property(nonatomic, assign, getter=isSharingMode) BOOL sharingMode;

@end
