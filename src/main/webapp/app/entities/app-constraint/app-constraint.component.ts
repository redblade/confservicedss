import { Component, OnInit, OnDestroy } from '@angular/core';
import { HttpHeaders, HttpResponse } from '@angular/common/http';
import { ActivatedRoute, ParamMap, Router, Data } from '@angular/router';
import { Subscription, combineLatest } from 'rxjs';
import { JhiEventManager } from 'ng-jhipster';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';

import { IAppConstraint } from 'app/shared/model/app-constraint.model';

import { ITEMS_PER_PAGE } from 'app/shared/constants/pagination.constants';
import { AppConstraintService } from './app-constraint.service';
import { AppConstraintDeleteDialogComponent } from './app-constraint-delete-dialog.component';

@Component({
  selector: 'jhi-app-constraint',
  templateUrl: './app-constraint.component.html',
})
export class AppConstraintComponent implements OnInit, OnDestroy {
  appConstraints?: IAppConstraint[];
  eventSubscriber?: Subscription;
  totalItems = 0;
  itemsPerPage = ITEMS_PER_PAGE;
  page!: number;
  predicate!: string;
  ascending!: boolean;
  ngbPaginationPage = 1;

  constructor(
    protected appConstraintService: AppConstraintService,
    protected activatedRoute: ActivatedRoute,
    protected router: Router,
    protected eventManager: JhiEventManager,
    protected modalService: NgbModal
  ) {}

  loadPage(page?: number, dontNavigate?: boolean): void {
    const pageToLoad: number = page || this.page || 1;

    this.appConstraintService
      .query({
        page: pageToLoad - 1,
        size: this.itemsPerPage,
        sort: this.sort(),
      })
      .subscribe(
        (res: HttpResponse<IAppConstraint[]>) => this.onSuccess(res.body, res.headers, pageToLoad, !dontNavigate),
        () => this.onError()
      );
  }

  ngOnInit(): void {
    this.handleNavigation();
    this.registerChangeInAppConstraints();
  }

  public isExposeVisible(appConstraint: IAppConstraint) : boolean {
	return appConstraint.category === 'skupper';
  }

  public isUnexposeVisible(appConstraint: IAppConstraint) : boolean {
	return appConstraint.category === 'skupper';
  }

  expose(appConstraint: IAppConstraint): void {
	this.appConstraintService.expose(appConstraint).subscribe(() => {
		
    });    
  }
  unexpose(appConstraint: IAppConstraint): void {
	this.appConstraintService.unexpose(appConstraint).subscribe(() => {
		
    });    
  }

  protected handleNavigation(): void {
    combineLatest(this.activatedRoute.data, this.activatedRoute.queryParamMap, (data: Data, params: ParamMap) => {
      const page = params.get('page');
      const pageNumber = page !== null ? +page : 1;
      const sort = (params.get('sort') ?? data['defaultSort']).split(',');
      const predicate = sort[0];
      const ascending = sort[1] === 'asc';
      if (pageNumber !== this.page || predicate !== this.predicate || ascending !== this.ascending) {
        this.predicate = predicate;
        this.ascending = ascending;
        this.loadPage(pageNumber, true);
      }
    }).subscribe();
  }

  ngOnDestroy(): void {
    if (this.eventSubscriber) {
      this.eventManager.destroy(this.eventSubscriber);
    }
  }

  trackId(index: number, item: IAppConstraint): number {
    // eslint-disable-next-line @typescript-eslint/no-unnecessary-type-assertion
    return item.id!;
  }

  registerChangeInAppConstraints(): void {
    this.eventSubscriber = this.eventManager.subscribe('appConstraintListModification', () => this.loadPage());
  }

  delete(appConstraint: IAppConstraint): void {
    const modalRef = this.modalService.open(AppConstraintDeleteDialogComponent, { size: 'lg', backdrop: 'static' });
    modalRef.componentInstance.appConstraint = appConstraint;
  }

  sort(): string[] {
    const result = [this.predicate + ',' + (this.ascending ? 'asc' : 'desc')];
    if (this.predicate !== 'id') {
      result.push('id');
    }
    return result;
  }

  protected onSuccess(data: IAppConstraint[] | null, headers: HttpHeaders, page: number, navigate: boolean): void {
    this.totalItems = Number(headers.get('X-Total-Count'));
    this.page = page;
    if (navigate) {
      this.router.navigate(['/app-constraint'], {
        queryParams: {
          page: this.page,
          size: this.itemsPerPage,
          sort: this.predicate + ',' + (this.ascending ? 'asc' : 'desc'),
        },
      });
    }
    this.appConstraints = data || [];
    this.ngbPaginationPage = this.page;
  }

  protected onError(): void {
    this.ngbPaginationPage = this.page ?? 1;
  }
}
