import { Component } from '@angular/core';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { JhiEventManager } from 'ng-jhipster';

import { INode } from 'app/shared/model/node.model';
import { NodeService } from './node.service';

@Component({
  templateUrl: './node-delete-dialog.component.html',
})
export class NodeDeleteDialogComponent {
  node?: INode;

  constructor(protected nodeService: NodeService, public activeModal: NgbActiveModal, protected eventManager: JhiEventManager) {}

  cancel(): void {
    this.activeModal.dismiss();
  }

  confirmDelete(id: number): void {
    this.nodeService.delete(id).subscribe(() => {
      this.eventManager.broadcast('nodeListModification');
      this.activeModal.close();
    });
  }
}
