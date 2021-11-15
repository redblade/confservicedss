import { Injectable } from '@angular/core';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { Observable } from 'rxjs';

import { SERVER_API_URL } from 'app/app.constants';
import { createRequestOption } from 'app/shared/util/request-util';
import { IInfrastructureProvider } from 'app/shared/model/infrastructure-provider.model';

type EntityResponseType = HttpResponse<IInfrastructureProvider>;
type EntityArrayResponseType = HttpResponse<IInfrastructureProvider[]>;

@Injectable({ providedIn: 'root' })
export class InfrastructureProviderService {
  public resourceUrl = SERVER_API_URL + 'api/infrastructure-providers';

  constructor(protected http: HttpClient) {}

  create(infrastructureProvider: IInfrastructureProvider): Observable<EntityResponseType> {
    return this.http.post<IInfrastructureProvider>(this.resourceUrl, infrastructureProvider, { observe: 'response' });
  }

  update(infrastructureProvider: IInfrastructureProvider): Observable<EntityResponseType> {
    return this.http.put<IInfrastructureProvider>(this.resourceUrl, infrastructureProvider, { observe: 'response' });
  }

  find(id: number): Observable<EntityResponseType> {
    return this.http.get<IInfrastructureProvider>(`${this.resourceUrl}/${id}`, { observe: 'response' });
  }

  query(req?: any): Observable<EntityArrayResponseType> {
    const options = createRequestOption(req);
    return this.http.get<IInfrastructureProvider[]>(this.resourceUrl, { params: options, observe: 'response' });
  }

  delete(id: number): Observable<HttpResponse<{}>> {
    return this.http.delete(`${this.resourceUrl}/${id}`, { observe: 'response' });
  }
}
