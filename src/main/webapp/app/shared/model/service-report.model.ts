import { Moment } from 'moment';
import { IService } from 'app/shared/model/service.model';

export interface IServiceReport {
  id?: number;
  timestamp?: Moment;
  group?: string;
  category?: string;
  key?: string;
  value?: number;
  service?: IService;
}

export class ServiceReport implements IServiceReport {
  constructor(
    public id?: number,
    public timestamp?: Moment,
    public group?: string,
    public category?: string,
    public key?: string,
    public value?: number,
    public service?: IService
  ) {}
}
