import { Component } from '@angular/core';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { JhiEventManager } from 'ng-jhipster';

import { IServiceConstraint } from 'app/shared/model/service-constraint.model';
import { ServiceConstraintService } from './service-constraint.service';

@Component({
  templateUrl: './service-constraint-delete-dialog.component.html',
})
export class ServiceConstraintDeleteDialogComponent {
  serviceConstraint?: IServiceConstraint;

  constructor(
    protected serviceConstraintService: ServiceConstraintService,
    public activeModal: NgbActiveModal,
    protected eventManager: JhiEventManager
  ) {}

  cancel(): void {
    this.activeModal.dismiss();
  }

  confirmDelete(id: number): void {
    this.serviceConstraintService.delete(id).subscribe(() => {
      this.eventManager.broadcast('serviceConstraintListModification');
      this.activeModal.close();
    });
  }
}
