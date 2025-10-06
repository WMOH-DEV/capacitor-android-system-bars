import { WebPlugin } from '@capacitor/core';

import type {
  AndroidSystemBarsPlugin,
  InitializeResult,
  SetStyleOptions,
  EnterFullscreenOptions,
  ExitFullscreenOptions,
  SetOverlayOptions,
  InsetsResult,
  SetNavigationBarStyleOptions,
} from './definitions';

export class AndroidSystemBarsWeb extends WebPlugin implements AndroidSystemBarsPlugin {
  async initialize(): Promise<InitializeResult> {
    // Web implementation - return default values
    return {
      apiLevel: 0, // Not applicable for web
      isAndroid35Plus: false,
      supportsEdgeToEdge: false,
      supportsWindowInsets: false,
      statusBarHeight: 0,
      navigationBarHeight: 0,
    };
  }

  async setStyle(options: SetStyleOptions): Promise<void> {
    console.log('AndroidSystemBars.setStyle called on web platform', options);
    // Web platforms don't have native system bars
  }

  async hide(): Promise<void> {
    console.log('AndroidSystemBars.hide called on web platform');
    // Web platforms don't have native system bars
  }

  async show(): Promise<void> {
    console.log('AndroidSystemBars.show called on web platform');
    // Web platforms don't have native system bars
  }

  async enterFullscreen(options: EnterFullscreenOptions): Promise<void> {
    console.log('AndroidSystemBars.enterFullscreen called on web platform', options);
    // For web, we can use the Fullscreen API
    try {
      if (document.documentElement.requestFullscreen) {
        await document.documentElement.requestFullscreen();
      }
    } catch (error) {
      console.warn('Fullscreen not supported on this browser', error);
    }
  }

  async exitFullscreen(options: ExitFullscreenOptions): Promise<void> {
    console.log('AndroidSystemBars.exitFullscreen called on web platform', options);
    // For web, exit fullscreen
    try {
      if (document.exitFullscreen && document.fullscreenElement) {
        await document.exitFullscreen();
      }
    } catch (error) {
      console.warn('Exit fullscreen failed', error);
    }
  }

  async setOverlay(options: SetOverlayOptions): Promise<void> {
    console.log('AndroidSystemBars.setOverlay called on web platform', options);
    // Not applicable for web
  }

  async getInsets(): Promise<InsetsResult> {
    // Web implementation - return default values
    return {
      top: 0,
      bottom: 0,
      left: 0,
      right: 0,
      statusBarVisible: false,
      navigationBarVisible: false,
    };
  }

  async setNavigationBarStyle(options: SetNavigationBarStyleOptions): Promise<void> {
    console.log('AndroidSystemBars.setNavigationBarStyle called on web platform', options);
    // Web platforms don't have native navigation bars
  }

  async hideNavigationBar(): Promise<void> {
    console.log('AndroidSystemBars.hideNavigationBar called on web platform');
    // Web platforms don't have native navigation bars
  }

  async showNavigationBar(): Promise<void> {
    // Not supported on web
    throw this.unimplemented('Not implemented on web.');
  }
}
