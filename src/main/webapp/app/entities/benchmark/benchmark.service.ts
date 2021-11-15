import { Injectable } from '@angular/core';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { Observable } from 'rxjs';

import { SERVER_API_URL } from 'app/app.constants';
import { createRequestOption } from 'app/shared/util/request-util';
import { IBenchmark } from 'app/shared/model/benchmark.model';

type EntityResponseType = HttpResponse<IBenchmark>;
type EntityArrayResponseType = HttpResponse<IBenchmark[]>;

@Injectable({ providedIn: 'root' })
export class BenchmarkService {
  public resourceUrl = SERVER_API_URL + 'api/benchmarks';

  constructor(protected http: HttpClient) {}

  create(benchmark: IBenchmark): Observable<EntityResponseType> {
    return this.http.post<IBenchmark>(this.resourceUrl, benchmark, { observe: 'response' });
  }

  update(benchmark: IBenchmark): Observable<EntityResponseType> {
    return this.http.put<IBenchmark>(this.resourceUrl, benchmark, { observe: 'response' });
  }

  find(id: number): Observable<EntityResponseType> {
    return this.http.get<IBenchmark>(`${this.resourceUrl}/${id}`, { observe: 'response' });
  }

  query(req?: any): Observable<EntityArrayResponseType> {
    const options = createRequestOption(req);
    return this.http.get<IBenchmark[]>(this.resourceUrl, { params: options, observe: 'response' });
  }

  delete(id: number): Observable<HttpResponse<{}>> {
    return this.http.delete(`${this.resourceUrl}/${id}`, { observe: 'response' });
  }
}
