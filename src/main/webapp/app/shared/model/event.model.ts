import { Moment } from 'moment';
import { IServiceProvider } from 'app/shared/model/service-provider.model';

export interface IEvent {
  id?: number;
  timestamp?: Moment;
  severity?: string;
  category?: string;
  details?: string;
  serviceProvider?: IServiceProvider;
}

export class Event implements IEvent {
  constructor(
    public id?: number,
    public timestamp?: Moment,
    public severity?: string,
    public category?: string,
    public details?: string,
    public serviceProvider?: IServiceProvider
  ) {}
}
