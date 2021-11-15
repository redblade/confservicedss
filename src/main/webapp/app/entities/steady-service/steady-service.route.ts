import { Injectable } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
import { Resolve, ActivatedRouteSnapshot, Routes, Router } from '@angular/router';
import { Observable, of, EMPTY } from 'rxjs';
import { flatMap } from 'rxjs/operators';

import { Authority } from 'app/shared/constants/authority.constants';
import { UserRouteAccessService } from 'app/core/auth/user-route-access-service';
import { ISteadyService, SteadyService } from 'app/shared/model/steady-service.model';
import { SteadyServiceService } from './steady-service.service';
import { SteadyServiceComponent } from './steady-service.component';
import { SteadyServiceDetailComponent } from './steady-service-detail.component';
import { SteadyServiceUpdateComponent } from './steady-service-update.component';

@Injectable({ providedIn: 'root' })
export class SteadyServiceResolve implements Resolve<ISteadyService> {
  constructor(private service: SteadyServiceService, private router: Router) {}

  resolve(route: ActivatedRouteSnapshot): Observable<ISteadyService> | Observable<never> {
    const id = route.params['id'];
    if (id) {
      return this.service.find(id).pipe(
        flatMap((steadyService: HttpResponse<SteadyService>) => {
          if (steadyService.body) {
            return of(steadyService.body);
          } else {
            this.router.navigate(['404']);
            return EMPTY;
          }
        })
      );
    }
    return of(new SteadyService());
  }
}

export const steadyServiceRoute: Routes = [
  {
    path: '',
    component: SteadyServiceComponent,
    data: {
      authorities: [Authority.SP],
      defaultSort: 'id,asc',
      pageTitle: 'SteadyServices',
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/view',
    component: SteadyServiceDetailComponent,
    resolve: {
      steadyService: SteadyServiceResolve,
    },
    data: {
      authorities: [Authority.SP],
      pageTitle: 'SteadyServices',
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: 'new',
    component: SteadyServiceUpdateComponent,
    resolve: {
      steadyService: SteadyServiceResolve,
    },
    data: {
      authorities: [Authority.SP],
      pageTitle: 'SteadyServices',
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/edit',
    component: SteadyServiceUpdateComponent,
    resolve: {
      steadyService: SteadyServiceResolve,
    },
    data: {
      authorities: [Authority.SP],
      pageTitle: 'SteadyServices',
    },
    canActivate: [UserRouteAccessService],
  },
];
