import { Component } from '@angular/core';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { JhiEventManager } from 'ng-jhipster';

import { IPenalty } from 'app/shared/model/penalty.model';
import { PenaltyService } from './penalty.service';

@Component({
  templateUrl: './penalty-delete-dialog.component.html',
})
export class PenaltyDeleteDialogComponent {
  penalty?: IPenalty;

  constructor(protected penaltyService: PenaltyService, public activeModal: NgbActiveModal, protected eventManager: JhiEventManager) {}

  cancel(): void {
    this.activeModal.dismiss();
  }

  confirmDelete(id: number): void {
    this.penaltyService.delete(id).subscribe(() => {
      this.eventManager.broadcast('penaltyListModification');
      this.activeModal.close();
    });
  }
}
