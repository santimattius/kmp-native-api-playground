import SwiftUI
import Shared
import Bugsnag

@main
struct iOSApp: App {
    
    init() {
        // Initialization
         let config = BugsnagConfiguration.loadConfig()
               config.appVersion = "1.0.0-alpha"

         Bugsnag.shared.initialize(config: config)
         // Send test exception
         let exception = NSException(name:NSExceptionName(rawValue: "NamedException"),
                              reason:"Something happened",
                              userInfo:nil)
         Bugsnag.shared.track(exception: exception)
    }
    
	var body: some Scene {
		WindowGroup {
			ContentView()
		}
	}
}
