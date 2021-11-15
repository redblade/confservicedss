import { IApp } from 'app/shared/model/app.model';

export interface IAppDeploymentOptions {
  id?: number;
  options?: string;
  app?: IApp;
}

export class AppDeploymentOptions implements IAppDeploymentOptions {
  constructor(public id?: number, public options?: string, public app?: IApp) {}
}
