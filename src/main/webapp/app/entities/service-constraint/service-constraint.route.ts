import { Injectable } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
import { Resolve, ActivatedRouteSnapshot, Routes, Router } from '@angular/router';
import { Observable, of, EMPTY } from 'rxjs';
import { flatMap } from 'rxjs/operators';

import { Authority } from 'app/shared/constants/authority.constants';
import { UserRouteAccessService } from 'app/core/auth/user-route-access-service';
import { IServiceConstraint, ServiceConstraint } from 'app/shared/model/service-constraint.model';
import { ServiceConstraintService } from './service-constraint.service';
import { ServiceConstraintComponent } from './service-constraint.component';
import { ServiceConstraintDetailComponent } from './service-constraint-detail.component';
import { ServiceConstraintUpdateComponent } from './service-constraint-update.component';

@Injectable({ providedIn: 'root' })
export class ServiceConstraintResolve implements Resolve<IServiceConstraint> {
  constructor(private service: ServiceConstraintService, private router: Router) {}

  resolve(route: ActivatedRouteSnapshot): Observable<IServiceConstraint> | Observable<never> {
    const id = route.params['id'];
    if (id) {
      return this.service.find(id).pipe(
        flatMap((serviceConstraint: HttpResponse<ServiceConstraint>) => {
          if (serviceConstraint.body) {
            return of(serviceConstraint.body);
          } else {
            this.router.navigate(['404']);
            return EMPTY;
          }
        })
      );
    }
    return of(new ServiceConstraint());
  }
}

export const serviceConstraintRoute: Routes = [
  {
    path: '',
    component: ServiceConstraintComponent,
    data: {
      authorities: [Authority.ADMIN,Authority.SP,Authority.IP],
      defaultSort: 'id,asc',
      pageTitle: 'ServiceConstraints',
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/view',
    component: ServiceConstraintDetailComponent,
    resolve: {
      serviceConstraint: ServiceConstraintResolve,
    },
    data: {
      authorities: [Authority.ADMIN,Authority.SP,Authority.IP],
      pageTitle: 'ServiceConstraints',
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: 'new',
    component: ServiceConstraintUpdateComponent,
    resolve: {
      serviceConstraint: ServiceConstraintResolve,
    },
    data: {
      authorities: [Authority.ADMIN,Authority.SP,Authority.IP],
      pageTitle: 'ServiceConstraints',
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/edit',
    component: ServiceConstraintUpdateComponent,
    resolve: {
      serviceConstraint: ServiceConstraintResolve,
    },
    data: {
      authorities: [Authority.ADMIN,Authority.SP,Authority.IP],
      pageTitle: 'ServiceConstraints',
    },
    canActivate: [UserRouteAccessService],
  },
];
