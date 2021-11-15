import { Component } from '@angular/core';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { JhiEventManager } from 'ng-jhipster';

import { IApp } from 'app/shared/model/app.model';
import { AppService } from './app.service';

@Component({
  templateUrl: './app-delete-dialog.component.html',
})
export class AppDeleteDialogComponent {
  app?: IApp;

  constructor(protected appService: AppService, public activeModal: NgbActiveModal, protected eventManager: JhiEventManager) {}

  cancel(): void {
    this.activeModal.dismiss();
  }

  confirmDelete(id: number): void {
    this.appService.delete(id).subscribe(() => {
      this.eventManager.broadcast('appListModification');
      this.activeModal.close();
    });
  }
}
