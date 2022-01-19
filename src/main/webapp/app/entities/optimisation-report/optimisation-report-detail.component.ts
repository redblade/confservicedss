import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';

import { IOptimisationReport } from 'app/shared/model/optimisation-report.model';

@Component({
  selector: 'jhi-optimisation-report-detail',
  templateUrl: './optimisation-report-detail.component.html',
})
export class OptimisationReportDetailComponent implements OnInit {
  optimisationReport: IOptimisationReport | null = null;

  constructor(protected activatedRoute: ActivatedRoute) {}

  ngOnInit(): void {
    this.activatedRoute.data.subscribe(({ optimisationReport }) => (this.optimisationReport = optimisationReport));
  }

  previousState(): void {
    window.history.back();
  }
}
