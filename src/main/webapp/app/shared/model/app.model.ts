import { IService } from 'app/shared/model/service.model';
import { IServiceProvider } from 'app/shared/model/service-provider.model';
import { ICatalogApp } from 'app/shared/model/catalog-app.model';
import { ManagementType } from 'app/shared/model/enumerations/management-type.model';
import { ExecStatus } from 'app/shared/model/enumerations/exec-status.model';

export interface IApp {
  id?: number;
  name?: string;
  managementType?: ManagementType;
  status?: ExecStatus;
  appDescriptor?: string;
  serviceSets?: IService[];
  serviceProvider?: IServiceProvider;
  catalogApp?: ICatalogApp;
}

export class App implements IApp {
  constructor(
    public id?: number,
    public name?: string,
    public managementType?: ManagementType,
    public status?: ExecStatus,
    public appDescriptor?: string,
    public serviceSets?: IService[],
    public serviceProvider?: IServiceProvider,
    public catalogApp?: ICatalogApp
  ) {}
}
