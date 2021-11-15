import { Moment } from 'moment';
import { ISlaViolation } from 'app/shared/model/sla-violation.model';
import { IGuarantee } from 'app/shared/model/guarantee.model';
import { IInfrastructureProvider } from 'app/shared/model/infrastructure-provider.model';
import { IServiceProvider } from 'app/shared/model/service-provider.model';
import { IService } from 'app/shared/model/service.model';

export interface ISla {
  id?: number;
  name?: string;
  type?: string;
  creation?: Moment;
  expiration?: Moment;
  slaViolationSets?: ISlaViolation[];
  guaranteeSets?: IGuarantee[];
  infrastructureProvider?: IInfrastructureProvider;
  serviceProvider?: IServiceProvider;
  service?: IService;
}

export class Sla implements ISla {
  constructor(
    public id?: number,
    public name?: string,
    public type?: string,
    public creation?: Moment,
    public expiration?: Moment,
    public slaViolationSets?: ISlaViolation[],
    public guaranteeSets?: IGuarantee[],
    public infrastructureProvider?: IInfrastructureProvider,
    public serviceProvider?: IServiceProvider,
    public service?: IService
  ) {}
}
