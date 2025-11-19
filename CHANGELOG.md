# Changelog

All notable changes to this project will be documented in this file.

## [1.2.4] - 2025-11-19

### Fixed

- **Android 15+ Compatibility**: Removed deprecated `setStatusBarColor()` and `setNavigationBarColor()` API calls on Android 35+
  - These methods are automatically handled by the system when edge-to-edge is enabled
  - Fixes Google Play Console warning about deprecated APIs
  - Maintains full compatibility with Android API 21-35+
  - System bars remain transparent on Android 15+ without using deprecated methods

### Technical

- Updated `SystemBarsManager.java` to remove deprecated API usage in `enableEdgeToEdge()` method
- Edge-to-edge transparency now achieved through `WindowCompat.setDecorFitsSystemWindows()` only
- Compliant with Android 15's edge-to-edge best practices

---

## [1.2.3] - 2025-10-07

### Fixed

- **Fullscreen Mode Lifecycle**: Fixed critical issue where system bars would reappear after screen lock/unlock or app backgrounding while in fullscreen mode
  - Added fullscreen state tracking to persist across lifecycle events
  - Implemented `reapplyFullscreenIfActive()` method to restore fullscreen after resume
  - System bars now remain hidden through screen lock, app switch, and phone call interruptions
  - Mode preservation (IMMERSIVE/LEAN) maintained across all lifecycle events

### Enhanced

- **LifecycleHandler**: Improved `onResume()` logic to check fullscreen state before re-applying system UI
- **FullscreenManager**: Added `currentFullscreenMode` tracking and `getCurrentFullscreenMode()` getter
- **State Management**: Enhanced fullscreen state persistence across Android API 21-35+

### Technical

- Updated `FullscreenManager.java` with state tracking and re-application logic
- Enhanced `LifecycleHandler.java` with conditional system UI restoration
- Added `getFullscreenManager()` getter to `SystemBarsManagerPlugin.java`
- Created comprehensive documentation: `FULLSCREEN_LIFECYCLE_FIX.md`, `FULLSCREEN_FLOW.md`, `FULLSCREEN_TEST_GUIDE.md`

---

## [1.2.2] - 2024-12-28

### Fixed

- **Null Safety**: Fixed null pointer handling in `setOverlay()` method to prevent crashes when options parameter is null
- **Type Safety**: Improved null checking in `setSystemBarsStyle()` method for better robustness

### Enhanced

- **Documentation**: Streamlined README for improved clarity and usability with better examples and migration guidance
- **API Documentation**: Enhanced method descriptions and usage examples in README

---

## [1.2.0] - 2024-12-28

### Added

- **Unified System Bars API**: New `setSystemBarsStyle()` method to handle both status and navigation bars in a single call with flexible configuration options
- **Enhanced Fullscreen Management**: New `isFullscreenActive()` and `forceExitFullscreen()` methods for better fullscreen state control
- **Clear Method Naming**: Added specific methods like `setStatusBarStyle()`, `hideStatusBar()`, and `showStatusBar()` for unambiguous API usage
- **Professional Fullscreen Exit**: Enhanced `exitFullscreen()` method with explicit restoration configurations for both system bars
- **New API Demo**: Added demonstration method in example app to showcase new API capabilities

### Enhanced

- **API Design**: Complete redesign for improved clarity and functionality with backward compatibility
- **TypeScript Definitions**: Updated interface with new methods and improved type safety
- **Example App**: Enhanced with new demo button and comprehensive API testing scenarios
- **Documentation**: Updated README and created NEW_API_DESIGN.md with migration guide
- **Plugin Implementation**: Native Android code updated to support new unified API methods

### Changed

- **Method Naming**: Replaced ambiguous method names with specific, clear alternatives
- **Fullscreen Exit**: Now supports structured restoration configuration instead of simple parameters
- **API Structure**: Unified approach for system bars control while maintaining individual bar access

### Deprecated

- **Legacy Methods**: `setStyle()`, `hide()`, and `show()` methods marked as deprecated with clear migration path
- **Old Fullscreen Exit**: Simple parameter-based exit fullscreen deprecated in favor of structured restoration

### Technical

- **Backward Compatibility**: All existing code continues to work with deprecation warnings
- **Unified Configuration**: Single method call can now set both status and navigation bars
- **Flexible Options**: Support for shorthand (same style/color for both bars) and individual bar control
- **State Management**: Enhanced fullscreen state tracking and emergency recovery mechanisms

---

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
