import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';

import { IAppReport } from 'app/shared/model/app-report.model';

@Component({
  selector: 'jhi-app-report-detail',
  templateUrl: './app-report-detail.component.html',
})
export class AppReportDetailComponent implements OnInit {
  appReport: IAppReport | null = null;

  constructor(protected activatedRoute: ActivatedRoute) {}

  ngOnInit(): void {
    this.activatedRoute.data.subscribe(({ appReport }) => (this.appReport = appReport));
  }

  previousState(): void {
    window.history.back();
  }
}
