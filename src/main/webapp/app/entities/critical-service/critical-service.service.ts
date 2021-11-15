import { Injectable } from '@angular/core';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import * as moment from 'moment';

import { SERVER_API_URL } from 'app/app.constants';
import { createRequestOption } from 'app/shared/util/request-util';
import { ICriticalService } from 'app/shared/model/critical-service.model';

type EntityResponseType = HttpResponse<ICriticalService>;
type EntityArrayResponseType = HttpResponse<ICriticalService[]>;

@Injectable({ providedIn: 'root' })
export class CriticalServiceService {
  public resourceUrl = SERVER_API_URL + 'api/critical-services';

  constructor(protected http: HttpClient) {}

  create(criticalService: ICriticalService): Observable<EntityResponseType> {
    const copy = this.convertDateFromClient(criticalService);
    return this.http
      .post<ICriticalService>(this.resourceUrl, copy, { observe: 'response' })
      .pipe(map((res: EntityResponseType) => this.convertDateFromServer(res)));
  }

  update(criticalService: ICriticalService): Observable<EntityResponseType> {
    const copy = this.convertDateFromClient(criticalService);
    return this.http
      .put<ICriticalService>(this.resourceUrl, copy, { observe: 'response' })
      .pipe(map((res: EntityResponseType) => this.convertDateFromServer(res)));
  }

  find(id: number): Observable<EntityResponseType> {
    return this.http
      .get<ICriticalService>(`${this.resourceUrl}/${id}`, { observe: 'response' })
      .pipe(map((res: EntityResponseType) => this.convertDateFromServer(res)));
  }

  query(req?: any): Observable<EntityArrayResponseType> {
    const options = createRequestOption(req);
    return this.http
      .get<ICriticalService[]>(this.resourceUrl, { params: options, observe: 'response' })
      .pipe(map((res: EntityArrayResponseType) => this.convertDateArrayFromServer(res)));
  }

  delete(id: number): Observable<HttpResponse<{}>> {
    return this.http.delete(`${this.resourceUrl}/${id}`, { observe: 'response' });
  }

  protected convertDateFromClient(criticalService: ICriticalService): ICriticalService {
    const copy: ICriticalService = Object.assign({}, criticalService, {
      timestampCreated:
        criticalService.timestampCreated && criticalService.timestampCreated.isValid()
          ? criticalService.timestampCreated.toJSON()
          : undefined,
      timestampProcessed:
        criticalService.timestampProcessed && criticalService.timestampProcessed.isValid()
          ? criticalService.timestampProcessed.toJSON()
          : undefined,
    });
    return copy;
  }

  protected convertDateFromServer(res: EntityResponseType): EntityResponseType {
    if (res.body) {
      res.body.timestampCreated = res.body.timestampCreated ? moment(res.body.timestampCreated) : undefined;
      res.body.timestampProcessed = res.body.timestampProcessed ? moment(res.body.timestampProcessed) : undefined;
    }
    return res;
  }

  protected convertDateArrayFromServer(res: EntityArrayResponseType): EntityArrayResponseType {
    if (res.body) {
      res.body.forEach((criticalService: ICriticalService) => {
        criticalService.timestampCreated = criticalService.timestampCreated ? moment(criticalService.timestampCreated) : undefined;
        criticalService.timestampProcessed = criticalService.timestampProcessed ? moment(criticalService.timestampProcessed) : undefined;
      });
    }
    return res;
  }
}
