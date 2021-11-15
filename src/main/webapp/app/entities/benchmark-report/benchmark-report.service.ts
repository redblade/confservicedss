import { Injectable } from '@angular/core';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import * as moment from 'moment';

import { SERVER_API_URL } from 'app/app.constants';
import { createRequestOption } from 'app/shared/util/request-util';
import { IBenchmarkReport } from 'app/shared/model/benchmark-report.model';

type EntityResponseType = HttpResponse<IBenchmarkReport>;
type EntityArrayResponseType = HttpResponse<IBenchmarkReport[]>;

@Injectable({ providedIn: 'root' })
export class BenchmarkReportService {
  public resourceUrl = SERVER_API_URL + 'api/benchmark-reports';

  constructor(protected http: HttpClient) {}

  create(benchmarkReport: IBenchmarkReport): Observable<EntityResponseType> {
    const copy = this.convertDateFromClient(benchmarkReport);
    return this.http
      .post<IBenchmarkReport>(this.resourceUrl, copy, { observe: 'response' })
      .pipe(map((res: EntityResponseType) => this.convertDateFromServer(res)));
  }

  update(benchmarkReport: IBenchmarkReport): Observable<EntityResponseType> {
    const copy = this.convertDateFromClient(benchmarkReport);
    return this.http
      .put<IBenchmarkReport>(this.resourceUrl, copy, { observe: 'response' })
      .pipe(map((res: EntityResponseType) => this.convertDateFromServer(res)));
  }

  find(id: number): Observable<EntityResponseType> {
    return this.http
      .get<IBenchmarkReport>(`${this.resourceUrl}/${id}`, { observe: 'response' })
      .pipe(map((res: EntityResponseType) => this.convertDateFromServer(res)));
  }

  query(req?: any): Observable<EntityArrayResponseType> {
    const options = createRequestOption(req);
    return this.http
      .get<IBenchmarkReport[]>(this.resourceUrl, { params: options, observe: 'response' })
      .pipe(map((res: EntityArrayResponseType) => this.convertDateArrayFromServer(res)));
  }

  delete(id: number): Observable<HttpResponse<{}>> {
    return this.http.delete(`${this.resourceUrl}/${id}`, { observe: 'response' });
  }

  protected convertDateFromClient(benchmarkReport: IBenchmarkReport): IBenchmarkReport {
    const copy: IBenchmarkReport = Object.assign({}, benchmarkReport, {
      time: benchmarkReport.time && benchmarkReport.time.isValid() ? benchmarkReport.time.toJSON() : undefined,
    });
    return copy;
  }

  protected convertDateFromServer(res: EntityResponseType): EntityResponseType {
    if (res.body) {
      res.body.time = res.body.time ? moment(res.body.time) : undefined;
    }
    return res;
  }

  protected convertDateArrayFromServer(res: EntityArrayResponseType): EntityArrayResponseType {
    if (res.body) {
      res.body.forEach((benchmarkReport: IBenchmarkReport) => {
        benchmarkReport.time = benchmarkReport.time ? moment(benchmarkReport.time) : undefined;
      });
    }
    return res;
  }
}
