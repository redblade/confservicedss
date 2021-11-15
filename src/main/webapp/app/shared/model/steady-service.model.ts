import { Moment } from 'moment';
import { IService } from 'app/shared/model/service.model';

export interface ISteadyService {
  id?: number;
  timestampCreated?: Moment;
  timestampProcessed?: Moment;
  actionTaken?: string;
  score?: number;
  details?: string;
  monitoringPeriodSec?: number;
  service?: IService;
}

export class SteadyService implements ISteadyService {
  constructor(
    public id?: number,
    public timestampCreated?: Moment,
    public timestampProcessed?: Moment,
    public actionTaken?: string,
    public score?: number,
    public details?: string,
    public monitoringPeriodSec?: number,
    public service?: IService
  ) {}
}
