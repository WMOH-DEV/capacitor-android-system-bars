package com.wael.capacitor.systembars;

import android.app.Activity;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.Window;
import android.webkit.WebView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

/**
 * FullscreenManager - Handles immersive fullscreen mode across all Android versions
 *
 * Android 35+ KEY DESIGN:
 * Capacitor's adjustMarginsForEdgeToEdge="auto" sets a WindowInsets listener that
 * applies margins from systemBars + displayCutout. During fullscreen:
 * - systemBars insets become 0 (bars hidden)
 * - BUT displayCutout insets REMAIN (physical notch doesn't disappear)
 * - So Capacitor's listener would still apply top margin for the notch
 *
 * FIX: We temporarily REPLACE Capacitor's listener with our own that zeros
 * all margins during fullscreen. On exit, we restore Capacitor's original
 * listener behavior and request a new insets dispatch.
 */
public class FullscreenManager {

    private static final String TAG = "FullscreenManager";

    private final Activity activity;
    private final SystemBarsManager systemBarsManager;
    private final WebViewPaddingManager paddingManager;
    private final Window window;
    private WebView webView;

    private volatile boolean isFullscreenActive = false;
    private volatile String currentFullscreenMode = "IMMERSIVE";

    public FullscreenManager(
            Activity activity,
            SystemBarsManager systemBarsManager,
            WebViewPaddingManager paddingManager) {
        this.activity = activity;
        this.systemBarsManager = systemBarsManager;
        this.paddingManager = paddingManager;
        this.window = activity.getWindow();
    }

    /**
     * Set WebView reference for Android 35+ margin management
     */
    public void setWebView(WebView webView) {
        this.webView = webView;
    }

