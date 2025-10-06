import { WebPlugin } from '@capacitor/core';

import type { SystemBarsManagerPlugin } from './definitions';

export class SystemBarsManagerWeb extends WebPlugin implements SystemBarsManagerPlugin {
  async echo(options: { value: string }): Promise<{ value: string }> {
    console.log('ECHO', options);
    return options;
  }
}
