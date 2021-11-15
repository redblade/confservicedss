import { IBenchmarkReport } from 'app/shared/model/benchmark-report.model';
import { INodeReport } from 'app/shared/model/node-report.model';
import { IInfrastructure } from 'app/shared/model/infrastructure.model';

export interface INode {
  id?: number;
  name?: string;
  ipaddress?: string;
  properties?: string;
  features?: string;
  totalResources?: string;
  benchmarkReportSets?: IBenchmarkReport[];
  nodeReportSets?: INodeReport[];
  nodeReportDestinationSets?: INodeReport[];
  infrastructure?: IInfrastructure;
}

export class Node implements INode {
  constructor(
    public id?: number,
    public name?: string,
    public ipaddress?: string,
    public properties?: string,
    public features?: string,
    public totalResources?: string,
    public benchmarkReportSets?: IBenchmarkReport[],
    public nodeReportSets?: INodeReport[],
    public nodeReportDestinationSets?: INodeReport[],
    public infrastructure?: IInfrastructure
  ) {}
}
