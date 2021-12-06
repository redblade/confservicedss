import { Injectable } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
import { Resolve, ActivatedRouteSnapshot, Routes, Router } from '@angular/router';
import { Observable, of, EMPTY } from 'rxjs';
import { flatMap } from 'rxjs/operators';

import { Authority } from 'app/shared/constants/authority.constants';
import { UserRouteAccessService } from 'app/core/auth/user-route-access-service';
import { IServiceBenchmarkMatch, ServiceBenchmarkMatch } from 'app/shared/model/service-benchmark-match.model';
import { ServiceBenchmarkMatchService } from './service-benchmark-match.service';
import { ServiceBenchmarkMatchComponent } from './service-benchmark-match.component';
import { ServiceBenchmarkMatchDetailComponent } from './service-benchmark-match-detail.component';

@Injectable({ providedIn: 'root' })
export class ServiceBenchmarkMatchResolve implements Resolve<IServiceBenchmarkMatch> {
  constructor(private service: ServiceBenchmarkMatchService, private router: Router) {}

  resolve(route: ActivatedRouteSnapshot): Observable<IServiceBenchmarkMatch> | Observable<never> {
    const id = route.params['id'];
    if (id) {
      return this.service.find(id).pipe(
        flatMap((serviceBenchmarkMatch: HttpResponse<ServiceBenchmarkMatch>) => {
          if (serviceBenchmarkMatch.body) {
            return of(serviceBenchmarkMatch.body);
          } else {
            this.router.navigate(['404']);
            return EMPTY;
          }
        })
      );
    }
    return of(new ServiceBenchmarkMatch());
  }
}

export const serviceBenchmarkMatchRoute: Routes = [
  {
    path: '',
    component: ServiceBenchmarkMatchComponent,
    data: {
      authorities: [Authority.ADMIN,Authority.SP],
      defaultSort: 'id,asc',
      pageTitle: 'ServiceBenchmarkMatches',
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/view',
    component: ServiceBenchmarkMatchDetailComponent,
    resolve: {
      serviceBenchmarkMatch: ServiceBenchmarkMatchResolve,
    },
    data: {
      authorities: [Authority.ADMIN,Authority.SP],
      pageTitle: 'ServiceBenchmarkMatches',
    },
    canActivate: [UserRouteAccessService],
  },
];
