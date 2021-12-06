import { Injectable } from '@angular/core';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { Observable } from 'rxjs';

import { SERVER_API_URL } from 'app/app.constants';
import { createRequestOption } from 'app/shared/util/request-util';
import { IBenchmarkSummary } from 'app/shared/model/benchmark-summary.model';

type EntityResponseType = HttpResponse<IBenchmarkSummary>;
type EntityArrayResponseType = HttpResponse<IBenchmarkSummary[]>;

@Injectable({ providedIn: 'root' })
export class BenchmarkSummaryService {
  public resourceUrl = SERVER_API_URL + 'api/benchmark-summary';

  constructor(protected http: HttpClient) {}

  find(id: number): Observable<EntityResponseType> {
    return this.http.get<IBenchmarkSummary>(`${this.resourceUrl}/${id}`, { observe: 'response' });
  }

  query(req?: any): Observable<EntityArrayResponseType> {
    const options = createRequestOption(req);
    return this.http.get<IBenchmarkSummary[]>(this.resourceUrl, { params: options, observe: 'response' });
  }
}
