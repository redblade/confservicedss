import { Injectable } from '@angular/core';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import * as moment from 'moment';

import { SERVER_API_URL } from 'app/app.constants';
import { createRequestOption } from 'app/shared/util/request-util';
import { ISla } from 'app/shared/model/sla.model';

type EntityResponseType = HttpResponse<ISla>;
type EntityArrayResponseType = HttpResponse<ISla[]>;

@Injectable({ providedIn: 'root' })
export class SlaService {
  public resourceUrl = SERVER_API_URL + 'api/slas';

  constructor(protected http: HttpClient) {}

  create(sla: ISla): Observable<EntityResponseType> {
    const copy = this.convertDateFromClient(sla);
    return this.http
      .post<ISla>(this.resourceUrl, copy, { observe: 'response' })
      .pipe(map((res: EntityResponseType) => this.convertDateFromServer(res)));
  }

  update(sla: ISla): Observable<EntityResponseType> {
    const copy = this.convertDateFromClient(sla);
    return this.http
      .put<ISla>(this.resourceUrl, copy, { observe: 'response' })
      .pipe(map((res: EntityResponseType) => this.convertDateFromServer(res)));
  }

  find(id: number): Observable<EntityResponseType> {
    return this.http
      .get<ISla>(`${this.resourceUrl}/${id}`, { observe: 'response' })
      .pipe(map((res: EntityResponseType) => this.convertDateFromServer(res)));
  }

  query(req?: any): Observable<EntityArrayResponseType> {
    const options = createRequestOption(req);
    return this.http
      .get<ISla[]>(this.resourceUrl, { params: options, observe: 'response' })
      .pipe(map((res: EntityArrayResponseType) => this.convertDateArrayFromServer(res)));
  }

  delete(id: number): Observable<HttpResponse<{}>> {
    return this.http.delete(`${this.resourceUrl}/${id}`, { observe: 'response' });
  }

  protected convertDateFromClient(sla: ISla): ISla {
    const copy: ISla = Object.assign({}, sla, {
      creation: sla.creation && sla.creation.isValid() ? sla.creation.toJSON() : undefined,
      expiration: sla.expiration && sla.expiration.isValid() ? sla.expiration.toJSON() : undefined,
    });
    return copy;
  }

  protected convertDateFromServer(res: EntityResponseType): EntityResponseType {
    if (res.body) {
      res.body.creation = res.body.creation ? moment(res.body.creation) : undefined;
      res.body.expiration = res.body.expiration ? moment(res.body.expiration) : undefined;
    }
    return res;
  }

  protected convertDateArrayFromServer(res: EntityArrayResponseType): EntityArrayResponseType {
    if (res.body) {
      res.body.forEach((sla: ISla) => {
        sla.creation = sla.creation ? moment(sla.creation) : undefined;
        sla.expiration = sla.expiration ? moment(sla.expiration) : undefined;
      });
    }
    return res;
  }
}
