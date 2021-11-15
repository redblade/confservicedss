import { Moment } from 'moment';
import { IApp } from 'app/shared/model/app.model';

export interface IAppReport {
  id?: number;
  timestamp?: Moment;
  group?: string;
  category?: string;
  key?: string;
  value?: number;
  app?: IApp;
}

export class AppReport implements IAppReport {
  constructor(
    public id?: number,
    public timestamp?: Moment,
    public group?: string,
    public category?: string,
    public key?: string,
    public value?: number,
    public app?: IApp
  ) {}
}
