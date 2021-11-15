import { Injectable } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
import { Resolve, ActivatedRouteSnapshot, Routes, Router } from '@angular/router';
import { Observable, of, EMPTY } from 'rxjs';
import { flatMap } from 'rxjs/operators';

import { Authority } from 'app/shared/constants/authority.constants';
import { UserRouteAccessService } from 'app/core/auth/user-route-access-service';
import { INode, Node } from 'app/shared/model/node.model';
import { NodeService } from './node.service';
import { NodeComponent } from './node.component';
import { NodeDetailComponent } from './node-detail.component';
import { NodeUpdateComponent } from './node-update.component';

@Injectable({ providedIn: 'root' })
export class NodeResolve implements Resolve<INode> {
  constructor(private service: NodeService, private router: Router) {}

  resolve(route: ActivatedRouteSnapshot): Observable<INode> | Observable<never> {
    const id = route.params['id'];
    if (id) {
      return this.service.find(id).pipe(
        flatMap((node: HttpResponse<Node>) => {
          if (node.body) {
            return of(node.body);
          } else {
            this.router.navigate(['404']);
            return EMPTY;
          }
        })
      );
    }
    return of(new Node());
  }
}

export const nodeRoute: Routes = [
  {
    path: '',
    component: NodeComponent,
    data: {
      authorities: [Authority.ADMIN,Authority.SP,Authority.IP],
      defaultSort: 'id,asc',
      pageTitle: 'Nodes',
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/view',
    component: NodeDetailComponent,
    resolve: {
      node: NodeResolve,
    },
    data: {
      authorities: [Authority.ADMIN,Authority.SP,Authority.IP],
      pageTitle: 'Nodes',
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: 'new',
    component: NodeUpdateComponent,
    resolve: {
      node: NodeResolve,
    },
    data: {
      authorities: [Authority.ADMIN,Authority.SP,Authority.IP],
      pageTitle: 'Nodes',
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/edit',
    component: NodeUpdateComponent,
    resolve: {
      node: NodeResolve,
    },
    data: {
      authorities: [Authority.ADMIN,Authority.SP,Authority.IP],
      pageTitle: 'Nodes',
    },
    canActivate: [UserRouteAccessService],
  },
];
