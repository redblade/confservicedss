import { Injectable } from '@angular/core';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import * as moment from 'moment';

import { SERVER_API_URL } from 'app/app.constants';
import { createRequestOption } from 'app/shared/util/request-util';
import { IAppReport } from 'app/shared/model/app-report.model';

type EntityResponseType = HttpResponse<IAppReport>;
type EntityArrayResponseType = HttpResponse<IAppReport[]>;

@Injectable({ providedIn: 'root' })
export class AppReportService {
  public resourceUrl = SERVER_API_URL + 'api/app-reports';

  constructor(protected http: HttpClient) {}

  create(appReport: IAppReport): Observable<EntityResponseType> {
    const copy = this.convertDateFromClient(appReport);
    return this.http
      .post<IAppReport>(this.resourceUrl, copy, { observe: 'response' })
      .pipe(map((res: EntityResponseType) => this.convertDateFromServer(res)));
  }

  update(appReport: IAppReport): Observable<EntityResponseType> {
    const copy = this.convertDateFromClient(appReport);
    return this.http
      .put<IAppReport>(this.resourceUrl, copy, { observe: 'response' })
      .pipe(map((res: EntityResponseType) => this.convertDateFromServer(res)));
  }

  find(id: number): Observable<EntityResponseType> {
    return this.http
      .get<IAppReport>(`${this.resourceUrl}/${id}`, { observe: 'response' })
      .pipe(map((res: EntityResponseType) => this.convertDateFromServer(res)));
  }

  query(req?: any): Observable<EntityArrayResponseType> {
    const options = createRequestOption(req);
    return this.http
      .get<IAppReport[]>(this.resourceUrl, { params: options, observe: 'response' })
      .pipe(map((res: EntityArrayResponseType) => this.convertDateArrayFromServer(res)));
  }

  delete(id: number): Observable<HttpResponse<{}>> {
    return this.http.delete(`${this.resourceUrl}/${id}`, { observe: 'response' });
  }

  protected convertDateFromClient(appReport: IAppReport): IAppReport {
    const copy: IAppReport = Object.assign({}, appReport, {
      timestamp: appReport.timestamp && appReport.timestamp.isValid() ? appReport.timestamp.toJSON() : undefined,
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
      res.body.forEach((appReport: IAppReport) => {
        appReport.timestamp = appReport.timestamp ? moment(appReport.timestamp) : undefined;
      });
    }
    return res;
  }
}
