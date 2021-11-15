import { Component } from '@angular/core';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { JhiEventManager } from 'ng-jhipster';

import { ICatalogApp } from 'app/shared/model/catalog-app.model';
import { CatalogAppService } from './catalog-app.service';

@Component({
  templateUrl: './catalog-app-delete-dialog.component.html',
})
export class CatalogAppDeleteDialogComponent {
  catalogApp?: ICatalogApp;

  constructor(
    protected catalogAppService: CatalogAppService,
    public activeModal: NgbActiveModal,
    protected eventManager: JhiEventManager
  ) {}

  cancel(): void {
    this.activeModal.dismiss();
  }

  confirmDelete(id: number): void {
    this.catalogAppService.delete(id).subscribe(() => {
      this.eventManager.broadcast('catalogAppListModification');
      this.activeModal.close();
    });
  }
}
