# Changelog

All notable changes to this project will be documented in this file.

## [1.1.0] - 2024-12-19

### Added

- **Navigation Bar Color Control**: Full support for Android navigation bar background color and styling
- **Android 15+ Compatibility**: Proper handling of deprecated `setNavigationBarColor()` API in Android 35+
- **Edge-to-Edge Navigation Support**: Modern approach using `WindowInsetsController` for Android 15+
- **Navigation Bar Icon Styling**: Light/dark navigation bar icons with automatic fallbacks
- **Comprehensive API Support**: Navigation bar features across Android API 26-35+

### Enhanced

- **SystemBarsManager**: Added `setNavigationBarStyle()`, `hideNavigationBar()`, `showNavigationBar()` methods
- **Plugin Interface**: Enhanced TypeScript definitions with navigation bar options
- **Example App**: Added navigation bar testing interface with color controls and visibility toggles
- **Documentation**: Created `NAVIGATION_BAR_USAGE.md` with comprehensive migration guide

### Technical

- **Deprecation Handling**: Automatic transparency enforcement on Android 15+ with proper edge-to-edge design
- **API Compatibility Matrix**: Full backward compatibility while utilizing latest Android APIs
- **Testing Interface**: Enhanced example app with navigation bar color testing and insets inspection

### Documentation

- Added detailed navigation bar usage guide
- Included Android 15+ migration strategies
- Provided troubleshooting guide for common issues
- Enhanced API reference with navigation bar methods

## [1.0.2] - 2024-12-18

### Initial Release

- Status bar color and style control
- Fullscreen mode support (immersive and lean)
- Edge-to-edge display compatibility
- Android API 21-35+ support
- WebView sizing and padding management
- Lifecycle event handling
- Example app with testing interface

### Features

- Status bar visibility toggle
- Status bar color customization
- Status bar icon styling (light/dark)
- Immersive fullscreen mode
- Smooth theme transitions
- Automatic WebView padding for Android < 35
- Screen unlock state restoration
