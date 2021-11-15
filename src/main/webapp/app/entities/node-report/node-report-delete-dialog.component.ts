import { Component } from '@angular/core';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { JhiEventManager } from 'ng-jhipster';

import { INodeReport } from 'app/shared/model/node-report.model';
import { NodeReportService } from './node-report.service';

@Component({
  templateUrl: './node-report-delete-dialog.component.html',
})
export class NodeReportDeleteDialogComponent {
  nodeReport?: INodeReport;

  constructor(
    protected nodeReportService: NodeReportService,
    public activeModal: NgbActiveModal,
    protected eventManager: JhiEventManager
  ) {}

  cancel(): void {
    this.activeModal.dismiss();
  }

  confirmDelete(id: number): void {
    this.nodeReportService.delete(id).subscribe(() => {
      this.eventManager.broadcast('nodeReportListModification');
      this.activeModal.close();
    });
  }
}
