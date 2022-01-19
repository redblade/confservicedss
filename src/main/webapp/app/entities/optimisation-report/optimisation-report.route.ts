import { Injectable } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
import { Resolve, ActivatedRouteSnapshot, Routes, Router } from '@angular/router';
import { Observable, of, EMPTY } from 'rxjs';
import { flatMap } from 'rxjs/operators';

import { Authority } from 'app/shared/constants/authority.constants';
import { UserRouteAccessService } from 'app/core/auth/user-route-access-service';
import { IOptimisationReport, OptimisationReport } from 'app/shared/model/optimisation-report.model';
import { OptimisationReportService } from './optimisation-report.service';
import { OptimisationReportComponent } from './optimisation-report.component';
import { OptimisationReportDetailComponent } from './optimisation-report-detail.component';

@Injectable({ providedIn: 'root' })
export class OptimisationReportResolve implements Resolve<IOptimisationReport> {
  constructor(private service: OptimisationReportService, private router: Router) {}

  resolve(route: ActivatedRouteSnapshot): Observable<IOptimisationReport> | Observable<never> {
    const id = route.params['id'];
    if (id) {
      return this.service.find(id).pipe(
        flatMap((optimisationReport: HttpResponse<OptimisationReport>) => {
          if (optimisationReport.body) {
            return of(optimisationReport.body);
          } else {
            this.router.navigate(['404']);
            return EMPTY;
          }
        })
      );
    }
    return of(new OptimisationReport());
  }
}

export const optimisationReportRoute: Routes = [
  {
    path: '',
    component: OptimisationReportComponent,
    data: {
      authorities: [Authority.ADMIN,Authority.SP],
      defaultSort: 'id,asc',
      pageTitle: 'OptimisationReports',
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/view',
    component: OptimisationReportDetailComponent,
    resolve: {
      optimisationReport: OptimisationReportResolve,
    },
    data: {
      authorities: [Authority.ADMIN,Authority.SP],
      pageTitle: 'OptimisationReports',
    },
    canActivate: [UserRouteAccessService],
  },
];
