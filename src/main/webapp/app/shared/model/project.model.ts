import { IInfrastructure } from 'app/shared/model/infrastructure.model';
import { IServiceProvider } from 'app/shared/model/service-provider.model';

export interface IProject {
  id?: number;
  name?: string;
  group?: string;
  properties?: string;
  quotaCpuMillicore?: number;
  quotaMemMB?: number;
  quotaDiskGB?: number;
  credentials?: string;
  enableBenchmark?: boolean;
  privateBenchmark?: boolean;
  infrastructure?: IInfrastructure;
  serviceProvider?: IServiceProvider;
}

export class Project implements IProject {
  constructor(
    public id?: number,
    public name?: string,
    public group?: string,
    public properties?: string,
    public quotaCpuMillicore?: number,
    public quotaMemMB?: number,
    public quotaDiskGB?: number,
    public credentials?: string,
    public enableBenchmark?: boolean,
    public privateBenchmark?: boolean,
    public infrastructure?: IInfrastructure,
    public serviceProvider?: IServiceProvider
  ) {
    this.enableBenchmark = this.enableBenchmark || false;
    this.privateBenchmark = this.privateBenchmark || false;
  }
}
