import { Injectable } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
import { Resolve, ActivatedRouteSnapshot, Routes, Router } from '@angular/router';
import { Observable, of, EMPTY } from 'rxjs';
import { flatMap } from 'rxjs/operators';

import { Authority } from 'app/shared/constants/authority.constants';
import { UserRouteAccessService } from 'app/core/auth/user-route-access-service';
import { IGuarantee, Guarantee } from 'app/shared/model/guarantee.model';
import { GuaranteeService } from './guarantee.service';
import { GuaranteeComponent } from './guarantee.component';
import { GuaranteeDetailComponent } from './guarantee-detail.component';
import { GuaranteeUpdateComponent } from './guarantee-update.component';

@Injectable({ providedIn: 'root' })
export class GuaranteeResolve implements Resolve<IGuarantee> {
  constructor(private service: GuaranteeService, private router: Router) {}

  resolve(route: ActivatedRouteSnapshot): Observable<IGuarantee> | Observable<never> {
    const id = route.params['id'];
    if (id) {
      return this.service.find(id).pipe(
        flatMap((guarantee: HttpResponse<Guarantee>) => {
          if (guarantee.body) {
            return of(guarantee.body);
          } else {
            this.router.navigate(['404']);
            return EMPTY;
          }
        })
      );
    }
    return of(new Guarantee());
  }
}

export const guaranteeRoute: Routes = [
  {
    path: '',
    component: GuaranteeComponent,
    data: {
      authorities: [Authority.ADMIN,Authority.SP,Authority.IP],
      defaultSort: 'id,asc',
      pageTitle: 'Guarantees',
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/view',
    component: GuaranteeDetailComponent,
    resolve: {
      guarantee: GuaranteeResolve,
    },
    data: {
      authorities: [Authority.ADMIN,Authority.SP,Authority.IP],
      pageTitle: 'Guarantees',
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: 'new',
    component: GuaranteeUpdateComponent,
    resolve: {
      guarantee: GuaranteeResolve,
    },
    data: {
      authorities: [Authority.ADMIN,Authority.SP,Authority.IP],
      pageTitle: 'Guarantees',
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/edit',
    component: GuaranteeUpdateComponent,
    resolve: {
      guarantee: GuaranteeResolve,
    },
    data: {
      authorities: [Authority.ADMIN,Authority.SP,Authority.IP],
      pageTitle: 'Guarantees',
    },
    canActivate: [UserRouteAccessService],
  },
];
