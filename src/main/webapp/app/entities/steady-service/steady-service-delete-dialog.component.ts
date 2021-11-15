import { Component } from '@angular/core';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { JhiEventManager } from 'ng-jhipster';

import { ISteadyService } from 'app/shared/model/steady-service.model';
import { SteadyServiceService } from './steady-service.service';

@Component({
  templateUrl: './steady-service-delete-dialog.component.html',
})
export class SteadyServiceDeleteDialogComponent {
  steadyService?: ISteadyService;

  constructor(
    protected steadyServiceService: SteadyServiceService,
    public activeModal: NgbActiveModal,
    protected eventManager: JhiEventManager
  ) {}

  cancel(): void {
    this.activeModal.dismiss();
  }

  confirmDelete(id: number): void {
    this.steadyServiceService.delete(id).subscribe(() => {
      this.eventManager.broadcast('steadyServiceListModification');
      this.activeModal.close();
    });
  }
}
