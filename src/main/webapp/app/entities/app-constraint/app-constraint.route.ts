import { Injectable } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
import { Resolve, ActivatedRouteSnapshot, Routes, Router } from '@angular/router';
import { Observable, of, EMPTY } from 'rxjs';
import { flatMap } from 'rxjs/operators';

import { Authority } from 'app/shared/constants/authority.constants';
import { UserRouteAccessService } from 'app/core/auth/user-route-access-service';
import { IAppConstraint, AppConstraint } from 'app/shared/model/app-constraint.model';
import { AppConstraintService } from './app-constraint.service';
import { AppConstraintComponent } from './app-constraint.component';
import { AppConstraintDetailComponent } from './app-constraint-detail.component';
import { AppConstraintUpdateComponent } from './app-constraint-update.component';

@Injectable({ providedIn: 'root' })
export class AppConstraintResolve implements Resolve<IAppConstraint> {
  constructor(private service: AppConstraintService, private router: Router) {}

  resolve(route: ActivatedRouteSnapshot): Observable<IAppConstraint> | Observable<never> {
    const id = route.params['id'];
    if (id) {
      return this.service.find(id).pipe(
        flatMap((appConstraint: HttpResponse<AppConstraint>) => {
          if (appConstraint.body) {
            return of(appConstraint.body);
          } else {
            this.router.navigate(['404']);
            return EMPTY;
          }
        })
      );
    }
    return of(new AppConstraint());
  }
}

export const appConstraintRoute: Routes = [
  {
    path: '',
    component: AppConstraintComponent,
    data: {
      authorities: [Authority.ADMIN,Authority.SP,Authority.IP],
      defaultSort: 'id,asc',
      pageTitle: 'AppConstraints',
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/view',
    component: AppConstraintDetailComponent,
    resolve: {
      appConstraint: AppConstraintResolve,
    },
    data: {
      authorities: [Authority.ADMIN,Authority.SP,Authority.IP],
      pageTitle: 'AppConstraints',
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: 'new',
    component: AppConstraintUpdateComponent,
    resolve: {
      appConstraint: AppConstraintResolve,
    },
    data: {
      authorities: [Authority.ADMIN,Authority.SP,Authority.IP],
      pageTitle: 'AppConstraints',
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/edit',
    component: AppConstraintUpdateComponent,
    resolve: {
      appConstraint: AppConstraintResolve,
    },
    data: {
      authorities: [Authority.ADMIN,Authority.SP,Authority.IP],
      pageTitle: 'AppConstraints',
    },
    canActivate: [UserRouteAccessService],
  },
];
