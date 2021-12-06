import { Injectable } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
import { Resolve, ActivatedRouteSnapshot, Routes, Router } from '@angular/router';
import { Observable, of, EMPTY } from 'rxjs';
import { flatMap } from 'rxjs/operators';

import { Authority } from 'app/shared/constants/authority.constants';
import { UserRouteAccessService } from 'app/core/auth/user-route-access-service';
import { IBenchmarkSummary, BenchmarkSummary } from 'app/shared/model/benchmark-summary.model';
import { BenchmarkSummaryService } from './benchmark-summary.service';
import { BenchmarkSummaryComponent } from './benchmark-summary.component';
import { BenchmarkSummaryDetailComponent } from './benchmark-summary-detail.component';

@Injectable({ providedIn: 'root' })
export class BenchmarkSummaryResolve implements Resolve<IBenchmarkSummary> {
  constructor(private service: BenchmarkSummaryService, private router: Router) {}

  resolve(route: ActivatedRouteSnapshot): Observable<IBenchmarkSummary> | Observable<never> {
    const id = route.params['id'];
    if (id) {
      return this.service.find(id).pipe(
        flatMap((benchmarkSummary: HttpResponse<BenchmarkSummary>) => {
          if (benchmarkSummary.body) {
            return of(benchmarkSummary.body);
          } else {
            this.router.navigate(['404']);
            return EMPTY;
          }
        })
      );
    }
    return of(new BenchmarkSummary());
  }
}

export const benchmarkSummaryRoute: Routes = [
  {
    path: '',
    component: BenchmarkSummaryComponent,
    data: {
      authorities: [Authority.ADMIN,Authority.SP,Authority.IP],
      defaultSort: 'id,asc',
      pageTitle: 'BenchmarkSummaries',
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/view',
    component: BenchmarkSummaryDetailComponent,
    resolve: {
      benchmarkSummary: BenchmarkSummaryResolve,
    },
    data: {
      authorities: [Authority.ADMIN,Authority.SP,Authority.IP],
      pageTitle: 'BenchmarkSummaries',
    },
    canActivate: [UserRouteAccessService],
  },
];
