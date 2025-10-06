import { AndroidSystemBars } from 'capacitor-android-system-bars';

class SystemBarsExample {
    constructor() {
        this.isInitialized = false;
        this.deviceInfo = null;
    }

    async initialize() {
        try {
            // Initialize the plugin
            this.deviceInfo = await AndroidSystemBars.initialize();
            console.log('📱 Device Info:', this.deviceInfo);

            this.isInitialized = true;
            this.updateUI();

            // Set initial style with edge-to-edge support
            await this.setTheme('light');

        } catch (error) {
            console.error('❌ Failed to initialize AndroidSystemBars:', error);
        }
    }

    async setTheme(theme) {
        if (!this.isInitialized) return;

        try {
            const style = theme === 'dark' ? 'DARK' : 'LIGHT';
            const color = theme === 'dark' ? '#111827' : '#ffffff';

            await AndroidSystemBars.setStyle({ style, color });

            // Also set navigation bar style
            await AndroidSystemBars.setNavigationBarStyle({ style, color });

            console.log(`🎨 Theme set to ${theme}`);
            this.logToUI(`Theme set to ${theme}`);
        } catch (error) {
            console.error('❌ Failed to set theme:', error);
            this.logToUI(`Failed to set theme: ${error.message}`);
        }
    }

    async enterFullscreen() {
        if (!this.isInitialized) return;

        try {
            await AndroidSystemBars.enterFullscreen({ mode: 'IMMERSIVE' });
            console.log('🔍 Entered fullscreen mode');
            this.logToUI('Entered fullscreen mode');
        } catch (error) {
            console.error('❌ Failed to enter fullscreen:', error);
            this.logToUI(`Failed to enter fullscreen: ${error.message}`);
        }
    }

    async exitFullscreen() {
        if (!this.isInitialized) return;

        try {
            await AndroidSystemBars.exitFullscreen({
                style: 'LIGHT',
                color: '#ffffff'
            });
            console.log('🔍 Exited fullscreen mode');
            this.logToUI('Exited fullscreen mode');
        } catch (error) {
            console.error('❌ Failed to exit fullscreen:', error);
            this.logToUI(`Failed to exit fullscreen: ${error.message}`);
        }
    }

    async toggleStatusBar() {
        if (!this.isInitialized) return;

        try {
            const insets = await AndroidSystemBars.getInsets();

            if (insets.statusBarVisible) {
                await AndroidSystemBars.hide();
                console.log('📱 Status bar hidden');
                this.logToUI('Status bar hidden');
            } else {
                await AndroidSystemBars.show();
                console.log('📱 Status bar shown');
                this.logToUI('Status bar shown');
            }
        } catch (error) {
            console.error('❌ Failed to toggle status bar:', error);
            this.logToUI(`Failed to toggle status bar: ${error.message}`);
        }
    }

    async testEdgeToEdge() {
        if (!this.isInitialized) return;

        try {
            if (this.deviceInfo.isAndroid35Plus) {
                await AndroidSystemBars.setOverlay({ overlay: true });
                console.log('🚀 Edge-to-edge enabled (Android 35+)');
                this.logToUI('Edge-to-edge enabled (Android 35+)');
            } else {
                console.log('ℹ️ Edge-to-edge not available (Android < 35)');
                this.logToUI('Edge-to-edge not available (Android < 35). Plugin handles webview padding automatically.');
            }
        } catch (error) {
            console.error('❌ Failed to enable edge-to-edge:', error);
            this.logToUI(`Failed to enable edge-to-edge: ${error.message}`);
        }
    }



    updateUI() {
        if (this.deviceInfo) {
            document.getElementById('api-level').innerText = this.deviceInfo.apiLevel.toString();
            document.getElementById('android-35-plus').innerText = this.deviceInfo.isAndroid35Plus ? 'Yes' : 'No';
            document.getElementById('edge-to-edge-support').innerText = this.deviceInfo.supportsEdgeToEdge ? 'Yes' : 'No';
            document.getElementById('window-insets-support').innerText = this.deviceInfo.supportsWindowInsets ? 'Yes' : 'No';
            document.getElementById('status-bar-height').innerText = `${this.deviceInfo.statusBarHeight}px`;
            document.getElementById('navigation-bar-height').innerText = `${this.deviceInfo.navigationBarHeight}px`;
        }
    }

    logToUI(message) {
        const logElement = document.getElementById('log');
        if (logElement) {
            const timestamp = new Date().toLocaleTimeString();
            logElement.innerHTML += `<div>[${timestamp}] ${message}</div>`;
            logElement.scrollTop = logElement.scrollHeight;
        }
    }
}

// Initialize when page loads
const systemBars = new SystemBarsExample();

document.addEventListener('DOMContentLoaded', async () => {
    await systemBars.initialize();

    // Set up event listeners
    window.setLightTheme = () => systemBars.setTheme('light');
    window.setDarkTheme = () => systemBars.setTheme('dark');
    window.enterFullscreen = () => systemBars.enterFullscreen();
    window.exitFullscreen = () => systemBars.exitFullscreen();
    window.toggleStatusBar = () => systemBars.toggleStatusBar();
    window.testEdgeToEdge = () => systemBars.testEdgeToEdge();
});
