import { Injectable } from '@angular/core';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { Observable } from 'rxjs';

import { SERVER_API_URL } from 'app/app.constants';
import { createRequestOption } from 'app/shared/util/request-util';
import { IInfrastructure } from 'app/shared/model/infrastructure.model';

type EntityResponseType = HttpResponse<IInfrastructure>;
type EntityArrayResponseType = HttpResponse<IInfrastructure[]>;

@Injectable({ providedIn: 'root' })
export class InfrastructureService {
  public resourceUrl = SERVER_API_URL + 'api/infrastructures';

  constructor(protected http: HttpClient) {}

  create(infrastructure: IInfrastructure): Observable<EntityResponseType> {
    return this.http.post<IInfrastructure>(this.resourceUrl, infrastructure, { observe: 'response' });
  }

  update(infrastructure: IInfrastructure): Observable<EntityResponseType> {
    return this.http.put<IInfrastructure>(this.resourceUrl, infrastructure, { observe: 'response' });
  }

  find(id: number): Observable<EntityResponseType> {
    return this.http.get<IInfrastructure>(`${this.resourceUrl}/${id}`, { observe: 'response' });
  }

  query(req?: any): Observable<EntityArrayResponseType> {
    const options = createRequestOption(req);
    return this.http.get<IInfrastructure[]>(this.resourceUrl, { params: options, observe: 'response' });
  }

  delete(id: number): Observable<HttpResponse<{}>> {
    return this.http.delete(`${this.resourceUrl}/${id}`, { observe: 'response' });
  }
}
