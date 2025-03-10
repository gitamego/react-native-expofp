#import <React/RCTViewManager.h>

@interface RCT_EXTERN_MODULE(ExpofpViewManager, RCTViewManager)

RCT_EXPORT_VIEW_PROPERTY(settings, NSDictionary)

// Add preload method export
RCT_EXTERN_METHOD(preload:(NSString *)url
                  resolver:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject)

@end
