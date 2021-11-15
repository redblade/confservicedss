import { Injectable } from '@angular/core';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { Observable } from 'rxjs';

import { SERVER_API_URL } from 'app/app.constants';
import { createRequestOption } from 'app/shared/util/request-util';
import { IServiceOptimisation } from 'app/shared/model/service-optimisation.model';

type EntityResponseType = HttpResponse<IServiceOptimisation>;
type EntityArrayResponseType = HttpResponse<IServiceOptimisation[]>;

@Injectable({ providedIn: 'root' })
export class ServiceOptimisationService {
  public resourceUrl = SERVER_API_URL + 'api/service-optimisations';

  constructor(protected http: HttpClient) {}

  create(serviceOptimisation: IServiceOptimisation): Observable<EntityResponseType> {
    return this.http.post<IServiceOptimisation>(this.resourceUrl, serviceOptimisation, { observe: 'response' });
  }

  update(serviceOptimisation: IServiceOptimisation): Observable<EntityResponseType> {
    return this.http.put<IServiceOptimisation>(this.resourceUrl, serviceOptimisation, { observe: 'response' });
  }

  find(id: number): Observable<EntityResponseType> {
    return this.http.get<IServiceOptimisation>(`${this.resourceUrl}/${id}`, { observe: 'response' });
  }

  query(req?: any): Observable<EntityArrayResponseType> {
    const options = createRequestOption(req);
    return this.http.get<IServiceOptimisation[]>(this.resourceUrl, { params: options, observe: 'response' });
  }

  delete(id: number): Observable<HttpResponse<{}>> {
    return this.http.delete(`${this.resourceUrl}/${id}`, { observe: 'response' });
  }
}
