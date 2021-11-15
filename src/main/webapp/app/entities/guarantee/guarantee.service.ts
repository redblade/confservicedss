import { Injectable } from '@angular/core';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { Observable } from 'rxjs';

import { SERVER_API_URL } from 'app/app.constants';
import { createRequestOption } from 'app/shared/util/request-util';
import { IGuarantee } from 'app/shared/model/guarantee.model';

type EntityResponseType = HttpResponse<IGuarantee>;
type EntityArrayResponseType = HttpResponse<IGuarantee[]>;

@Injectable({ providedIn: 'root' })
export class GuaranteeService {
  public resourceUrl = SERVER_API_URL + 'api/guarantees';

  constructor(protected http: HttpClient) {}

  create(guarantee: IGuarantee): Observable<EntityResponseType> {
    return this.http.post<IGuarantee>(this.resourceUrl, guarantee, { observe: 'response' });
  }

  update(guarantee: IGuarantee): Observable<EntityResponseType> {
    return this.http.put<IGuarantee>(this.resourceUrl, guarantee, { observe: 'response' });
  }

  find(id: number): Observable<EntityResponseType> {
    return this.http.get<IGuarantee>(`${this.resourceUrl}/${id}`, { observe: 'response' });
  }

  query(req?: any): Observable<EntityArrayResponseType> {
    const options = createRequestOption(req);
    return this.http.get<IGuarantee[]>(this.resourceUrl, { params: options, observe: 'response' });
  }

  delete(id: number): Observable<HttpResponse<{}>> {
    return this.http.delete(`${this.resourceUrl}/${id}`, { observe: 'response' });
  }
}
