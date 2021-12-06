import { INode } from 'app/shared/model/node.model';
import { IBenchmark } from 'app/shared/model/benchmark.model';

export interface IBenchmarkSummary {
  id?: number;
  score?: number;
  node?: INode;
  benchmark?: IBenchmark;
}

export class BenchmarkSummary implements IBenchmarkSummary {
  constructor(public id?: number, public score?: number, public node?: INode, public benchmark?: IBenchmark) {}
}
