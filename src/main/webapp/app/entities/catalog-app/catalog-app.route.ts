import { Injectable } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
import { Resolve, ActivatedRouteSnapshot, Routes, Router } from '@angular/router';
import { Observable, of, EMPTY } from 'rxjs';
import { flatMap } from 'rxjs/operators';

import { Authority } from 'app/shared/constants/authority.constants';
import { UserRouteAccessService } from 'app/core/auth/user-route-access-service';
import { ICatalogApp, CatalogApp } from 'app/shared/model/catalog-app.model';
import { CatalogAppService } from './catalog-app.service';
import { CatalogAppComponent } from './catalog-app.component';
import { CatalogAppDetailComponent } from './catalog-app-detail.component';
import { CatalogAppUpdateComponent } from './catalog-app-update.component';

@Injectable({ providedIn: 'root' })
export class CatalogAppResolve implements Resolve<ICatalogApp> {
  constructor(private service: CatalogAppService, private router: Router) {}

  resolve(route: ActivatedRouteSnapshot): Observable<ICatalogApp> | Observable<never> {
    const id = route.params['id'];
    if (id) {
      return this.service.find(id).pipe(
        flatMap((catalogApp: HttpResponse<CatalogApp>) => {
          if (catalogApp.body) {
            return of(catalogApp.body);
          } else {
            this.router.navigate(['404']);
            return EMPTY;
          }
        })
      );
    }
    return of(new CatalogApp());
  }
}

export const catalogAppRoute: Routes = [
  {
    path: '',
    component: CatalogAppComponent,
    data: {
      authorities: [Authority.ADMIN,Authority.SP,Authority.IP],
      defaultSort: 'id,asc',
      pageTitle: 'CatalogApps',
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/view',
    component: CatalogAppDetailComponent,
    resolve: {
      catalogApp: CatalogAppResolve,
    },
    data: {
      authorities: [Authority.ADMIN,Authority.SP,Authority.IP],
      pageTitle: 'CatalogApps',
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: 'new',
    component: CatalogAppUpdateComponent,
    resolve: {
      catalogApp: CatalogAppResolve,
    },
    data: {
      authorities: [Authority.ADMIN,Authority.SP,Authority.IP],
      pageTitle: 'CatalogApps',
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/edit',
    component: CatalogAppUpdateComponent,
    resolve: {
      catalogApp: CatalogAppResolve,
    },
    data: {
      authorities: [Authority.ADMIN,Authority.SP,Authority.IP],
      pageTitle: 'CatalogApps',
    },
    canActivate: [UserRouteAccessService],
  },
];
