import { Injectable } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
import { Resolve, ActivatedRouteSnapshot, Routes, Router } from '@angular/router';
import { Observable, of, EMPTY } from 'rxjs';
import { flatMap } from 'rxjs/operators';

import { Authority } from 'app/shared/constants/authority.constants';
import { UserRouteAccessService } from 'app/core/auth/user-route-access-service';
import { INodeReport, NodeReport } from 'app/shared/model/node-report.model';
import { NodeReportService } from './node-report.service';
import { NodeReportComponent } from './node-report.component';
import { NodeReportDetailComponent } from './node-report-detail.component';
import { NodeReportUpdateComponent } from './node-report-update.component';

@Injectable({ providedIn: 'root' })
export class NodeReportResolve implements Resolve<INodeReport> {
  constructor(private service: NodeReportService, private router: Router) {}

  resolve(route: ActivatedRouteSnapshot): Observable<INodeReport> | Observable<never> {
    const id = route.params['id'];
    if (id) {
      return this.service.find(id).pipe(
        flatMap((nodeReport: HttpResponse<NodeReport>) => {
          if (nodeReport.body) {
            return of(nodeReport.body);
          } else {
            this.router.navigate(['404']);
            return EMPTY;
          }
        })
      );
    }
    return of(new NodeReport());
  }
}

export const nodeReportRoute: Routes = [
  {
    path: '',
    component: NodeReportComponent,
    data: {
      authorities: [Authority.ADMIN,Authority.SP,Authority.IP],
      defaultSort: 'id,asc',
      pageTitle: 'NodeReports',
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/view',
    component: NodeReportDetailComponent,
    resolve: {
      nodeReport: NodeReportResolve,
    },
    data: {
      authorities: [Authority.ADMIN,Authority.SP,Authority.IP],
      pageTitle: 'NodeReports',
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: 'new',
    component: NodeReportUpdateComponent,
    resolve: {
      nodeReport: NodeReportResolve,
    },
    data: {
      authorities: [Authority.ADMIN,Authority.SP,Authority.IP],
      pageTitle: 'NodeReports',
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/edit',
    component: NodeReportUpdateComponent,
    resolve: {
      nodeReport: NodeReportResolve,
    },
    data: {
      authorities: [Authority.ADMIN,Authority.SP,Authority.IP],
      pageTitle: 'NodeReports',
    },
    canActivate: [UserRouteAccessService],
  },
];
