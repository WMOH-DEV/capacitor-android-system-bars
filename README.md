# capacitor-android-system-bars

Unified plugin to handle Android system bars (status bar & navigation bar), webview sizing, edge-to-edge display, and fullscreen mode with full compatibility for Android API 21-35+.

**Replaces:**

- `@capawesome/capacitor-android-edge-to-edge-support`
- `@capacitor/status-bar`
- Custom fullscreen implementations

## 🚀 Features

- ✅ **Android API 21-35+ Compatibility** - Full support from Android 5.0 to Android 15+
- ✅ **Edge-to-Edge Display** - Native Android 15+ edge-to-edge with backward compatibility
- ✅ **System Bar Management** - Status bar and navigation bar styling and visibility
- ✅ **Fullscreen Mode** - Immersive and lean fullscreen modes
- ✅ **WebView Padding** - Automatic webview padding management for Android < 35
- ✅ **Lifecycle Handling** - Automatic state restoration after screen lock/unlock
- ✅ **Modern APIs** - Uses latest WindowInsetsController for Android 30+

## Install

```bash
npm install capacitor-android-system-bars
npx cap sync
```

## Android Configuration

### AndroidManifest.xml

Add the following to your `android/app/src/main/AndroidManifest.xml`:

```xml
<activity
    android:name=".MainActivity"
    android:windowSoftInputMode="adjustNothing"
    android:theme="@style/AppTheme.NoActionBarLaunch">
</activity>
```

### Capacitor Configuration

```typescript
// capacitor.config.ts
import { CapacitorConfig } from '@capacitor/cli';

const config: CapacitorConfig = {
  plugins: {
    Keyboard: {
      resizeOnFullScreen: false, // Important for edge-to-edge
    },
  },
};

export default config;
```

## Usage

### Initialize the Plugin

```typescript
import { AndroidSystemBars } from 'capacitor-android-system-bars';

export class AppComponent {
  async ngOnInit() {
    await this.initializeSystemBars();
  }

  async initializeSystemBars() {
    try {
      const info = await AndroidSystemBars.initialize();
      console.log('Android API Level:', info.apiLevel);
      console.log('Edge-to-edge supported:', info.supportsEdgeToEdge);

      // Configure based on Android version
      if (info.isAndroid35Plus) {
        // Android 15+: Use overlay mode
        await AndroidSystemBars.setOverlay({ overlay: true });
      }

      // Set status bar style
      const isDark = document.body.classList.contains('dark');
      await AndroidSystemBars.setStyle({
        style: isDark ? 'DARK' : 'LIGHT',
        color: isDark ? '#111827' : '#f5efef',
      });
    } catch (error) {
      console.error('Error initializing system bars:', error);
    }
  }
}
```

### Theme Integration (Ionic)

```typescript
// theme.service.ts
import { AndroidSystemBars } from 'capacitor-android-system-bars';

export class ThemeService {
  async toggleTheme() {
    const isDark = document.body.classList.toggle('dark');

    // Update system bars to match theme
    await AndroidSystemBars.setStyle({
      style: isDark ? 'DARK' : 'LIGHT',
      color: isDark ? '#111827' : '#ffffff',
    });
  }
}
```

### Fullscreen Mode

```typescript
import { AndroidSystemBars } from 'capacitor-android-system-bars';

export class FullscreenService {
  async enterFullscreen() {
    await AndroidSystemBars.enterFullscreen({ mode: 'IMMERSIVE' });
  }

  async exitFullscreen() {
    const isDark = document.body.classList.contains('dark');
    await AndroidSystemBars.exitFullscreen({
      style: isDark ? 'DARK' : 'LIGHT',
      color: isDark ? '#111827' : '#ffffff',
    });
  }
}
```

## API Compatibility Matrix

| Android Version | API Level | Edge-to-Edge | System UI Flags | WindowInsets API | Notes                         |
| --------------- | --------- | ------------ | --------------- | ---------------- | ----------------------------- |
| 15+             | 35+       | ✅ Native    | ✅ Deprecated   | ✅ Required      | Full edge-to-edge enforcement |
| 14              | 34        | ⚠️ Partial   | ✅ Works        | ✅ Available     | Transitional support          |
| 11-13           | 30-33     | ⚠️ Manual    | ✅ Works        | ✅ Available     | Manual webview padding        |
| 8.1-10          | 27-29     | ❌ No        | ✅ Primary      | ⚠️ Limited       | Use System UI flags only      |
| 5.0-8.0         | 21-26     | ❌ No        | ✅ Primary      | ❌ No            | Legacy System UI flags        |

## Migration Guide

### From @capawesome/capacitor-android-edge-to-edge-support

```typescript
// Before
import { EdgeToEdge } from '@capawesome/capacitor-android-edge-to-edge-support';
await EdgeToEdge.enable();

// After
import { AndroidSystemBars } from 'capacitor-android-system-bars';
const info = await AndroidSystemBars.initialize();
if (info.isAndroid35Plus) {
  await AndroidSystemBars.setOverlay({ overlay: true });
}
```

### From @capacitor/status-bar

