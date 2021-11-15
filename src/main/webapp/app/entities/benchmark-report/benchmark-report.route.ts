import { Injectable } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
import { Resolve, ActivatedRouteSnapshot, Routes, Router } from '@angular/router';
import { Observable, of, EMPTY } from 'rxjs';
import { flatMap } from 'rxjs/operators';

import { Authority } from 'app/shared/constants/authority.constants';
import { UserRouteAccessService } from 'app/core/auth/user-route-access-service';
import { IBenchmarkReport, BenchmarkReport } from 'app/shared/model/benchmark-report.model';
import { BenchmarkReportService } from './benchmark-report.service';
import { BenchmarkReportComponent } from './benchmark-report.component';
import { BenchmarkReportDetailComponent } from './benchmark-report-detail.component';
import { BenchmarkReportUpdateComponent } from './benchmark-report-update.component';

@Injectable({ providedIn: 'root' })
export class BenchmarkReportResolve implements Resolve<IBenchmarkReport> {
  constructor(private service: BenchmarkReportService, private router: Router) {}

  resolve(route: ActivatedRouteSnapshot): Observable<IBenchmarkReport> | Observable<never> {
    const id = route.params['id'];
    if (id) {
      return this.service.find(id).pipe(
        flatMap((benchmarkReport: HttpResponse<BenchmarkReport>) => {
          if (benchmarkReport.body) {
            return of(benchmarkReport.body);
          } else {
            this.router.navigate(['404']);
            return EMPTY;
          }
        })
      );
    }
    return of(new BenchmarkReport());
  }
}

export const benchmarkReportRoute: Routes = [
  {
    path: '',
    component: BenchmarkReportComponent,
    data: {
      authorities: [Authority.ADMIN,Authority.SP,Authority.IP],
      defaultSort: 'id,asc',
      pageTitle: 'BenchmarkReports',
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/view',
    component: BenchmarkReportDetailComponent,
    resolve: {
      benchmarkReport: BenchmarkReportResolve,
    },
    data: {
      authorities: [Authority.ADMIN,Authority.SP,Authority.IP],
      pageTitle: 'BenchmarkReports',
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: 'new',
    component: BenchmarkReportUpdateComponent,
    resolve: {
      benchmarkReport: BenchmarkReportResolve,
    },
    data: {
      authorities: [Authority.ADMIN,Authority.SP,Authority.IP],
      pageTitle: 'BenchmarkReports',
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/edit',
    component: BenchmarkReportUpdateComponent,
    resolve: {
      benchmarkReport: BenchmarkReportResolve,
    },
    data: {
      authorities: [Authority.ADMIN,Authority.SP,Authority.IP],
      pageTitle: 'BenchmarkReports',
    },
    canActivate: [UserRouteAccessService],
  },
];
