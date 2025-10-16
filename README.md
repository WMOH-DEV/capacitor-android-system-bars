# capacitor-android-system-bars

Unified plugin to handle Android system bars (status bar & navigation bar), webview sizing, edge-to-edge display, and fullscreen mode with full compatibility for Android API 21-35+.

**Replaces:**

- `@capawesome/capacitor-android-edge-to-edge-support`
- `@capacitor/status-bar`
- Custom fullscreen implementations

## 🚀 Features

- ✅ **Android API 21-35+ Compatibility** - Full support from Android 5.0 to Android 15+
- ✅ **Edge-to-Edge Display** - Native Android 15+ edge-to-edge with backward compatibility
- ✅ **Unified System Bars API** - Single method to control both status and navigation bars
- ✅ **Status Bar Control** - Color, styling, and visibility management
- ✅ **Navigation Bar Control** - Background color and icon styling (with Android 15+ deprecation handling)
- ✅ **Fullscreen Mode** - Immersive and lean fullscreen modes with professional state management
- ✅ **WebView Padding** - Automatic webview padding management for Android < 35
- ✅ **Lifecycle Handling** - Automatic state restoration after screen lock/unlock
- ✅ **Modern APIs** - Uses latest WindowInsetsController with proper deprecation handling

## Install

```bash
npm install capacitor-android-system-bars
npx cap sync
```

## Android Configuration

### AndroidManifest.xml

**The plugin works with ANY `windowSoftInputMode` setting.** No specific configuration is required.

