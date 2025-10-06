# Contributing to Android System Bars Plugin

Thank you for your interest in contributing to the Android System Bars plugin! This guide will help you get started.

## Development Setup

### Prerequisites

- Node.js 16+ and npm
- Android Studio and Android SDK
- Java 17+
- Capacitor CLI (`npm install -g @capacitor/cli`)

### Getting Started

1. **Clone the repository**

   ```bash
   git clone https://github.com/WMOH-DEV/capacitor-android-system-bars.git
   cd android-system-bars
   ```

2. **Install dependencies**

   ```bash
   npm install
   ```

3. **Build the plugin**

   ```bash
   npm run build
   ```

4. **Set up the example app**

   ```bash
   cd example-app
   npm install
   npx cap sync android
   ```

5. **Run the example app**
   ```bash
   npx cap run android
   ```

## Plugin Architecture

### TypeScript Layer (`src/`)

- `definitions.ts` - Plugin interfaces and types
- `index.ts` - Main plugin export
- `web.ts` - Web platform implementation (stubs)

### Android Layer (`android/src/main/java/`)

- `SystemBarsManagerPlugin.java` - Main plugin class with Capacitor bridge
- `SystemBarsManager.java` - Core system UI management
- `WebViewPaddingManager.java` - WebView padding for Android < 35
- `FullscreenManager.java` - Fullscreen mode handling
- `LifecycleHandler.java` - App lifecycle event management

## Android API Compatibility

The plugin supports Android API 21-35+ with different approaches:

### Android 35+ (API 35+)

- Uses native edge-to-edge enforcement
- WindowInsetsController for modern inset handling
- Automatic system bar transparency

### Android 30-34 (API 30-34)

- WindowInsetsControllerCompat from AndroidX
- Manual edge-to-edge setup
- WebView padding management

### Android 21-29 (API 21-29)

- Legacy System UI flags
- Manual system bar styling
- WebView padding management

## Testing Guidelines

### Key Test Scenarios

- [ ] **App startup** - System bars display correctly
- [ ] **Theme changes** - Status bar icons change with theme
- [ ] **Screen lock/unlock** - State is restored properly
- [ ] **Keyboard interaction** - No webview resize issues
- [ ] **Fullscreen mode** - Enter/exit works on all versions
- [ ] **Orientation changes** - System bars adjust correctly

## Scripts

### `npm run build`

Build the plugin web assets and generate plugin API documentation using [`@capacitor/docgen`](https://github.com/ionic-team/capacitor-docgen).

### `npm run verify`

Build and validate the web and native projects.

### `npm run lint` / `npm run fmt`

Check formatting and code quality, autoformat/autofix if possible.

## Code Style

### TypeScript

- Use TypeScript strict mode
- Follow ESLint configuration
- Document all public APIs with JSDoc

### Java

- Follow Android coding conventions
- Use proper null checking
- Add JavaDoc for public methods
- Handle API level differences properly

## Submitting Changes

### Pull Request Process

1. **Fork the repository**
2. **Create a feature branch**

   ```bash
   git checkout -b feature/your-feature-name
   ```

3. **Make your changes**
   - Write tests for new functionality
   - Update documentation if needed
   - Follow the code style guidelines

4. **Test thoroughly**
   - Test on multiple Android versions
   - Test on different devices (phone/tablet)

5. **Submit the pull request**
   - Provide a clear description
   - Reference any related issues
   - Include screenshots/videos for UI changes

## Publishing

There is a `prepublishOnly` hook in `package.json` which prepares the plugin before publishing, so all you need to do is run:

```shell
npm publish
```

> **Note**: The [`files`](https://docs.npmjs.com/cli/v7/configuring-npm/package-json#files) array in `package.json` specifies which files get published.

## Android Version Support Matrix

| Android Version | API Level | Support Level | Notes                  |
| --------------- | --------- | ------------- | ---------------------- |
| 15+             | 35+       | ✅ Full       | Native edge-to-edge    |
| 14              | 34        | ✅ Full       | Transitional support   |
| 11-13           | 30-33     | ✅ Full       | Manual webview padding |
| 8.1-10          | 27-29     | ✅ Full       | System UI flags        |
| 5.0-8.0         | 21-26     | ✅ Basic      | Legacy flags only      |

## Resources

- [Android Edge-to-Edge Guide](https://developer.android.com/develop/ui/views/layout/edge-to-edge)
- [WindowInsetsController API](https://developer.android.com/reference/android/view/WindowInsetsController)
- [System UI Visibility](https://developer.android.com/training/system-ui/immersive)
- [Capacitor Plugin Development](https://capacitorjs.com/docs/plugins/creating-plugins)

Thank you for contributing! 🚀
