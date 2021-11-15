import { Injectable } from '@angular/core';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { Observable } from 'rxjs';

import { SERVER_API_URL } from 'app/app.constants';
import { createRequestOption } from 'app/shared/util/request-util';
import { IAppConstraint } from 'app/shared/model/app-constraint.model';

type EntityResponseType = HttpResponse<IAppConstraint>;
type EntityArrayResponseType = HttpResponse<IAppConstraint[]>;

@Injectable({ providedIn: 'root' })
export class AppConstraintService {
  public resourceUrl = SERVER_API_URL + 'api/app-constraints';

  constructor(protected http: HttpClient) {}

  create(appConstraint: IAppConstraint): Observable<EntityResponseType> {
    return this.http.post<IAppConstraint>(this.resourceUrl, appConstraint, { observe: 'response' });
  }

  update(appConstraint: IAppConstraint): Observable<EntityResponseType> {
    return this.http.put<IAppConstraint>(this.resourceUrl, appConstraint, { observe: 'response' });
  }

  find(id: number): Observable<EntityResponseType> {
    return this.http.get<IAppConstraint>(`${this.resourceUrl}/${id}`, { observe: 'response' });
  }

  query(req?: any): Observable<EntityArrayResponseType> {
    const options = createRequestOption(req);
    return this.http.get<IAppConstraint[]>(this.resourceUrl, { params: options, observe: 'response' });
  }

  delete(id: number): Observable<HttpResponse<{}>> {
    return this.http.delete(`${this.resourceUrl}/${id}`, { observe: 'response' });
  }
}