    /**
     * Enter fullscreen mode
     *
     * @param mode "IMMERSIVE" or "LEAN"
     */
    public void enterFullscreen(String mode) {
        activity.runOnUiThread(() -> {
            isFullscreenActive = true;
            currentFullscreenMode = mode;

            // Update visibility tracking
            systemBarsManager.setBarVisibility(false, false);

            View decorView = window.getDecorView();

            if (Build.VERSION.SDK_INT >= 30) {
                WindowInsetsControllerCompat controller = WindowCompat.getInsetsController(window,
                        decorView);

                if (Build.VERSION.SDK_INT >= 35 && webView != null) {
                    // CRITICAL: Replace Capacitor's margin listener with one that
                    // zeros all margins. This prevents the displayCutout insets
                    // from keeping a top margin during fullscreen.
                    installFullscreenInsetsListener();
                } else if (Build.VERSION.SDK_INT < 35) {
                    // Android 30-34: Remove WebView padding manually
                    paddingManager.removePadding();
                }

                controller.hide(WindowInsetsCompat.Type.systemBars());

                if (mode.equals("IMMERSIVE")) {
                    controller.setSystemBarsBehavior(
                            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
                } else {
                    controller.setSystemBarsBehavior(
                            WindowInsetsControllerCompat.BEHAVIOR_DEFAULT);
                }

                Log.d(TAG, "Entered fullscreen (modern): mode=" + mode);
            } else {
                // Legacy: remove padding + set system UI flags
                paddingManager.removePadding();

                int flags = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN;

                if (mode.equals("IMMERSIVE")) {
                    flags |= View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
                } else {
                    flags |= View.SYSTEM_UI_FLAG_IMMERSIVE;
                }

                decorView.setSystemUiVisibility(flags);
                Log.d(TAG, "Entered fullscreen (legacy): mode=" + mode);
            }
        });
    }

    /**
     * Install a fullscreen insets listener that zeros all margins.
     * Replaces Capacitor's edge-to-edge margin listener temporarily.
     */
    private void installFullscreenInsetsListener() {
        if (webView == null) return;

        ViewCompat.setOnApplyWindowInsetsListener(webView, (v, windowInsets) -> {
            // During fullscreen: force all margins to 0
            if (v.getLayoutParams() instanceof MarginLayoutParams) {
                MarginLayoutParams mlp = (MarginLayoutParams) v.getLayoutParams();
                if (mlp.leftMargin != 0 || mlp.topMargin != 0
                        || mlp.rightMargin != 0 || mlp.bottomMargin != 0) {
                    mlp.setMargins(0, 0, 0, 0);
                    v.setLayoutParams(mlp);
                }
            }
            return WindowInsetsCompat.CONSUMED;
        });

        // Trigger a new insets dispatch to apply our zero-margin listener
        ViewCompat.requestApplyInsets(webView);
    }

    /**
     * Restore Capacitor's edge-to-edge margin listener.
     * Replicates the logic from CapacitorWebView.edgeToEdgeHandler().
     */
    private void restoreCapacitorInsetsListener() {
        if (webView == null) return;

        ViewCompat.setOnApplyWindowInsetsListener(webView, (v, windowInsets) -> {
            Insets insets = windowInsets.getInsets(
                    WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.displayCutout());
            if (v.getLayoutParams() instanceof MarginLayoutParams) {
                MarginLayoutParams mlp = (MarginLayoutParams) v.getLayoutParams();
                mlp.leftMargin = insets.left;
                mlp.bottomMargin = insets.bottom;
                mlp.rightMargin = insets.right;
                mlp.topMargin = insets.top;
                v.setLayoutParams(mlp);
            }
            return WindowInsetsCompat.CONSUMED;
        });

        // Trigger a new insets dispatch so margins are restored with real values
        ViewCompat.requestApplyInsets(webView);
        Log.d(TAG, "Restored Capacitor's edge-to-edge margin listener");
    }

    /**
     * Exit fullscreen and restore normal state (applies same style to both bars)
     */
    public void exitFullscreen(String style, String color) {
        exitFullscreen(style, color, style, color);
    }

    /**
     * Exit fullscreen and restore both status and navigation bars with
     * individual styles.
     */
    public void exitFullscreen(String statusStyle, String statusColor, String navStyle, String navColor) {
        activity.runOnUiThread(() -> {
            View decorView = window.getDecorView();

            if (Build.VERSION.SDK_INT >= 30) {
                WindowInsetsControllerCompat controller = WindowCompat.getInsetsController(window,
                        decorView);

                controller.show(WindowInsetsCompat.Type.systemBars());
                controller.setSystemBarsBehavior(
                        WindowInsetsControllerCompat.BEHAVIOR_DEFAULT);

                if (Build.VERSION.SDK_INT >= 35 && webView != null) {
                    // Restore Capacitor's margin listener so margins come back
                    restoreCapacitorInsetsListener();
                    // Re-apply system UI state (colors, icon styles)
                    systemBarsManager.reapplySystemUI();
                } else {
                    // Android 30-34: Re-apply padding
                    decorView.post(() -> paddingManager.applyPadding());
                }

            } else {
                int flags = decorView.getSystemUiVisibility();

                flags &= ~(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);

                flags |= View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
                decorView.setSystemUiVisibility(flags);
                decorView.post(() -> paddingManager.applyPadding());
            }

            isFullscreenActive = false;

            // Update visibility tracking
            systemBarsManager.setBarVisibility(true, true);

            // Restore bar styles with a slight delay for UI to settle.
            // Guard: skip if fullscreen was re-entered during the delay.
            decorView.postDelayed(() -> {
                if (isFullscreenActive) return; // Rapid re-enter guard

                if (statusStyle != null || statusColor != null) {
                    systemBarsManager.setStatusBarStyle(
                            statusStyle != null ? statusStyle : "DEFAULT",
                            statusColor);
                }
                if (navStyle != null || navColor != null) {
                    systemBarsManager.setNavigationBarStyle(
                            navStyle != null ? navStyle : "DEFAULT",
                            navColor);
                }
            }, 50);

            Log.d(TAG, "Exited fullscreen");
        });
    }

    /**
     * Exit fullscreen to system default (no custom restoration)
     */
    public void exitFullscreen() {
        exitFullscreen("DEFAULT", null, "DEFAULT", null);
    }

    public boolean isFullscreenActive() {
        return isFullscreenActive;
    }

    public String getCurrentFullscreenMode() {
        return currentFullscreenMode;
    }

    /**
     * Force exit fullscreen mode (emergency fallback)
     */
    public void forceExit() {
        activity.runOnUiThread(() -> {
            View decorView = window.getDecorView();

            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE);

            if (Build.VERSION.SDK_INT >= 30) {
                WindowInsetsControllerCompat controller = WindowCompat.getInsetsController(window,
                        decorView);
                controller.show(WindowInsetsCompat.Type.systemBars());
                controller.setSystemBarsBehavior(
                        WindowInsetsControllerCompat.BEHAVIOR_DEFAULT);
            }

            if (Build.VERSION.SDK_INT >= 35 && webView != null) {
                restoreCapacitorInsetsListener();
                systemBarsManager.reapplySystemUI();
            } else {
                paddingManager.applyPadding();
            }

            isFullscreenActive = false;
            currentFullscreenMode = "IMMERSIVE";

            decorView.postDelayed(() -> {
                systemBarsManager.setStatusBarStyle("DEFAULT", null);
                systemBarsManager.setNavigationBarStyle("DEFAULT", null);
            }, 100);

            Log.d(TAG, "Force exited fullscreen");
        });
    }

    /**
     * Re-apply fullscreen mode after resume/screen unlock.
     */
    public void reapplyFullscreenIfActive() {
        if (isFullscreenActive) {
            activity.runOnUiThread(() -> {
                View decorView = window.getDecorView();

                if (Build.VERSION.SDK_INT >= 30) {
                    WindowInsetsControllerCompat controller = WindowCompat.getInsetsController(window,
                            decorView);

                    // Re-install fullscreen listener on 35+
                    if (Build.VERSION.SDK_INT >= 35 && webView != null) {
                        installFullscreenInsetsListener();
                    } else if (Build.VERSION.SDK_INT < 35) {
                        // Android 30-34: Remove WebView padding
                        paddingManager.removePadding();
                    }

                    controller.hide(WindowInsetsCompat.Type.systemBars());

                    if (currentFullscreenMode.equals("IMMERSIVE")) {
                        controller.setSystemBarsBehavior(
                                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
                    } else {
                        controller.setSystemBarsBehavior(
                                WindowInsetsControllerCompat.BEHAVIOR_DEFAULT);
                    }
                } else {
                    int flags = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN;

                    if (currentFullscreenMode.equals("IMMERSIVE")) {
                        flags |= View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
                    } else {
                        flags |= View.SYSTEM_UI_FLAG_IMMERSIVE;
                    }

                    decorView.setSystemUiVisibility(flags);
                    paddingManager.removePadding();
                }

                Log.d(TAG, "Re-applied fullscreen after resume");
            });
        }
    }
}
