import { Component, OnInit, OnDestroy } from '@angular/core';
import { HttpHeaders, HttpResponse } from '@angular/common/http';
import { ActivatedRoute, ParamMap, Router, Data } from '@angular/router';
import { Subscription, interval, combineLatest } from 'rxjs';
import { JhiEventManager } from 'ng-jhipster';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';
import * as humanizeDuration from 'humanize-duration';
import { IBenchmarkReport } from 'app/shared/model/benchmark-report.model';

import { ITEMS_PER_PAGE } from 'app/shared/constants/pagination.constants';
import { BenchmarkReportService } from './benchmark-report.service';
import { BenchmarkReportDeleteDialogComponent } from './benchmark-report-delete-dialog.component';

@Component({
  selector: 'jhi-benchmark-report',
  templateUrl: './benchmark-report.component.html',
})
export class BenchmarkReportComponent implements OnInit, OnDestroy {
  benchmarkReports?: IBenchmarkReport[];
  eventSubscriber?: Subscription;
  totalItems = 0;
  itemsPerPage = ITEMS_PER_PAGE;
  page!: number;
  predicate!: string;
  ascending!: boolean;
  ngbPaginationPage = 1;
  source = interval(10000);
  subscription: Subscription;

  constructor(
    protected benchmarkReportService: BenchmarkReportService,
    protected activatedRoute: ActivatedRoute,
    protected router: Router,
    protected eventManager: JhiEventManager,
    protected modalService: NgbModal
  ) {
    this.subscription = this.source.subscribe(() => this.eventManager.broadcast('benchmarkReportListModification'));
  }

  loadPage(page?: number, dontNavigate?: boolean): void {
    const pageToLoad: number = page || this.page || 1;

    this.benchmarkReportService
      .query({
        page: pageToLoad - 1,
        size: this.itemsPerPage,
        sort: this.sort(),
      })
      .subscribe(
        (res: HttpResponse<IBenchmarkReport[]>) => this.onSuccess(res.body, res.headers, pageToLoad, !dontNavigate),
        () => this.onError()
      );
  }

  ngOnInit(): void {
    this.handleNavigation();
    this.registerChangeInBenchmarkReports();
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

  trackId(index: number, item: IBenchmarkReport): number {
    // eslint-disable-next-line @typescript-eslint/no-unnecessary-type-assertion
    return item.id!;
  }

  registerChangeInBenchmarkReports(): void {
    this.eventSubscriber = this.eventManager.subscribe('benchmarkReportListModification', () => this.loadPage());
  }

  delete(benchmarkReport: IBenchmarkReport): void {
    const modalRef = this.modalService.open(BenchmarkReportDeleteDialogComponent, { size: 'lg', backdrop: 'static' });
    modalRef.componentInstance.benchmarkReport = benchmarkReport;
  }

  sort(): string[] {
    const result = [this.predicate + ',' + (this.ascending ? 'asc' : 'desc')];
    if (this.predicate !== 'id') {
      result.push('id');
    }
    return result;
  }

  humanizeInterval(r: IBenchmarkReport): string {
    // "interval" arrives in seconds in Kafka messages, but it is stored in hours
    // humanizeDuration() takes the interval in milliseconds
    // So we transform interval from hours to seconds ("*60 * 60") and then to milliseconds ("* 1000")
    return humanizeDuration((r.interval || 0) * 60 * 60 * 1000)
  }

  generateGrafanaURL(r: IBenchmarkReport): string {
    /*
    FIXME: very very hugly, temporary and hardcorded solution.

    A better approach would be:

    1. baseUrl is taken from Confservice configuration (read from env var)

    2. tool, workload and version tokens are sent from the Analytics module. For this we need to
       modify the BenchmarkReport model and the kafka message to include these values
    OR
    2. the full url is buid from the analyzer and sent in the Kafka message (cons: we need to persist all the urls)

    TODO:
    1. what to do about authentication?
    2. also the provider and metric variables in Grafana could be set from the values in the benchmark report
    3. what about doing a dashboard specific for this (not the generic "Results")

    */

    const baseUrl = "http://192.168.70.13:30400/charts/d/4NJSceKGz/results?orgId=1&var-datasource=BenchsuiteMetrics&var-provider=All&var-metric=All"

    const [tool, workload, version] = ((r.benchmark ?? {}).name ?? "").split(':')
    const toolNameMapping = {
      'phoronix': 'Phoronix',
      'iperf': 'Iperf',
      'sysbench-cpu': 'Sysbench CPU',
      'sysbench-mysql': 'sysbench-mysql',
      'tfb-nodejs': 'tfb-nodejs',
      'tfb-django': 'tfb-django',
      'ycsb-mongodb': 'YCSB MongoDB',
      'cloudsuite-in-memory-analytics': 'CloudSuite In Memory Analytics'
    }

    // eslint-disable-next-line no-console
    console.log(tool)

    return baseUrl + "&var-tool=" + toolNameMapping[tool] + "&var-workload=" + workload + "&var-version=" + version
  }

  protected onSuccess(data: IBenchmarkReport[] | null, headers: HttpHeaders, page: number, navigate: boolean): void {
    this.totalItems = Number(headers.get('X-Total-Count'));
    this.page = page;
    if (navigate) {
      this.router.navigate(['/benchmark-report'], {
        queryParams: {
          page: this.page,
          size: this.itemsPerPage,
          sort: this.predicate + ',' + (this.ascending ? 'asc' : 'desc'),
        },
      });
    }
    this.benchmarkReports = data || [];
    this.ngbPaginationPage = this.page;
  }

  protected onError(): void {
    this.ngbPaginationPage = this.page ?? 1;
  }
}