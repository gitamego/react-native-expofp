import ExpoFpFplan
import ExpoFpCommon
import SwiftUI
import ExpoFpCrowdConnected

@objc(ExpofpViewManager)
class ExpofpViewManager: RCTViewManager {
    
    private var expoFPViewProxy: ExpoFPViewProxy?
    
    override func view() -> UIView! {
        if expoFPViewProxy == nil {
            expoFPViewProxy = ExpoFPViewProxy()
        }
        return expoFPViewProxy
    }
    
    @objc override static func requiresMainQueueSetup() -> Bool {
        return true
    }
}

class ExpoFPViewProxy: UIView {
    
    var returningView: UIView?
    let dataStore: ExpoFPDataStore = .init()
    
    override init(frame: CGRect) {
        super.init(frame: frame)
        let vc = UIHostingController(rootView: ExpoFP().environmentObject(dataStore))
        vc.view.frame = bounds
        self.addSubview(vc.view)
        self.returningView = vc.view
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }

    @objc var settings: NSDictionary = [:] {
        didSet {
            if let url = settings["url"] as? NSString {
                if let appKey = settings["appKey"] as? String,
                    let token = settings["token"] as? String,
                    let secret = settings["secret"] as? String {
                    // let ccSettings = Settings(appKey: appKey, token: token, secret: secret, mode: Mode.IPS_AND_GPS, inBackground: true, enableHeading: true)
                    let ccSettings = Settings(appKey: appKey, token: token, secret: secret, mode: Mode.IPS_AND_GPS, inBackground: false, enableHeading: true)
                    if let onesignalUserId = settings["oneSignalUserId"] as? String {
                        ccSettings.addAlias("onesignal_user_id", onesignalUserId)
                    }
                    let locationProvider: LocationProvider = CrowdConnectedProvider(ccSettings);
                    GlobalLocationProvider.initialize(locationProvider)
                    GlobalLocationProvider.start()
                    dataStore.url = url
                } else {
                    dataStore.url = url
                }
            }
        }
    }
    
    override func layoutSubviews() {
        super.layoutSubviews()
        self.returningView?.frame = bounds
    }
}

class ExpoFPDataStore: ObservableObject {
    @Published var url: NSString = ""
}

struct ExpoFP: View {
    @EnvironmentObject var dataStore: ExpoFPDataStore
        
    var fplanView = SharedFplanView()
    
    @State private var loadedUrl: NSString? = nil
    
    var body: some View {
        VStack
        {
            fplanView.onAppear{
                if (loadedUrl !== dataStore.url) {
                    let expoKey = (URL(string: dataStore.url as String)?.host ?? "").components(separatedBy: ".").first ?? ""
                    print("expoKey: \(expoKey)")

                    if let cachePath = SharedFplanView.getFilePathFromCache() {
                        let pathComponents = cachePath.absoluteString.components(separatedBy: "/")
                        let cachedExpoKey = pathComponents[pathComponents.count - 2]
                        print("cachePath: \(cachePath.absoluteString)")
                        print("cachedExpoKey: \(cachedExpoKey)")
                        if cachedExpoKey == expoKey {
                            print("loading from cache")
                            fplanView.openFile(htmlFilePathUrl: cachePath, params: nil, settings: ExpoFpFplan.Settings(useGlobalLocationProvider: true))
                        }
                    } else if let path = Bundle.main.path(forResource: expoKey, ofType: "zip", inDirectory: "maps") {
                        print("loading from preloaded map path: \(path)")
                        fplanView.openZip(path, params: nil, useGlobalLocationProvider: true)
                    } else {
                        print("loading from url")
                        fplanView.load(dataStore.url as String, useGlobalLocationProvider: true)
                    }
                    loadedUrl = dataStore.url
                    print("downlpading the map")
                    fplanView.downloadZipToCache(dataStore.url as String) { htmlFilePath, error in
                        if let error = error {
                            print("error doenaloding the map")
                            print(error)
                        } else {
                            print("success downloading the map")
                            if let htmlFilePath = htmlFilePath {
                                print("htmlFilePath: \(htmlFilePath)")
                            }
                        }
                    }
                }
            }.onDisappear{
                fplanView.clear()
            }
        }
    }
}
