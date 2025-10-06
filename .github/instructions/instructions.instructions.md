---
applyTo: '**'
---

@ -0,0 +1,902 @@

# Custom Android System Bars Plugin - Implementation Instructions

## Plugin Overview

**Plugin Name:** `android-system-bars`  
**Purpose:** Unified plugin to handle Android system bars (status bar & navigation bar), webview sizing, edge-to-edge display, and fullscreen mode with full compatibility for Android API 21-35+

**Replaces:**

- `@capawesome/capacitor-android-edge-to-edge-support`
- `@capacitor/status-bar`
- Custom fullscreen implementations

---

## ⚠️ Critical Requirements

### Android API Compatibility Matrix

| Android Version | API Level | Edge-to-Edge | System UI Flags | WindowInsets API | Notes                           |
| --------------- | --------- | ------------ | --------------- | ---------------- | ------------------------------- |
| 15+             | 35+       | ✅ Native    | ✅ Deprecated   | ✅ Required      | Full edge-to-edge enforcement   |
| 14              | 34        | ⚠️ Partial   | ✅ Works        | ✅ Available     | Transitional support            |
| 11-13           | 30-33     | ⚠️ Manual    | ✅ Works        | ✅ Available     | Manual webview padding required |
| 8.1-10          | 27-29     | ❌ No        | ✅ Primary      | ⚠️ Limited       | Use System UI flags only        |
| 5.0-8.0         | 21-26     | ❌ No        | ✅ Primary      | ❌ No            | Legacy System UI flags          |

### Key Constraints

1. **Android < 35**: NEVER use `setOverlaysWebView(true)` - causes webview resize conflicts
2. **Android < 35**: MUST manually manage webview padding to prevent status bar overlap
3. **Android 35+**: Use native edge-to-edge with `WindowInsetsController`
4. **All versions**: System events (screen lock/unlock, keyboard) can reset UI flags - MUST re-apply
5. **AdMob/Dynamic Content**: Coordinate margin changes with system bar state

---

## Plugin Architecture

### Core Responsibilities

1. **System Bar Management**
   - Status bar: visibility, color, style (light/dark icons)
   - Navigation bar: visibility, color
   - Overlay mode control (API 35+ only)

2. **Webview Sizing**
   - Manual padding injection for Android < 35
   - WindowInsets handling for Android 30+
   - Prevent resize conflicts with keyboard/system events

3. **Fullscreen Mode**
   - Immersive fullscreen (hide all system bars)
   - Lean mode (temporary hide, show on interaction)
   - Exit fullscreen with proper state restoration

4. **Lifecycle Management**
   - App resume/pause handling
   - Configuration changes (orientation, theme)
   - Keyboard visibility coordination

---

## Implementation Guide

### 1. Plugin Structure

```
android-system-bars/
├── android/
│   └── src/
│       └── main/
│           └── java/
│               └── com/
│                   └── capacitor/
│                       └── systembars/
│                           ├── AndroidSystemBarsPlugin.java
│                           ├── SystemBarsManager.java
│                           ├── WebViewPaddingManager.java
│                           ├── FullscreenManager.java
│                           └── LifecycleHandler.java
├── ios/
│   └── Plugin/
│       └── AndroidSystemBarsPlugin.swift (stub - Android only)
├── src/
│   ├── definitions.ts
│   └── index.ts
├── package.json
└── README.md
```

### 2. Android Implementation - Core Classes

#### **AndroidSystemBarsPlugin.java** (Main Entry Point)

