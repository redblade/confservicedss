import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';

import { IInfrastructureReport } from 'app/shared/model/infrastructure-report.model';

@Component({
  selector: 'jhi-infrastructure-report-detail',
  templateUrl: './infrastructure-report-detail.component.html',
})
export class InfrastructureReportDetailComponent implements OnInit {
  infrastructureReport: IInfrastructureReport | null = null;

  constructor(protected activatedRoute: ActivatedRoute) {}

  ngOnInit(): void {
    this.activatedRoute.data.subscribe(({ infrastructureReport }) => (this.infrastructureReport = infrastructureReport));
  }

  previousState(): void {
    window.history.back();
  }
}
