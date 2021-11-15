import { Component, OnInit, OnDestroy } from '@angular/core';
import { HttpHeaders, HttpResponse } from '@angular/common/http';
import { ActivatedRoute, ParamMap, Router, Data } from '@angular/router';
import { Subscription, interval, combineLatest } from 'rxjs';
import { JhiEventManager } from 'ng-jhipster';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';

import { INodeReport } from 'app/shared/model/node-report.model';

import { ITEMS_PER_PAGE } from 'app/shared/constants/pagination.constants';
import { NodeReportService } from './node-report.service';
import { NodeReportDeleteDialogComponent } from './node-report-delete-dialog.component';

@Component({
  selector: 'jhi-node-report',
  templateUrl: './node-report.component.html',
})
export class NodeReportComponent implements OnInit, OnDestroy {
  nodeReports?: INodeReport[];
  eventSubscriber?: Subscription;
  totalItems = 0;
  itemsPerPage = ITEMS_PER_PAGE;
  page!: number;
  predicate!: string;
  ascending!: boolean;
  ngbPaginationPage = 1;
  categoryFilter = "";
  source = interval(10000);
  subscription: Subscription;

  constructor(
    protected nodeReportService: NodeReportService,
    protected activatedRoute: ActivatedRoute,
    protected router: Router,
    protected eventManager: JhiEventManager,
    protected modalService: NgbModal
  ) {
    this.subscription = this.source.subscribe(() => this.eventManager.broadcast('nodeReportListModification'));	
  }

  loadPage(page?: number, dontNavigate?: boolean): void {
    const pageToLoad: number = page || this.page || 1;

    this.nodeReportService
      .query(this.categoryFilter, {
        page: pageToLoad - 1,
        size: this.itemsPerPage,
        sort: this.sort(),
      })
      .subscribe(
        (res: HttpResponse<INodeReport[]>) => this.onSuccess(res.body, res.headers, pageToLoad, !dontNavigate),
        () => this.onError()
      );
  }

  ngOnInit(): void {
    this.handleNavigation();
    this.registerChangeInNodeReports();
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
      this.subscription.unsubscribe();
    }
  }

  trackId(index: number, item: INodeReport): number {
    // eslint-disable-next-line @typescript-eslint/no-unnecessary-type-assertion
    return item.id!;
  }

  registerChangeInNodeReports(): void {
    this.eventSubscriber = this.eventManager.subscribe('nodeReportListModification', () => this.loadPage());
  }

  delete(nodeReport: INodeReport): void {
    const modalRef = this.modalService.open(NodeReportDeleteDialogComponent, { size: 'lg', backdrop: 'static' });
    modalRef.componentInstance.nodeReport = nodeReport;
  }

  sort(): string[] {
    const result = [this.predicate + ',' + (this.ascending ? 'asc' : 'desc')];
    if (this.predicate !== 'id') {
      result.push('id');
    }
    return result;
  }
  
  reload(): void {
	    this.eventManager.broadcast('nodeReportListModification');
  }

  protected onSuccess(data: INodeReport[] | null, headers: HttpHeaders, page: number, navigate: boolean): void {
    this.totalItems = Number(headers.get('X-Total-Count'));
    this.page = page;
    if (navigate) {
      this.router.navigate(['/node-report'], {
        queryParams: {
	      filter: this.categoryFilter,
          page: this.page,
          size: this.itemsPerPage,
          sort: this.predicate + ',' + (this.ascending ? 'asc' : 'desc'),
        },
      });
    }
    this.nodeReports = data || [];
    this.ngbPaginationPage = this.page;
  }

  protected onError(): void {
    this.ngbPaginationPage = this.page ?? 1;
  }
}
