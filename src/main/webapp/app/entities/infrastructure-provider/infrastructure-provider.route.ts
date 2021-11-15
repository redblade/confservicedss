import { Injectable } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
import { Resolve, ActivatedRouteSnapshot, Routes, Router } from '@angular/router';
import { Observable, of, EMPTY } from 'rxjs';
import { flatMap } from 'rxjs/operators';

import { Authority } from 'app/shared/constants/authority.constants';
import { UserRouteAccessService } from 'app/core/auth/user-route-access-service';
import { IInfrastructureProvider, InfrastructureProvider } from 'app/shared/model/infrastructure-provider.model';
import { InfrastructureProviderService } from './infrastructure-provider.service';
import { InfrastructureProviderComponent } from './infrastructure-provider.component';
import { InfrastructureProviderDetailComponent } from './infrastructure-provider-detail.component';
import { InfrastructureProviderUpdateComponent } from './infrastructure-provider-update.component';

@Injectable({ providedIn: 'root' })
export class InfrastructureProviderResolve implements Resolve<IInfrastructureProvider> {
  constructor(private service: InfrastructureProviderService, private router: Router) {}

  resolve(route: ActivatedRouteSnapshot): Observable<IInfrastructureProvider> | Observable<never> {
    const id = route.params['id'];
    if (id) {
      return this.service.find(id).pipe(
        flatMap((infrastructureProvider: HttpResponse<InfrastructureProvider>) => {
          if (infrastructureProvider.body) {
            return of(infrastructureProvider.body);
          } else {
            this.router.navigate(['404']);
            return EMPTY;
          }
        })
      );
    }
    return of(new InfrastructureProvider());
  }
}

export const infrastructureProviderRoute: Routes = [
  {
    path: '',
    component: InfrastructureProviderComponent,
    data: {
      authorities: [Authority.ADMIN,Authority.SP,Authority.IP],
      defaultSort: 'id,asc',
      pageTitle: 'InfrastructureProviders',
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/view',
    component: InfrastructureProviderDetailComponent,
    resolve: {
      infrastructureProvider: InfrastructureProviderResolve,
    },
    data: {
      authorities: [Authority.ADMIN,Authority.SP,Authority.IP],
      pageTitle: 'InfrastructureProviders',
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: 'new',
    component: InfrastructureProviderUpdateComponent,
    resolve: {
      infrastructureProvider: InfrastructureProviderResolve,
    },
    data: {
      authorities: [Authority.ADMIN,Authority.SP,Authority.IP],
      pageTitle: 'InfrastructureProviders',
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/edit',
    component: InfrastructureProviderUpdateComponent,
    resolve: {
      infrastructureProvider: InfrastructureProviderResolve,
    },
    data: {
      authorities: [Authority.ADMIN,Authority.SP,Authority.IP],
      pageTitle: 'InfrastructureProviders',
    },
    canActivate: [UserRouteAccessService],
  },
];
