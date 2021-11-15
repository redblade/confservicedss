import { Component } from '@angular/core';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { JhiEventManager } from 'ng-jhipster';

import { IServiceOptimisation } from 'app/shared/model/service-optimisation.model';
import { ServiceOptimisationService } from './service-optimisation.service';

@Component({
  templateUrl: './service-optimisation-delete-dialog.component.html',
})
export class ServiceOptimisationDeleteDialogComponent {
  serviceOptimisation?: IServiceOptimisation;

  constructor(
    protected serviceOptimisationService: ServiceOptimisationService,
    public activeModal: NgbActiveModal,
    protected eventManager: JhiEventManager
  ) {}

  cancel(): void {
    this.activeModal.dismiss();
  }

  confirmDelete(id: number): void {
    this.serviceOptimisationService.delete(id).subscribe(() => {
      this.eventManager.broadcast('serviceOptimisationListModification');
      this.activeModal.close();
    });
  }
}
