import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';

import { IBenchmarkSummary } from 'app/shared/model/benchmark-summary.model';

@Component({
  selector: 'jhi-benchmark-summary-detail',
  templateUrl: './benchmark-summary-detail.component.html',
})
export class BenchmarkSummaryDetailComponent implements OnInit {
  benchmarkSummary: IBenchmarkSummary | null = null;

  constructor(protected activatedRoute: ActivatedRoute) {}

  ngOnInit(): void {
    this.activatedRoute.data.subscribe(({ benchmarkSummary }) => (this.benchmarkSummary = benchmarkSummary));
  }

  previousState(): void {
    window.history.back();
  }
}
