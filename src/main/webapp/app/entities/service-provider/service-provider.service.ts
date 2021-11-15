import { Injectable } from '@angular/core';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { Observable } from 'rxjs';

import { SERVER_API_URL } from 'app/app.constants';
import { createRequestOption } from 'app/shared/util/request-util';
import { IServiceProvider } from 'app/shared/model/service-provider.model';

type EntityResponseType = HttpResponse<IServiceProvider>;
type EntityArrayResponseType = HttpResponse<IServiceProvider[]>;

@Injectable({ providedIn: 'root' })
export class ServiceProviderService {
  public resourceUrl = SERVER_API_URL + 'api/service-providers';

  constructor(protected http: HttpClient) {}

  create(serviceProvider: IServiceProvider): Observable<EntityResponseType> {
    return this.http.post<IServiceProvider>(this.resourceUrl, serviceProvider, { observe: 'response' });
  }

  update(serviceProvider: IServiceProvider): Observable<EntityResponseType> {
    return this.http.put<IServiceProvider>(this.resourceUrl, serviceProvider, { observe: 'response' });
  }

  find(id: number): Observable<EntityResponseType> {
    return this.http.get<IServiceProvider>(`${this.resourceUrl}/${id}`, { observe: 'response' });
  }

  query(req?: any): Observable<EntityArrayResponseType> {
    const options = createRequestOption(req);
    return this.http.get<IServiceProvider[]>(this.resourceUrl, { params: options, observe: 'response' });
  }

  delete(id: number): Observable<HttpResponse<{}>> {
    return this.http.delete(`${this.resourceUrl}/${id}`, { observe: 'response' });
  }
}
