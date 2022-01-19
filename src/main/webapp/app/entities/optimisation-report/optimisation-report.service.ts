import { Injectable } from '@angular/core';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { Observable } from 'rxjs';

import { SERVER_API_URL } from 'app/app.constants';
import { createRequestOption } from 'app/shared/util/request-util';
import { IOptimisationReport } from 'app/shared/model/optimisation-report.model';

type EntityResponseType = HttpResponse<IOptimisationReport>;
type EntityArrayResponseType = HttpResponse<IOptimisationReport[]>;

@Injectable({ providedIn: 'root' })
export class OptimisationReportService {
  public resourceUrl = SERVER_API_URL + 'api/optimisation-reports';

  constructor(protected http: HttpClient) {}

  find(id: number): Observable<EntityResponseType> {
    return this.http.get<IOptimisationReport>(`${this.resourceUrl}/${id}`, { observe: 'response' });
  }

  query(req?: any): Observable<EntityArrayResponseType> {
    const options = createRequestOption(req);
    return this.http.get<IOptimisationReport[]>(this.resourceUrl, { params: options, observe: 'response' });
  }
}
