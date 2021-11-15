import { Injectable } from '@angular/core';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import * as moment from 'moment';

import { SERVER_API_URL } from 'app/app.constants';
import { createRequestOption } from 'app/shared/util/request-util';
import { IServiceReport } from 'app/shared/model/service-report.model';

type EntityResponseType = HttpResponse<IServiceReport>;
type EntityArrayResponseType = HttpResponse<IServiceReport[]>;

@Injectable({ providedIn: 'root' })
export class ServiceReportService {
  public resourceUrl = SERVER_API_URL + 'api/service-reports';

  constructor(protected http: HttpClient) {}

  create(serviceReport: IServiceReport): Observable<EntityResponseType> {
    const copy = this.convertDateFromClient(serviceReport);
    return this.http
      .post<IServiceReport>(this.resourceUrl, copy, { observe: 'response' })
      .pipe(map((res: EntityResponseType) => this.convertDateFromServer(res)));
  }

  update(serviceReport: IServiceReport): Observable<EntityResponseType> {
    const copy = this.convertDateFromClient(serviceReport);
    return this.http
      .put<IServiceReport>(this.resourceUrl, copy, { observe: 'response' })
      .pipe(map((res: EntityResponseType) => this.convertDateFromServer(res)));
  }

  find(id: number): Observable<EntityResponseType> {
    return this.http
      .get<IServiceReport>(`${this.resourceUrl}/${id}`, { observe: 'response' })
      .pipe(map((res: EntityResponseType) => this.convertDateFromServer(res)));
  }

  query(categoryFilter: string, req?: any): Observable<EntityArrayResponseType> {
    let options = createRequestOption(req);
    options = options.set("categoryFilter", categoryFilter);
    return this.http
      .get<IServiceReport[]>(this.resourceUrl, { params: options, observe: 'response' })
      .pipe(map((res: EntityArrayResponseType) => this.convertDateArrayFromServer(res)));
  }

  delete(id: number): Observable<HttpResponse<{}>> {
    return this.http.delete(`${this.resourceUrl}/${id}`, { observe: 'response' });
  }

  protected convertDateFromClient(serviceReport: IServiceReport): IServiceReport {
    const copy: IServiceReport = Object.assign({}, serviceReport, {
      timestamp: serviceReport.timestamp && serviceReport.timestamp.isValid() ? serviceReport.timestamp.toJSON() : undefined,
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
      res.body.forEach((serviceReport: IServiceReport) => {
        serviceReport.timestamp = serviceReport.timestamp ? moment(serviceReport.timestamp) : undefined;
      });
    }
    return res;
  }
}