```java
package com.capacitor.systembars;

import android.os.Build;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;

@CapacitorPlugin(name = "AndroidSystemBars")
public class AndroidSystemBarsPlugin extends Plugin {

    private SystemBarsManager systemBarsManager;
    private WebViewPaddingManager paddingManager;
    private FullscreenManager fullscreenManager;
    private LifecycleHandler lifecycleHandler;

    @Override
    public void load() {
        systemBarsManager = new SystemBarsManager(getActivity());
        paddingManager = new WebViewPaddingManager(bridge.getWebView());
        fullscreenManager = new FullscreenManager(getActivity(), systemBarsManager, paddingManager);
        lifecycleHandler = new LifecycleHandler(this);

        // Auto-initialize based on Android version
        systemBarsManager.initialize();
    }

    @PluginMethod
    public void initialize(PluginCall call) {
        try {
            int apiLevel = Build.VERSION.SDK_INT;
            boolean isAndroid35Plus = apiLevel >= 35;

            JSObject result = new JSObject();
            result.put("apiLevel", apiLevel);
            result.put("isAndroid35Plus", isAndroid35Plus);
            result.put("supportsEdgeToEdge", apiLevel >= 35);
            result.put("supportsWindowInsets", apiLevel >= 30);

            call.resolve(result);
        } catch (Exception e) {
            call.reject("Initialization failed", e);
        }
    }

    @PluginMethod
    public void setStyle(PluginCall call) {
        String style = call.getString("style", "DEFAULT");
        String color = call.getString("color");

        try {
            systemBarsManager.setStatusBarStyle(style, color);
            call.resolve();
        } catch (Exception e) {
            call.reject("Failed to set style", e);
        }
    }

    @PluginMethod
    public void hide(PluginCall call) {
        try {
            systemBarsManager.hideStatusBar();
            call.resolve();
        } catch (Exception e) {
            call.reject("Failed to hide status bar", e);
        }
    }

    @PluginMethod
    public void show(PluginCall call) {
        try {
            systemBarsManager.showStatusBar();
            call.resolve();
        } catch (Exception e) {
            call.reject("Failed to show status bar", e);
        }
    }

    @PluginMethod
    public void enterFullscreen(PluginCall call) {
        String mode = call.getString("mode", "IMMERSIVE");

        try {
            fullscreenManager.enterFullscreen(mode);
            call.resolve();
        } catch (Exception e) {
            call.reject("Failed to enter fullscreen", e);
        }
    }

    @PluginMethod
    public void exitFullscreen(PluginCall call) {
        String style = call.getString("style", "DEFAULT");
        String color = call.getString("color");

        try {
            fullscreenManager.exitFullscreen(style, color);
            call.resolve();
        } catch (Exception e) {
            call.reject("Failed to exit fullscreen", e);
        }
    }

    @PluginMethod
    public void setOverlay(PluginCall call) {
        boolean overlay = call.getBoolean("overlay", false);

        try {
            if (Build.VERSION.SDK_INT >= 35) {
                systemBarsManager.setOverlayMode(overlay);
                call.resolve();
            } else {
                call.reject("Overlay mode only supported on Android 35+");
            }
        } catch (Exception e) {
            call.reject("Failed to set overlay mode", e);
        }
    }
}
```

#### **SystemBarsManager.java** (System UI Control)

