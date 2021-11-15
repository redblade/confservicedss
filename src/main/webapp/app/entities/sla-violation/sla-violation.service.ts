import { Injectable } from '@angular/core';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import * as moment from 'moment';

import { SERVER_API_URL } from 'app/app.constants';
import { createRequestOption } from 'app/shared/util/request-util';
import { ISlaViolation } from 'app/shared/model/sla-violation.model';

type EntityResponseType = HttpResponse<ISlaViolation>;
type EntityArrayResponseType = HttpResponse<ISlaViolation[]>;

@Injectable({ providedIn: 'root' })
export class SlaViolationService {
  public resourceUrl = SERVER_API_URL + 'api/sla-violations';

  constructor(protected http: HttpClient) {}

  create(slaViolation: ISlaViolation): Observable<EntityResponseType> {
    const copy = this.convertDateFromClient(slaViolation);
    return this.http
      .post<ISlaViolation>(this.resourceUrl, copy, { observe: 'response' })
      .pipe(map((res: EntityResponseType) => this.convertDateFromServer(res)));
  }

  update(slaViolation: ISlaViolation): Observable<EntityResponseType> {
    const copy = this.convertDateFromClient(slaViolation);
    return this.http
      .put<ISlaViolation>(this.resourceUrl, copy, { observe: 'response' })
      .pipe(map((res: EntityResponseType) => this.convertDateFromServer(res)));
  }

  find(id: number): Observable<EntityResponseType> {
    return this.http
      .get<ISlaViolation>(`${this.resourceUrl}/${id}`, { observe: 'response' })
      .pipe(map((res: EntityResponseType) => this.convertDateFromServer(res)));
  }

  query(req?: any): Observable<EntityArrayResponseType> {
    const options = createRequestOption(req);
    return this.http
      .get<ISlaViolation[]>(this.resourceUrl, { params: options, observe: 'response' })
      .pipe(map((res: EntityArrayResponseType) => this.convertDateArrayFromServer(res)));
  }

  delete(id: number): Observable<HttpResponse<{}>> {
    return this.http.delete(`${this.resourceUrl}/${id}`, { observe: 'response' });
  }

  protected convertDateFromClient(slaViolation: ISlaViolation): ISlaViolation {
    const copy: ISlaViolation = Object.assign({}, slaViolation, {
      timestamp: slaViolation.timestamp && slaViolation.timestamp.isValid() ? slaViolation.timestamp.toJSON() : undefined,
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
      res.body.forEach((slaViolation: ISlaViolation) => {
        slaViolation.timestamp = slaViolation.timestamp ? moment(slaViolation.timestamp) : undefined;
      });
    }
    return res;
  }
}
