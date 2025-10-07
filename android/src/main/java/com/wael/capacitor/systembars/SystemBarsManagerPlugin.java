package com.wael.capacitor.systembars;

import android.os.Build;
import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;

@CapacitorPlugin(name = "AndroidSystemBars")
public class SystemBarsManagerPlugin extends Plugin {

    private SystemBarsManager systemBarsManager;
    private WebViewPaddingManager paddingManager;
    private FullscreenManager fullscreenManager;
    private LifecycleHandler lifecycleHandler;

    @Override
    public void load() {
        super.load();
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
            result.put("statusBarHeight", paddingManager.getStatusBarHeight());
            result.put("navigationBarHeight", paddingManager.getNavigationBarHeight());

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
        try {
            // Check for new API structure
            JSObject restoreConfig = call.getObject("restore");

            if (restoreConfig != null) {
                // New API: Handle restore configuration
                String globalStyle = restoreConfig.getString("style");
                String globalColor = restoreConfig.getString("color");

                JSObject statusBarConfig = null;
                JSObject navigationBarConfig = null;

                if (restoreConfig.has("statusBar")) {
                    try {
                        statusBarConfig = restoreConfig.getJSObject("statusBar");
                    } catch (Exception e) {
                        // Handle if statusBar is not a JSObject
                    }
                }

                if (restoreConfig.has("navigationBar")) {
                    try {
                        navigationBarConfig = restoreConfig.getJSObject("navigationBar");
                    } catch (Exception e) {
                        // Handle if navigationBar is not a JSObject
                    }
                }

                // Determine final configurations
                String statusStyle = globalStyle != null ? globalStyle : "DEFAULT";
                String statusColor = globalColor;
                String navStyle = globalStyle != null ? globalStyle : "DEFAULT";
                String navColor = globalColor;

                // Override with individual configs if provided
                if (statusBarConfig != null) {
                    if (statusBarConfig.has("style")) {
                        statusStyle = statusBarConfig.getString("style");
                    }
                    if (statusBarConfig.has("color")) {
                        statusColor = statusBarConfig.getString("color");
                    }
                }

                if (navigationBarConfig != null) {
                    if (navigationBarConfig.has("style")) {
                        navStyle = navigationBarConfig.getString("style");
                    }
                    if (navigationBarConfig.has("color")) {
                        navColor = navigationBarConfig.getString("color");
                    }
                }

                // Exit fullscreen with specific restoration
                fullscreenManager.exitFullscreen(statusStyle, statusColor, navStyle, navColor);
            } else {
                // Check for legacy API (backward compatibility)
                String legacyStyle = call.getString("style");
                String legacyColor = call.getString("color");

                if (legacyStyle != null || legacyColor != null) {
                    // Legacy API: Apply to both bars
                    fullscreenManager.exitFullscreen(
                            legacyStyle != null ? legacyStyle : "DEFAULT",
                            legacyColor);
                } else {
                    // No restore config: Exit to system default
                    fullscreenManager.exitFullscreen();
                }
            }

            call.resolve();
        } catch (Exception e) {
            call.reject("Failed to exit fullscreen", e);
        }
    }