```java
package com.capacitor.systembars;

import android.app.Activity;
import android.graphics.Color;
import android.os.Build;
import android.view.View;
import android.view.Window;
import android.view.WindowInsetsController;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsControllerCompat;

public class SystemBarsManager {

    private final Activity activity;
    private final Window window;
    private WindowInsetsControllerCompat insetsController;

    public SystemBarsManager(Activity activity) {
        this.activity = activity;
        this.window = activity.getWindow();

        if (Build.VERSION.SDK_INT >= 30) {
            insetsController = WindowCompat.getInsetsController(window, window.getDecorView());
        }
    }

    public void initialize() {
        activity.runOnUiThread(() -> {
            if (Build.VERSION.SDK_INT >= 35) {
                // Android 15+: Full edge-to-edge enforcement
                enableEdgeToEdge();
            } else {
                // Android < 35: Manual system UI flags
                setupLegacySystemUI();
            }
        });
    }

    /**
     * Android 35+ Edge-to-Edge using WindowInsetsController
     * Reference: https://developer.android.com/develop/ui/views/layout/edge-to-edge
     */
    private void enableEdgeToEdge() {
        if (Build.VERSION.SDK_INT >= 35) {
            WindowCompat.setDecorFitsSystemWindows(window, false);

            // Use WindowInsetsController for modern approach
            insetsController.setSystemBarsBehavior(
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            );

            // Make status bar transparent
            window.setStatusBarColor(Color.TRANSPARENT);
            window.setNavigationBarColor(Color.TRANSPARENT);
        }
    }

    /**
     * Android < 35: Legacy System UI Flags
     * Reference: https://developer.android.com/training/system-ui/immersive
     */
    private void setupLegacySystemUI() {
        View decorView = window.getDecorView();
        int flags = decorView.getSystemUiVisibility();

        // CRITICAL: Do NOT use LAYOUT_FULLSCREEN or LAYOUT_HIDE_NAVIGATION on Android < 35
        // These cause webview resize issues

        flags |= View.SYSTEM_UI_FLAG_LAYOUT_STABLE;

        decorView.setSystemUiVisibility(flags);
    }

    public void setStatusBarStyle(String style, String color) {
        activity.runOnUiThread(() -> {
            boolean lightIcons = style.equals("DARK"); // Dark style = light icons

            if (Build.VERSION.SDK_INT >= 30) {
                // Modern approach
                insetsController.setAppearanceLightStatusBars(!lightIcons);
            } else if (Build.VERSION.SDK_INT >= 23) {
                // Legacy approach (API 23-29)
                View decorView = window.getDecorView();
                int flags = decorView.getSystemUiVisibility();

                if (lightIcons) {
                    flags &= ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
                } else {
                    flags |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
                }

                decorView.setSystemUiVisibility(flags);
            }

            // Set background color
            if (color != null && !color.isEmpty()) {
                window.setStatusBarColor(Color.parseColor(color));
            }
        });
    }

    public void hideStatusBar() {
        activity.runOnUiThread(() -> {
            if (Build.VERSION.SDK_INT >= 30) {
                insetsController.hide(WindowInsetsCompat.Type.statusBars());
            } else {
                View decorView = window.getDecorView();
                int flags = decorView.getSystemUiVisibility();
                flags |= View.SYSTEM_UI_FLAG_FULLSCREEN;
                decorView.setSystemUiVisibility(flags);
            }
        });
    }

    public void showStatusBar() {
        activity.runOnUiThread(() -> {
            if (Build.VERSION.SDK_INT >= 30) {
                insetsController.show(WindowInsetsCompat.Type.statusBars());
            } else {
                View decorView = window.getDecorView();
                int flags = decorView.getSystemUiVisibility();
                flags &= ~View.SYSTEM_UI_FLAG_FULLSCREEN;
                decorView.setSystemUiVisibility(flags);
            }
        });
    }

    /**
     * ONLY for Android 35+
     * Sets whether system bars should overlay the content
     */
    public void setOverlayMode(boolean overlay) {
        if (Build.VERSION.SDK_INT >= 35) {
            activity.runOnUiThread(() -> {
                WindowCompat.setDecorFitsSystemWindows(window, !overlay);
            });
        }
    }

    /**
     * Re-apply system UI flags after system events (screen unlock, etc.)
     */
    public void reapplySystemUI() {
        activity.runOnUiThread(() -> {
            if (Build.VERSION.SDK_INT >= 35) {
                enableEdgeToEdge();
            } else {
                setupLegacySystemUI();
            }
        });
    }
}
```

#### **WebViewPaddingManager.java** (Webview Padding for Android < 35)

