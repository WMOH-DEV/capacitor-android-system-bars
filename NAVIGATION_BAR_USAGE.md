# Navigation Bar Color Control - Usage Guide

This document explains how to control the navigation bar background color and icon styling using the `capacitor-android-system-bars` plugin.

## 🚨 Important Android 15+ Changes

**Android 15 (API 35) introduced breaking changes:**

- `setNavigationBarColor()` is **deprecated**
- Navigation bar color is **automatically transparent** and cannot be changed
- Apps should draw proper backgrounds behind `WindowInsets.Type.navigationBars()` instead

## API Support Matrix

| Android Version | API Level | Navigation Bar Color | Icon Styling | Notes                     |
| --------------- | --------- | -------------------- | ------------ | ------------------------- |
| 15+             | 35+       | ❌ Transparent only  | ✅ Yes       | Color control deprecated  |
| 11-14           | 30-34     | ✅ Yes               | ✅ Yes       | Full support              |
| 8.0-10          | 26-29     | ✅ Yes               | ✅ Yes       | Legacy implementation     |
| < 8.0           | < 26      | ❌ Not supported     | ❌ No        | No navigation bar styling |

## Basic Usage

### 1. Initialize the Plugin

```typescript
import { AndroidSystemBars } from 'capacitor-android-system-bars';

async function initializeSystemBars() {
  const info = await AndroidSystemBars.initialize();
  console.log('API Level:', info.apiLevel);
  console.log('Supports Edge-to-Edge:', info.supportsEdgeToEdge);
}
```

### 2. Set Navigation Bar Style (Android < 35)

```typescript
// Dark navigation bar with light icons
await AndroidSystemBars.setNavigationBarStyle({
  style: 'DARK',
  color: '#1f2937', // Dark gray
});

// Light navigation bar with dark icons
await AndroidSystemBars.setNavigationBarStyle({
  style: 'LIGHT',
  color: '#f3f4f6', // Light gray
});

// Transparent navigation bar (Android < 35)
await AndroidSystemBars.setNavigationBarStyle({
  style: 'LIGHT',
  color: '#00000000', // Fully transparent
});
```

### 3. Navigation Bar Icon Styling (All Supported Versions)

```typescript
// Light icons (for dark backgrounds)
await AndroidSystemBars.setNavigationBarStyle({
  style: 'DARK',
  // No color needed on Android 35+ - automatically transparent
});

// Dark icons (for light backgrounds)
await AndroidSystemBars.setNavigationBarStyle({
  style: 'LIGHT',
  // No color needed on Android 35+ - automatically transparent
});
```

### 4. Hide/Show Navigation Bar

```typescript
// Hide navigation bar (immersive mode)
await AndroidSystemBars.hideNavigationBar();

// Show navigation bar
await AndroidSystemBars.showNavigationBar();
```

## Complete Theme Implementation

```typescript
interface ThemeConfig {
  isDark: boolean;
  statusBarColor: string;
  navigationBarColor: string;
}

async function applyTheme(theme: ThemeConfig) {
  const info = await AndroidSystemBars.initialize();

  const style = theme.isDark ? 'DARK' : 'LIGHT';

  // Status bar
  await AndroidSystemBars.setStyle({
    style,
    color: info.isAndroid35Plus ? undefined : theme.statusBarColor,
  });

  // Navigation bar
  await AndroidSystemBars.setNavigationBarStyle({
    style,
    color: info.isAndroid35Plus ? undefined : theme.navigationBarColor,
  });
}

// Usage examples
const lightTheme: ThemeConfig = {
  isDark: false,
  statusBarColor: '#ffffff',
  navigationBarColor: '#ffffff',
};

const darkTheme: ThemeConfig = {
  isDark: true,
  statusBarColor: '#1f2937',
  navigationBarColor: '#1f2937',
};

// Apply themes
await applyTheme(lightTheme);
await applyTheme(darkTheme);
```

## Android 15+ Edge-to-Edge Approach

For Android 15+, you should design your UI to work with transparent system bars:

```typescript
async function handleAndroid15Plus() {
  const info = await AndroidSystemBars.initialize();

  if (info.isAndroid35Plus) {
    // Android 15+: System bars are automatically transparent
    // Design your UI to extend behind the navigation bar

    // Only control icon styling
    await AndroidSystemBars.setNavigationBarStyle({
      style: 'LIGHT', // or 'DARK' based on your content
    });

    // Use WindowInsets to handle overlaps in your UI
    const insets = await AndroidSystemBars.getInsets();
    console.log('Navigation bar area:', insets.bottom);
  }
}
```

## CSS Integration for Edge-to-Edge

```css
/* For Android 15+ edge-to-edge design */
.content-with-navigation-padding {
  /* Add padding to avoid navigation bar overlap */
  padding-bottom: env(safe-area-inset-bottom, 0px);
}

.navigation-bar-background {
  /* Create your own navigation bar background */
  background: linear-gradient(to top, rgba(255, 255, 255, 0.9) 0%, transparent 100%);
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  height: calc(env(safe-area-inset-bottom, 0px) + 20px);
  pointer-events: none;
}
```

## Migration Guide

### From Community Plugins

If you're migrating from other system bar plugins:

```typescript
// Old approach (deprecated on Android 15+)
// await StatusBar.setBackgroundColor({ color: '#1f2937' });

// New approach (handles Android 15+ properly)
await AndroidSystemBars.setNavigationBarStyle({
  style: 'DARK',
  color: '#1f2937', // Ignored on Android 15+
});
```

