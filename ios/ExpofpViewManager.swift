import ExpoFpFplan
import ExpoFpCommon
import SwiftUI
import ExpoFpCrowdConnected

@objc(ExpofpViewManager)
class ExpofpViewManager: RCTViewManager {
    
    override func view() -> ExpoFPViewProxy? {
        return ExpoFPViewProxy()
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
        
        // let locationProvider: LocationProvider = CrowdConnectedProvider(Settings("APP_KEY", "TOKEN", "SECRET"))
        // GlobalLocationProvider.initialize(locationProvider)
        // GlobalLocationProvider.start()
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    @objc var url: NSString = "" {
        didSet{
            dataStore.url = url
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
    
    var body: some View {
        VStack
        {
            fplanView.onAppear{
                fplanView.load(dataStore.url as String, useGlobalLocationProvider: true)
            }
            .onDisappear {
                fplanView.destoy()
            }
        }
    }
}
