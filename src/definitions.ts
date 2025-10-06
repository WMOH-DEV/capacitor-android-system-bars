export interface SystemBarsManagerPlugin {
  echo(options: { value: string }): Promise<{ value: string }>;
}
