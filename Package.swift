// swift-tools-version:5.5
import PackageDescription

let package = Package(
    name: "react-native-expofp",
    platforms: [
        .iOS(.v15)
    ],
    products: [
        .library(
            name: "react-native-expofp",
            targets: ["react-native-expofp"]
        )
    ],
    dependencies: [
        .package(
            name: "ExpoFpFplan",
            url: "https://github.com/expofp/expofp-fplan-ios-spm",
            .exact("4.8.18")
        ),
        .package(
            name: "ExpoFpCrowdConnected",
            url: "https://github.com/expofp/expofp-crowdconnected-ios-spm",
            .exact("4.8.18")
        )
    ],
    targets: [
        .target(
            name: "react-native-expofp",
            dependencies: [
                "ExpoFpFplan",
                "ExpoFpCrowdConnected"
            ],
            path: "ios"
        )
    ]
) 