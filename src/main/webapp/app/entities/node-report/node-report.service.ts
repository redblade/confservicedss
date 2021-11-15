import { Injectable } from '@angular/core';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import * as moment from 'moment';

import { SERVER_API_URL } from 'app/app.constants';
import { createRequestOption } from 'app/shared/util/request-util';
import { INodeReport } from 'app/shared/model/node-report.model';

type EntityResponseType = HttpResponse<INodeReport>;
type EntityArrayResponseType = HttpResponse<INodeReport[]>;

@Injectable({ providedIn: 'root' })
export class NodeReportService {
  public resourceUrl = SERVER_API_URL + 'api/node-reports';

  constructor(protected http: HttpClient) {}

  create(nodeReport: INodeReport): Observable<EntityResponseType> {
    const copy = this.convertDateFromClient(nodeReport);
    return this.http
      .post<INodeReport>(this.resourceUrl, copy, { observe: 'response' })
      .pipe(map((res: EntityResponseType) => this.convertDateFromServer(res)));
  }

  update(nodeReport: INodeReport): Observable<EntityResponseType> {
    const copy = this.convertDateFromClient(nodeReport);
    return this.http
      .put<INodeReport>(this.resourceUrl, copy, { observe: 'response' })
      .pipe(map((res: EntityResponseType) => this.convertDateFromServer(res)));
  }

  find(id: number): Observable<EntityResponseType> {
    return this.http
      .get<INodeReport>(`${this.resourceUrl}/${id}`, { observe: 'response' })
      .pipe(map((res: EntityResponseType) => this.convertDateFromServer(res)));
  }

  query(categoryFilter: string, req?: any): Observable<EntityArrayResponseType> {
    let options = createRequestOption(req);
    options = options.set("categoryFilter", categoryFilter);
    return this.http
      .get<INodeReport[]>(this.resourceUrl, { params: options, observe: 'response' })
      .pipe(map((res: EntityArrayResponseType) => this.convertDateArrayFromServer(res)));
  }

  delete(id: number): Observable<HttpResponse<{}>> {
    return this.http.delete(`${this.resourceUrl}/${id}`, { observe: 'response' });
  }

  protected convertDateFromClient(nodeReport: INodeReport): INodeReport {
    const copy: INodeReport = Object.assign({}, nodeReport, {
      timestamp: nodeReport.timestamp && nodeReport.timestamp.isValid() ? nodeReport.timestamp.toJSON() : undefined,
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
      res.body.forEach((nodeReport: INodeReport) => {
        nodeReport.timestamp = nodeReport.timestamp ? moment(nodeReport.timestamp) : undefined;
      });
    }
    return res;
  }
}