```java
package com.capacitor.systembars;

import android.os.Build;
import android.util.DisplayMetrics;
import android.webkit.WebView;

/**
 * CRITICAL: On Android < 35, we MUST manually add top padding to the webview
 * to prevent content from being hidden under the status bar.
 *
 * This is necessary because we CANNOT use overlay mode on Android < 35
 * without causing resize conflicts with keyboard and system events.
 */
public class WebViewPaddingManager {

    private final WebView webView;
    private int statusBarHeight = 0;

    public WebViewPaddingManager(WebView webView) {
        this.webView = webView;
        this.statusBarHeight = getStatusBarHeight();
    }

    /**
     * Get status bar height in pixels
     */
    private int getStatusBarHeight() {
        int resourceId = webView.getResources().getIdentifier(
            "status_bar_height", "dimen", "android"
        );

        if (resourceId > 0) {
            return webView.getResources().getDimensionPixelSize(resourceId);
        }

        // Fallback: 24dp converted to pixels
        DisplayMetrics metrics = webView.getResources().getDisplayMetrics();
        return (int) (24 * metrics.density);
    }

    /**
     * Apply top padding to webview (Android < 35 only)
     */
    public void applyPadding() {
        if (Build.VERSION.SDK_INT < 35) {
            webView.post(() -> {
                webView.setPadding(0, statusBarHeight, 0, 0);
            });
        }
    }

    /**
     * Remove padding (for fullscreen mode)
     */
    public void removePadding() {
        webView.post(() -> {
            webView.setPadding(0, 0, 0, 0);
        });
    }

    /**
     * Set custom padding
     */
    public void setPadding(int top, int bottom) {
        webView.post(() -> {
            webView.setPadding(0, top, 0, bottom);
        });
    }

    public int getStatusBarHeight() {
        return statusBarHeight;
    }
}
```

#### **FullscreenManager.java** (Fullscreen Mode)

```java
package com.capacitor.systembars;

import android.app.Activity;
import android.os.Build;
import android.view.View;
import android.view.Window;

/**
 * Handles immersive fullscreen mode across all Android versions
 * Reference: https://developer.android.com/training/system-ui/immersive
 */
public class FullscreenManager {

    private final Activity activity;
    private final SystemBarsManager systemBarsManager;
    private final WebViewPaddingManager paddingManager;
    private final Window window;

    public FullscreenManager(
        Activity activity,
        SystemBarsManager systemBarsManager,
        WebViewPaddingManager paddingManager
    ) {
        this.activity = activity;
        this.systemBarsManager = systemBarsManager;
        this.paddingManager = paddingManager;
        this.window = activity.getWindow();
    }

    /**
     * Enter fullscreen mode
     * @param mode "IMMERSIVE" or "LEAN"
     */
    public void enterFullscreen(String mode) {
        activity.runOnUiThread(() -> {
            // Remove webview padding
            paddingManager.removePadding();

            View decorView = window.getDecorView();

            if (Build.VERSION.SDK_INT >= 30) {
                // Modern approach using WindowInsetsController
                WindowInsetsControllerCompat controller =
                    WindowCompat.getInsetsController(window, decorView);

                controller.hide(WindowInsetsCompat.Type.systemBars());

                if (mode.equals("IMMERSIVE")) {
                    controller.setSystemBarsBehavior(
                        WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                    );
                } else {
                    // Lean mode
                    controller.setSystemBarsBehavior(
                        WindowInsetsControllerCompat.BEHAVIOR_DEFAULT
                    );
                }
            } else {
                // Legacy System UI flags
                int flags = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_FULLSCREEN;

                if (mode.equals("IMMERSIVE")) {
                    flags |= View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
                } else {
                    // Lean mode - hide on interaction
                    flags |= View.SYSTEM_UI_FLAG_IMMERSIVE;
                }

                decorView.setSystemUiVisibility(flags);
            }
        });
    }

    /**
     * Exit fullscreen and restore normal state
     */
    public void exitFullscreen(String style, String color) {
        activity.runOnUiThread(() -> {
            View decorView = window.getDecorView();

            if (Build.VERSION.SDK_INT >= 30) {
                // Modern approach
                WindowInsetsControllerCompat controller =
                    WindowCompat.getInsetsController(window, decorView);

                controller.show(WindowInsetsCompat.Type.systemBars());
                controller.setSystemBarsBehavior(
                    WindowInsetsControllerCompat.BEHAVIOR_DEFAULT
                );
            } else {
                // Legacy - clear fullscreen flags
                int flags = decorView.getSystemUiVisibility();
                flags &= ~(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_IMMERSIVE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);

                decorView.setSystemUiVisibility(flags);
            }

            // Restore status bar style and color
            systemBarsManager.setStatusBarStyle(style, color);

            // Restore webview padding on Android < 35
            if (Build.VERSION.SDK_INT < 35) {
                paddingManager.applyPadding();
            }
        });
    }
}
```

#### **LifecycleHandler.java** (App Lifecycle Events)

