
#import "mBACategoriesTableViewController.h"
#import "MBANewAppFormPresenter.h"
#import "UIColor+RGB.h"

@interface mBACategoriesTableViewController (){
  NSInteger selectedCategoryRow;
  NSInteger tmpCategoryRow;
  
  UIBarButtonItem *cancelBarButtonItem;
  UIBarButtonItem *doneBarButtonItem;
}
@end

@implementation mBACategoriesTableViewController

- (id)initWithStyle:(UITableViewStyle)style
{
    self = [super initWithStyle:style];
    return self;
}

- (void) placeSeparatorOnToolbar:(UIView *)toolbar
{
  CGFloat originY = toolbar.frame.origin.y + toolbar.frame.size.height;
  CGRect separatorFrame = (CGRect){0.0f, originY, toolbar.frame.size.width, kToolbarSeparatorHeight};
  
  UIView *separator = [[UIView alloc] initWithFrame:separatorFrame];
  separator.backgroundColor = kToolbarSeparatorColor;
  toolbar.clipsToBounds = NO;
  
  [self.view addSubview:separator];
}

- (void)viewDidLoad
{
    [super viewDidLoad];
    // Uncomment the following line to preserve selection between presentations.
    // self.clearsSelectionOnViewWillAppear = NO;
    
    // Uncomment the following line to display an Edit button in the navigation bar for this view controller.
    // self.navigationItem.rightBarButtonItem = self.editButtonItem;
  
  UIToolbar *myStatusBar = nil;
  
    UIColor *statusBarColor  = kToolbarColor;
    
    myStatusBar  = [[UIToolbar alloc]init];
    myStatusBar.frame = (CGRect){0, 0, self.view.bounds.size.width, 20};
    myStatusBar.tintColor = statusBarColor;
    myStatusBar.backgroundColor = statusBarColor;
    
    [self.navigationController.view addSubview:myStatusBar];

  self.tableView.backgroundColor = [UIColor colorWithRGB: 0xefefef];
  
  self.navigationItem.title = @"Categories";
  
  cancelBarButtonItem = [[UIBarButtonItem alloc] initWithBarButtonSystemItem:UIBarButtonSystemItemCancel target:self action:@selector(dismissCategoriesTable:)];
  doneBarButtonItem = [[UIBarButtonItem alloc] initWithBarButtonSystemItem:UIBarButtonSystemItemDone target:self action:@selector(dismissCategoriesTable:)];
  
    cancelBarButtonItem.tintColor = [UIColor whiteColor];
    doneBarButtonItem.tintColor = [UIColor whiteColor];

  self.navigationItem.leftBarButtonItem = cancelBarButtonItem;
  self.navigationItem.rightBarButtonItem = doneBarButtonItem;
  
  [self placeSeparatorOnToolbar:myStatusBar];
}

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

-(void)dismissCategoriesTable:(id)sender{
  if(sender == doneBarButtonItem){
    selectedCategoryRow = tmpCategoryRow;
    NSString *selectedCategoryTitle = [self.categories[selectedCategoryRow] objectForKey:@"title"];
    id parent = self.presentingViewController;
    
    if([parent isKindOfClass:[mBANewAppFormPresenter class]]){
      [parent setCategoryNameLabelText:selectedCategoryTitle];
      
      NSString *selectedCategoryId = [self.categories[selectedCategoryRow] objectForKey:@"id"];
      [parent setSelectedCategoryId:selectedCategoryId];
    }
  } else if(sender == cancelBarButtonItem){
    tmpCategoryRow = selectedCategoryRow;
  }
  [self.tableView reloadData];
  
  [self dismissViewControllerAnimated:YES completion:nil];
}

#pragma mark - Table view data source

- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView
{
    return 1;
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section
{
    return [self.categories count];
}


- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath
{
  static NSString *reuseIdentifier = @"Cell";
  UITableViewCell *cell = [tableView dequeueReusableCellWithIdentifier:reuseIdentifier];
  if(cell == nil){
    cell = [[UITableViewCell alloc] initWithStyle:UITableViewCellStyleSubtitle reuseIdentifier:reuseIdentifier];
  }
  
  cell.textLabel.text = [self.categories[indexPath.row] objectForKey:@"title"];
  if(indexPath.row == tmpCategoryRow){
    cell.accessoryType = UITableViewCellAccessoryCheckmark;
  } else {
    cell.accessoryType = UITableViewCellAccessoryNone;
  }
  return cell;
}

-(void) tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath{
  tmpCategoryRow = indexPath.row;
  [self.tableView reloadData];
}

@end
