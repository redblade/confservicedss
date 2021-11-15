import { Component } from '@angular/core';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { JhiEventManager } from 'ng-jhipster';

import { IServiceProvider } from 'app/shared/model/service-provider.model';
import { ServiceProviderService } from './service-provider.service';

@Component({
  templateUrl: './service-provider-delete-dialog.component.html',
})
export class ServiceProviderDeleteDialogComponent {
  serviceProvider?: IServiceProvider;

  constructor(
    protected serviceProviderService: ServiceProviderService,
    public activeModal: NgbActiveModal,
    protected eventManager: JhiEventManager
  ) {}

  cancel(): void {
    this.activeModal.dismiss();
  }

  confirmDelete(id: number): void {
    this.serviceProviderService.delete(id).subscribe(() => {
      this.eventManager.broadcast('serviceProviderListModification');
      this.activeModal.close();
    });
  }
}