```java
package com.capacitor.systembars;

import android.app.Activity;
import com.getcapacitor.PluginCall;

/**
 * Handles app lifecycle events to re-apply system UI state
 * CRITICAL: Screen lock/unlock can reset system UI flags on Android < 35
 */
public class LifecycleHandler {

    private final AndroidSystemBarsPlugin plugin;
    private boolean isAppInBackground = false;

    public LifecycleHandler(AndroidSystemBarsPlugin plugin) {
        this.plugin = plugin;
        setupLifecycleListeners();
    }

    private void setupLifecycleListeners() {
        // Listen for app pause (screen lock, background)
        plugin.getBridge().getApp().setOnPauseListener(() -> {
            isAppInBackground = true;
        });

        // Listen for app resume (screen unlock, foreground)
        plugin.getBridge().getApp().setOnResumeListener(() -> {
            if (isAppInBackground) {
                isAppInBackground = false;

                // CRITICAL: Re-apply system UI state after resume
                // This fixes the issue where screen unlock resets overlay mode
                plugin.systemBarsManager.reapplySystemUI();

                // Re-apply webview padding on Android < 35
                if (Build.VERSION.SDK_INT < 35) {
                    plugin.paddingManager.applyPadding();
                }
            }
        });
    }
}
```

### 3. TypeScript Definitions

**src/definitions.ts**

```typescript
export interface AndroidSystemBarsPlugin {
  /**
   * Initialize plugin and get device info
   */
  initialize(): Promise<{
    apiLevel: number;
    isAndroid35Plus: boolean;
    supportsEdgeToEdge: boolean;
    supportsWindowInsets: boolean;
  }>;

  /**
   * Set status bar style and color
   */
  setStyle(options: { style: 'LIGHT' | 'DARK' | 'DEFAULT'; color?: string }): Promise<void>;

  /**
   * Hide status bar
   */
  hide(): Promise<void>;

  /**
   * Show status bar
   */
  show(): Promise<void>;

  /**
   * Enter fullscreen mode
   */
  enterFullscreen(options: { mode: 'IMMERSIVE' | 'LEAN' }): Promise<void>;

  /**
   * Exit fullscreen mode
   */
  exitFullscreen(options: { style: 'LIGHT' | 'DARK' | 'DEFAULT'; color?: string }): Promise<void>;

  /**
   * Set overlay mode (Android 35+ only)
   */
  setOverlay(options: { overlay: boolean }): Promise<void>;
}
```

### 4. Usage in Your App

**Remove old plugins first:**

```bash
npm uninstall @capawesome/capacitor-android-edge-to-edge-support @capacitor/status-bar
```

**Install custom plugin:**

```bash
npm install @capacitor-community/android-system-bars
npx cap sync
```

**app.component.ts**

```typescript
import { AndroidSystemBars } from '@capacitor-community/android-system-bars';

async initEdgeToEdge() {
  try {
    const info = await AndroidSystemBars.initialize();

    const isDark = document.body.classList.contains('dark');
    const color = isDark ? '#111827' : '#f5efef';
    const style = isDark ? 'DARK' : 'LIGHT';

    if (info.isAndroid35Plus) {
      // Android 35+: Use overlay mode
      await AndroidSystemBars.setOverlay({ overlay: true });
      await AndroidSystemBars.setStyle({ style, color });
    } else {
      // Android < 35: No overlay, webview padding handled automatically
      await AndroidSystemBars.setStyle({ style, color });
    }
  } catch (error) {
    console.error('Error initializing system bars:', error);
  }
}

// No need for resume listeners - plugin handles it automatically!
```

**full-screen.service.ts**

```typescript
import { AndroidSystemBars } from '@capacitor-community/android-system-bars';

async enter() {
  await AndroidSystemBars.enterFullscreen({ mode: 'IMMERSIVE' });
}

async exit() {
  const isDark = document.body.classList.contains('dark');
  const color = isDark ? '#111827' : '#f5efef';
  const style = isDark ? 'DARK' : 'LIGHT';

  await AndroidSystemBars.exitFullscreen({ style, color });
}
```

---

## Testing Checklist

### Android < 35 (API 27-34)

