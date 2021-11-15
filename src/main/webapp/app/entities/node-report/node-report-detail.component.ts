import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';

import { INodeReport } from 'app/shared/model/node-report.model';

@Component({
  selector: 'jhi-node-report-detail',
  templateUrl: './node-report-detail.component.html',
})
export class NodeReportDetailComponent implements OnInit {
  nodeReport: INodeReport | null = null;

  constructor(protected activatedRoute: ActivatedRoute) {}

  ngOnInit(): void {
    this.activatedRoute.data.subscribe(({ nodeReport }) => (this.nodeReport = nodeReport));
  }

  previousState(): void {
    window.history.back();
  }
}
