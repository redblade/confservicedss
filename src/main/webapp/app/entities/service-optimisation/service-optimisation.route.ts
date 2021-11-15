import { Injectable } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
import { Resolve, ActivatedRouteSnapshot, Routes, Router } from '@angular/router';
import { Observable, of, EMPTY } from 'rxjs';
import { flatMap } from 'rxjs/operators';

import { Authority } from 'app/shared/constants/authority.constants';
import { UserRouteAccessService } from 'app/core/auth/user-route-access-service';
import { IServiceOptimisation, ServiceOptimisation } from 'app/shared/model/service-optimisation.model';
import { ServiceOptimisationService } from './service-optimisation.service';
import { ServiceOptimisationComponent } from './service-optimisation.component';
import { ServiceOptimisationDetailComponent } from './service-optimisation-detail.component';
import { ServiceOptimisationUpdateComponent } from './service-optimisation-update.component';

@Injectable({ providedIn: 'root' })
export class ServiceOptimisationResolve implements Resolve<IServiceOptimisation> {
  constructor(private service: ServiceOptimisationService, private router: Router) {}

  resolve(route: ActivatedRouteSnapshot): Observable<IServiceOptimisation> | Observable<never> {
    const id = route.params['id'];
    if (id) {
      return this.service.find(id).pipe(
        flatMap((serviceOptimisation: HttpResponse<ServiceOptimisation>) => {
          if (serviceOptimisation.body) {
            return of(serviceOptimisation.body);
          } else {
            this.router.navigate(['404']);
            return EMPTY;
          }
        })
      );
    }
    return of(new ServiceOptimisation());
  }
}

export const serviceOptimisationRoute: Routes = [
  {
    path: '',
    component: ServiceOptimisationComponent,
    data: {
      authorities: [Authority.ADMIN,Authority.SP],
      defaultSort: 'id,asc',
      pageTitle: 'ServiceOptimisations',
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/view',
    component: ServiceOptimisationDetailComponent,
    resolve: {
      serviceOptimisation: ServiceOptimisationResolve,
    },
    data: {
      authorities: [Authority.ADMIN,Authority.SP],
      pageTitle: 'ServiceOptimisations',
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: 'new',
    component: ServiceOptimisationUpdateComponent,
    resolve: {
      serviceOptimisation: ServiceOptimisationResolve,
    },
    data: {
      authorities: [Authority.ADMIN,Authority.SP],
      pageTitle: 'ServiceOptimisations',
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/edit',
    component: ServiceOptimisationUpdateComponent,
    resolve: {
      serviceOptimisation: ServiceOptimisationResolve,
    },
    data: {
      authorities: [Authority.ADMIN,Authority.SP],
      pageTitle: 'ServiceOptimisations',
    },
    canActivate: [UserRouteAccessService],
  },
];
