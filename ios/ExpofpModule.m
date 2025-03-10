
#import "React/RCTBridgeModule.h"

@interface RCT_EXTERN_MODULE(ExpofpModule, NSObject)
RCT_EXTERN_METHOD(preload:(NSString *)url resolve:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject)
@end