import { registerPlugin } from '@capacitor/core';

import type { AndroidSystemBarsPlugin } from './definitions';

const AndroidSystemBars = registerPlugin<AndroidSystemBarsPlugin>('AndroidSystemBars', {
  web: () => import('./web').then((m) => new m.AndroidSystemBarsWeb()),
});

export * from './definitions';
export { AndroidSystemBars };
