import { Injectable } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
import { Resolve, ActivatedRouteSnapshot, Routes, Router } from '@angular/router';
import { Observable, of, EMPTY } from 'rxjs';
import { flatMap } from 'rxjs/operators';

import { Authority } from 'app/shared/constants/authority.constants';
import { UserRouteAccessService } from 'app/core/auth/user-route-access-service';
import { IServiceProvider, ServiceProvider } from 'app/shared/model/service-provider.model';
import { ServiceProviderService } from './service-provider.service';
import { ServiceProviderComponent } from './service-provider.component';
import { ServiceProviderDetailComponent } from './service-provider-detail.component';
import { ServiceProviderUpdateComponent } from './service-provider-update.component';

@Injectable({ providedIn: 'root' })
export class ServiceProviderResolve implements Resolve<IServiceProvider> {
  constructor(private service: ServiceProviderService, private router: Router) {}

  resolve(route: ActivatedRouteSnapshot): Observable<IServiceProvider> | Observable<never> {
    const id = route.params['id'];
    if (id) {
      return this.service.find(id).pipe(
        flatMap((serviceProvider: HttpResponse<ServiceProvider>) => {
          if (serviceProvider.body) {
            return of(serviceProvider.body);
          } else {
            this.router.navigate(['404']);
            return EMPTY;
          }
        })
      );
    }
    return of(new ServiceProvider());
  }
}

export const serviceProviderRoute: Routes = [
  {
    path: '',
    component: ServiceProviderComponent,
    data: {
      authorities: [Authority.ADMIN,Authority.SP,Authority.IP],
      defaultSort: 'id,asc',
      pageTitle: 'ServiceProviders',
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/view',
    component: ServiceProviderDetailComponent,
    resolve: {
      serviceProvider: ServiceProviderResolve,
    },
    data: {
      authorities: [Authority.ADMIN,Authority.SP,Authority.IP],
      pageTitle: 'ServiceProviders',
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: 'new',
    component: ServiceProviderUpdateComponent,
    resolve: {
      serviceProvider: ServiceProviderResolve,
    },
    data: {
      authorities: [Authority.ADMIN,Authority.SP,Authority.IP],
      pageTitle: 'ServiceProviders',
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/edit',
    component: ServiceProviderUpdateComponent,
    resolve: {
      serviceProvider: ServiceProviderResolve,
    },
    data: {
      authorities: [Authority.ADMIN,Authority.SP,Authority.IP],
      pageTitle: 'ServiceProviders',
    },
    canActivate: [UserRouteAccessService],
  },
];
