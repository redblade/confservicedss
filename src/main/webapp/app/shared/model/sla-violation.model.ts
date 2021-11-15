import { Moment } from 'moment';
import { ISla } from 'app/shared/model/sla.model';
import { SlaViolationType } from 'app/shared/model/enumerations/sla-violation-type.model';

export interface ISlaViolation {
  id?: number;
  timestamp?: Moment;
  violationName?: string;
  severityType?: SlaViolationType;
  description?: string;
  status?: string;
  sla?: ISla;
}

export class SlaViolation implements ISlaViolation {
  constructor(
    public id?: number,
    public timestamp?: Moment,
    public violationName?: string,
    public severityType?: SlaViolationType,
    public description?: string,
    public status?: string,
    public sla?: ISla
  ) {}
}
