import { Injectable } from '@angular/core';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { Observable } from 'rxjs';

import { SERVER_API_URL } from 'app/app.constants';
import { createRequestOption } from 'app/shared/util/request-util';
import { IPenalty } from 'app/shared/model/penalty.model';

type EntityResponseType = HttpResponse<IPenalty>;
type EntityArrayResponseType = HttpResponse<IPenalty[]>;

@Injectable({ providedIn: 'root' })
export class PenaltyService {
  public resourceUrl = SERVER_API_URL + 'api/penalties';

  constructor(protected http: HttpClient) {}

  create(penalty: IPenalty): Observable<EntityResponseType> {
    return this.http.post<IPenalty>(this.resourceUrl, penalty, { observe: 'response' });
  }

  update(penalty: IPenalty): Observable<EntityResponseType> {
    return this.http.put<IPenalty>(this.resourceUrl, penalty, { observe: 'response' });
  }

  find(id: number): Observable<EntityResponseType> {
    return this.http.get<IPenalty>(`${this.resourceUrl}/${id}`, { observe: 'response' });
  }

  query(req?: any): Observable<EntityArrayResponseType> {
    const options = createRequestOption(req);
    return this.http.get<IPenalty[]>(this.resourceUrl, { params: options, observe: 'response' });
  }

  delete(id: number): Observable<HttpResponse<{}>> {
    return this.http.delete(`${this.resourceUrl}/${id}`, { observe: 'response' });
  }
}
