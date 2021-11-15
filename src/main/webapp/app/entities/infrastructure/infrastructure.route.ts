import { Injectable } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
import { Resolve, ActivatedRouteSnapshot, Routes, Router } from '@angular/router';
import { Observable, of, EMPTY } from 'rxjs';
import { flatMap } from 'rxjs/operators';

import { Authority } from 'app/shared/constants/authority.constants';
import { UserRouteAccessService } from 'app/core/auth/user-route-access-service';
import { IInfrastructure, Infrastructure } from 'app/shared/model/infrastructure.model';
import { InfrastructureService } from './infrastructure.service';
import { InfrastructureComponent } from './infrastructure.component';
import { InfrastructureDetailComponent } from './infrastructure-detail.component';
import { InfrastructureUpdateComponent } from './infrastructure-update.component';

@Injectable({ providedIn: 'root' })
export class InfrastructureResolve implements Resolve<IInfrastructure> {
  constructor(private service: InfrastructureService, private router: Router) {}

  resolve(route: ActivatedRouteSnapshot): Observable<IInfrastructure> | Observable<never> {
    const id = route.params['id'];
    if (id) {
      return this.service.find(id).pipe(
        flatMap((infrastructure: HttpResponse<Infrastructure>) => {
          if (infrastructure.body) {
            return of(infrastructure.body);
          } else {
            this.router.navigate(['404']);
            return EMPTY;
          }
        })
      );
    }
    return of(new Infrastructure());
  }
}

export const infrastructureRoute: Routes = [
  {
    path: '',
    component: InfrastructureComponent,
    data: {
      authorities: [Authority.ADMIN,Authority.SP,Authority.IP],
      defaultSort: 'id,asc',
      pageTitle: 'Infrastructures',
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/view',
    component: InfrastructureDetailComponent,
    resolve: {
      infrastructure: InfrastructureResolve,
    },
    data: {
      authorities: [Authority.ADMIN,Authority.SP,Authority.IP],
      pageTitle: 'Infrastructures',
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: 'new',
    component: InfrastructureUpdateComponent,
    resolve: {
      infrastructure: InfrastructureResolve,
    },
    data: {
      authorities: [Authority.ADMIN,Authority.SP,Authority.IP],
      pageTitle: 'Infrastructures',
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/edit',
    component: InfrastructureUpdateComponent,
    resolve: {
      infrastructure: InfrastructureResolve,
    },
    data: {
      authorities: [Authority.ADMIN,Authority.SP,Authority.IP],
      pageTitle: 'Infrastructures',
    },
    canActivate: [UserRouteAccessService],
  },
];