    @PluginMethod
    public void setOverlay(PluginCall call) {
        boolean overlay = Boolean.TRUE.equals(call.getBoolean("overlay", false));

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

    @PluginMethod
    public void getInsets(PluginCall call) {
        try {
            JSObject insets = systemBarsManager.getInsets();
            call.resolve(insets);
        } catch (Exception e) {
            call.reject("Failed to get insets", e);
        }
    }

    @PluginMethod
    public void setNavigationBarStyle(PluginCall call) {
        String style = call.getString("style", "DEFAULT");
        String color = call.getString("color");

        try {
            systemBarsManager.setNavigationBarStyle(style, color);
            call.resolve();
        } catch (Exception e) {
            call.reject("Failed to set navigation bar style", e);
        }
    }

    @PluginMethod
    public void hideNavigationBar(PluginCall call) {
        try {
            systemBarsManager.hideNavigationBar();
            call.resolve();
        } catch (Exception e) {
            call.reject("Failed to hide navigation bar", e);
        }
    }

    @PluginMethod
    public void showNavigationBar(PluginCall call) {
        try {
            systemBarsManager.showNavigationBar();
            call.resolve();
        } catch (Exception e) {
            call.reject("Failed to show navigation bar", e);
        }
    }

    // === NEW UNIFIED SYSTEM BARS API ===

    @PluginMethod
    public void setSystemBarsStyle(PluginCall call) {
        try {
            JSObject options = call.getObject("options", new JSObject());

            // Handle shorthand style/color (applies to both bars)
            String globalStyle = call.getString("style");
            String globalColor = call.getString("color");

            // Handle individual bar configurations
            JSObject statusBarConfig = null;
            JSObject navigationBarConfig = null;

            assert options != null;
            if (options.has("statusBar")) {
                try {
                    statusBarConfig = options.getJSObject("statusBar");
                } catch (Exception e) {
                    // Handle if statusBar is not a JSObject
                }
            }

            if (options.has("navigationBar")) {
                try {
                    navigationBarConfig = options.getJSObject("navigationBar");
                } catch (Exception e) {
                    // Handle if navigationBar is not a JSObject
                }
            }

            // Determine final configurations
            String statusStyle = globalStyle;
            String statusColor = globalColor;
            String navStyle = globalStyle;
            String navColor = globalColor;

            // Override with individual configs if provided
            if (statusBarConfig != null) {
                if (statusBarConfig.has("style")) {
                    statusStyle = statusBarConfig.getString("style");
                }
                if (statusBarConfig.has("color")) {
                    statusColor = statusBarConfig.getString("color");
                }
            }

            if (navigationBarConfig != null) {
                if (navigationBarConfig.has("style")) {
                    navStyle = navigationBarConfig.getString("style");
                }
                if (navigationBarConfig.has("color")) {
                    navColor = navigationBarConfig.getString("color");
                }
            }

            // Apply configurations
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

            call.resolve();
        } catch (Exception e) {
            call.reject("Failed to set system bars style", e);
        }
    }

    // === NEW CLEAR METHOD NAMES ===

    @PluginMethod
    public void setStatusBarStyle(PluginCall call) {
        String style = call.getString("style", "DEFAULT");
        String color = call.getString("color");

        try {
            systemBarsManager.setStatusBarStyle(style, color);
            call.resolve();
        } catch (Exception e) {
            call.reject("Failed to set status bar style", e);
        }
    }

    @PluginMethod
    public void hideStatusBar(PluginCall call) {
        try {
            systemBarsManager.hideStatusBar();
            call.resolve();
        } catch (Exception e) {
            call.reject("Failed to hide status bar", e);
        }
    }

    @PluginMethod
    public void showStatusBar(PluginCall call) {
        try {
            systemBarsManager.showStatusBar();
            call.resolve();
        } catch (Exception e) {
            call.reject("Failed to show status bar", e);
        }
    }

    @PluginMethod
    public void isFullscreenActive(PluginCall call) {
        try {
            boolean active = fullscreenManager.isFullscreenActive();
            JSObject result = new JSObject();
            result.put("active", active);
            call.resolve(result);
        } catch (Exception e) {
            call.reject("Failed to check fullscreen status", e);
        }
    }

    @PluginMethod
    public void forceExitFullscreen(PluginCall call) {
        try {
            fullscreenManager.forceExit();
            call.resolve();
        } catch (Exception e) {
            call.reject("Failed to force exit fullscreen", e);
        }
    }

    // Getter methods for lifecycle handler
    public SystemBarsManager getSystemBarsManager() {
        return systemBarsManager;
    }

    public WebViewPaddingManager getPaddingManager() {
        return paddingManager;
    }

    public FullscreenManager getFullscreenManager() {
        return fullscreenManager;
    }

    @Override
    protected void handleOnPause() {
        super.handleOnPause();
        if (lifecycleHandler != null) {
            lifecycleHandler.onPause();
        }
    }

    @Override
    protected void handleOnResume() {
        super.handleOnResume();
        if (lifecycleHandler != null) {
            lifecycleHandler.onResume();
        }
    }
}