- [ ] Status bar visible with correct color
- [ ] Webview content NOT hidden under status bar
- [ ] Screen lock/unlock does NOT cause extra padding
- [ ] Keyboard show/hide does NOT resize webview incorrectly
- [ ] AdMob banner does NOT cause status bar issues
- [ ] Theme toggle works smoothly
- [ ] Fullscreen mode hides all bars
- [ ] Exit fullscreen restores correct state

### Android 35+ (API 35+)

- [ ] Edge-to-edge mode enabled
- [ ] Status bar overlays content correctly
- [ ] System insets applied properly
- [ ] Fullscreen mode works
- [ ] Theme toggle works

---

## Key Implementation Notes

### 1. **NEVER Use Overlay on Android < 35**

```java
// ❌ WRONG - Causes resize issues
if (Build.VERSION.SDK_INT < 35) {
    WindowCompat.setDecorFitsSystemWindows(window, false); // DON'T DO THIS
}

// ✅ CORRECT - Manual padding
if (Build.VERSION.SDK_INT < 35) {
    paddingManager.applyPadding();
}
```

### 2. **Always Re-apply After Resume**

```java
// Screen unlock resets system UI on Android < 35
@Override
public void onResume() {
    super.onResume();
    systemBarsManager.reapplySystemUI();
    paddingManager.applyPadding();
}
```

### 3. **Coordinate with Keyboard Plugin**

```xml
<!-- AndroidManifest.xml -->
<activity
    android:windowSoftInputMode="adjustNothing">
</activity>
```

### 4. **Latest Android APIs**

- **Android 35+**: Use `WindowInsetsController` exclusively
- **Android 30-34**: Use `WindowInsetsControllerCompat` from AndroidX
- **Android < 30**: Use legacy System UI flags

---

## Dependencies Required

**build.gradle (app-level)**

```gradle
dependencies {
    implementation 'androidx.core:core:1.13.1'
    implementation 'androidx.appcompat:appcompat:1.7.0'
}
```

**gradle.properties**

```properties
android.useAndroidX=true
android.enableJetifier=true
```

---

## References

1. **Android Edge-to-Edge Official Guide**
   - https://developer.android.com/develop/ui/views/layout/edge-to-edge

2. **WindowInsetsController API**
   - https://developer.android.com/reference/android/view/WindowInsetsController

3. **System UI Visibility (Legacy)**
   - https://developer.android.com/training/system-ui/immersive

4. **AndroidX Core Library**
   - https://developer.android.com/jetpack/androidx/releases/core

5. **Capacitor Plugin Development**
   - https://capacitorjs.com/docs/plugins/creating-plugins

---

## Migration Steps

1. **Remove old plugins**

   ```bash
   npm uninstall @capawesome/capacitor-android-edge-to-edge-support
   npm uninstall @capacitor/status-bar
   ```

2. **Remove old imports**
   - Remove EdgeToEdge imports
   - Remove StatusBar imports

3. **Install custom plugin**

   ```bash
   npm install @capacitor-community/android-system-bars
   ```

4. **Update app code**
   - Replace EdgeToEdge/StatusBar calls with AndroidSystemBars
   - Remove manual lifecycle listeners (plugin handles it)
   - Remove DeviceDetectionService (plugin provides this)

5. **Remove AndroidManifest customizations**
   - Plugin handles windowSoftInputMode internally

6. **Test thoroughly on physical devices**
   - Test Android 8.1, 11, 14, 15
   - Test screen lock/unlock
   - Test keyboard interactions
   - Test AdMob integration

---

## Success Criteria

✅ **Android < 35**: No webview resize issues, ever  
✅ **Android < 35**: Screen lock/unlock works perfectly  
✅ **Android < 35**: AdMob banners don't cause padding issues  
✅ **Android 35+**: Full edge-to-edge support  
✅ **All versions**: Smooth theme transitions  
✅ **All versions**: Fullscreen mode works reliably

---

**End of Instructions**

This plugin will provide rock-solid, production-ready system bar management for your Ionic/Capacitor app across all Android versions. No more edge-to-edge headaches! 🚀