### Handling Different Android Versions

```typescript
async function setNavigationBarSafely(color: string, isDark: boolean) {
  const info = await AndroidSystemBars.initialize();

  if (info.apiLevel >= 35) {
    // Android 15+: Only set icon style
    await AndroidSystemBars.setNavigationBarStyle({
      style: isDark ? 'DARK' : 'LIGHT',
    });
    console.log('Navigation bar color is automatically transparent on Android 15+');
  } else if (info.apiLevel >= 26) {
    // Android 8.0-14: Full control
    await AndroidSystemBars.setNavigationBarStyle({
      style: isDark ? 'DARK' : 'LIGHT',
      color: color,
    });
  } else {
    console.log('Navigation bar styling not supported on this Android version');
  }
}
```

## Best Practices

### 1. Check API Support First

```typescript
const info = await AndroidSystemBars.initialize();
if (info.apiLevel < 26) {
  console.warn('Navigation bar styling not supported');
  return;
}
```

### 2. Design for Transparency

```typescript
// Always design assuming transparent navigation bar on Android 15+
if (info.isAndroid35Plus) {
  // Your UI should work with transparent navigation bar
  setupEdgeToEdgeLayout();
}
```

### 3. Consistent Color Schemes

```typescript
// Keep status bar and navigation bar colors consistent
const style = isDark ? 'DARK' : 'LIGHT';
const color = isDark ? '#1f2937' : '#ffffff';

await AndroidSystemBars.setStyle({ style, color });
await AndroidSystemBars.setNavigationBarStyle({ style, color });
```

### 4. Handle Lifecycle Events

```typescript
// Re-apply navigation bar styling after app resume
document.addEventListener('resume', async () => {
  await AndroidSystemBars.setNavigationBarStyle({
    style: currentTheme.isDark ? 'DARK' : 'LIGHT',
    color: currentTheme.navigationBarColor,
  });
});
```

## Troubleshooting

### Navigation Bar Color Not Changing

**Problem:** Navigation bar color doesn't change on newer Android versions.

**Solution:** Check if you're running Android 15+:

```typescript
const info = await AndroidSystemBars.initialize();
if (info.isAndroid35Plus) {
  console.log('Navigation bar color control is deprecated on Android 15+');
  // Design for transparent navigation bar instead
}
```

### Icon Colors Wrong

**Problem:** Navigation bar icons don't match your background.

**Solution:** Use the correct style:

```typescript
// For dark backgrounds, use light icons
await AndroidSystemBars.setNavigationBarStyle({ style: 'DARK' });

// For light backgrounds, use dark icons
await AndroidSystemBars.setNavigationBarStyle({ style: 'LIGHT' });
```

### Android Version Compatibility

**Problem:** Crashes on older Android versions.

**Solution:** Check API level support:

```typescript
const info = await AndroidSystemBars.initialize();
if (info.apiLevel >= 26) {
  // Safe to use navigation bar styling
  await AndroidSystemBars.setNavigationBarStyle({
    style: 'LIGHT',
    color: '#ffffff',
  });
}
```

## Complete Example

```typescript
import { AndroidSystemBars } from 'capacitor-android-system-bars';

class SystemBarManager {
  private currentTheme: 'light' | 'dark' = 'light';
  private apiInfo: any;

  async initialize() {
    this.apiInfo = await AndroidSystemBars.initialize();
    console.log(`Android API ${this.apiInfo.apiLevel}`);

    if (this.apiInfo.isAndroid35Plus) {
      console.log('🎉 Edge-to-edge mode enabled automatically');
    }
  }

  async setTheme(theme: 'light' | 'dark') {
    this.currentTheme = theme;

    const isDark = theme === 'dark';
    const style = isDark ? 'DARK' : 'LIGHT';

    // Colors for Android < 35
    const statusBarColor = isDark ? '#1f2937' : '#ffffff';
    const navBarColor = isDark ? '#1f2937' : '#ffffff';

    try {
      // Status bar
      await AndroidSystemBars.setStyle({
        style,
        color: this.apiInfo.isAndroid35Plus ? undefined : statusBarColor,
      });

      // Navigation bar
      if (this.apiInfo.apiLevel >= 26) {
        await AndroidSystemBars.setNavigationBarStyle({
          style,
          color: this.apiInfo.isAndroid35Plus ? undefined : navBarColor,
        });
      }
    } catch (error) {
      console.error('Failed to apply theme:', error);
    }
  }

  async enterFullscreen() {
    try {
      await AndroidSystemBars.enterFullscreen({ mode: 'IMMERSIVE' });
    } catch (error) {
      console.error('Failed to enter fullscreen:', error);
    }
  }

  async exitFullscreen() {
    try {
      const style = this.currentTheme === 'dark' ? 'DARK' : 'LIGHT';
      await AndroidSystemBars.exitFullscreen({ style });
    } catch (error) {
      console.error('Failed to exit fullscreen:', error);
    }
  }
}

// Usage
const systemBars = new SystemBarManager();
await systemBars.initialize();
await systemBars.setTheme('dark');
```

## Summary

The navigation bar color control feature provides:

✅ **Full color control** on Android 8.0-14  
✅ **Icon styling** on all supported versions  
✅ **Automatic transparency** on Android 15+ (following new guidelines)  
✅ **Edge-to-edge compatibility** across all Android versions  
✅ **Backward compatibility** with proper fallbacks

The plugin automatically handles the Android 15+ deprecation of `setNavigationBarColor()` while maintaining full functionality on older versions.
