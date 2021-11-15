import { Injectable } from '@angular/core';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { Observable } from 'rxjs';

import { SERVER_API_URL } from 'app/app.constants';
import { createRequestOption } from 'app/shared/util/request-util';
import { IServiceConstraint } from 'app/shared/model/service-constraint.model';

type EntityResponseType = HttpResponse<IServiceConstraint>;
type EntityArrayResponseType = HttpResponse<IServiceConstraint[]>;

@Injectable({ providedIn: 'root' })
export class ServiceConstraintService {
  public resourceUrl = SERVER_API_URL + 'api/service-constraints';

  constructor(protected http: HttpClient) {}

  create(serviceConstraint: IServiceConstraint): Observable<EntityResponseType> {
    return this.http.post<IServiceConstraint>(this.resourceUrl, serviceConstraint, { observe: 'response' });
  }

  update(serviceConstraint: IServiceConstraint): Observable<EntityResponseType> {
    return this.http.put<IServiceConstraint>(this.resourceUrl, serviceConstraint, { observe: 'response' });
  }

  find(id: number): Observable<EntityResponseType> {
    return this.http.get<IServiceConstraint>(`${this.resourceUrl}/${id}`, { observe: 'response' });
  }

  query(req?: any): Observable<EntityArrayResponseType> {
    const options = createRequestOption(req);
    return this.http.get<IServiceConstraint[]>(this.resourceUrl, { params: options, observe: 'response' });
  }

  delete(id: number): Observable<HttpResponse<{}>> {
    return this.http.delete(`${this.resourceUrl}/${id}`, { observe: 'response' });
  }
}
