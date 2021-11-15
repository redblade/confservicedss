import { ISla } from 'app/shared/model/sla.model';
import { IInfrastructure } from 'app/shared/model/infrastructure.model';

export interface IInfrastructureProvider {
  id?: number;
  name?: string;
  organisation?: string;
  slaSets?: ISla[];
  infrastructureSets?: IInfrastructure[];
}

export class InfrastructureProvider implements IInfrastructureProvider {
  constructor(
    public id?: number,
    public name?: string,
    public organisation?: string,
    public slaSets?: ISla[],
    public infrastructureSets?: IInfrastructure[]
  ) {}
}
