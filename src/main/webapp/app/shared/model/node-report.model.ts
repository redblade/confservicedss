import { Moment } from 'moment';
import { INode } from 'app/shared/model/node.model';

export interface INodeReport {
  id?: number;
  timestamp?: Moment;
  category?: string;
  key?: string;
  value?: number;
  node?: INode;
  nodeDestination?: INode;
}

export class NodeReport implements INodeReport {
  constructor(
    public id?: number,
    public timestamp?: Moment,
    public category?: string,
    public key?: string,
    public value?: number,
    public node?: INode,
    public nodeDestination?: INode
  ) {}
}
