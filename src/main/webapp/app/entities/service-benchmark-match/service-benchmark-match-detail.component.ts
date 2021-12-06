import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';

import { IServiceBenchmarkMatch } from 'app/shared/model/service-benchmark-match.model';

@Component({
  selector: 'jhi-service-benchmark-match-detail',
  templateUrl: './service-benchmark-match-detail.component.html',
})
export class ServiceBenchmarkMatchDetailComponent implements OnInit {
  serviceBenchmarkMatch: IServiceBenchmarkMatch | null = null;

  constructor(protected activatedRoute: ActivatedRoute) {}

  ngOnInit(): void {
    this.activatedRoute.data.subscribe(({ serviceBenchmarkMatch }) => (this.serviceBenchmarkMatch = serviceBenchmarkMatch));
  }

  previousState(): void {
    window.history.back();
  }
}
