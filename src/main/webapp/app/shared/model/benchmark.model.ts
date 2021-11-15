import { IBenchmarkReport } from 'app/shared/model/benchmark-report.model';
import { IInfrastructure } from 'app/shared/model/infrastructure.model';
import { IServiceProvider } from 'app/shared/model/service-provider.model';

export interface IBenchmark {
  id?: number;
  name?: string;
  category?: string;
  benchmarkReportSets?: IBenchmarkReport[];
  infrastructure?: IInfrastructure;
  serviceProvider?: IServiceProvider;
}

export class Benchmark implements IBenchmark {
  constructor(
    public id?: number,
    public name?: string,
    public category?: string,
    public benchmarkReportSets?: IBenchmarkReport[],
    public infrastructure?: IInfrastructure,
    public serviceProvider?: IServiceProvider
  ) {}
}
