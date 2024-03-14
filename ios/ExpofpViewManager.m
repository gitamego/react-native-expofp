#import <React/RCTViewManager.h>

@interface RCT_EXTERN_MODULE(ExpofpViewManager, RCTViewManager)

RCT_EXPORT_VIEW_PROPERTY(url, NSString)
RCT_EXPORT_VIEW_PROPERTY(crowdConnectedSettings, NSDictionary)

@end
