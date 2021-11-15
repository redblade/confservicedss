import { Injectable } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
import { Resolve, ActivatedRouteSnapshot, Routes, Router } from '@angular/router';
import { Observable, of, EMPTY } from 'rxjs';
import { flatMap } from 'rxjs/operators';

import { Authority } from 'app/shared/constants/authority.constants';
import { UserRouteAccessService } from 'app/core/auth/user-route-access-service';
import { ISla, Sla } from 'app/shared/model/sla.model';
import { SlaService } from './sla.service';
import { SlaComponent } from './sla.component';
import { SlaDetailComponent } from './sla-detail.component';
import { SlaUpdateComponent } from './sla-update.component';

@Injectable({ providedIn: 'root' })
export class SlaResolve implements Resolve<ISla> {
  constructor(private service: SlaService, private router: Router) {}

  resolve(route: ActivatedRouteSnapshot): Observable<ISla> | Observable<never> {
    const id = route.params['id'];
    if (id) {
      return this.service.find(id).pipe(
        flatMap((sla: HttpResponse<Sla>) => {
          if (sla.body) {
            return of(sla.body);
          } else {
            this.router.navigate(['404']);
            return EMPTY;
          }
        })
      );
    }
    return of(new Sla());
  }
}

export const slaRoute: Routes = [
  {
    path: '',
    component: SlaComponent,
    data: {
      authorities: [Authority.ADMIN,Authority.SP,Authority.IP],
      defaultSort: 'id,asc',
      pageTitle: 'SLA list',
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/view',
    component: SlaDetailComponent,
    resolve: {
      sla: SlaResolve,
    },
    data: {
      authorities: [Authority.ADMIN,Authority.SP,Authority.IP],
      pageTitle: 'SLA list',
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: 'new',
    component: SlaUpdateComponent,
    resolve: {
      sla: SlaResolve,
    },
    data: {
      authorities: [Authority.ADMIN,Authority.SP,Authority.IP],
      pageTitle: 'SLA list',
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/edit',
    component: SlaUpdateComponent,
    resolve: {
      sla: SlaResolve,
    },
    data: {
      authorities: [Authority.ADMIN,Authority.SP,Authority.IP],
      pageTitle: 'SLA list',
    },
    canActivate: [UserRouteAccessService],
  },
];
