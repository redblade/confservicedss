import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';

import { IBenchmarkReport } from 'app/shared/model/benchmark-report.model';

@Component({
  selector: 'jhi-benchmark-report-detail',
  templateUrl: './benchmark-report-detail.component.html',
})
export class BenchmarkReportDetailComponent implements OnInit {
  benchmarkReport: IBenchmarkReport | null = null;

  constructor(protected activatedRoute: ActivatedRoute) {}

  ngOnInit(): void {
    this.activatedRoute.data.subscribe(({ benchmarkReport }) => (this.benchmarkReport = benchmarkReport));
  }

  previousState(): void {
    window.history.back();
  }
}
