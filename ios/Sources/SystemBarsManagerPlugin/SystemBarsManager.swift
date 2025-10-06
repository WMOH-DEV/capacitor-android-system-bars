import Foundation

@objc public class SystemBarsManager: NSObject {
    @objc public func echo(_ value: String) -> String {
        print(value)
        return value
    }
}
