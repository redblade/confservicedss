import { Injectable } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
import { Resolve, ActivatedRouteSnapshot, Routes, Router } from '@angular/router';
import { Observable, of, EMPTY } from 'rxjs';
import { flatMap } from 'rxjs/operators';

import { Authority } from 'app/shared/constants/authority.constants';
import { UserRouteAccessService } from 'app/core/auth/user-route-access-service';
import { IApp, App } from 'app/shared/model/app.model';
import { AppService } from './app.service';
import { AppComponent } from './app.component';
import { AppDetailComponent } from './app-detail.component';
import { AppUpdateComponent } from './app-update.component';
import { AppManageComponent } from './app-manage.component';

@Injectable({ providedIn: 'root' })
export class AppResolve implements Resolve<IApp> {
  constructor(private service: AppService, private router: Router) {}

  resolve(route: ActivatedRouteSnapshot): Observable<IApp> | Observable<never> {
    const id = route.params['id'];
    if (id) {
      return this.service.find(id).pipe(
        flatMap((app: HttpResponse<App>) => {
          if (app.body) {
            return of(app.body);
          } else {
            this.router.navigate(['404']);
            return EMPTY;
          }
        })
      );
    }
    return of(new App());
  }
}

export const appRoute: Routes = [
  {
    path: '',
    component: AppComponent,
    data: {
      authorities: [Authority.ADMIN,Authority.SP,Authority.IP],
      defaultSort: 'id,asc',
      pageTitle: 'Apps',
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/view',
    component: AppDetailComponent,
    resolve: {
      app: AppResolve,
    },
    data: {
      authorities: [Authority.ADMIN,Authority.SP,Authority.IP],
      pageTitle: 'Apps',
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: 'new',
    component: AppUpdateComponent,
    resolve: {
      app: AppResolve,
    },
    data: {
      authorities: [Authority.ADMIN,Authority.SP,Authority.IP],
      pageTitle: 'Apps',
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/edit',
    component: AppUpdateComponent,
    resolve: {
      app: AppResolve,
    },
    data: {
      authorities: [Authority.ADMIN,Authority.SP,Authority.IP],
      pageTitle: 'Apps',
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/manage',
    component: AppManageComponent,
    resolve: {
      app: AppResolve,
    },
    data: {
      authorities: [Authority.ADMIN,Authority.SP,Authority.IP],
      pageTitle: 'Apps',
    },
    canActivate: [UserRouteAccessService],
  }
];
