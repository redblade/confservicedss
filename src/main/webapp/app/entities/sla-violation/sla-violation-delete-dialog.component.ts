import { Component } from '@angular/core';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { JhiEventManager } from 'ng-jhipster';

import { ISlaViolation } from 'app/shared/model/sla-violation.model';
import { SlaViolationService } from './sla-violation.service';

@Component({
  templateUrl: './sla-violation-delete-dialog.component.html',
})
export class SlaViolationDeleteDialogComponent {
  slaViolation?: ISlaViolation;

  constructor(
    protected slaViolationService: SlaViolationService,
    public activeModal: NgbActiveModal,
    protected eventManager: JhiEventManager
  ) {}

  cancel(): void {
    this.activeModal.dismiss();
  }

  confirmDelete(id: number): void {
    this.slaViolationService.delete(id).subscribe(() => {
      this.eventManager.broadcast('slaViolationListModification');
      this.activeModal.close();
    });
  }
}
