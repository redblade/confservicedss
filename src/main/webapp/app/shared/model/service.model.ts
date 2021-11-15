import { ICriticalService } from 'app/shared/model/critical-service.model';
import { ISteadyService } from 'app/shared/model/steady-service.model';
import { IAppConstraint } from 'app/shared/model/app-constraint.model';
import { IServiceReport } from 'app/shared/model/service-report.model';
import { IServiceConstraint } from 'app/shared/model/service-constraint.model';
import { ISla } from 'app/shared/model/sla.model';
import { IServiceOptimisation } from 'app/shared/model/service-optimisation.model';
import { IApp } from 'app/shared/model/app.model';
import { DeployType } from 'app/shared/model/enumerations/deploy-type.model';
import { ExecStatus } from 'app/shared/model/enumerations/exec-status.model';

export interface IService {
  id?: number;
  name?: string;
  profile?: string;
  priority?: number;
  initialConfiguration?: string;
  runtimeConfiguration?: string;
  deployType?: DeployType;
  deployDescriptor?: string;
  status?: ExecStatus;
  criticalServiceSets?: ICriticalService[];
  steadyServiceSets?: ISteadyService[];
  appConstraintSourceSets?: IAppConstraint[];
  appConstraintDestinationSets?: IAppConstraint[];
  serviceReportSets?: IServiceReport[];
  serviceConstraintSets?: IServiceConstraint[];
  slaSets?: ISla[];
  serviceOptimisation?: IServiceOptimisation;
  app?: IApp;
}

export class Service implements IService {
  constructor(
    public id?: number,
    public name?: string,
    public profile?: string,
    public priority?: number,
    public initialConfiguration?: string,
    public runtimeConfiguration?: string,
    public deployType?: DeployType,
    public deployDescriptor?: string,
    public status?: ExecStatus,
    public criticalServiceSets?: ICriticalService[],
    public steadyServiceSets?: ISteadyService[],
    public appConstraintSourceSets?: IAppConstraint[],
    public appConstraintDestinationSets?: IAppConstraint[],
    public serviceReportSets?: IServiceReport[],
    public serviceConstraintSets?: IServiceConstraint[],
    public slaSets?: ISla[],
    public serviceOptimisation?: IServiceOptimisation,
    public app?: IApp
  ) {}
}
