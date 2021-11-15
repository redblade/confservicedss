import { Injectable } from '@angular/core';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { Observable } from 'rxjs';

import { SERVER_API_URL } from 'app/app.constants';
import { createRequestOption } from 'app/shared/util/request-util';
import { INode } from 'app/shared/model/node.model';

type EntityResponseType = HttpResponse<INode>;
type EntityArrayResponseType = HttpResponse<INode[]>;

@Injectable({ providedIn: 'root' })
export class NodeService {
  public resourceUrl = SERVER_API_URL + 'api/nodes';

  constructor(protected http: HttpClient) {}

  create(node: INode): Observable<EntityResponseType> {
    return this.http.post<INode>(this.resourceUrl, node, { observe: 'response' });
  }

  update(node: INode): Observable<EntityResponseType> {
    return this.http.put<INode>(this.resourceUrl, node, { observe: 'response' });
  }

  find(id: number): Observable<EntityResponseType> {
    return this.http.get<INode>(`${this.resourceUrl}/${id}`, { observe: 'response' });
  }

  query(req?: any): Observable<EntityArrayResponseType> {
    const options = createRequestOption(req);
    return this.http.get<INode[]>(this.resourceUrl, { params: options, observe: 'response' });
  }

  delete(id: number): Observable<HttpResponse<{}>> {
    return this.http.delete(`${this.resourceUrl}/${id}`, { observe: 'response' });
  }
}
