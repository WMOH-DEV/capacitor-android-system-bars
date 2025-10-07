package com.wael.capacitor.systembars;

import android.app.Activity;
import android.graphics.Color;
import android.os.Build;
import android.view.View;
import android.view.Window;
import android.view.WindowInsetsController;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import com.getcapacitor.JSObject;

/**
 * SystemBarsManager - Core system UI control for Android API 21-35+
 * 
 * Handles Android system bars (status bar & navigation bar) with full
 * compatibility:
 * - Android 35+: Native edge-to-edge with WindowInsetsController
 * - Android 30-34: WindowInsetsControllerCompat from AndroidX
 * - Android 21-29: Legacy System UI flags
 */
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

    /**
     * Initialize system bars based on Android version
     */
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
                    WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);

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

        // CRITICAL: Do NOT use LAYOUT_FULLSCREEN or LAYOUT_HIDE_NAVIGATION on Android <
        // 35
        // These cause webview resize issues

        flags |= View.SYSTEM_UI_FLAG_LAYOUT_STABLE;

        decorView.setSystemUiVisibility(flags);
    }

    /**
     * Set status bar style and color
     * 
     * Android 35+: setStatusBarColor() is deprecated - status bar is automatically
     * transparent
     * Android < 35: Use setStatusBarColor() with WindowInsetsControllerCompat for
     * icon styling
     */
    public void setStatusBarStyle(String style, String color) {
        activity.runOnUiThread(() -> {
            boolean lightIcons = style.equals("DARK"); // Dark style = light icons

            if (Build.VERSION.SDK_INT >= 35) {
                // Android 15+: Status bar color is automatically transparent and cannot be
                // changed
                // Only control icon styling - the system handles background based on
                // WindowInsets
                if (Build.VERSION.SDK_INT >= 30) {
                    insetsController.setAppearanceLightStatusBars(!lightIcons);
                }
                // Note: For Android 35+, apps should draw proper background behind
                // WindowInsets.Type.statusBars() instead of setting window colors

            } else if (Build.VERSION.SDK_INT >= 30) {
                // Android 11-14: Modern approach using WindowInsetsController
                insetsController.setAppearanceLightStatusBars(!lightIcons);

                // Set background color (still supported on Android < 35)
                if (color != null && !color.isEmpty()) {
                    try {
                        window.setStatusBarColor(Color.parseColor(color));
                    } catch (IllegalArgumentException e) {
                        // Invalid color format, ignore
                    }
                }

            } else if (Build.VERSION.SDK_INT >= 23) {
                // Android 6.0-10: Legacy approach using System UI flags
                View decorView = window.getDecorView();
                int flags = decorView.getSystemUiVisibility();

                if (lightIcons) {
                    flags &= ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
                } else {
                    flags |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
                }

                decorView.setSystemUiVisibility(flags);

                // Set background color
                if (color != null && !color.isEmpty()) {
                    try {
                        window.setStatusBarColor(Color.parseColor(color));
                    } catch (IllegalArgumentException e) {
                        // Invalid color format, ignore
                    }
                }
            }
            // Android < 23: Status bar styling not supported
        });
    }

    /**
     * Set navigation bar style and color
     * 
     * Android 35+: setNavigationBarColor() is deprecated - navigation bar is
     * automatically transparent
     * Android < 35: Use setNavigationBarColor() with WindowInsetsControllerCompat
     * for icon styling
     */
    public void setNavigationBarStyle(String style, String color) {
        activity.runOnUiThread(() -> {
            boolean lightIcons = style.equals("DARK"); // Dark style = light icons

            if (Build.VERSION.SDK_INT >= 35) {
                // Android 15+: Navigation bar color is automatically transparent and cannot be
                // changed
                // Only control icon styling - the system handles background based on
                // WindowInsets
                if (Build.VERSION.SDK_INT >= 30) {
                    insetsController.setAppearanceLightNavigationBars(!lightIcons);
                }
                // Note: For Android 35+, apps should draw proper background behind
                // WindowInsets.Type.navigationBars() instead of setting window colors

            } else if (Build.VERSION.SDK_INT >= 30) {
                // Android 11-14: Modern approach using WindowInsetsController
                insetsController.setAppearanceLightNavigationBars(!lightIcons);

                // Set background color (still supported on Android < 35)
                if (color != null && !color.isEmpty()) {
                    try {
                        window.setNavigationBarColor(Color.parseColor(color));
                    } catch (IllegalArgumentException e) {
                        // Invalid color format, ignore
                    }
                }

            } else if (Build.VERSION.SDK_INT >= 26) {
                // Android 8.0-10: Legacy approach using System UI flags
                View decorView = window.getDecorView();
                int flags = decorView.getSystemUiVisibility();

                if (lightIcons) {
                    flags &= ~View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
                } else {
                    flags |= View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
                }

                decorView.setSystemUiVisibility(flags);

                // Set background color
                if (color != null && !color.isEmpty()) {
                    try {
                        window.setNavigationBarColor(Color.parseColor(color));
                    } catch (IllegalArgumentException e) {
                        // Invalid color format, ignore
                    }
                }
            }
            // Android < 26: Navigation bar styling not supported
        });
    }

    /**
     * Hide status bar
     */
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

    /**
     * Show status bar
     */
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
     * Hide navigation bar
     */
    public void hideNavigationBar() {
        activity.runOnUiThread(() -> {
            if (Build.VERSION.SDK_INT >= 30) {
                insetsController.hide(WindowInsetsCompat.Type.navigationBars());
            } else {
                View decorView = window.getDecorView();
                int flags = decorView.getSystemUiVisibility();
                flags |= View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
                decorView.setSystemUiVisibility(flags);
            }
        });
    }

    /**
     * Show navigation bar
     */
    public void showNavigationBar() {
        activity.runOnUiThread(() -> {
            if (Build.VERSION.SDK_INT >= 30) {
                insetsController.show(WindowInsetsCompat.Type.navigationBars());
            } else {
                View decorView = window.getDecorView();
                int flags = decorView.getSystemUiVisibility();
                flags &= ~View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
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
     * Get current window insets information
     */
    public JSObject getInsets() {
        JSObject result = new JSObject();

        // For now, return basic info - can be enhanced with actual inset values
        result.put("top", 0);
        result.put("bottom", 0);
        result.put("left", 0);
        result.put("right", 0);
        result.put("statusBarVisible", true);
        result.put("navigationBarVisible", true);

        return result;
    }

    /**
     * Re-apply system UI flags after system events (screen unlock, etc.)
     * CRITICAL: Screen unlock resets system UI on Android < 35
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
