package com.wael.capacitor.systembars;

import android.app.Activity;
import android.graphics.Color;
import android.os.Build;
import android.view.View;
import android.view.Window;
import android.view.WindowInsetsController;
import android.webkit.WebView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
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
    private WebView webView; // Add reference to WebView for insets handling

    public SystemBarsManager(Activity activity) {
        this.activity = activity;
        this.window = activity.getWindow();

        if (Build.VERSION.SDK_INT >= 30) {
            insetsController = WindowCompat.getInsetsController(window, window.getDecorView());
        }
    }

    /**
     * Set WebView reference for Android 35+ insets handling
     */
    public void setWebView(WebView webView) {
        this.webView = webView;
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

            // Note: setStatusBarColor() and setNavigationBarColor() are deprecated in Android 15+
            // The system automatically makes bars transparent when edge-to-edge is enabled
            // For custom status bar colors, apps should draw background in their WebView/content

            // Set up WindowInsets listener for proper inset handling (required by Android 15+)
            // Apply insets to WebView instead of decor view to allow custom coloring
            if (webView != null) {
                // Post with delay to ensure WebView is fully attached to window
                webView.post(() -> {
                    setupWindowInsetsListener();
                });
            }
        }
    }

    /**
     * Setup WindowInsets listener to properly handle system bar insets
     * This is required for Android 15+ edge-to-edge compliance
     * Reference: https://developer.android.com/develop/ui/views/layout/edge-to-edge#kotlin
     * 
     * IMPORTANT: We apply insets to WebView, NOT the decor view, to allow
     * custom background colors in the web content (via CSS)
     */
    private void setupWindowInsetsListener() {
        if (webView == null) return;
        
        // Ensure the WebView can receive insets
        webView.setFitsSystemWindows(false);
        
        ViewCompat.setOnApplyWindowInsetsListener(webView, (v, windowInsets) -> {
            // Get system bar insets
            Insets systemBarsInsets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            
            // Apply insets as padding to the WebView
            // This ensures content is not hidden behind system bars
            // while allowing the app to set custom background colors via CSS
            v.setPadding(
                systemBarsInsets.left,
                systemBarsInsets.top,
                systemBarsInsets.right,
                systemBarsInsets.bottom
            );
            
            // Return insets to allow parent views to handle them too
            return windowInsets;
        });
        
        // Request insets to be applied immediately
        ViewCompat.requestApplyInsets(webView);
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
     * Android 35+: setStatusBarColor() is deprecated - inject CSS to color the status bar area
     * Android < 35: Use setStatusBarColor() with WindowInsetsControllerCompat for icon styling
     */
    public void setStatusBarStyle(String style, String color) {
        activity.runOnUiThread(() -> {
            boolean lightIcons = style.equals("DARK"); // Dark style = light icons

            if (Build.VERSION.SDK_INT >= 35) {
                // Android 15+: Status bar color is automatically transparent
                // Set icon styling
                if (Build.VERSION.SDK_INT >= 30) {
                    insetsController.setAppearanceLightStatusBars(!lightIcons);
                }
                
                // Inject CSS to color the status bar area in the WebView
                if (color != null && !color.isEmpty() && webView != null) {
                    injectStatusBarBackgroundCSS(color);
                }

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
     * Inject CSS to set status bar background color on Android 35+
     * This is the proper way to color the status bar area in edge-to-edge mode
     */
    private void injectStatusBarBackgroundCSS(String color) {
        if (webView == null) return;
        
        String javascript = 
            "(function() {" +
            "  let style = document.getElementById('capacitor-status-bar-style');" +
            "  if (!style) {" +
            "    style = document.createElement('style');" +
            "    style.id = 'capacitor-status-bar-style';" +
            "    document.head.appendChild(style);" +
            "  }" +
            "  style.textContent = 'body { --status-bar-color: " + color + "; }';" +
            "  document.body.style.background = '" + color + "';" +
            "})();";
        
        webView.post(() -> {
            webView.evaluateJavascript(javascript, null);
        });
    }

    /**
     * Set navigation bar style and color
     * 
     * Android 35+: setNavigationBarColor() is deprecated - inject CSS to color the navigation bar area
     * Android < 35: Use setNavigationBarColor() with WindowInsetsControllerCompat for icon styling
     */
    public void setNavigationBarStyle(String style, String color) {
        activity.runOnUiThread(() -> {
            boolean lightIcons = style.equals("DARK"); // Dark style = light icons

            if (Build.VERSION.SDK_INT >= 35) {
                // Android 15+: Navigation bar color is automatically transparent
                // Set icon styling
                if (Build.VERSION.SDK_INT >= 30) {
                    insetsController.setAppearanceLightNavigationBars(!lightIcons);
                }
                
                // Inject CSS to color the navigation bar area in the WebView
                if (color != null && !color.isEmpty() && webView != null) {
                    injectNavigationBarBackgroundCSS(color);
                }

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
     * Inject CSS to set navigation bar background color on Android 35+
     * This creates a fixed div at the bottom with the navigation bar color
     */
    private void injectNavigationBarBackgroundCSS(String color) {
        if (webView == null) return;
        
        String javascript = 
            "(function() {" +
            "  let navBar = document.getElementById('capacitor-navigation-bar-bg');" +
            "  if (!navBar) {" +
            "    navBar = document.createElement('div');" +
            "    navBar.id = 'capacitor-navigation-bar-bg';" +
            "    navBar.style.cssText = 'position: fixed; bottom: 0; left: 0; right: 0; height: env(safe-area-inset-bottom); z-index: 9999;';" +
            "    document.body.appendChild(navBar);" +
            "  }" +
            "  navBar.style.backgroundColor = '" + color + "';" +
            "})();";
        
        webView.post(() -> {
            webView.evaluateJavascript(javascript, null);
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
