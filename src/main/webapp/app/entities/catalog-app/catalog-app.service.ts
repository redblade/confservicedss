import { Injectable } from '@angular/core';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { Observable } from 'rxjs';

import { SERVER_API_URL } from 'app/app.constants';
import { createRequestOption } from 'app/shared/util/request-util';
import { ICatalogApp } from 'app/shared/model/catalog-app.model';

type EntityResponseType = HttpResponse<ICatalogApp>;
type EntityArrayResponseType = HttpResponse<ICatalogApp[]>;

@Injectable({ providedIn: 'root' })
export class CatalogAppService {
  public resourceUrl = SERVER_API_URL + 'api/catalog-apps';

  constructor(protected http: HttpClient) {}

  create(catalogApp: ICatalogApp): Observable<EntityResponseType> {
    return this.http.post<ICatalogApp>(this.resourceUrl, catalogApp, { observe: 'response' });
  }

  update(catalogApp: ICatalogApp): Observable<EntityResponseType> {
    return this.http.put<ICatalogApp>(this.resourceUrl, catalogApp, { observe: 'response' });
  }

  find(id: number): Observable<EntityResponseType> {
    return this.http.get<ICatalogApp>(`${this.resourceUrl}/${id}`, { observe: 'response' });
  }

  query(req?: any): Observable<EntityArrayResponseType> {
    const options = createRequestOption(req);
    return this.http.get<ICatalogApp[]>(this.resourceUrl, { params: options, observe: 'response' });
  }

  delete(id: number): Observable<HttpResponse<{}>> {
    return this.http.delete(`${this.resourceUrl}/${id}`, { observe: 'response' });
  }
}
