import { Injectable } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
import { Resolve, ActivatedRouteSnapshot, Routes, Router } from '@angular/router';
import { Observable, of, EMPTY } from 'rxjs';
import { flatMap } from 'rxjs/operators';

import { Authority } from 'app/shared/constants/authority.constants';
import { UserRouteAccessService } from 'app/core/auth/user-route-access-service';
import { IPenalty, Penalty } from 'app/shared/model/penalty.model';
import { PenaltyService } from './penalty.service';
import { PenaltyComponent } from './penalty.component';
import { PenaltyDetailComponent } from './penalty-detail.component';
import { PenaltyUpdateComponent } from './penalty-update.component';

@Injectable({ providedIn: 'root' })
export class PenaltyResolve implements Resolve<IPenalty> {
  constructor(private service: PenaltyService, private router: Router) {}

  resolve(route: ActivatedRouteSnapshot): Observable<IPenalty> | Observable<never> {
    const id = route.params['id'];
    if (id) {
      return this.service.find(id).pipe(
        flatMap((penalty: HttpResponse<Penalty>) => {
          if (penalty.body) {
            return of(penalty.body);
          } else {
            this.router.navigate(['404']);
            return EMPTY;
          }
        })
      );
    }
    return of(new Penalty());
  }
}

export const penaltyRoute: Routes = [
  {
    path: '',
    component: PenaltyComponent,
    data: {
      authorities: [Authority.ADMIN,Authority.SP,Authority.IP],
      defaultSort: 'id,asc',
      pageTitle: 'Penalties',
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/view',
    component: PenaltyDetailComponent,
    resolve: {
      penalty: PenaltyResolve,
    },
    data: {
      authorities: [Authority.ADMIN,Authority.SP,Authority.IP],
      pageTitle: 'Penalties',
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: 'new',
    component: PenaltyUpdateComponent,
    resolve: {
      penalty: PenaltyResolve,
    },
    data: {
      authorities: [Authority.ADMIN,Authority.SP,Authority.IP],
      pageTitle: 'Penalties',
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/edit',
    component: PenaltyUpdateComponent,
    resolve: {
      penalty: PenaltyResolve,
    },
    data: {
      authorities: [Authority.ADMIN,Authority.SP,Authority.IP],
      pageTitle: 'Penalties',
    },
    canActivate: [UserRouteAccessService],
  },
];
