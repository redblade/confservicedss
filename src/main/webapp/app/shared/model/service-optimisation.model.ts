import { IService } from 'app/shared/model/service.model';

export interface IServiceOptimisation {
  id?: number;
  name?: string;
  optimisation?: string;
  parameters?: string;
  service?: IService;
}

export class ServiceOptimisation implements IServiceOptimisation {
  constructor(
    public id?: number,
    public name?: string,
    public optimisation?: string,
    public parameters?: string,
    public service?: IService
  ) {}
}
