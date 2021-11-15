import { Component, OnInit, OnDestroy } from '@angular/core';
import { HttpHeaders, HttpResponse } from '@angular/common/http';
import { ActivatedRoute, ParamMap, Router, Data } from '@angular/router';
import { Subscription, combineLatest } from 'rxjs';
import { JhiEventManager } from 'ng-jhipster';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';

import { IInfrastructure } from 'app/shared/model/infrastructure.model';

import { ITEMS_PER_PAGE } from 'app/shared/constants/pagination.constants';
import { InfrastructureService } from './infrastructure.service';
import { InfrastructureDeleteDialogComponent } from './infrastructure-delete-dialog.component';

@Component({
  selector: 'jhi-infrastructure',
  templateUrl: './infrastructure.component.html',
})
export class InfrastructureComponent implements OnInit, OnDestroy {
  infrastructures?: IInfrastructure[];
  eventSubscriber?: Subscription;
  totalItems = 0;
  itemsPerPage = ITEMS_PER_PAGE;
  page!: number;
  predicate!: string;
  ascending!: boolean;
  ngbPaginationPage = 1;

  constructor(
    protected infrastructureService: InfrastructureService,
    protected activatedRoute: ActivatedRoute,
    protected router: Router,
    protected eventManager: JhiEventManager,
    protected modalService: NgbModal
  ) {}

  loadPage(page?: number, dontNavigate?: boolean): void {
    const pageToLoad: number = page || this.page || 1;

    this.infrastructureService
      .query({
        page: pageToLoad - 1,
        size: this.itemsPerPage,
        sort: this.sort(),
      })
      .subscribe(
        (res: HttpResponse<IInfrastructure[]>) => this.onSuccess(res.body, res.headers, pageToLoad, !dontNavigate),
        () => this.onError()
      );
  }

  ngOnInit(): void {
    this.handleNavigation();
    this.registerChangeInInfrastructures();
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

  trackId(index: number, item: IInfrastructure): number {
    // eslint-disable-next-line @typescript-eslint/no-unnecessary-type-assertion
    return item.id!;
  }

  registerChangeInInfrastructures(): void {
    this.eventSubscriber = this.eventManager.subscribe('infrastructureListModification', () => this.loadPage());
  }

  delete(infrastructure: IInfrastructure): void {
    const modalRef = this.modalService.open(InfrastructureDeleteDialogComponent, { size: 'lg', backdrop: 'static' });
    modalRef.componentInstance.infrastructure = infrastructure;
  }

  sort(): string[] {
    const result = [this.predicate + ',' + (this.ascending ? 'asc' : 'desc')];
    if (this.predicate !== 'id') {
      result.push('id');
    }
    return result;
  }

  protected onSuccess(data: IInfrastructure[] | null, headers: HttpHeaders, page: number, navigate: boolean): void {
    this.totalItems = Number(headers.get('X-Total-Count'));
    this.page = page;
    if (navigate) {
      this.router.navigate(['/infrastructure'], {
        queryParams: {
          page: this.page,
          size: this.itemsPerPage,
          sort: this.predicate + ',' + (this.ascending ? 'asc' : 'desc'),
        },
      });
    }
    this.infrastructures = data || [];
    this.ngbPaginationPage = this.page;
  }

  protected onError(): void {
    this.ngbPaginationPage = this.page ?? 1;
  }

  public isLatencyURLEmpty(infrastructure: IInfrastructure) : boolean {
	const result = this.getLatencyURL(infrastructure).length === 0;
	return result;
  }
  public getLatencyURL(infrastructure: IInfrastructure) : string{
	let latencyURL = '';
	let monitoringPlugin = infrastructure.monitoringPlugin?infrastructure.monitoringPlugin:"";

	if(monitoringPlugin !== undefined) {
		const monitoringPluginLength = monitoringPlugin.length;
		if(monitoringPluginLength > 0){
			const stringToSearch = '\'goldpinger_endpoint\': \'';
			const indexStart = monitoringPlugin.indexOf(stringToSearch);
			if(indexStart >= 0){
				monitoringPlugin = monitoringPlugin.substring(indexStart + stringToSearch.length);
				const indexStop = monitoringPlugin.indexOf('\'');
				if(indexStop > 0){
					latencyURL = monitoringPlugin.substr(0, indexStop);
				}
			}
		}
	}
	
	return latencyURL;
  }
}
