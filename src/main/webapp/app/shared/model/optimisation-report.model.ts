export interface IOptimisationReport {
  id?: number;
  optimisationType?: string;
  appName?: string;
  serviceName?: string;
  servicePriority?: number;
  requestCpu?: number;
  requestMem?: number;
  startupTime?: number;
  node?: string;
  nodeCategory?: string;
  optimisationScore?: string;
  serviceProvider?: string;
}

export class OptimisationReport implements IOptimisationReport {
  constructor(
    public id?: number,
    public optimisationType?: string,
    public appName?: string,
    public serviceName?: string,
    public servicePriority?: number,
    public requestCpu?: number,
    public requestMem?: number,
    public startupTime?: number,
    public node?: string,
    public nodeCategory?: string,
    public optimisationScore?: string,
    public serviceProvider?: string
  ) {}
}
