import { Injectable } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
import { Resolve, ActivatedRouteSnapshot, Routes, Router } from '@angular/router';
import { Observable, of, EMPTY } from 'rxjs';
import { flatMap } from 'rxjs/operators';

import { Authority } from 'app/shared/constants/authority.constants';
import { UserRouteAccessService } from 'app/core/auth/user-route-access-service';
import { ISlaViolation, SlaViolation } from 'app/shared/model/sla-violation.model';
import { SlaViolationService } from './sla-violation.service';
import { SlaViolationComponent } from './sla-violation.component';
import { SlaViolationDetailComponent } from './sla-violation-detail.component';
import { SlaViolationUpdateComponent } from './sla-violation-update.component';

@Injectable({ providedIn: 'root' })
export class SlaViolationResolve implements Resolve<ISlaViolation> {
  constructor(private service: SlaViolationService, private router: Router) {}

  resolve(route: ActivatedRouteSnapshot): Observable<ISlaViolation> | Observable<never> {
    const id = route.params['id'];
    if (id) {
      return this.service.find(id).pipe(
        flatMap((slaViolation: HttpResponse<SlaViolation>) => {
          if (slaViolation.body) {
            return of(slaViolation.body);
          } else {
            this.router.navigate(['404']);
            return EMPTY;
          }
        })
      );
    }
    return of(new SlaViolation());
  }
}

export const slaViolationRoute: Routes = [
  {
    path: '',
    component: SlaViolationComponent,
    data: {
      authorities: [Authority.ADMIN,Authority.SP,Authority.IP],
      defaultSort: 'id,asc',
      pageTitle: 'SlaViolations',
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/view',
    component: SlaViolationDetailComponent,
    resolve: {
      slaViolation: SlaViolationResolve,
    },
    data: {
      authorities: [Authority.ADMIN,Authority.SP,Authority.IP],
      pageTitle: 'SlaViolations',
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: 'new',
    component: SlaViolationUpdateComponent,
    resolve: {
      slaViolation: SlaViolationResolve,
    },
    data: {
      authorities: [Authority.ADMIN,Authority.SP,Authority.IP],
      pageTitle: 'SlaViolations',
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/edit',
    component: SlaViolationUpdateComponent,
    resolve: {
      slaViolation: SlaViolationResolve,
    },
    data: {
      authorities: [Authority.ADMIN,Authority.SP,Authority.IP],
      pageTitle: 'SlaViolations',
    },
    canActivate: [UserRouteAccessService],
  },
];
