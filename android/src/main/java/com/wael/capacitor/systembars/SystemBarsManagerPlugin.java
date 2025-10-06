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
        String style = call.getString("style", "DEFAULT");
        String color = call.getString("color");

        try {
            fullscreenManager.exitFullscreen(style, color);
            call.resolve();
        } catch (Exception e) {
            call.reject("Failed to exit fullscreen", e);
        }
    }

    @PluginMethod
    public void setOverlay(PluginCall call) {
        boolean overlay = call.getBoolean("overlay", false);

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

    // Getter methods for lifecycle handler
    public SystemBarsManager getSystemBarsManager() {
        return systemBarsManager;
    }

    public WebViewPaddingManager getPaddingManager() {
        return paddingManager;
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
