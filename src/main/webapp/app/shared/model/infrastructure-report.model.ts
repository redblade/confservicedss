import { Moment } from 'moment';
import { IInfrastructure } from 'app/shared/model/infrastructure.model';

export interface IInfrastructureReport {
  id?: number;
  timestamp?: Moment;
  group?: string;
  category?: string;
  key?: string;
  value?: number;
  infrastructure?: IInfrastructure;
}

export class InfrastructureReport implements IInfrastructureReport {
  constructor(
    public id?: number,
    public timestamp?: Moment,
    public group?: string,
    public category?: string,
    public key?: string,
    public value?: number,
    public infrastructure?: IInfrastructure
  ) {}
}
