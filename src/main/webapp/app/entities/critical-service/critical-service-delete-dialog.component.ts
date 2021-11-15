import { Component } from '@angular/core';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { JhiEventManager } from 'ng-jhipster';

import { ICriticalService } from 'app/shared/model/critical-service.model';
import { CriticalServiceService } from './critical-service.service';

@Component({
  templateUrl: './critical-service-delete-dialog.component.html',
})
export class CriticalServiceDeleteDialogComponent {
  criticalService?: ICriticalService;

  constructor(
    protected criticalServiceService: CriticalServiceService,
    public activeModal: NgbActiveModal,
    protected eventManager: JhiEventManager
  ) {}

  cancel(): void {
    this.activeModal.dismiss();
  }

  confirmDelete(id: number): void {
    this.criticalServiceService.delete(id).subscribe(() => {
      this.eventManager.broadcast('criticalServiceListModification');
      this.activeModal.close();
    });
  }
}
