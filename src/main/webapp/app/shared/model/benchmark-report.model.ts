import { Moment } from 'moment';
import { INode } from 'app/shared/model/node.model';
import { IBenchmark } from 'app/shared/model/benchmark.model';

export interface IBenchmarkReport {
  id?: number;
  time?: Moment;
  metric?: string;
  tool?: string;
  mean?: number;
  interval?: number;
  stabilityIndex?: number;
  node?: INode;
  benchmark?: IBenchmark;
}

export class BenchmarkReport implements IBenchmarkReport {
  constructor(
    public id?: number,
    public time?: Moment,
    public metric?: string,
    public tool?: string,
    public mean?: number,
    public interval?: number,
    public stabilityIndex?: number,
    public node?: INode,
    public benchmark?: IBenchmark
  ) {}
}
