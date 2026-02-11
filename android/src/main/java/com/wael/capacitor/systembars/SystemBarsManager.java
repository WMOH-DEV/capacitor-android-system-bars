package com.wael.capacitor.systembars;

import android.app.Activity;
import android.graphics.Color;
import android.os.Build;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.FrameLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import com.getcapacitor.JSObject;

/**
 * SystemBarsManager - Core system UI control for Android API 21-35+
 *
 * ARCHITECTURE (Android 35+):
 * - Capacitor's adjustMarginsForEdgeToEdge="auto" handles spacing via WebView margins
 * - This plugin handles ONLY: icon appearance (light/dark) + per-bar background color
 * - Per-bar colors use two Views (status bar bg + nav bar bg) inserted behind the WebView
 *   in android.R.id.content, sized by WindowInsets. Supports different colors per bar.
 * - No CSS injection, no WebView padding needed
 *
 * ARCHITECTURE (Android < 35):
 * - Native setStatusBarColor()/setNavigationBarColor() for colors
 * - Icon appearance via legacy system UI flags or WindowInsetsControllerCompat
 * - WebViewPaddingManager handles top padding for status bar overlay
 */
public class SystemBarsManager {

    private static final String TAG = "SystemBarsManager";

    private final Activity activity;
    private final Window window;
    private WindowInsetsControllerCompat insetsController;

    // Store pending bar colors/styles for re-application after lifecycle events
    private String currentStatusBarColor = null;
    private String currentNavBarColor = null;
    private String currentStatusBarStyle = "DEFAULT";
    private String currentNavBarStyle = "DEFAULT";

    // Android 35+: per-bar background colors (shown via Views behind transparent bars)
    private String currentStatusBarBgColor = null;
    private String currentNavBarBgColor = null;
    private View statusBarBgView;
    private View navBarBgView;

    // Track bar visibility for getInsets() API
    private volatile boolean statusBarVisible = true;
    private volatile boolean navigationBarVisible = true;

    // Device density for px-to-dp conversion
    private final float density;

