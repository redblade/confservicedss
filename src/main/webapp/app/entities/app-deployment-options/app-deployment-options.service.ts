import { Injectable } from '@angular/core';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { Observable } from 'rxjs';

import { SERVER_API_URL } from 'app/app.constants';
import { createRequestOption } from 'app/shared/util/request-util';
import { IAppDeploymentOptions } from 'app/shared/model/app-deployment-options.model';

type EntityResponseType = HttpResponse<IAppDeploymentOptions>;
type EntityArrayResponseType = HttpResponse<IAppDeploymentOptions[]>;

@Injectable({ providedIn: 'root' })
export class AppDeploymentOptionsService {
  public resourceUrl = SERVER_API_URL + 'api/app-deployment-options';

  constructor(protected http: HttpClient) {}

  create(appDeploymentOptions: IAppDeploymentOptions): Observable<EntityResponseType> {
    return this.http.post<IAppDeploymentOptions>(this.resourceUrl, appDeploymentOptions, { observe: 'response' });
  }

  update(appDeploymentOptions: IAppDeploymentOptions): Observable<EntityResponseType> {
    return this.http.put<IAppDeploymentOptions>(this.resourceUrl, appDeploymentOptions, { observe: 'response' });
  }

  find(id: number): Observable<EntityResponseType> {
    return this.http.get<IAppDeploymentOptions>(`${this.resourceUrl}/${id}`, { observe: 'response' });
  }

  query(req?: any): Observable<EntityArrayResponseType> {
    const options = createRequestOption(req);
    return this.http.get<IAppDeploymentOptions[]>(this.resourceUrl, { params: options, observe: 'response' });
  }

  delete(id: number): Observable<HttpResponse<{}>> {
    return this.http.delete(`${this.resourceUrl}/${id}`, { observe: 'response' });
  }
}
