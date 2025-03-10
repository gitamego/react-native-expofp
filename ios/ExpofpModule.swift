import ExpoFpFplan
import ExpoFpCommon
import Foundation

@objc(ExpofpModule)
class ExpofpModule: NSObject {

  @objc static func requiresMainQueueSetup() -> Bool {
    return false
  }
  
  @objc func preload(_ url: String, resolve: @escaping RCTPromiseResolveBlock, reject: @escaping RCTPromiseRejectBlock) {
    SharedFplanView.preload(url, settings: Settings())
    resolve(nil)
  }
}