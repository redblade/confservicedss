import { Component } from '@angular/core';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { JhiEventManager } from 'ng-jhipster';

import { IInfrastructureReport } from 'app/shared/model/infrastructure-report.model';
import { InfrastructureReportService } from './infrastructure-report.service';

@Component({
  templateUrl: './infrastructure-report-delete-dialog.component.html',
})
export class InfrastructureReportDeleteDialogComponent {
  infrastructureReport?: IInfrastructureReport;

  constructor(
    protected infrastructureReportService: InfrastructureReportService,
    public activeModal: NgbActiveModal,
    protected eventManager: JhiEventManager
  ) {}

  cancel(): void {
    this.activeModal.dismiss();
  }

  confirmDelete(id: number): void {
    this.infrastructureReportService.delete(id).subscribe(() => {
      this.eventManager.broadcast('infrastructureReportListModification');
      this.activeModal.close();
    });
  }
}
