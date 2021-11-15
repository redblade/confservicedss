import { Injectable } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
import { Resolve, ActivatedRouteSnapshot, Routes, Router } from '@angular/router';
import { Observable, of, EMPTY } from 'rxjs';
import { flatMap } from 'rxjs/operators';

import { Authority } from 'app/shared/constants/authority.constants';
import { UserRouteAccessService } from 'app/core/auth/user-route-access-service';
import { IBenchmark, Benchmark } from 'app/shared/model/benchmark.model';
import { BenchmarkService } from './benchmark.service';
import { BenchmarkComponent } from './benchmark.component';
import { BenchmarkDetailComponent } from './benchmark-detail.component';
import { BenchmarkUpdateComponent } from './benchmark-update.component';

@Injectable({ providedIn: 'root' })
export class BenchmarkResolve implements Resolve<IBenchmark> {
  constructor(private service: BenchmarkService, private router: Router) {}

  resolve(route: ActivatedRouteSnapshot): Observable<IBenchmark> | Observable<never> {
    const id = route.params['id'];
    if (id) {
      return this.service.find(id).pipe(
        flatMap((benchmark: HttpResponse<Benchmark>) => {
          if (benchmark.body) {
            return of(benchmark.body);
          } else {
            this.router.navigate(['404']);
            return EMPTY;
          }
        })
      );
    }
    return of(new Benchmark());
  }
}

export const benchmarkRoute: Routes = [
  {
    path: '',
    component: BenchmarkComponent,
    data: {
      authorities: [Authority.ADMIN,Authority.SP,Authority.IP],
      defaultSort: 'id,asc',
      pageTitle: 'Benchmarks',
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/view',
    component: BenchmarkDetailComponent,
    resolve: {
      benchmark: BenchmarkResolve,
    },
    data: {
      authorities: [Authority.ADMIN,Authority.SP,Authority.IP],
      pageTitle: 'Benchmarks',
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: 'new',
    component: BenchmarkUpdateComponent,
    resolve: {
      benchmark: BenchmarkResolve,
    },
    data: {
      authorities: [Authority.ADMIN,Authority.SP,Authority.IP],
      pageTitle: 'Benchmarks',
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/edit',
    component: BenchmarkUpdateComponent,
    resolve: {
      benchmark: BenchmarkResolve,
    },
    data: {
      authorities: [Authority.ADMIN,Authority.SP,Authority.IP],
      pageTitle: 'Benchmarks',
    },
    canActivate: [UserRouteAccessService],
  },
];
