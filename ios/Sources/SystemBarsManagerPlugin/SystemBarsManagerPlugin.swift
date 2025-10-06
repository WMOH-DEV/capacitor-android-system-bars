import Foundation
import Capacitor

/**
 * Please read the Capacitor iOS Plugin Development Guide
 * here: https://capacitorjs.com/docs/plugins/ios
 */
@objc(SystemBarsManagerPlugin)
public class SystemBarsManagerPlugin: CAPPlugin, CAPBridgedPlugin {
    public let identifier = "SystemBarsManagerPlugin"
    public let jsName = "SystemBarsManager"
    public let pluginMethods: [CAPPluginMethod] = [
        CAPPluginMethod(name: "echo", returnType: CAPPluginReturnPromise)
    ]
    private let implementation = SystemBarsManager()

    @objc func echo(_ call: CAPPluginCall) {
        let value = call.getString("value") ?? ""
        call.resolve([
            "value": implementation.echo(value)
        ])
    }
}
