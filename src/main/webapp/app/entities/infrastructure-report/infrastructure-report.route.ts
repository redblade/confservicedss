import { Injectable } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
import { Resolve, ActivatedRouteSnapshot, Routes, Router } from '@angular/router';
import { Observable, of, EMPTY } from 'rxjs';
import { flatMap } from 'rxjs/operators';

import { Authority } from 'app/shared/constants/authority.constants';
import { UserRouteAccessService } from 'app/core/auth/user-route-access-service';
import { IInfrastructureReport, InfrastructureReport } from 'app/shared/model/infrastructure-report.model';
import { InfrastructureReportService } from './infrastructure-report.service';
import { InfrastructureReportComponent } from './infrastructure-report.component';
import { InfrastructureReportDetailComponent } from './infrastructure-report-detail.component';
import { InfrastructureReportUpdateComponent } from './infrastructure-report-update.component';

@Injectable({ providedIn: 'root' })
export class InfrastructureReportResolve implements Resolve<IInfrastructureReport> {
  constructor(private service: InfrastructureReportService, private router: Router) {}

  resolve(route: ActivatedRouteSnapshot): Observable<IInfrastructureReport> | Observable<never> {
    const id = route.params['id'];
    if (id) {
      return this.service.find(id).pipe(
        flatMap((infrastructureReport: HttpResponse<InfrastructureReport>) => {
          if (infrastructureReport.body) {
            return of(infrastructureReport.body);
          } else {
            this.router.navigate(['404']);
            return EMPTY;
          }
        })
      );
    }
    return of(new InfrastructureReport());
  }
}

export const infrastructureReportRoute: Routes = [
  {
    path: '',
    component: InfrastructureReportComponent,
    data: {
      authorities: [Authority.ADMIN,Authority.SP,Authority.IP],
      defaultSort: 'id,asc',
      pageTitle: 'InfrastructureReports',
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/view',
    component: InfrastructureReportDetailComponent,
    resolve: {
      infrastructureReport: InfrastructureReportResolve,
    },
    data: {
      authorities: [Authority.ADMIN,Authority.SP,Authority.IP],
      pageTitle: 'InfrastructureReports',
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: 'new',
    component: InfrastructureReportUpdateComponent,
    resolve: {
      infrastructureReport: InfrastructureReportResolve,
    },
    data: {
      authorities: [Authority.ADMIN,Authority.SP,Authority.IP],
      pageTitle: 'InfrastructureReports',
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/edit',
    component: InfrastructureReportUpdateComponent,
    resolve: {
      infrastructureReport: InfrastructureReportResolve,
    },
    data: {
      authorities: [Authority.ADMIN,Authority.SP,Authority.IP],
      pageTitle: 'InfrastructureReports',
    },
    canActivate: [UserRouteAccessService],
  },
];
