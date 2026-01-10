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
            let urlString: String?
            if let url = settings["url"] as? String {
                urlString = url
            } else if let url = settings["url"] as? NSString {
                urlString = url as String
            } else {
                urlString = nil
            }

            if let urlString {
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
                    dataStore.url = urlString
                    dataStore.loadRequestId += 1
                } else {
                    dataStore.url = urlString
                    dataStore.loadRequestId += 1
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
    @Published var url: String = ""
    @Published var loadRequestId: Int = 0
}

struct ExpoFP: View {
    @EnvironmentObject var dataStore: ExpoFPDataStore
        
    var fplanView = SharedFplanView()
    
    @State private var handledRequestId: Int = -1
    
    private var defaultSettings: ExpoFpFplan.Settings { .init(useGlobalLocationProvider: true) }
    
    private func expoKey(from urlString: String) -> String {
        return (URL(string: urlString)?.host ?? "").components(separatedBy: ".").first ?? ""
    }
    
    private func openMap(for urlString: String) {
        let key = expoKey(from: urlString)
        print("expoKey: \(key)")

        // ExpoFP supports passing "params" separately (e.g. deep-link query like "?mandalay-bay-ballroom-f-level-2").
        // Use the SDK helper to extract the correct params format from the URL.
        let extractedParams = Helper.getParams(urlString)
        let paramsOrNil: String? = extractedParams.isEmpty ? nil : extractedParams

        if let cachePath = SharedFplanView.getFilePathFromCache() {
            let pathComponents = cachePath.absoluteString.components(separatedBy: "/")
            let cachedExpoKey = pathComponents.count >= 2 ? pathComponents[pathComponents.count - 2] : ""
            print("cachePath: \(cachePath.absoluteString)")
            print("cachedExpoKey: \(cachedExpoKey)")
            if cachedExpoKey == key {
                print("loading from cache")
                fplanView.openFile(htmlFilePathUrl: cachePath, params: paramsOrNil, settings: defaultSettings)
                return
            } else {
                print("cache key mismatch, expected: \(key), found: \(cachedExpoKey)")
            }
        }

        if let path = Bundle.main.path(forResource: key, ofType: "zip", inDirectory: "maps") {
            print("loading from preloaded map path: \(path)")
            fplanView.openZip(path, params: paramsOrNil, useGlobalLocationProvider: true)
            return
        }

        print("loading from url")
        fplanView.load(urlString, useGlobalLocationProvider: true)
    }
    
    private func downloadOffline(for urlString: String) {
        print("downloading the map")
        fplanView.downloadZipToCache(urlString) { htmlFilePath, error in
            if let error = error {
                print("error downloading the map: \(error)")
            } else {
                print("success downloading the map")
                if let htmlFilePath = htmlFilePath {
                    print("htmlFilePath: \(htmlFilePath)")
                }
            }
        }
    }

    private func handleLoadRequestIfNeeded() {
        let requestId = dataStore.loadRequestId
        guard requestId != handledRequestId else { return }
        handledRequestId = requestId

        let urlString = dataStore.url.trimmingCharacters(in: .whitespacesAndNewlines)
        guard !urlString.isEmpty else { return }

        openMap(for: urlString)
        downloadOffline(for: urlString)
    }
    
    var body: some View {
        VStack
        {
            fplanView.onAppear{
                handleLoadRequestIfNeeded()
            }.onChange(of: dataStore.loadRequestId) { _ in
                handleLoadRequestIfNeeded()
            }.onDisappear{
                fplanView.clear()
            }
        }
    }
}
