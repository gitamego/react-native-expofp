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
    
    @objc var url: NSString = "" {
        didSet{
            dataStore.url = url
        }
    }

    @objc var crowdConnectedSettings: NSDictionary = [:] {
        didSet {
            if let appKey = crowdConnectedSettings["appKey"] as? String,
                let token = crowdConnectedSettings["token"] as? String,
                let secret = crowdConnectedSettings["secret"] as? String {
                let locationProvider: LocationProvider = CrowdConnectedProvider(Settings(appKey, token, secret, Mode.IPS_AND_GPS));               
                GlobalLocationProvider.initialize(locationProvider)
                GlobalLocationProvider.start()
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
