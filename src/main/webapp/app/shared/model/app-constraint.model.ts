import { IService } from 'app/shared/model/service.model';

export interface IAppConstraint {
  id?: number;
  name?: string;
  category?: string;
  value?: string;
  valueType?: string;
  serviceSource?: IService;
  serviceDestination?: IService;
}

export class AppConstraint implements IAppConstraint {
  constructor(
    public id?: number,
    public name?: string,
    public category?: string,
    public value?: string,
    public valueType?: string,
    public serviceSource?: IService,
    public serviceDestination?: IService
  ) {}
}
