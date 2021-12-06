import { Injectable } from '@angular/core';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { Observable } from 'rxjs';

import { SERVER_API_URL } from 'app/app.constants';
import { createRequestOption } from 'app/shared/util/request-util';
import { IServiceBenchmarkMatch } from 'app/shared/model/service-benchmark-match.model';

type EntityResponseType = HttpResponse<IServiceBenchmarkMatch>;
type EntityArrayResponseType = HttpResponse<IServiceBenchmarkMatch[]>;

@Injectable({ providedIn: 'root' })
export class ServiceBenchmarkMatchService {
  public resourceUrl = SERVER_API_URL + 'api/service-benchmark-match';

  constructor(protected http: HttpClient) {}

  find(id: number): Observable<EntityResponseType> {
    return this.http.get<IServiceBenchmarkMatch>(`${this.resourceUrl}/${id}`, { observe: 'response' });
  }

  query(req?: any): Observable<EntityArrayResponseType> {
    const options = createRequestOption(req);
    return this.http.get<IServiceBenchmarkMatch[]>(this.resourceUrl, { params: options, observe: 'response' });
  }
}