```typescript
// Before
import { StatusBar, Style } from '@capacitor/status-bar';
await StatusBar.setStyle({ style: Style.Dark });

// After
import { AndroidSystemBars } from 'capacitor-android-system-bars';
await AndroidSystemBars.setStyle({ style: 'DARK' });
```

## API

<docgen-index>

* [`initialize()`](#initialize)
* [`setStyle(...)`](#setstyle)
* [`hide()`](#hide)
* [`show()`](#show)
* [`enterFullscreen(...)`](#enterfullscreen)
* [`exitFullscreen(...)`](#exitfullscreen)
* [`setOverlay(...)`](#setoverlay)
* [`getInsets()`](#getinsets)
* [`setNavigationBarStyle(...)`](#setnavigationbarstyle)
* [`hideNavigationBar()`](#hidenavigationbar)
* [`showNavigationBar()`](#shownavigationbar)
* [`restoreSystemUIAfterAd(...)`](#restoresystemuiafterad)
* [`setAdMobCompatibilityMode(...)`](#setadmobcompatibilitymode)
* [`getSystemUIState()`](#getsystemuistate)
* [Interfaces](#interfaces)

</docgen-index>

<docgen-api>
<!--Update the source file JSDoc comments and rerun docgen to update the docs below-->

### initialize()

```typescript
initialize() => Promise<InitializeResult>
```

Initialize plugin and get device info

**Returns:** <code>Promise&lt;<a href="#initializeresult">InitializeResult</a>&gt;</code>

--------------------


### setStyle(...)

```typescript
setStyle(options: SetStyleOptions) => Promise<void>
```

Set status bar style and color

| Param         | Type                                                        |
| ------------- | ----------------------------------------------------------- |
| **`options`** | <code><a href="#setstyleoptions">SetStyleOptions</a></code> |

--------------------


### hide()

```typescript
hide() => Promise<void>
```

Hide status bar

--------------------


### show()

```typescript
show() => Promise<void>
```

Show status bar

--------------------


### enterFullscreen(...)

```typescript
enterFullscreen(options: EnterFullscreenOptions) => Promise<void>
```

Enter fullscreen mode

| Param         | Type                                                                      |
| ------------- | ------------------------------------------------------------------------- |
| **`options`** | <code><a href="#enterfullscreenoptions">EnterFullscreenOptions</a></code> |

--------------------


### exitFullscreen(...)

```typescript
exitFullscreen(options: ExitFullscreenOptions) => Promise<void>
```

Exit fullscreen mode

| Param         | Type                                                                    |
| ------------- | ----------------------------------------------------------------------- |
| **`options`** | <code><a href="#exitfullscreenoptions">ExitFullscreenOptions</a></code> |

--------------------


### setOverlay(...)

```typescript
setOverlay(options: SetOverlayOptions) => Promise<void>
```

Set overlay mode (Android 35+ only)

| Param         | Type                                                            |
| ------------- | --------------------------------------------------------------- |
| **`options`** | <code><a href="#setoverlayoptions">SetOverlayOptions</a></code> |

--------------------


### getInsets()

```typescript
getInsets() => Promise<InsetsResult>
```

Get current window insets information

**Returns:** <code>Promise&lt;<a href="#insetsresult">InsetsResult</a>&gt;</code>

--------------------


### setNavigationBarStyle(...)

```typescript
setNavigationBarStyle(options: SetNavigationBarStyleOptions) => Promise<void>
```

Set navigation bar style and color

| Param         | Type                                                                                  |
| ------------- | ------------------------------------------------------------------------------------- |
| **`options`** | <code><a href="#setnavigationbarstyleoptions">SetNavigationBarStyleOptions</a></code> |

--------------------


### hideNavigationBar()

```typescript
hideNavigationBar() => Promise<void>
```

Hide navigation bar

--------------------


### showNavigationBar()

```typescript
showNavigationBar() => Promise<void>
```

Show navigation bar

--------------------


### restoreSystemUIAfterAd(...)

```typescript
restoreSystemUIAfterAd(options: RestoreSystemUIOptions) => Promise<void>
```

Restore system UI state after AdMob ad interference
Call this after AdMob ads to fix system bar issues

| Param         | Type                                                                      |
| ------------- | ------------------------------------------------------------------------- |
| **`options`** | <code><a href="#restoresystemuioptions">RestoreSystemUIOptions</a></code> |

--------------------


### setAdMobCompatibilityMode(...)

```typescript
setAdMobCompatibilityMode(options: SetAdMobCompatibilityOptions) => Promise<AdMobCompatibilityResult>
```

Enable/disable AdMob compatibility mode

| Param         | Type                                                                                  |
| ------------- | ------------------------------------------------------------------------------------- |
| **`options`** | <code><a href="#setadmobcompatibilityoptions">SetAdMobCompatibilityOptions</a></code> |

**Returns:** <code>Promise&lt;<a href="#admobcompatibilityresult">AdMobCompatibilityResult</a>&gt;</code>

--------------------


### getSystemUIState()

```typescript
getSystemUIState() => Promise<SystemUIStateResult>
```

Get current system UI state (for debugging)

**Returns:** <code>Promise&lt;<a href="#systemuistateresult">SystemUIStateResult</a>&gt;</code>

--------------------


### Interfaces


#### InitializeResult

| Prop                       | Type                 | Description                                   |
| -------------------------- | -------------------- | --------------------------------------------- |
| **`apiLevel`**             | <code>number</code>  | Android API level                             |
| **`isAndroid35Plus`**      | <code>boolean</code> | Whether device is running Android 35+         |
| **`supportsEdgeToEdge`**   | <code>boolean</code> | Whether device supports edge-to-edge natively |
| **`supportsWindowInsets`** | <code>boolean</code> | Whether device supports WindowInsets API      |
| **`statusBarHeight`**      | <code>number</code>  | Status bar height in pixels                   |
| **`navigationBarHeight`**  | <code>number</code>  | Navigation bar height in pixels               |


#### SetStyleOptions

| Prop        | Type                                        | Description                                                    |
| ----------- | ------------------------------------------- | -------------------------------------------------------------- |
| **`style`** | <code>'LIGHT' \| 'DARK' \| 'DEFAULT'</code> | Status bar style                                               |
| **`color`** | <code>string</code>                         | Status bar background color (hex format: #RRGGBB or #AARRGGBB) |


#### EnterFullscreenOptions

| Prop       | Type                               | Description          |
| ---------- | ---------------------------------- | -------------------- |
| **`mode`** | <code>'IMMERSIVE' \| 'LEAN'</code> | Fullscreen mode type |


#### ExitFullscreenOptions

| Prop        | Type                                        | Description                 |
| ----------- | ------------------------------------------- | --------------------------- |
| **`style`** | <code>'LIGHT' \| 'DARK' \| 'DEFAULT'</code> | Status bar style to restore |
| **`color`** | <code>string</code>                         | Status bar color to restore |


#### SetOverlayOptions

| Prop          | Type                 | Description                                       |
| ------------- | -------------------- | ------------------------------------------------- |
| **`overlay`** | <code>boolean</code> | Whether to enable overlay mode (Android 35+ only) |


#### InsetsResult

| Prop                       | Type                 | Description                        |
| -------------------------- | -------------------- | ---------------------------------- |
| **`top`**                  | <code>number</code>  | Top inset (status bar area)        |
| **`bottom`**               | <code>number</code>  | Bottom inset (navigation bar area) |
| **`left`**                 | <code>number</code>  | Left inset                         |
| **`right`**                | <code>number</code>  | Right inset                        |
| **`statusBarVisible`**     | <code>boolean</code> | Whether status bar is visible      |
| **`navigationBarVisible`** | <code>boolean</code> | Whether navigation bar is visible  |


#### SetNavigationBarStyleOptions

| Prop        | Type                                        | Description                                                        |
| ----------- | ------------------------------------------- | ------------------------------------------------------------------ |
| **`style`** | <code>'LIGHT' \| 'DARK' \| 'DEFAULT'</code> | Navigation bar style                                               |
| **`color`** | <code>string</code>                         | Navigation bar background color (hex format: #RRGGBB or #AARRGGBB) |


#### RestoreSystemUIOptions

| Prop        | Type                                        | Description                 |
| ----------- | ------------------------------------------- | --------------------------- |
| **`style`** | <code>'LIGHT' \| 'DARK' \| 'DEFAULT'</code> | Status bar style to restore |
| **`color`** | <code>string</code>                         | Status bar color to restore |


#### AdMobCompatibilityResult

| Prop          | Type                 | Description                                 |
| ------------- | -------------------- | ------------------------------------------- |
| **`enabled`** | <code>boolean</code> | Whether AdMob compatibility mode is enabled |


#### SetAdMobCompatibilityOptions

| Prop          | Type                 | Description                                |
| ------------- | -------------------- | ------------------------------------------ |
| **`enabled`** | <code>boolean</code> | Whether to enable AdMob compatibility mode |


#### SystemUIStateResult

| Prop                         | Type                 | Description                                 |
| ---------------------------- | -------------------- | ------------------------------------------- |
| **`lastStyle`**              | <code>string</code>  | Last applied status bar style               |
| **`lastColor`**              | <code>string</code>  | Last applied status bar color               |
| **`apiLevel`**               | <code>number</code>  | Android API level                           |
| **`adMobCompatibilityMode`** | <code>boolean</code> | Whether AdMob compatibility mode is enabled |

</docgen-api>

## Troubleshooting

### Common Issues

1. **Content hidden under status bar (Android < 35)**
   - The plugin automatically applies webview padding
   - No manual padding needed

2. **Screen unlock resets system UI**
   - Plugin automatically handles lifecycle events
   - State is restored automatically

3. **Keyboard resize issues**
   - Set `android:windowSoftInputMode="adjustNothing"` in AndroidManifest.xml
   - Disable `resizeOnFullScreen` in Keyboard plugin config

4. **AdMob banner issues**
   - Plugin coordinates with system bar state changes
   - No additional configuration needed

## Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md) for details.

## License

MIT © [Wael M.Elsaid](https://github.com/WMOH-DEV)
