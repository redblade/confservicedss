import { Injectable } from '@angular/core';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import * as moment from 'moment';

import { SERVER_API_URL } from 'app/app.constants';
import { createRequestOption } from 'app/shared/util/request-util';
import { IInfrastructureReport } from 'app/shared/model/infrastructure-report.model';

type EntityResponseType = HttpResponse<IInfrastructureReport>;
type EntityArrayResponseType = HttpResponse<IInfrastructureReport[]>;

@Injectable({ providedIn: 'root' })
export class InfrastructureReportService {
  public resourceUrl = SERVER_API_URL + 'api/infrastructure-reports';

  constructor(protected http: HttpClient) {}

  create(infrastructureReport: IInfrastructureReport): Observable<EntityResponseType> {
    const copy = this.convertDateFromClient(infrastructureReport);
    return this.http
      .post<IInfrastructureReport>(this.resourceUrl, copy, { observe: 'response' })
      .pipe(map((res: EntityResponseType) => this.convertDateFromServer(res)));
  }

  update(infrastructureReport: IInfrastructureReport): Observable<EntityResponseType> {
    const copy = this.convertDateFromClient(infrastructureReport);
    return this.http
      .put<IInfrastructureReport>(this.resourceUrl, copy, { observe: 'response' })
      .pipe(map((res: EntityResponseType) => this.convertDateFromServer(res)));
  }

  find(id: number): Observable<EntityResponseType> {
    return this.http
      .get<IInfrastructureReport>(`${this.resourceUrl}/${id}`, { observe: 'response' })
      .pipe(map((res: EntityResponseType) => this.convertDateFromServer(res)));
  }

  query(categoryFilter: string, req?: any): Observable<EntityArrayResponseType> {
    let options = createRequestOption(req);
    options = options.set("categoryFilter", categoryFilter);
    return this.http
      .get<IInfrastructureReport[]>(this.resourceUrl, { params: options, observe: 'response' })
      .pipe(map((res: EntityArrayResponseType) => this.convertDateArrayFromServer(res)));
  }

  delete(id: number): Observable<HttpResponse<{}>> {
    return this.http.delete(`${this.resourceUrl}/${id}`, { observe: 'response' });
  }

  protected convertDateFromClient(infrastructureReport: IInfrastructureReport): IInfrastructureReport {
    const copy: IInfrastructureReport = Object.assign({}, infrastructureReport, {
      timestamp:
        infrastructureReport.timestamp && infrastructureReport.timestamp.isValid() ? infrastructureReport.timestamp.toJSON() : undefined,
    });
    return copy;
  }

  protected convertDateFromServer(res: EntityResponseType): EntityResponseType {
    if (res.body) {
      res.body.timestamp = res.body.timestamp ? moment(res.body.timestamp) : undefined;
    }
    return res;
  }

  protected convertDateArrayFromServer(res: EntityArrayResponseType): EntityArrayResponseType {
    if (res.body) {
      res.body.forEach((infrastructureReport: IInfrastructureReport) => {
        infrastructureReport.timestamp = infrastructureReport.timestamp ? moment(infrastructureReport.timestamp) : undefined;
      });
    }
    return res;
  }
}
