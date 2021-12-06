import { IService } from 'app/shared/model/service.model';
import { IBenchmark } from 'app/shared/model/benchmark.model';

export interface IServiceBenchmarkMatch {
  id?: number;
  rationale?: string;
  service?: IService;
  benchmark?: IBenchmark;
}

export class ServiceBenchmarkMatch implements IServiceBenchmarkMatch {
  constructor(public id?: number, public rationale?: string, public service?: IService, public benchmark?: IBenchmark) {}
}
