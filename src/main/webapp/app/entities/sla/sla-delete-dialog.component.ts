import { Component } from '@angular/core';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { JhiEventManager } from 'ng-jhipster';

import { ISla } from 'app/shared/model/sla.model';
import { SlaService } from './sla.service';

@Component({
  templateUrl: './sla-delete-dialog.component.html',
})
export class SlaDeleteDialogComponent {
  sla?: ISla;

  constructor(protected slaService: SlaService, public activeModal: NgbActiveModal, protected eventManager: JhiEventManager) {}

  cancel(): void {
    this.activeModal.dismiss();
  }

  confirmDelete(id: number): void {
    this.slaService.delete(id).subscribe(() => {
      this.eventManager.broadcast('slaListModification');
      this.activeModal.close();
    });
  }
}
