import { Component } from '@angular/core';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { JhiEventManager } from 'ng-jhipster';

import { IInfrastructureProvider } from 'app/shared/model/infrastructure-provider.model';
import { InfrastructureProviderService } from './infrastructure-provider.service';

@Component({
  templateUrl: './infrastructure-provider-delete-dialog.component.html',
})
export class InfrastructureProviderDeleteDialogComponent {
  infrastructureProvider?: IInfrastructureProvider;

  constructor(
    protected infrastructureProviderService: InfrastructureProviderService,
    public activeModal: NgbActiveModal,
    protected eventManager: JhiEventManager
  ) {}

  cancel(): void {
    this.activeModal.dismiss();
  }

  confirmDelete(id: number): void {
    this.infrastructureProviderService.delete(id).subscribe(() => {
      this.eventManager.broadcast('infrastructureProviderListModification');
      this.activeModal.close();
    });
  }
}
