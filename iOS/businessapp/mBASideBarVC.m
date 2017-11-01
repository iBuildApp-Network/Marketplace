
#import "mBASideBarVC.h"
#import "mBASideBarView.h"

#define kHiddenViewWidth kSideBarWidth
#define kVelocityShowThreshold 120.0f
#define kDiffXShowThreshold 60.0f
#define kSwipeThreshold 1000.0f

@interface mBASideBarVC ()
{
    CGFloat currentX; // used for calculating distance finger moved on
    BOOL shifted; // are we in completed transition state?
}

@end

@implementation mBASideBarVC

@synthesize shifted = shifted;

- (id) initWithMainViewController:(UIView *)main hiddenViewController:(UIView *)hidden
{
    self = [super init];
    
    if(self){
        self.mainView = main;
        self.hiddenView = hidden;
        currentX = -1.0f;
        shifted = NO;
    }
    return self;
}

- (BOOL) shouldAutorotate
{
    return  NO;
}

-(void)setMainView:(UIView *)newMainView
{
    [_mainView removeFromSuperview];
    
    _mainView = newMainView;
    [self.view addSubview:_mainView];
}

-(void)setHiddenView:(UIView *)newHiddenView
{
    [_hiddenView removeFromSuperview];
    
    _hiddenView = newHiddenView;
    [self.view addSubview:_hiddenView];
    [self.view sendSubviewToBack:self.hiddenView];
}

- (void)viewDidLoad
{
    [super viewDidLoad];
    
    [self.view addSubview:self.hiddenView];
    [self.view addSubview:self.mainView];
    
    [self.view sendSubviewToBack:self.hiddenView];
    [self.view bringSubviewToFront:self.mainView];
    
    UIPanGestureRecognizer *panGesture = [[UIPanGestureRecognizer alloc] initWithTarget:self action:@selector(dragOccured:)];
    
    [self.mainView addGestureRecognizer:panGesture];
}

- (void)viewWillAppear:(BOOL)animated
{
  [super viewWillAppear:animated];
  //[self hideSideBarAnimated:NO];
}

// shift on full hidden view width
- (void) doShift
{
    CGFloat shift = shifted ? -kHiddenViewWidth : kHiddenViewWidth;
    [self moveMainViewHorizontally:shift];
    shifted = !shifted;
}

// shifts horizontally on pixels amount specified by shift
-(void)moveMainViewHorizontally:(CGFloat)shift
{
    CGRect newMainFrame = self.mainView.frame;
    newMainFrame.origin.x += shift;
    
    NSTimeInterval duration = fabsf(shift / kHiddenViewWidth) * 0.5;
    
    [UIView animateWithDuration:duration
                          delay:0.0f
                        options:UIViewAnimationOptionCurveEaseOut
                     animations:^{
                         self.mainView.frame = newMainFrame;
                     } completion:^(BOOL finished) {
                     }];
}


- (void) dragOccured:(UIPanGestureRecognizer*) gestureRecognizer
{
    CGFloat x = [gestureRecognizer locationInView:self.view].x;
    CGPoint velocity = [gestureRecognizer velocityInView:self.view];
  
    // init distance first point
    // prevents first drag distance to be 273 pts or similar big value
    if(x != currentX){
        if(currentX == -1){
            currentX = x;
            return;
        } else {
          //prevents from dragging over the screen bounds
          //without this thing image dribbled hard on drags
          //in clear shifted or !shifted states
            if(self.mainView.frame.origin.x == kHiddenViewWidth){
                if(velocity.x > 0){
                    return;
                }
                if(currentX < x){
                    currentX = x;
                    return;
                }
            } else if (self.mainView.frame.origin.x == 0){
                if (velocity.x < 0){
                    return;
                }
                if(currentX > x){
                    currentX = x;
                    return;
                }
            }
            CGFloat panDiffX = x - currentX;
          
            // moves sidebar by finger drag
            if(self.mainView.frame.origin.x <= kHiddenViewWidth && self.mainView.frame.origin.x >= 0.0f){
                if(fabs(panDiffX) > kHiddenViewWidth){
                    panDiffX = currentX > x ? -kHiddenViewWidth : kHiddenViewWidth;
                }
                [self moveMainViewHorizontally: panDiffX];
                currentX = x;
            }
        }
    }
    
    if(gestureRecognizer.state == UIGestureRecognizerStateEnded ||
       gestureRecognizer.state == UIGestureRecognizerStateCancelled ||
       gestureRecognizer.state == UIGestureRecognizerStateFailed)
    {
      //Toggle views with swipe
        if(fabs(velocity.x) > kSwipeThreshold){
        CGFloat shiftOffset = 0.0f;
        
        if(shifted){
          if(velocity.x < 0.0f){ // swiped left <---
            shiftOffset = -self.mainView.frame.origin.x;
            [self moveMainViewHorizontally:shiftOffset];
            shifted = NO;
          }
        } else {
          if(velocity.x > 0.0f){ // swiped right --->
            shiftOffset = kHiddenViewWidth - self.mainView.frame.origin.x;
            [self moveMainViewHorizontally:shiftOffset];
            currentX = -1;
            shifted = YES;
            return; // prevent collapsing. Detect opening swipe, toggle views, exit.
          }
        }
      }
      // after drag completes do not leave side bar in indetermined state
        if(shifted){
          // when side bar opened, if it dragged left enough, make it collapse
            if(self.mainView.frame.origin.x < 2 * kHiddenViewWidth / 3){
                [self moveMainViewHorizontally: -self.mainView.frame.origin.x];
                shifted = NO;
            } else {
              // when side bar is opened, if it dragged left slightly, restore its opened state
                [self moveMainViewHorizontally: kHiddenViewWidth - self.mainView.frame.origin.x];
            }
        } else {
          // when side bar is hidden, if it dragged right enough, fully open it
            if(self.mainView.frame.origin.x >= kHiddenViewWidth / 4){
                [self moveMainViewHorizontally: kHiddenViewWidth - self.mainView.frame.origin.x];
                shifted = YES;
            } else {
              // when side bar is hidden, if it dragged right slightly, collapse it
                [self moveMainViewHorizontally: -self.mainView.frame.origin.x];
            }
        }
        currentX = -1;
    }
}

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
}

- (void) toggleHiddenView
{
  if (self.mainView.frame.origin.x == 0)
    shifted = NO;
  
    [self doShift];
}

- (void) showSideBarAnimated:(BOOL)animated
{
  if (self.mainView.frame.origin.x == 0)
    shifted = NO;
  
  if(!shifted){
    [self slideMainView:animated];
  }
}

- (void) hideSideBarAnimated:(BOOL)animated
{
  if (self.mainView.frame.origin.x == kHiddenViewWidth)
    shifted = YES;
  
  if(shifted){
    [self slideMainView:animated];
  }
}

- (void) slideMainView:(BOOL)animated {
  if(animated){
    [self doShift];
  } else {
    CGRect frame = self.mainView.frame;
    frame.origin.x = shifted ? 0.0f : kHiddenViewWidth;
    self.mainView.frame = frame;
    shifted = !shifted;
  }
}

@end
