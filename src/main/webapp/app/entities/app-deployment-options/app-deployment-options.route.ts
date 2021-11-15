import { Injectable } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
import { Resolve, ActivatedRouteSnapshot, Routes, Router } from '@angular/router';
import { Observable, of, EMPTY } from 'rxjs';
import { flatMap } from 'rxjs/operators';

import { Authority } from 'app/shared/constants/authority.constants';
import { UserRouteAccessService } from 'app/core/auth/user-route-access-service';
import { IAppDeploymentOptions, AppDeploymentOptions } from 'app/shared/model/app-deployment-options.model';
import { AppDeploymentOptionsService } from './app-deployment-options.service';
import { AppDeploymentOptionsComponent } from './app-deployment-options.component';
import { AppDeploymentOptionsDetailComponent } from './app-deployment-options-detail.component';
import { AppDeploymentOptionsUpdateComponent } from './app-deployment-options-update.component';

@Injectable({ providedIn: 'root' })
export class AppDeploymentOptionsResolve implements Resolve<IAppDeploymentOptions> {
  constructor(private service: AppDeploymentOptionsService, private router: Router) {}

  resolve(route: ActivatedRouteSnapshot): Observable<IAppDeploymentOptions> | Observable<never> {
    const id = route.params['id'];
    if (id) {
      return this.service.find(id).pipe(
        flatMap((appDeploymentOptions: HttpResponse<AppDeploymentOptions>) => {
          if (appDeploymentOptions.body) {
            return of(appDeploymentOptions.body);
          } else {
            this.router.navigate(['404']);
            return EMPTY;
          }
        })
      );
    }
    return of(new AppDeploymentOptions());
  }
}

export const appDeploymentOptionsRoute: Routes = [
  {
    path: '',
    component: AppDeploymentOptionsComponent,
    data: {
      authorities: [Authority.ADMIN,Authority.SP,Authority.IP],
      defaultSort: 'id,asc',
      pageTitle: 'AppDeploymentOptions',
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/view',
    component: AppDeploymentOptionsDetailComponent,
    resolve: {
      appDeploymentOptions: AppDeploymentOptionsResolve,
    },
    data: {
      authorities: [Authority.ADMIN,Authority.SP,Authority.IP],
      pageTitle: 'AppDeploymentOptions',
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: 'new',
    component: AppDeploymentOptionsUpdateComponent,
    resolve: {
      appDeploymentOptions: AppDeploymentOptionsResolve,
    },
    data: {
      authorities: [Authority.ADMIN,Authority.SP,Authority.IP],
      pageTitle: 'AppDeploymentOptions',
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/edit',
    component: AppDeploymentOptionsUpdateComponent,
    resolve: {
      appDeploymentOptions: AppDeploymentOptionsResolve,
    },
    data: {
      authorities: [Authority.ADMIN,Authority.SP,Authority.IP],
      pageTitle: 'AppDeploymentOptions',
    },
    canActivate: [UserRouteAccessService],
  },
];
