import { registerPlugin } from '@capacitor/core';

import type { SystemBarsManagerPlugin } from './definitions';

const SystemBarsManager = registerPlugin<SystemBarsManagerPlugin>('SystemBarsManager', {
  web: () => import('./web').then((m) => new m.SystemBarsManagerWeb()),
});

export * from './definitions';
export { SystemBarsManager };
