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
                    let ccSettings = Settings(
                        appKey,
                        token,
                        secret,
                        Mode.IPS_AND_GPS
                    );
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
        
    var fplanView = FplanView()
    
    @State private var loadedUrl: NSString? = nil
    
    var body: some View {
        VStack
        {
            fplanView.onAppear{
                if (loadedUrl !== dataStore.url) {
                    fplanView.load(dataStore.url as String, useGlobalLocationProvider: true)
                    loadedUrl = dataStore.url
                }
            }.onDisappear{
                fplanView.clear()
            }
        }
    }
}
