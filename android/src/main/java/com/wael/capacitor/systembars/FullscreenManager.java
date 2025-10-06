package com.wael.capacitor.systembars;

import android.app.Activity;
import android.os.Build;
import android.view.View;
import android.view.Window;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

/**
 * FullscreenManager - Handles immersive fullscreen mode across all Android
 * versions
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
            WebViewPaddingManager paddingManager) {
        this.activity = activity;
        this.systemBarsManager = systemBarsManager;
        this.paddingManager = paddingManager;
        this.window = activity.getWindow();
    }

    /**
     * Enter fullscreen mode
     * 
     * @param mode "IMMERSIVE" or "LEAN"
     */
    public void enterFullscreen(String mode) {
        activity.runOnUiThread(() -> {
            // Remove webview padding
            paddingManager.removePadding();

            View decorView = window.getDecorView();

            if (Build.VERSION.SDK_INT >= 30) {
                // Modern approach using WindowInsetsController
                WindowInsetsControllerCompat controller = WindowCompat.getInsetsController(window, decorView);

                controller.hide(WindowInsetsCompat.Type.systemBars());

                if (mode.equals("IMMERSIVE")) {
                    controller.setSystemBarsBehavior(
                            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
                } else {
                    // Lean mode
                    controller.setSystemBarsBehavior(
                            WindowInsetsControllerCompat.BEHAVIOR_DEFAULT);
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

            if (Build.VERSION.SDK_INT >= 35) {
                // Android 35+: Use modern WindowInsetsController
                WindowInsetsControllerCompat controller = WindowCompat.getInsetsController(window, decorView);

                controller.show(WindowInsetsCompat.Type.systemBars());
                controller.setSystemBarsBehavior(WindowInsetsControllerCompat.BEHAVIOR_DEFAULT);

                // Restore edge-to-edge mode
                systemBarsManager.reapplySystemUI();

            } else if (Build.VERSION.SDK_INT >= 30) {
                // Android 30-34: Use WindowInsetsController but apply padding
                WindowInsetsControllerCompat controller = WindowCompat.getInsetsController(window, decorView);

                controller.show(WindowInsetsCompat.Type.systemBars());
                controller.setSystemBarsBehavior(WindowInsetsControllerCompat.BEHAVIOR_DEFAULT);

                // CRITICAL: Apply padding after system bars are shown
                decorView.post(() -> {
                    paddingManager.applyPadding();
                });

            } else {
                // Android < 30: Legacy System UI flags
                int flags = decorView.getSystemUiVisibility();

                // Clear all fullscreen flags
                flags &= ~(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);

                // Restore stable layout flags
                flags |= View.SYSTEM_UI_FLAG_LAYOUT_STABLE;

                decorView.setSystemUiVisibility(flags);

                // CRITICAL: Apply padding after layout flags are set
                decorView.post(() -> {
                    paddingManager.applyPadding();
                });
            }

            // Restore status bar style and color with a slight delay
            decorView.postDelayed(() -> {
                systemBarsManager.setStatusBarStyle(style, color);
            }, 50);
        });
    }
}