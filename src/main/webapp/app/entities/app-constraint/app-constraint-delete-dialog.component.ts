import { Component } from '@angular/core';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { JhiEventManager } from 'ng-jhipster';

import { IAppConstraint } from 'app/shared/model/app-constraint.model';
import { AppConstraintService } from './app-constraint.service';

@Component({
  templateUrl: './app-constraint-delete-dialog.component.html',
})
export class AppConstraintDeleteDialogComponent {
  appConstraint?: IAppConstraint;

  constructor(
    protected appConstraintService: AppConstraintService,
    public activeModal: NgbActiveModal,
    protected eventManager: JhiEventManager
  ) {}

  cancel(): void {
    this.activeModal.dismiss();
  }

  confirmDelete(id: number): void {
    this.appConstraintService.delete(id).subscribe(() => {
      this.eventManager.broadcast('appConstraintListModification');
      this.activeModal.close();
    });
  }
}
