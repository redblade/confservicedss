import { Injectable } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
import { Resolve, ActivatedRouteSnapshot, Routes, Router } from '@angular/router';
import { Observable, of, EMPTY } from 'rxjs';
import { flatMap } from 'rxjs/operators';

import { Authority } from 'app/shared/constants/authority.constants';
import { UserRouteAccessService } from 'app/core/auth/user-route-access-service';
import { ICriticalService, CriticalService } from 'app/shared/model/critical-service.model';
import { CriticalServiceService } from './critical-service.service';
import { CriticalServiceComponent } from './critical-service.component';
import { CriticalServiceDetailComponent } from './critical-service-detail.component';
import { CriticalServiceUpdateComponent } from './critical-service-update.component';

@Injectable({ providedIn: 'root' })
export class CriticalServiceResolve implements Resolve<ICriticalService> {
  constructor(private service: CriticalServiceService, private router: Router) {}

  resolve(route: ActivatedRouteSnapshot): Observable<ICriticalService> | Observable<never> {
    const id = route.params['id'];
    if (id) {
      return this.service.find(id).pipe(
        flatMap((criticalService: HttpResponse<CriticalService>) => {
          if (criticalService.body) {
            return of(criticalService.body);
          } else {
            this.router.navigate(['404']);
            return EMPTY;
          }
        })
      );
    }
    return of(new CriticalService());
  }
}

export const criticalServiceRoute: Routes = [
  {
    path: '',
    component: CriticalServiceComponent,
    data: {
      authorities: [Authority.SP],
      defaultSort: 'id,asc',
      pageTitle: 'CriticalServices',
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/view',
    component: CriticalServiceDetailComponent,
    resolve: {
      criticalService: CriticalServiceResolve,
    },
    data: {
      authorities: [Authority.SP],
      pageTitle: 'CriticalServices',
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: 'new',
    component: CriticalServiceUpdateComponent,
    resolve: {
      criticalService: CriticalServiceResolve,
    },
    data: {
      authorities: [Authority.SP],
      pageTitle: 'CriticalServices',
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/edit',
    component: CriticalServiceUpdateComponent,
    resolve: {
      criticalService: CriticalServiceResolve,
    },
    data: {
      authorities: [Authority.SP],
      pageTitle: 'CriticalServices',
    },
    canActivate: [UserRouteAccessService],
  },
];
