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
     */
    public void setStatusBarStyle(String style, String color) {
        activity.runOnUiThread(() -> {
            boolean lightIcons = style.equals("DARK"); // Dark style = light icons

            if (Build.VERSION.SDK_INT >= 30) {
                // Modern approach using WindowInsetsController
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
                try {
                    window.setStatusBarColor(Color.parseColor(color));
                } catch (IllegalArgumentException e) {
                    // Invalid color format, ignore
                }
            }
        });
    }

    /**
     * Set navigation bar style and color
     */
    public void setNavigationBarStyle(String style, String color) {
        activity.runOnUiThread(() -> {
            boolean lightIcons = style.equals("DARK"); // Dark style = light icons

            if (Build.VERSION.SDK_INT >= 30) {
                // Modern approach
                insetsController.setAppearanceLightNavigationBars(!lightIcons);
            } else if (Build.VERSION.SDK_INT >= 26) {
                // Legacy approach (API 26-29)
                View decorView = window.getDecorView();
                int flags = decorView.getSystemUiVisibility();

                if (lightIcons) {
                    flags &= ~View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
                } else {
                    flags |= View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
                }

                decorView.setSystemUiVisibility(flags);
            }

            // Set background color
            if (color != null && !color.isEmpty()) {
                try {
                    window.setNavigationBarColor(Color.parseColor(color));
                } catch (IllegalArgumentException e) {
                    // Invalid color format, ignore
                }
            }
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

    /**
     * ADMOB COMPATIBILITY METHODS
     * AdMob's immersiveMode uses SYSTEM_UI_FLAG_IMMERSIVE_STICKY &
     * SYSTEM_UI_FLAG_HIDE_NAVIGATION
     * which interferes with our system bar management. These methods help restore
     * proper state.
     */

    private boolean adMobCompatibilityMode = true;
    private String lastStatusBarStyle = "DEFAULT";
    private String lastStatusBarColor = null;

    /**
     * Force restore system UI state after AdMob ad interference
     * This method aggressively clears AdMob's immersive flags and restores our
     * state
     */
    public void forceRestoreSystemUI(String style, String color) {
        activity.runOnUiThread(() -> {
            View decorView = window.getDecorView();

            if (Build.VERSION.SDK_INT >= 35) {
                // Android 35+: Use modern approach
                enableEdgeToEdge();

            } else if (Build.VERSION.SDK_INT >= 30) {
                // Android 30-34: Clear AdMob flags and restore
                insetsController.show(WindowInsetsCompat.Type.systemBars());
                insetsController.setSystemBarsBehavior(WindowInsetsControllerCompat.BEHAVIOR_DEFAULT);

            } else {
                // Android < 30: Aggressively clear ALL AdMob immersive flags
                int flags = decorView.getSystemUiVisibility();

                // CRITICAL: Clear ALL AdMob-set immersive flags
                flags &= ~(View.SYSTEM_UI_FLAG_IMMERSIVE
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

                // Restore our stable layout
                flags |= View.SYSTEM_UI_FLAG_LAYOUT_STABLE;

                decorView.setSystemUiVisibility(flags);
            }

            // Store current style for future restoration
            lastStatusBarStyle = style;
            lastStatusBarColor = color;

            // Restore status bar style after clearing AdMob flags
            decorView.postDelayed(() -> {
                setStatusBarStyle(style, color);
            }, 100); // Longer delay for AdMob cleanup
        });
    }

    /**
     * Enable/disable AdMob compatibility mode
     * When enabled, the plugin will more aggressively restore system UI state
     */
    public void setAdMobCompatibilityMode(boolean enabled) {
        this.adMobCompatibilityMode = enabled;
    }

    /**
     * Get the last applied status bar style (for AdMob restoration)
     */
    public String getLastStatusBarStyle() {
        return lastStatusBarStyle;
    }

    /**
     * Get the last applied status bar color (for AdMob restoration)
     */
    public String getLastStatusBarColor() {
        return lastStatusBarColor;
    }
}
