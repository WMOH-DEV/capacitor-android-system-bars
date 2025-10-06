// swift-tools-version: 5.9
import PackageDescription

let package = Package(
    name: "AndroidSystemBars",
    platforms: [.iOS(.v14)],
    products: [
        .library(
            name: "AndroidSystemBars",
            targets: ["SystemBarsManagerPlugin"])
    ],
    dependencies: [
        .package(url: "https://github.com/ionic-team/capacitor-swift-pm.git", from: "7.0.0")
    ],
    targets: [
        .target(
            name: "SystemBarsManagerPlugin",
            dependencies: [
                .product(name: "Capacitor", package: "capacitor-swift-pm"),
                .product(name: "Cordova", package: "capacitor-swift-pm")
            ],
            path: "ios/Sources/SystemBarsManagerPlugin"),
        .testTarget(
            name: "SystemBarsManagerPluginTests",
            dependencies: ["SystemBarsManagerPlugin"],
            path: "ios/Tests/SystemBarsManagerPluginTests")
    ]
)