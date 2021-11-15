import { NgModule } from '@angular/core';
import { RouterModule } from '@angular/router';

import { ConfserviceSharedModule } from 'app/shared/shared.module';
import { CriticalServiceComponent } from './critical-service.component';
import { CriticalServiceDetailComponent } from './critical-service-detail.component';
import { CriticalServiceUpdateComponent } from './critical-service-update.component';
import { CriticalServiceDeleteDialogComponent } from './critical-service-delete-dialog.component';
import { criticalServiceRoute } from './critical-service.route';

@NgModule({
  imports: [ConfserviceSharedModule, RouterModule.forChild(criticalServiceRoute)],
  declarations: [
    CriticalServiceComponent,
    CriticalServiceDetailComponent,
    CriticalServiceUpdateComponent,
    CriticalServiceDeleteDialogComponent,
  ],
  entryComponents: [CriticalServiceDeleteDialogComponent],
})
export class ConfserviceCriticalServiceModule {}
