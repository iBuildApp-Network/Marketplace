// IBAHeader

#import "RestFetcher.h"

@implementation RestFetcher{
  NSMutableData *received;
}

@synthesize
  request,
  delegate;

- (void)start
{
  received = [[NSMutableData alloc]init];
  [[NSURLConnection alloc] initWithRequest:self.request delegate:self];
}

- (void)connection:(NSURLConnection *)connection didReceiveResponse:(NSURLResponse *)response
{
  [received setLength: 0];
}

- (void)connection:(NSURLConnection *)connection didReceiveData:(NSData *)data
{
  [received appendData: data];
}

- (void)connection:(NSURLConnection *)connection didFailWithError:(NSError *)error
{
  received = nil;
  
  // inform the user
  NSLog(@"Connection failed! Error - %@ %@",
    error.localizedDescription,
    error.userInfo[NSURLErrorFailingURLStringErrorKey]);
  
  [self cancelOperation];
}

- (void)connectionDidFinishLoading:(NSURLConnection *)connection
{
  NSLog(@"Succeeded! Received %d bytes of data",[received length]);
  NSDictionary *parsedObject = [NSJSONSerialization JSONObjectWithData:received options:0 error:nil];
  [self processResult: parsedObject];
  received = nil;
}

- (void)processResult:(NSDictionary *)parsedObject
{
  
}

- (void)cancelOperation
{
  
}


@end
