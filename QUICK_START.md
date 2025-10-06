# Quick Start Guide

## 🚀 Installation

```bash
# Install from npm
npm install capacitor-android-system-bars

# Sync with Capacitor
npx cap sync
```

## 📱 Basic Setup

```typescript
import { AndroidSystemBars } from 'capacitor-android-system-bars';

// Initialize the plugin
const info = await AndroidSystemBars.initialize();
console.log('Device info:', info);

// Enable AdMob compatibility (recommended)
await AndroidSystemBars.setAdMobCompatibilityMode({ enabled: true });

// Set status bar style
const isDark = document.body.classList.contains('dark');
await AndroidSystemBars.setStyle({
  style: isDark ? 'DARK' : 'LIGHT',
  color: isDark ? '#111827' : '#ffffff'
});

// For Android 35+: Enable edge-to-edge
if (info.isAndroid35Plus) {
  await AndroidSystemBars.setOverlay({ overlay: true });
}
```

## 🎯 AdMob Integration

```typescript
// After ANY AdMob ad interaction:
await AndroidSystemBars.restoreSystemUIAfterAd({
  style: 'LIGHT',
  color: '#ffffff'
});
```

## 📖 Full Documentation

- [Complete API Reference](./README.md)
- [AdMob Integration Guide](./ADMOB_INTEGRATION.md)

## 🌟 Features

✅ Android API 21-35+ compatibility  
✅ Auto webview padding management  
✅ Fullscreen mode support  
✅ AdMob interference fixes  
✅ Edge-to-edge on Android 35+  
✅ Zero configuration required  

## 🆚 Replaces

- `@capawesome/capacitor-android-edge-to-edge-support`
- `@capacitor/status-bar`
- Custom fullscreen implementations

**Your app's system bars will now work perfectly across all Android versions!** 🎉