    public SystemBarsManager(Activity activity) {
        this.activity = activity;
        this.window = activity.getWindow();
        this.density = activity.getResources().getDisplayMetrics().density;

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
                initializeEdgeToEdge();
            } else {
                setupLegacySystemUI();
            }
        });
    }

    /**
     * Android 35+ initialization.
     *
     * Edge-to-edge is enforced by the system. Capacitor's adjustMarginsForEdgeToEdge="auto"
     * sets WebView margins via its own WindowInsets listener on 35+. We just need to:
     * 1. Ensure edge-to-edge is declared (setDecorFitsSystemWindows)
     * 2. Set BEHAVIOR_DEFAULT so bars are permanent and report correct insets
     * 3. Set transparent bar colors at the Window level (enforced on 35+ anyway)
     */
    private void initializeEdgeToEdge() {
        WindowCompat.setDecorFitsSystemWindows(window, false);

        insetsController.setSystemBarsBehavior(
                WindowInsetsControllerCompat.BEHAVIOR_DEFAULT);

        window.setStatusBarColor(Color.TRANSPARENT);
        window.setNavigationBarColor(Color.TRANSPARENT);

        // Create per-bar colored Views behind the WebView
        setupBarBackgroundViews();

        Log.d(TAG, "Edge-to-edge initialized. Capacitor handles margins, we handle colors.");
    }

    /**
     * Android < 35: Legacy System UI Flags
     */
    private void setupLegacySystemUI() {
        View decorView = window.getDecorView();
        int flags = decorView.getSystemUiVisibility();
        flags |= View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
        decorView.setSystemUiVisibility(flags);
    }

    /**
     * Create two colored Views behind the transparent system bars (Android 35+).
     *
     * Views are inserted at indices 0 and 1 in android.R.id.content so they sit
     * BEHIND the WebView. During normal operation, the WebView has margins
     * (from Capacitor edge-to-edge) and the colored views show through.
     * During fullscreen, the WebView margins are zeroed and it covers these views.
     */
    private void setupBarBackgroundViews() {
        if (statusBarBgView != null) return; // already created

        FrameLayout contentView = activity.findViewById(android.R.id.content);
        if (contentView == null) return;

        // Status bar background — sits at the top, behind activity content
        statusBarBgView = new View(activity);
        statusBarBgView.setBackgroundColor(Color.TRANSPARENT);
        FrameLayout.LayoutParams statusParams = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT, 0);
        statusParams.gravity = Gravity.TOP;
        contentView.addView(statusBarBgView, 0, statusParams);

        // Navigation bar background — sits at the bottom, behind activity content
        navBarBgView = new View(activity);
        navBarBgView.setBackgroundColor(Color.TRANSPARENT);
        FrameLayout.LayoutParams navParams = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT, 0);
        navParams.gravity = Gravity.BOTTOM;
        contentView.addView(navBarBgView, 1, navParams);

        // Size the views to match real system bar insets (updates on rotation etc.)
        ViewCompat.setOnApplyWindowInsetsListener(contentView, (v, windowInsets) -> {
            Insets insets = windowInsets.getInsets(
                    WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.displayCutout());

            ViewGroup.LayoutParams sp = statusBarBgView.getLayoutParams();
            if (sp.height != insets.top) {
                sp.height = insets.top;
                statusBarBgView.setLayoutParams(sp);
            }

            ViewGroup.LayoutParams np = navBarBgView.getLayoutParams();
            if (np.height != insets.bottom) {
                np.height = insets.bottom;
                navBarBgView.setLayoutParams(np);
            }

            return windowInsets; // Don't consume — children (WebView) need insets too
        });
        ViewCompat.requestApplyInsets(contentView);

        Log.d(TAG, "Bar background views created behind WebView");
    }

    /**
     * Set the status bar area background color (Android 35+).
     */
    private void setStatusBarBgColor(String color) {
        if (color == null || color.isEmpty()) return;
        currentStatusBarBgColor = color;
        if (statusBarBgView == null) return;
        try {
            statusBarBgView.setBackgroundColor(Color.parseColor(color));
            Log.d(TAG, "Status bar background set to: " + color);
        } catch (IllegalArgumentException e) {
            Log.w(TAG, "Invalid status bar background color: " + color);
        }
    }

    /**
     * Set the navigation bar area background color (Android 35+).
     */
    private void setNavBarBgColor(String color) {
        if (color == null || color.isEmpty()) return;
        currentNavBarBgColor = color;
        if (navBarBgView == null) return;
        try {
            navBarBgView.setBackgroundColor(Color.parseColor(color));
            Log.d(TAG, "Navigation bar background set to: " + color);
        } catch (IllegalArgumentException e) {
            Log.w(TAG, "Invalid navigation bar background color: " + color);
        }
    }

    /**
     * Set status bar style and color
     */
    public void setStatusBarStyle(String style, String color) {
        activity.runOnUiThread(() -> {
            currentStatusBarStyle = style;
            currentStatusBarColor = color;
            boolean lightIcons = style.equals("DARK");

            if (Build.VERSION.SDK_INT >= 35) {
                insetsController.setAppearanceLightStatusBars(!lightIcons);

                if (color != null && !color.isEmpty()) {
                    setStatusBarBgColor(color);
                }

            } else if (Build.VERSION.SDK_INT >= 30) {
                insetsController.setAppearanceLightStatusBars(!lightIcons);

                if (color != null && !color.isEmpty()) {
                    try {
                        window.setStatusBarColor(Color.parseColor(color));
                    } catch (IllegalArgumentException e) {
                        Log.w(TAG, "Invalid status bar color: " + color);
                    }
                }

            } else if (Build.VERSION.SDK_INT >= 23) {
                View decorView = window.getDecorView();
                int flags = decorView.getSystemUiVisibility();

                if (lightIcons) {
                    flags &= ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
                } else {
                    flags |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
                }

                decorView.setSystemUiVisibility(flags);

                if (color != null && !color.isEmpty()) {
                    try {
                        window.setStatusBarColor(Color.parseColor(color));
                    } catch (IllegalArgumentException e) {
                        Log.w(TAG, "Invalid status bar color: " + color);
                    }
                }
            }
        });
    }

    /**
     * Set navigation bar style and color
     */
    public void setNavigationBarStyle(String style, String color) {
        activity.runOnUiThread(() -> {
            currentNavBarStyle = style;
            currentNavBarColor = color;
            boolean lightIcons = style.equals("DARK");

            if (Build.VERSION.SDK_INT >= 35) {
                insetsController.setAppearanceLightNavigationBars(!lightIcons);

                if (color != null && !color.isEmpty()) {
                    setNavBarBgColor(color);
                }

            } else if (Build.VERSION.SDK_INT >= 30) {
                insetsController.setAppearanceLightNavigationBars(!lightIcons);

                if (color != null && !color.isEmpty()) {
                    try {
                        window.setNavigationBarColor(Color.parseColor(color));
                    } catch (IllegalArgumentException e) {
                        Log.w(TAG, "Invalid navigation bar color: " + color);
                    }
                }

            } else if (Build.VERSION.SDK_INT >= 26) {
                View decorView = window.getDecorView();
                int flags = decorView.getSystemUiVisibility();

                if (lightIcons) {
                    flags &= ~View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
                } else {
                    flags |= View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
                }

                decorView.setSystemUiVisibility(flags);

                if (color != null && !color.isEmpty()) {
                    try {
                        window.setNavigationBarColor(Color.parseColor(color));
                    } catch (IllegalArgumentException e) {
                        Log.w(TAG, "Invalid navigation bar color: " + color);
                    }
                }
            }
        });
    }

    public void hideStatusBar() {
        activity.runOnUiThread(() -> {
            statusBarVisible = false;
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
            statusBarVisible = true;
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

    public void hideNavigationBar() {
        activity.runOnUiThread(() -> {
            navigationBarVisible = false;
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

    public void showNavigationBar() {
        activity.runOnUiThread(() -> {
            navigationBarVisible = true;
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
     * Set overlay mode — safe no-op.
     */
    public void setOverlayMode(boolean overlay) {
        Log.d(TAG, "setOverlayMode(" + overlay + ") - no-op, handled by Capacitor config");
    }

    /**
     * Get current window insets information.
     * Values are in CSS px (dp), not Android hardware px.
     */
    public JSObject getInsets() {
        JSObject result = new JSObject();

        View decorView = window.getDecorView();
        WindowInsetsCompat windowInsets = ViewCompat.getRootWindowInsets(decorView);

        if (windowInsets != null) {
            Insets bars = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            result.put("top", Math.round(bars.top / density));
            result.put("bottom", Math.round(bars.bottom / density));
            result.put("left", Math.round(bars.left / density));
            result.put("right", Math.round(bars.right / density));
        } else {
            result.put("top", 0);
            result.put("bottom", 0);
            result.put("left", 0);
            result.put("right", 0);
        }

        result.put("statusBarVisible", statusBarVisible);
        result.put("navigationBarVisible", navigationBarVisible);

        return result;
    }

    /**
     * Update visibility tracking (called by FullscreenManager)
     */
    public void setBarVisibility(boolean statusVisible, boolean navVisible) {
        this.statusBarVisible = statusVisible;
        this.navigationBarVisible = navVisible;
    }

    /**
     * Re-apply system UI state after lifecycle events (screen unlock, etc.)
     */
    public void reapplySystemUI() {
        activity.runOnUiThread(() -> {
            if (Build.VERSION.SDK_INT >= 35) {
                initializeEdgeToEdge();

                // Re-apply stored per-bar background colors
                if (currentStatusBarBgColor != null) {
                    setStatusBarBgColor(currentStatusBarBgColor);
                }
                if (currentNavBarBgColor != null) {
                    setNavBarBgColor(currentNavBarBgColor);
                }
                // Re-apply icon styles
                if (currentStatusBarStyle != null) {
                    boolean lightStatusIcons = currentStatusBarStyle.equals("DARK");
                    insetsController.setAppearanceLightStatusBars(!lightStatusIcons);
                }
                if (currentNavBarStyle != null) {
                    boolean lightNavIcons = currentNavBarStyle.equals("DARK");
                    insetsController.setAppearanceLightNavigationBars(!lightNavIcons);
                }
            } else {
                setupLegacySystemUI();
                if (currentStatusBarColor != null) {
                    try {
                        window.setStatusBarColor(Color.parseColor(currentStatusBarColor));
                    } catch (IllegalArgumentException ignored) {}
                }
                if (currentNavBarColor != null) {
                    try {
                        window.setNavigationBarColor(Color.parseColor(currentNavBarColor));
                    } catch (IllegalArgumentException ignored) {}
                }
            }
        });
    }
}
