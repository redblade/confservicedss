import { ISla } from 'app/shared/model/sla.model';

export interface IGuarantee {
  id?: number;
  name?: string;
  constraint?: string;
  thresholdWarning?: string;
  thresholdMild?: string;
  thresholdSerious?: string;
  thresholdSevere?: string;
  thresholdCatastrophic?: string;
  sla?: ISla;
}

export class Guarantee implements IGuarantee {
  constructor(
    public id?: number,
    public name?: string,
    public constraint?: string,
    public thresholdWarning?: string,
    public thresholdMild?: string,
    public thresholdSerious?: string,
    public thresholdSevere?: string,
    public thresholdCatastrophic?: string,
    public sla?: ISla
  ) {}
}
