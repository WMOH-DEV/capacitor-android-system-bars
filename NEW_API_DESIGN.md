# 🚀 New Professional API Design

## ✨ What Changed?

The API has been completely redesigned to be more intuitive, professional, and reduce code complexity.

### ❌ Old API Problems

```typescript
// Confusing: setStyle() - what does it set?
await AndroidSystemBars.setStyle({ style: 'DARK', color: '#000' });

// Need TWO calls to set both bars
await AndroidSystemBars.setNavigationBarStyle({ style: 'DARK', color: '#000' });

// Confusing exitFullscreen parameters
await AndroidSystemBars.exitFullscreen({
  style: 'LIGHT', // For what? Status? Navigation? Both?
  color: '#fff', // Same confusion
});
```

### ✅ New API Benefits

```typescript
// Crystal clear: Set BOTH bars in ONE call
await AndroidSystemBars.setSystemBarsStyle({
  style: 'DARK',
  color: '#000',
});

// Clear fullscreen exit - explicitly specify what to restore
await AndroidSystemBars.exitFullscreen({
  restore: {
    style: 'LIGHT', // Clear: applies to both bars
    color: '#ffffff', // Clear: applies to both bars
  },
});

// Precise individual control when needed
await AndroidSystemBars.setStatusBarStyle({ style: 'DARK', color: '#000' });
```

---

## 🎯 API Overview

### 1. **Unified System Bars Control** (Recommended)

The new `setSystemBarsStyle()` method handles both status and navigation bars intelligently:

```typescript
// Shorthand: Same style/color for both bars
await AndroidSystemBars.setSystemBarsStyle({
  style: 'DARK',
  color: '#111827',
});

// Individual control: Different styles for each bar
await AndroidSystemBars.setSystemBarsStyle({
  statusBar: { style: 'LIGHT', color: '#ffffff' },
  navigationBar: { style: 'DARK', color: '#000000' },
});

// Mixed: Shorthand + individual override
await AndroidSystemBars.setSystemBarsStyle({
  style: 'DARK', // Applies to both bars
  statusBar: {
    color: '#ef4444', // But status bar gets red color
  },
});
```

### 2. **Individual Bar Control** (When Needed)

For precise control of individual bars:

```typescript
// Control ONLY status bar
await AndroidSystemBars.setStatusBarStyle({
  style: 'DARK',
  color: '#111827',
});

// Control ONLY navigation bar
await AndroidSystemBars.setNavigationBarStyle({
  style: 'LIGHT',
  color: '#ffffff',
});
```

### 3. **Clear Visibility Methods**

No more ambiguous `hide()` and `show()`:

```typescript
// Crystal clear what you're hiding/showing
await AndroidSystemBars.hideStatusBar();
await AndroidSystemBars.showStatusBar();

await AndroidSystemBars.hideNavigationBar();
await AndroidSystemBars.showNavigationBar();
```

### 4. **Professional Fullscreen API**

Clear intent for fullscreen restoration:

```typescript
// Enter fullscreen (same as before)
await AndroidSystemBars.enterFullscreen({ mode: 'IMMERSIVE' });

// Exit with explicit restoration config
await AndroidSystemBars.exitFullscreen({
  restore: {
    // Option 1: Same for both bars
    style: 'LIGHT',
    color: '#ffffff',
  },
});

// Or restore with individual control
await AndroidSystemBars.exitFullscreen({
  restore: {
    statusBar: { style: 'LIGHT', color: '#ffffff' },
    navigationBar: { style: 'DARK', color: '#000000' },
  },
});

// Or just exit to system default (no restore config)
await AndroidSystemBars.exitFullscreen();
```

---

## 📊 Migration Examples

### Theme Switching

**Before (2 API calls):**

```typescript
const style = theme === 'dark' ? 'DARK' : 'LIGHT';
const color = theme === 'dark' ? '#111827' : '#ffffff';

await AndroidSystemBars.setStyle({ style, color }); // Status bar
await AndroidSystemBars.setNavigationBarStyle({ style, color }); // Navigation bar
```

**After (1 API call):**

```typescript
const style = theme === 'dark' ? 'DARK' : 'LIGHT';
const color = theme === 'dark' ? '#111827' : '#ffffff';

await AndroidSystemBars.setSystemBarsStyle({ style, color }); // Both bars!
```

### Fullscreen Mode

**Before (confusing):**

