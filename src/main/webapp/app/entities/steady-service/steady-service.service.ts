import { Injectable } from '@angular/core';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import * as moment from 'moment';

import { SERVER_API_URL } from 'app/app.constants';
import { createRequestOption } from 'app/shared/util/request-util';
import { ISteadyService } from 'app/shared/model/steady-service.model';

type EntityResponseType = HttpResponse<ISteadyService>;
type EntityArrayResponseType = HttpResponse<ISteadyService[]>;

@Injectable({ providedIn: 'root' })
export class SteadyServiceService {
  public resourceUrl = SERVER_API_URL + 'api/steady-services';

  constructor(protected http: HttpClient) {}

  create(steadyService: ISteadyService): Observable<EntityResponseType> {
    const copy = this.convertDateFromClient(steadyService);
    return this.http
      .post<ISteadyService>(this.resourceUrl, copy, { observe: 'response' })
      .pipe(map((res: EntityResponseType) => this.convertDateFromServer(res)));
  }

  update(steadyService: ISteadyService): Observable<EntityResponseType> {
    const copy = this.convertDateFromClient(steadyService);
    return this.http
      .put<ISteadyService>(this.resourceUrl, copy, { observe: 'response' })
      .pipe(map((res: EntityResponseType) => this.convertDateFromServer(res)));
  }

  find(id: number): Observable<EntityResponseType> {
    return this.http
      .get<ISteadyService>(`${this.resourceUrl}/${id}`, { observe: 'response' })
      .pipe(map((res: EntityResponseType) => this.convertDateFromServer(res)));
  }

  query(req?: any): Observable<EntityArrayResponseType> {
    const options = createRequestOption(req);
    return this.http
      .get<ISteadyService[]>(this.resourceUrl, { params: options, observe: 'response' })
      .pipe(map((res: EntityArrayResponseType) => this.convertDateArrayFromServer(res)));
  }

  delete(id: number): Observable<HttpResponse<{}>> {
    return this.http.delete(`${this.resourceUrl}/${id}`, { observe: 'response' });
  }

  protected convertDateFromClient(steadyService: ISteadyService): ISteadyService {
    const copy: ISteadyService = Object.assign({}, steadyService, {
      timestampCreated:
        steadyService.timestampCreated && steadyService.timestampCreated.isValid() ? steadyService.timestampCreated.toJSON() : undefined,
      timestampProcessed:
        steadyService.timestampProcessed && steadyService.timestampProcessed.isValid()
          ? steadyService.timestampProcessed.toJSON()
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
      res.body.forEach((steadyService: ISteadyService) => {
        steadyService.timestampCreated = steadyService.timestampCreated ? moment(steadyService.timestampCreated) : undefined;
        steadyService.timestampProcessed = steadyService.timestampProcessed ? moment(steadyService.timestampProcessed) : undefined;
      });
    }
    return res;
  }
}
