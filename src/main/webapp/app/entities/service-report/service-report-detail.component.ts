import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';

import { IServiceReport } from 'app/shared/model/service-report.model';

@Component({
  selector: 'jhi-service-report-detail',
  templateUrl: './service-report-detail.component.html',
})
export class ServiceReportDetailComponent implements OnInit {
  serviceReport: IServiceReport | null = null;

  constructor(protected activatedRoute: ActivatedRoute) {}

  ngOnInit(): void {
    this.activatedRoute.data.subscribe(({ serviceReport }) => (this.serviceReport = serviceReport));
  }

  previousState(): void {
    window.history.back();
  }
}
