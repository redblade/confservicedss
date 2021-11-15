import { Component } from '@angular/core';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { JhiEventManager } from 'ng-jhipster';

import { IGuarantee } from 'app/shared/model/guarantee.model';
import { GuaranteeService } from './guarantee.service';

@Component({
  templateUrl: './guarantee-delete-dialog.component.html',
})
export class GuaranteeDeleteDialogComponent {
  guarantee?: IGuarantee;

  constructor(protected guaranteeService: GuaranteeService, public activeModal: NgbActiveModal, protected eventManager: JhiEventManager) {}

  cancel(): void {
    this.activeModal.dismiss();
  }

  confirmDelete(id: number): void {
    this.guaranteeService.delete(id).subscribe(() => {
      this.eventManager.broadcast('guaranteeListModification');
      this.activeModal.close();
    });
  }
}
