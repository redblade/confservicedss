import { IApp } from 'app/shared/model/app.model';
import { IServiceProvider } from 'app/shared/model/service-provider.model';

export interface ICatalogApp {
  id?: number;
  name?: string;
  appDescriptor?: string;
  appSets?: IApp[];
  serviceProvider?: IServiceProvider;
}

export class CatalogApp implements ICatalogApp {
  constructor(
    public id?: number,
    public name?: string,
    public appDescriptor?: string,
    public appSets?: IApp[],
    public serviceProvider?: IServiceProvider
  ) {}
}