For the best keyboard experience, we recommend using the default behavior (don't specify `windowSoftInputMode`):

```xml
<activity
    android:name=".MainActivity"
    android:theme="@style/AppTheme.NoActionBarLaunch">
    <!-- No windowSoftInputMode needed - Android will use default adjustResize -->
</activity>
```

#### Advanced: Custom Keyboard Behavior (Optional)

If you need full control over keyboard handling for specific use cases:

```xml
<activity
    android:name=".MainActivity"
    android:windowSoftInputMode="adjustNothing"
    android:theme="@style/AppTheme.NoActionBarLaunch">
</activity>
```

> **⚠️ Note:** Using `adjustNothing` requires you to handle keyboard visibility manually with JavaScript using the `@capacitor/keyboard` plugin. For most apps, the default `adjustResize` behavior provides better user experience as Android automatically handles keyboard positioning.

### Ionic/Angular CSS Configuration

If you're using Ionic with Android 35+ (Android 15), you may need to add CSS to prevent modals and popovers from inheriting window insets padding. Add this to your `src/global.scss`:

```scss
// Fix Android 35+ modal/popover padding issue
// Android 35+ Modal Fix - Prevent header/footer from inheriting window insets
ion-modal {
  ion-header {
    // Remove extra padding from header that comes from window insets
    padding-top: 0 !important;

    ion-toolbar {
      padding-top: 0 !important;
      --padding-top: 0 !important;
      --min-height: 56px; // Standard toolbar height

      ion-title {
        padding-top: 0 !important;
        padding-bottom: 0 !important;
      }

      ion-buttons {
        padding-top: 0 !important;
        padding-bottom: 0 !important;
      }
    }
  }

  ion-footer {
    // Remove extra padding from footer that comes from window insets
    padding-bottom: 0 !important;

    ion-toolbar {
      padding-bottom: 0 !important;
      --padding-bottom: 0 !important;
      --min-height: 56px;
    }
  }
}
```

> **💡 Why?** On Android 35+, the plugin enables edge-to-edge mode which causes modals to inherit window insets. This CSS prevents unwanted padding in modal headers/footers.

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

      // Set both status and navigation bars in ONE call!
      const isDark = document.body.classList.contains('dark');
      await AndroidSystemBars.setSystemBarsStyle({
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

    // Update BOTH system bars in ONE call!
    await AndroidSystemBars.setSystemBarsStyle({
      style: isDark ? 'DARK' : 'LIGHT',
      color: isDark ? '#111827' : '#ffffff',
    });
  }
}
```

### Navigation Bar Control

```typescript
import { AndroidSystemBars } from 'capacitor-android-system-bars';

export class NavigationBarService {
  async setNavigationBarTheme(isDark: boolean) {
    const info = await AndroidSystemBars.initialize();

    await AndroidSystemBars.setNavigationBarStyle({
      style: isDark ? 'DARK' : 'LIGHT',
      // Color is ignored on Android 15+ (automatically transparent)
      color: info.isAndroid35Plus ? undefined : isDark ? '#111827' : '#ffffff',
    });
  }
}
```

> **💡 Pro Tip:** For most use cases, use `setSystemBarsStyle()` to control both bars in one call instead of individual methods.

> **⚠️ Android 15+ Notice:** Navigation bar color control is deprecated in Android 15+. The navigation bar is automatically transparent and apps should design for edge-to-edge layouts. See [NAVIGATION_BAR_USAGE.md](./NAVIGATION_BAR_USAGE.md) for detailed migration guide.

### Fullscreen Mode

```typescript
import { AndroidSystemBars } from 'capacitor-android-system-bars';

export class FullscreenService {
  async enterFullscreen() {
    await AndroidSystemBars.enterFullscreen({ mode: 'IMMERSIVE' });
  }

  async exitFullscreen() {
    const isDark = document.body.classList.contains('dark');

    // Exit fullscreen with explicit restoration configuration
    await AndroidSystemBars.exitFullscreen({
      restore: {
        style: isDark ? 'DARK' : 'LIGHT',
        color: isDark ? '#111827' : '#ffffff',
      },
    });
  }

  async checkFullscreenStatus() {
    const { active } = await AndroidSystemBars.isFullscreenActive();
    return active;
  }

  async forceExitFullscreen() {
    // Emergency exit if normal exit fails
    await AndroidSystemBars.forceExitFullscreen();
  }
}
```

### Unified System Bars API (Recommended)

For most use cases, use the new `setSystemBarsStyle()` method to control both status and navigation bars in a single call:

```typescript
import { AndroidSystemBars } from 'capacitor-android-system-bars';

export class ThemeService {
  async setAppTheme(theme: 'light' | 'dark') {
    const config =
      theme === 'dark' ? { style: 'DARK' as const, color: '#111827' } : { style: 'LIGHT' as const, color: '#ffffff' };

    // One call sets both status AND navigation bars!
    await AndroidSystemBars.setSystemBarsStyle(config);
  }

  async setCustomTheme() {
    // Different styles for each bar
    await AndroidSystemBars.setSystemBarsStyle({
      statusBar: { style: 'LIGHT', color: '#ffffff' },
      navigationBar: { style: 'DARK', color: '#000000' },
    });
  }
}
```

> **🎯 Best Practice:** Use `setSystemBarsStyle()` for theme changes and `setStatusBarStyle()`/`setNavigationBarStyle()` for individual bar control.

## API Compatibility Matrix

| Android Version | API Level | Edge-to-Edge | System UI Flags | WindowInsets API | Notes                         |
| --------------- | --------- | ------------ | --------------- | ---------------- | ----------------------------- |
| 15+             | 35+       | ✅ Native    | ✅ Deprecated   | ✅ Required      | Full edge-to-edge enforcement |
| 14              | 34        | ⚠️ Partial   | ✅ Works        | ✅ Available     | Transitional support          |
| 11-13           | 30-33     | ⚠️ Manual    | ✅ Works        | ✅ Available     | Manual webview padding        |
| 8.1-10          | 27-29     | ❌ No        | ✅ Primary      | ⚠️ Limited       | Use System UI flags only      |
| 5.0-8.0         | 21-26     | ❌ No        | ✅ Primary      | ❌ No            | Legacy System UI flags        |

## Migration Guide

### From v1.2.0 (API Redesign)

The API has been redesigned for improved clarity and reduced code complexity. All old methods are still supported with deprecation warnings.

#### Theme Changes (2 calls → 1 call)

**Before:**

```typescript
// Required TWO separate calls
await AndroidSystemBars.setStyle({ style: 'DARK', color: '#111827' });
await AndroidSystemBars.setNavigationBarStyle({ style: 'DARK', color: '#111827' });
```

**After:**

```typescript
// ONE call for both bars!
await AndroidSystemBars.setSystemBarsStyle({
  style: 'DARK',
  color: '#111827',
});
```

#### Fullscreen Exit (Confusing → Clear)

**Before:**

```typescript
// Confusing: what do these parameters apply to?
await AndroidSystemBars.exitFullscreen({
  style: 'LIGHT',
  color: '#ffffff',
});
```

**After:**

```typescript
// Crystal clear restoration intent
await AndroidSystemBars.exitFullscreen({
  restore: {
    style: 'LIGHT', // Clearly applies to both bars
    color: '#ffffff', // Clearly applies to both bars
  },
});
```

#### Method Naming (Ambiguous → Specific)

**Before:**

```typescript
await AndroidSystemBars.setStyle({ style: 'DARK' }); // What bar?
await AndroidSystemBars.hide(); // Hide what?
await AndroidSystemBars.show(); // Show what?
```

**After:**

```typescript
await AndroidSystemBars.setStatusBarStyle({ style: 'DARK' }); // Clear!
await AndroidSystemBars.hideStatusBar(); // Clear!
await AndroidSystemBars.showStatusBar(); // Clear!
```

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

// After (v1.2.0+)
import { AndroidSystemBars } from 'capacitor-android-system-bars';
await AndroidSystemBars.setSystemBarsStyle({ style: 'DARK' }); // Sets both bars!

// Or for status bar only
await AndroidSystemBars.setStatusBarStyle({ style: 'DARK' });
```

> **📖 For detailed migration examples and the complete new API guide, see [NEW_API_DESIGN.md](./NEW_API_DESIGN.md)**

## API

<docgen-index>

- [`initialize()`](#initialize)
- [`setSystemBarsStyle(...)`](#setsystembarsstyle)
- [`setStatusBarStyle(...)`](#setstatusbarstyle)
- [`setNavigationBarStyle(...)`](#setnavigationbarstyle)
- [`hideStatusBar()`](#hidestatusbar)
- [`showStatusBar()`](#showstatusbar)
- [`hideNavigationBar()`](#hidenavigationbar)
- [`showNavigationBar()`](#shownavigationbar)
- [`enterFullscreen(...)`](#enterfullscreen)
- [`exitFullscreen(...)`](#exitfullscreen)
- [`isFullscreenActive()`](#isfullscreenactive)
- [`forceExitFullscreen()`](#forceexitfullscreen)
- [`setOverlay(...)`](#setoverlay)
- [`getInsets()`](#getinsets)
- [`setStyle(...)`](#setstyle)
- [`hide()`](#hide)
- [`show()`](#show)
- [Interfaces](#interfaces)

</docgen-index>

<docgen-api>
<!--Update the source file JSDoc comments and rerun docgen to update the docs below-->

### initialize()

```typescript
initialize() => Promise<InitializeResult>
```

Initialize plugin and get device info

**Returns:** <code>Promise&lt;<a href="#initializeresult">InitializeResult</a>&gt;</code>

---

### setSystemBarsStyle(...)

```typescript
setSystemBarsStyle(options: SetSystemBarsStyleOptions) => Promise<void>
```

Set both status bar AND navigation bar style/color in one call
This is the recommended method for most use cases

| Param         | Type                                                                            |
| ------------- | ------------------------------------------------------------------------------- |
| **`options`** | <code><a href="#setsystembarsstyleoptions">SetSystemBarsStyleOptions</a></code> |

---

### setStatusBarStyle(...)

```typescript
setStatusBarStyle(options: SetStatusBarStyleOptions) => Promise<void>
```

Set ONLY status bar style and color

| Param         | Type                                                                          |
| ------------- | ----------------------------------------------------------------------------- |
| **`options`** | <code><a href="#setstatusbarstyleoptions">SetStatusBarStyleOptions</a></code> |

---

### setNavigationBarStyle(...)

```typescript
setNavigationBarStyle(options: SetNavigationBarStyleOptions) => Promise<void>
```

Set ONLY navigation bar style and color

| Param         | Type                                                                                  |
| ------------- | ------------------------------------------------------------------------------------- |
| **`options`** | <code><a href="#setnavigationbarstyleoptions">SetNavigationBarStyleOptions</a></code> |

---

### hideStatusBar()

```typescript
hideStatusBar() => Promise<void>
```

Hide status bar

---

### showStatusBar()

```typescript
showStatusBar() => Promise<void>
```

Show status bar

---

### hideNavigationBar()

```typescript
hideNavigationBar() => Promise<void>
```

Hide navigation bar

---

### showNavigationBar()

```typescript
showNavigationBar() => Promise<void>
```

Show navigation bar

---

### enterFullscreen(...)

```typescript
enterFullscreen(options: EnterFullscreenOptions) => Promise<void>
```

Enter fullscreen mode (hides both status and navigation bars)

| Param         | Type                                                                      |
| ------------- | ------------------------------------------------------------------------- |
| **`options`** | <code><a href="#enterfullscreenoptions">EnterFullscreenOptions</a></code> |

---

### exitFullscreen(...)

```typescript
exitFullscreen(options?: ExitFullscreenOptions | undefined) => Promise<void>
```

Exit fullscreen mode and restore system bars

| Param         | Type                                                                    |
| ------------- | ----------------------------------------------------------------------- |
| **`options`** | <code><a href="#exitfullscreenoptions">ExitFullscreenOptions</a></code> |

---

### isFullscreenActive()

```typescript
isFullscreenActive() => Promise<{ active: boolean; }>
```

Check if fullscreen mode is currently active

**Returns:** <code>Promise&lt;{ active: boolean; }&gt;</code>

---

### forceExitFullscreen()

```typescript
forceExitFullscreen() => Promise<void>
```

Force exit fullscreen mode (emergency fallback)

---

### setOverlay(...)

```typescript
setOverlay(options: SetOverlayOptions) => Promise<void>
```

Set overlay mode (Android 35+ only)

| Param         | Type                                                            |
| ------------- | --------------------------------------------------------------- |
| **`options`** | <code><a href="#setoverlayoptions">SetOverlayOptions</a></code> |

---

### getInsets()

```typescript
getInsets() => Promise<InsetsResult>
```

Get current window insets information

**Returns:** <code>Promise&lt;<a href="#insetsresult">InsetsResult</a>&gt;</code>

---

### setStyle(...)

```typescript
setStyle(options: SetStatusBarStyleOptions) => Promise<void>
```

| Param         | Type                                                                          |
| ------------- | ----------------------------------------------------------------------------- |
| **`options`** | <code><a href="#setstatusbarstyleoptions">SetStatusBarStyleOptions</a></code> |

---

### hide()

```typescript
hide() => Promise<void>
```

---

### show()

```typescript
show() => Promise<void>
```

---

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

#### SetSystemBarsStyleOptions

| Prop                | Type                                                                     | Description                                                                                                |
| ------------------- | ------------------------------------------------------------------------ | ---------------------------------------------------------------------------------------------------------- |
| **`statusBar`**     | <code>{ style?: 'LIGHT' \| 'DARK' \| 'DEFAULT'; color?: string; }</code> | Status bar configuration                                                                                   |
| **`navigationBar`** | <code>{ style?: 'LIGHT' \| 'DARK' \| 'DEFAULT'; color?: string; }</code> | Navigation bar configuration                                                                               |
| **`style`**         | <code>'LIGHT' \| 'DARK' \| 'DEFAULT'</code>                              | Apply same style to both bars (shorthand) If specified, overrides individual statusBar/navigationBar style |
| **`color`**         | <code>string</code>                                                      | Apply same color to both bars (shorthand) If specified, overrides individual statusBar/navigationBar color |

#### SetStatusBarStyleOptions

| Prop        | Type                                        | Description                                                    |
| ----------- | ------------------------------------------- | -------------------------------------------------------------- |
| **`style`** | <code>'LIGHT' \| 'DARK' \| 'DEFAULT'</code> | Status bar style                                               |
| **`color`** | <code>string</code>                         | Status bar background color (hex format: #RRGGBB or #AARRGGBB) |

#### SetNavigationBarStyleOptions

| Prop        | Type                                        | Description                                                        |
| ----------- | ------------------------------------------- | ------------------------------------------------------------------ |
| **`style`** | <code>'LIGHT' \| 'DARK' \| 'DEFAULT'</code> | Navigation bar style                                               |
| **`color`** | <code>string</code>                         | Navigation bar background color (hex format: #RRGGBB or #AARRGGBB) |

#### EnterFullscreenOptions

| Prop       | Type                               | Description          |
| ---------- | ---------------------------------- | -------------------- |
| **`mode`** | <code>'IMMERSIVE' \| 'LEAN'</code> | Fullscreen mode type |

#### ExitFullscreenOptions

| Prop          | Type                                                                                                                                                                                                                           | Description                                                                                                   |
| ------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ | ------------------------------------------------------------------------------------------------------------- |
| **`restore`** | <code>{ statusBar?: { style?: 'LIGHT' \| 'DARK' \| 'DEFAULT'; color?: string; }; navigationBar?: { style?: 'LIGHT' \| 'DARK' \| 'DEFAULT'; color?: string; }; style?: 'LIGHT' \| 'DARK' \| 'DEFAULT'; color?: string; }</code> | System bars configuration to restore after exiting fullscreen If not provided, will restore to system default |

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

</docgen-api>

## Troubleshooting

### Common Issues

1. **Content hidden under status bar (Android < 35)**
   - The plugin automatically applies webview padding
   - No manual padding needed

2. **Screen unlock resets system UI**
   - Plugin automatically handles lifecycle events
   - State is restored automatically

3. **Keyboard covering input fields**
   - **Recommended:** Don't specify `windowSoftInputMode` in AndroidManifest.xml (let Android use default)
   - Android will automatically handle keyboard positioning with `adjustResize` mode
   - **Advanced:** Use `adjustNothing` + `@capacitor/keyboard` plugin for manual control
   - See [KEYBOARD_MODE_EXPLANATION.md](./KEYBOARD_MODE_EXPLANATION.md) for details

4. **Modal headers/footers have extra padding (Android 35+)**
   - This is expected behavior with edge-to-edge mode
   - Fix by adding CSS to your `src/global.scss`:
     ```scss
     ion-modal,
     ion-popover {
       --ion-safe-area-top: 0 !important;
       --ion-safe-area-bottom: 0 !important;
       ion-header {
         padding-top: 0 !important;
       }
       ion-footer {
         padding-bottom: 0 !important;
       }
     }
     ```
   - See [ANDROID_35_MODAL_PADDING_FIX.md](./ANDROID_35_MODAL_PADDING_FIX.md) for complete solution

5. **AdMob banner positioning issues**
   - Plugin coordinates with system bar state changes
   - No additional configuration needed

6. **Using deprecated methods**
   - Old methods like `setStyle()`, `hide()`, `show()` still work but show deprecation warnings
   - Migrate to new unified API (`setSystemBarsStyle()`, `hideStatusBar()`, `showStatusBar()`) for better clarity

## Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md) for details.

## License

MIT © [Wael M.Elsaid](https://github.com/WMOH-DEV)
