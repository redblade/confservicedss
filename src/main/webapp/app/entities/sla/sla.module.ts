import { NgModule } from '@angular/core';
import { RouterModule } from '@angular/router';

import { ConfserviceSharedModule } from 'app/shared/shared.module';
import { SlaComponent } from './sla.component';
import { SlaDetailComponent } from './sla-detail.component';
import { SlaUpdateComponent } from './sla-update.component';
import { SlaDeleteDialogComponent } from './sla-delete-dialog.component';
import { slaRoute } from './sla.route';

@NgModule({
  imports: [ConfserviceSharedModule, RouterModule.forChild(slaRoute)],
  declarations: [SlaComponent, SlaDetailComponent, SlaUpdateComponent, SlaDeleteDialogComponent],
  entryComponents: [SlaDeleteDialogComponent],
})
export class ConfserviceSlaModule {}
