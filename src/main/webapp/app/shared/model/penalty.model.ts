import { IGuarantee } from 'app/shared/model/guarantee.model';

export interface IPenalty {
  id?: number;
  name?: string;
  type?: string;
  value?: number;
  guarantee?: IGuarantee;
}

export class Penalty implements IPenalty {
  constructor(public id?: number, public name?: string, public type?: string, public value?: number, public guarantee?: IGuarantee) {}
}
