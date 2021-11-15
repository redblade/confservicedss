import { ICatalogApp } from 'app/shared/model/catalog-app.model';

export interface ICatalogService {
  id?: number;
  name?: string;
  serviceDescriptor?: string;
  catalogApp?: ICatalogApp;
}

export class CatalogService implements ICatalogService {
  constructor(public id?: number, public name?: string, public serviceDescriptor?: string, public catalogApp?: ICatalogApp) {}
}
