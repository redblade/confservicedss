import { Component } from '@angular/core';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { JhiEventManager } from 'ng-jhipster';

import { IInfrastructure } from 'app/shared/model/infrastructure.model';
import { InfrastructureService } from './infrastructure.service';

@Component({
  templateUrl: './infrastructure-delete-dialog.component.html',
})
export class InfrastructureDeleteDialogComponent {
  infrastructure?: IInfrastructure;

  constructor(
    protected infrastructureService: InfrastructureService,
    public activeModal: NgbActiveModal,
    protected eventManager: JhiEventManager
  ) {}

  cancel(): void {
    this.activeModal.dismiss();
  }

  confirmDelete(id: number): void {
    this.infrastructureService.delete(id).subscribe(() => {
      this.eventManager.broadcast('infrastructureListModification');
      this.activeModal.close();
    });
  }
}
