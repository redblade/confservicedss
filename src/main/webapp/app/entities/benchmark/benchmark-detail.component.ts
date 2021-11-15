import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';

import { IBenchmark } from 'app/shared/model/benchmark.model';

@Component({
  selector: 'jhi-benchmark-detail',
  templateUrl: './benchmark-detail.component.html',
})
export class BenchmarkDetailComponent implements OnInit {
  benchmark: IBenchmark | null = null;

  constructor(protected activatedRoute: ActivatedRoute) {}

  ngOnInit(): void {
    this.activatedRoute.data.subscribe(({ benchmark }) => (this.benchmark = benchmark));
  }

  previousState(): void {
    window.history.back();
  }
}
