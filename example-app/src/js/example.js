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

            // ✨ NEW: Set both status and navigation bar in ONE call!
            await AndroidSystemBars.setSystemBarsStyle({
                style,
                color
            });

            console.log(`🎨 Theme set to ${theme} (both status & navigation bars)`);
            this.logToUI(`Theme set to ${theme} (both status & navigation bars)`);
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
            // ✨ NEW: Clear API - specify what to restore after fullscreen
            await AndroidSystemBars.exitFullscreen({
                restore: {
                    style: 'LIGHT',      // Apply to both status & navigation
                    color: '#ffffff'     // Apply to both status & navigation
                }
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
                // ✨ NEW: Clear method names
                await AndroidSystemBars.hideStatusBar();
                console.log('📱 Status bar hidden');
                this.logToUI('Status bar hidden');
            } else {
                await AndroidSystemBars.showStatusBar();
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

    async toggleNavigationBar() {
        if (!this.isInitialized) return;

        try {
            const insets = await AndroidSystemBars.getInsets();

            if (insets.navigationBarVisible) {
                await AndroidSystemBars.hideNavigationBar();
                console.log('🔽 Navigation bar hidden');
                this.logToUI('Navigation bar hidden');
            } else {
                await AndroidSystemBars.showNavigationBar();
                console.log('🔼 Navigation bar shown');
                this.logToUI('Navigation bar shown');
            }
        } catch (error) {
            console.error('❌ Failed to toggle navigation bar:', error);
            this.logToUI(`Failed to toggle navigation bar: ${error.message}`);
        }
    }

    async testNavigationBarColors() {
        if (!this.isInitialized) return;

        try {
            // Test different navigation bar colors
            const colors = [
                { name: 'Red', color: '#ef4444', style: 'LIGHT' },
                { name: 'Blue', color: '#3b82f6', style: 'LIGHT' },
                { name: 'Green', color: '#22c55e', style: 'LIGHT' },
                { name: 'Dark Gray', color: '#374151', style: 'DARK' }
            ];

            for (const colorTest of colors) {
                // ✨ NEW: Can use unified API or individual control
                await AndroidSystemBars.setNavigationBarStyle({
                    style: colorTest.style,
                    color: this.deviceInfo.isAndroid35Plus ? undefined : colorTest.color
                });

                const message = this.deviceInfo.isAndroid35Plus
                    ? `Navigation bar icons: ${colorTest.style} (Android 15+ - color is transparent)`
                    : `Navigation bar: ${colorTest.name} (${colorTest.color})`;

                console.log(`🎨 ${message}`);
                this.logToUI(message);

                // Wait 1.5 seconds between color changes
                await new Promise(resolve => setTimeout(resolve, 1500));
            }

            // Reset to default using unified API
            await AndroidSystemBars.setSystemBarsStyle({
                navigationBar: {
                    style: 'LIGHT',
                    color: this.deviceInfo.isAndroid35Plus ? undefined : '#ffffff'
                }
            });

            this.logToUI('Navigation bar reset to default');

        } catch (error) {
            console.error('❌ Failed to test navigation bar colors:', error);
            this.logToUI(`Failed to test navigation bar colors: ${error.message}`);
        }
    }

    async getInsets() {
        if (!this.isInitialized) return;

        try {
            const insets = await AndroidSystemBars.getInsets();
            console.log('📐 Window Insets:', insets);
            this.logToUI(`Insets - Top: ${insets.top}px, Bottom: ${insets.bottom}px, Left: ${insets.left}px, Right: ${insets.right}px`);
        } catch (error) {
            console.error('❌ Failed to get insets:', error);
            this.logToUI(`Failed to get insets: ${error.message}`);
        }
    }

    // ✨ NEW: Demonstrate the flexibility of the new API
    async demonstrateNewAPI() {
        if (!this.isInitialized) return;

        try {
            this.logToUI('🚀 Demonstrating New API Flexibility:');

            // Example 1: Set both bars with same style/color (shorthand)
            await AndroidSystemBars.setSystemBarsStyle({
                style: 'DARK',
                color: '#1f2937'
            });
            this.logToUI('✅ Both bars set to dark theme (shorthand)');
            await new Promise(resolve => setTimeout(resolve, 1500));

            // Example 2: Set bars with different styles (individual control)
            await AndroidSystemBars.setSystemBarsStyle({
                statusBar: { style: 'LIGHT', color: '#ffffff' },
                navigationBar: { style: 'DARK', color: '#111827' }
            });
            this.logToUI('✅ Status bar light, Navigation bar dark');
            await new Promise(resolve => setTimeout(resolve, 1500));

            // Example 3: Set only status bar (precise control)
            await AndroidSystemBars.setStatusBarStyle({
                style: 'DARK',
                color: '#ef4444'
            });
            this.logToUI('✅ Only status bar changed to red');
            await new Promise(resolve => setTimeout(resolve, 1500));

            // Example 4: Reset to default
            await AndroidSystemBars.setSystemBarsStyle({
                style: 'LIGHT',
                color: '#ffffff'
            });
            this.logToUI('✅ Both bars reset to light theme');

        } catch (error) {
            console.error('❌ Failed to demonstrate new API:', error);
            this.logToUI(`Failed to demonstrate new API: ${error.message}`);
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
    window.toggleNavigationBar = () => systemBars.toggleNavigationBar();
    window.testNavigationBarColors = () => systemBars.testNavigationBarColors();
    window.getInsets = () => systemBars.getInsets();
    window.testEdgeToEdge = () => systemBars.testEdgeToEdge();
    window.demonstrateNewAPI = () => systemBars.demonstrateNewAPI();
});
