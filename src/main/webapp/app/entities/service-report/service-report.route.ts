import { Injectable } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
import { Resolve, ActivatedRouteSnapshot, Routes, Router } from '@angular/router';
import { Observable, of, EMPTY } from 'rxjs';
import { flatMap } from 'rxjs/operators';

import { Authority } from 'app/shared/constants/authority.constants';
import { UserRouteAccessService } from 'app/core/auth/user-route-access-service';
import { IServiceReport, ServiceReport } from 'app/shared/model/service-report.model';
import { ServiceReportService } from './service-report.service';
import { ServiceReportComponent } from './service-report.component';
import { ServiceReportDetailComponent } from './service-report-detail.component';
import { ServiceReportUpdateComponent } from './service-report-update.component';

@Injectable({ providedIn: 'root' })
export class ServiceReportResolve implements Resolve<IServiceReport> {
  constructor(private service: ServiceReportService, private router: Router) {}

  resolve(route: ActivatedRouteSnapshot): Observable<IServiceReport> | Observable<never> {
    const id = route.params['id'];
    if (id) {
      return this.service.find(id).pipe(
        flatMap((serviceReport: HttpResponse<ServiceReport>) => {
          if (serviceReport.body) {
            return of(serviceReport.body);
          } else {
            this.router.navigate(['404']);
            return EMPTY;
          }
        })
      );
    }
    return of(new ServiceReport());
  }
}

export const serviceReportRoute: Routes = [
  {
    path: '',
    component: ServiceReportComponent,
    data: {
      authorities: [Authority.ADMIN,Authority.SP,Authority.IP],
      defaultSort: 'id,asc',
      pageTitle: 'ServiceReports',
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/view',
    component: ServiceReportDetailComponent,
    resolve: {
      serviceReport: ServiceReportResolve,
    },
    data: {
      authorities: [Authority.ADMIN,Authority.SP,Authority.IP],
      pageTitle: 'ServiceReports',
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: 'new',
    component: ServiceReportUpdateComponent,
    resolve: {
      serviceReport: ServiceReportResolve,
    },
    data: {
      authorities: [Authority.ADMIN,Authority.SP,Authority.IP],
      pageTitle: 'ServiceReports',
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/edit',
    component: ServiceReportUpdateComponent,
    resolve: {
      serviceReport: ServiceReportResolve,
    },
    data: {
      authorities: [Authority.ADMIN,Authority.SP,Authority.IP],
      pageTitle: 'ServiceReports',
    },
    canActivate: [UserRouteAccessService],
  },
];
