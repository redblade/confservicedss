import { Component } from '@angular/core';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { JhiEventManager } from 'ng-jhipster';

import { IAppDeploymentOptions } from 'app/shared/model/app-deployment-options.model';
import { AppDeploymentOptionsService } from './app-deployment-options.service';

@Component({
  templateUrl: './app-deployment-options-delete-dialog.component.html',
})
export class AppDeploymentOptionsDeleteDialogComponent {
  appDeploymentOptions?: IAppDeploymentOptions;

  constructor(
    protected appDeploymentOptionsService: AppDeploymentOptionsService,
    public activeModal: NgbActiveModal,
    protected eventManager: JhiEventManager
  ) {}

  cancel(): void {
    this.activeModal.dismiss();
  }

  confirmDelete(id: number): void {
    this.appDeploymentOptionsService.delete(id).subscribe(() => {
      this.eventManager.broadcast('appDeploymentOptionsListModification');
      this.activeModal.close();
    });
  }
}
