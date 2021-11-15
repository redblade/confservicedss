import { NgModule } from '@angular/core';
import { RouterModule } from '@angular/router';

import { ConfserviceSharedModule } from 'app/shared/shared.module';
import { ServiceOptimisationComponent } from './service-optimisation.component';
import { ServiceOptimisationDetailComponent } from './service-optimisation-detail.component';
import { ServiceOptimisationUpdateComponent } from './service-optimisation-update.component';
import { ServiceOptimisationDeleteDialogComponent } from './service-optimisation-delete-dialog.component';
import { serviceOptimisationRoute } from './service-optimisation.route';

@NgModule({
  imports: [ConfserviceSharedModule, RouterModule.forChild(serviceOptimisationRoute)],
  declarations: [
    ServiceOptimisationComponent,
    ServiceOptimisationDetailComponent,
    ServiceOptimisationUpdateComponent,
    ServiceOptimisationDeleteDialogComponent,
  ],
  entryComponents: [ServiceOptimisationDeleteDialogComponent],
})
export class ConfserviceServiceOptimisationModule {}
