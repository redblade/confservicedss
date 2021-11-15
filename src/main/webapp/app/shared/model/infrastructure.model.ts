import { INode } from 'app/shared/model/node.model';
import { IBenchmark } from 'app/shared/model/benchmark.model';
import { IInfrastructureReport } from 'app/shared/model/infrastructure-report.model';
import { IProject } from 'app/shared/model/project.model';
import { IInfrastructureProvider } from 'app/shared/model/infrastructure-provider.model';

export interface IInfrastructure {
  id?: number;
  name?: string;
  type?: string;
  endpoint?: string;
  credentials?: string;
  monitoringPlugin?: string;
  properties?: string;
  totalResources?: string;
  nodeSets?: INode[];
  benchmarkSets?: IBenchmark[];
  infrastructureReportSets?: IInfrastructureReport[];
  projectSets?: IProject[];
  infrastructureProvider?: IInfrastructureProvider;
}

export class Infrastructure implements IInfrastructure {
  constructor(
    public id?: number,
    public name?: string,
    public type?: string,
    public endpoint?: string,
    public credentials?: string,
    public monitoringPlugin?: string,
    public properties?: string,
    public totalResources?: string,
    public nodeSets?: INode[],
    public benchmarkSets?: IBenchmark[],
    public infrastructureReportSets?: IInfrastructureReport[],
    public projectSets?: IProject[],
    public infrastructureProvider?: IInfrastructureProvider
  ) {}
}
