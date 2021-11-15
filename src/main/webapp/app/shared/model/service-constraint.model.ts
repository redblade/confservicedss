import { IService } from 'app/shared/model/service.model';

export interface IServiceConstraint {
  id?: number;
  name?: string;
  category?: string;
  value?: string;
  valueType?: string;
  priority?: number;
  service?: IService;
}

export class ServiceConstraint implements IServiceConstraint {
  constructor(
    public id?: number,
    public name?: string,
    public category?: string,
    public value?: string,
    public valueType?: string,
    public priority?: number,
    public service?: IService
  ) {}
}
