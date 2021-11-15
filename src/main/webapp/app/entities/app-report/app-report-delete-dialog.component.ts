import { Component } from '@angular/core';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { JhiEventManager } from 'ng-jhipster';

import { IAppReport } from 'app/shared/model/app-report.model';
import { AppReportService } from './app-report.service';

@Component({
  templateUrl: './app-report-delete-dialog.component.html',
})
export class AppReportDeleteDialogComponent {
  appReport?: IAppReport;

  constructor(protected appReportService: AppReportService, public activeModal: NgbActiveModal, protected eventManager: JhiEventManager) {}

  cancel(): void {
    this.activeModal.dismiss();
  }

  confirmDelete(id: number): void {
    this.appReportService.delete(id).subscribe(() => {
      this.eventManager.broadcast('appReportListModification');
      this.activeModal.close();
    });
  }
}
