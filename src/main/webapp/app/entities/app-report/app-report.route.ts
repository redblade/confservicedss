import { Injectable } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
import { Resolve, ActivatedRouteSnapshot, Routes, Router } from '@angular/router';
import { Observable, of, EMPTY } from 'rxjs';
import { flatMap } from 'rxjs/operators';

import { Authority } from 'app/shared/constants/authority.constants';
import { UserRouteAccessService } from 'app/core/auth/user-route-access-service';
import { IAppReport, AppReport } from 'app/shared/model/app-report.model';
import { AppReportService } from './app-report.service';
import { AppReportComponent } from './app-report.component';
import { AppReportDetailComponent } from './app-report-detail.component';
import { AppReportUpdateComponent } from './app-report-update.component';

@Injectable({ providedIn: 'root' })
export class AppReportResolve implements Resolve<IAppReport> {
  constructor(private service: AppReportService, private router: Router) {}

  resolve(route: ActivatedRouteSnapshot): Observable<IAppReport> | Observable<never> {
    const id = route.params['id'];
    if (id) {
      return this.service.find(id).pipe(
        flatMap((appReport: HttpResponse<AppReport>) => {
          if (appReport.body) {
            return of(appReport.body);
          } else {
            this.router.navigate(['404']);
            return EMPTY;
          }
        })
      );
    }
    return of(new AppReport());
  }
}

export const appReportRoute: Routes = [
  {
    path: '',
    component: AppReportComponent,
    data: {
      authorities: [Authority.ADMIN,Authority.SP,Authority.IP],
      defaultSort: 'id,asc',
      pageTitle: 'AppReports',
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/view',
    component: AppReportDetailComponent,
    resolve: {
      appReport: AppReportResolve,
    },
    data: {
      authorities: [Authority.ADMIN,Authority.SP,Authority.IP],
      pageTitle: 'AppReports',
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: 'new',
    component: AppReportUpdateComponent,
    resolve: {
      appReport: AppReportResolve,
    },
    data: {
      authorities: [Authority.ADMIN,Authority.SP,Authority.IP],
      pageTitle: 'AppReports',
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/edit',
    component: AppReportUpdateComponent,
    resolve: {
      appReport: AppReportResolve,
    },
    data: {
      authorities: [Authority.ADMIN,Authority.SP,Authority.IP],
      pageTitle: 'AppReports',
    },
    canActivate: [UserRouteAccessService],
  },
];