```typescript
await AndroidSystemBars.exitFullscreen({
  style: 'LIGHT', // What does this apply to?
  color: '#fff', // Status? Navigation? Both?
});
```

**After (crystal clear):**

```typescript
await AndroidSystemBars.exitFullscreen({
  restore: {
    style: 'LIGHT', // Clearly applies to both
    color: '#ffffff', // Clearly applies to both
  },
});
```

### Individual Bar Control

**Before (unclear naming):**

```typescript
await AndroidSystemBars.setStyle({ style: 'DARK' }); // What bar?
await AndroidSystemBars.setNavigationBarStyle({ style: 'DARK' }); // Clear
```

**After (consistent naming):**

```typescript
await AndroidSystemBars.setStatusBarStyle({ style: 'DARK' }); // Clear
await AndroidSystemBars.setNavigationBarStyle({ style: 'DARK' }); // Clear
```

---

## 🎨 Real-World Usage Patterns

### 1. **Standard App Theme**

```typescript
export class ThemeService {
  async setTheme(theme: 'light' | 'dark') {
    const config =
      theme === 'dark' ? { style: 'DARK' as const, color: '#111827' } : { style: 'LIGHT' as const, color: '#ffffff' };

    // One call sets everything!
    await AndroidSystemBars.setSystemBarsStyle(config);
  }
}
```

### 2. **Gaming App (Different Bar Styles)**

```typescript
export class GameUIService {
  async setGameMode() {
    await AndroidSystemBars.setSystemBarsStyle({
      statusBar: {
        style: 'LIGHT', // Light icons on status bar
        color: '#000000', // Black background
      },
      navigationBar: {
        style: 'DARK', // Dark icons on navigation
        color: '#ffffff', // White background
      },
    });
  }
}
```

### 3. **Video Player App**

```typescript
export class VideoPlayerService {
  async enterVideoMode() {
    // Enter fullscreen
    await AndroidSystemBars.enterFullscreen({ mode: 'IMMERSIVE' });
  }

  async exitVideoMode() {
    // Exit and restore to app theme
    await AndroidSystemBars.exitFullscreen({
      restore: {
        style: 'DARK',
        color: '#1f2937',
      },
    });
  }
}
```

### 4. **E-commerce App (Status-only changes)**

```typescript
export class CheckoutService {
  async setSecureCheckoutStyle() {
    // Only change status bar for security indicator
    await AndroidSystemBars.setStatusBarStyle({
      style: 'LIGHT',
      color: '#10b981', // Green for secure
    });
  }

  async resetAfterCheckout() {
    // Reset both bars to app theme
    await AndroidSystemBars.setSystemBarsStyle({
      style: 'LIGHT',
      color: '#ffffff',
    });
  }
}
```

---

## 🔄 Backward Compatibility

**Don't worry!** All old methods are still supported with deprecation warnings:

```typescript
// These still work (but show deprecation warnings)
await AndroidSystemBars.setStyle({ style: 'DARK' }); // ⚠️ Deprecated
await AndroidSystemBars.hide(); // ⚠️ Deprecated
await AndroidSystemBars.show(); // ⚠️ Deprecated

// New equivalent methods
await AndroidSystemBars.setStatusBarStyle({ style: 'DARK' }); // ✅ New
await AndroidSystemBars.hideStatusBar(); // ✅ New
await AndroidSystemBars.showStatusBar(); // ✅ New
```

---

## 📈 Benefits Summary

| Aspect                     | Old API      | New API       |
| -------------------------- | ------------ | ------------- |
| **Calls to set both bars** | 2 calls      | 1 call        |
| **Method naming clarity**  | Ambiguous    | Crystal clear |
| **Fullscreen exit intent** | Confusing    | Explicit      |
| **Individual bar control** | Inconsistent | Consistent    |
| **Code complexity**        | Higher       | Lower         |
| **Developer experience**   | Frustrating  | Delightful    |

---

## 🚀 Migration Strategy

1. **Phase 1**: Start using new `setSystemBarsStyle()` for theme changes
2. **Phase 2**: Update fullscreen exit calls to use `restore` parameter
3. **Phase 3**: Replace `hide()`/`show()` with `hideStatusBar()`/`showStatusBar()`
4. **Phase 4**: Replace `setStyle()` with `setStatusBarStyle()`

The new API makes system bars management **professional, intuitive, and efficient**! 🎯
