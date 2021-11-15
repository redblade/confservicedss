import { Component } from '@angular/core';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { JhiEventManager } from 'ng-jhipster';

import { IBenchmarkReport } from 'app/shared/model/benchmark-report.model';
import { BenchmarkReportService } from './benchmark-report.service';

@Component({
  templateUrl: './benchmark-report-delete-dialog.component.html',
})
export class BenchmarkReportDeleteDialogComponent {
  benchmarkReport?: IBenchmarkReport;

  constructor(
    protected benchmarkReportService: BenchmarkReportService,
    public activeModal: NgbActiveModal,
    protected eventManager: JhiEventManager
  ) {}

  cancel(): void {
    this.activeModal.dismiss();
  }

  confirmDelete(id: number): void {
    this.benchmarkReportService.delete(id).subscribe(() => {
      this.eventManager.broadcast('benchmarkReportListModification');
      this.activeModal.close();
    });
  }
}
