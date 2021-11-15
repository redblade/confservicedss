import { IEvent } from 'app/shared/model/event.model';
import { ICatalogApp } from 'app/shared/model/catalog-app.model';
import { IApp } from 'app/shared/model/app.model';
import { IProject } from 'app/shared/model/project.model';
import { ISla } from 'app/shared/model/sla.model';
import { IBenchmark } from 'app/shared/model/benchmark.model';

export interface IServiceProvider {
  id?: number;
  name?: string;
  organisation?: string;
  preferences?: string;
  eventSets?: IEvent[];
  catalogAppSets?: ICatalogApp[];
  appSets?: IApp[];
  projectSets?: IProject[];
  slaSets?: ISla[];
  benchmarkSets?: IBenchmark[];
}

export class ServiceProvider implements IServiceProvider {
  constructor(
    public id?: number,
    public name?: string,
    public organisation?: string,
    public preferences?: string,
    public eventSets?: IEvent[],
    public catalogAppSets?: ICatalogApp[],
    public appSets?: IApp[],
    public projectSets?: IProject[],
    public slaSets?: ISla[],
    public benchmarkSets?: IBenchmark[]
  ) {}
}
