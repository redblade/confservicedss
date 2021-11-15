import { Component } from '@angular/core';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { JhiEventManager } from 'ng-jhipster';

import { IServiceReport } from 'app/shared/model/service-report.model';
import { ServiceReportService } from './service-report.service';

@Component({
  templateUrl: './service-report-delete-dialog.component.html',
})
export class ServiceReportDeleteDialogComponent {
  serviceReport?: IServiceReport;

  constructor(
    protected serviceReportService: ServiceReportService,
    public activeModal: NgbActiveModal,
    protected eventManager: JhiEventManager
  ) {}

  cancel(): void {
    this.activeModal.dismiss();
  }

  confirmDelete(id: number): void {
    this.serviceReportService.delete(id).subscribe(() => {
      this.eventManager.broadcast('serviceReportListModification');
      this.activeModal.close();
    });
  }
}
