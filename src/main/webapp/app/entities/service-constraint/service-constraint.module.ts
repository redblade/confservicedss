import { NgModule } from '@angular/core';
import { RouterModule } from '@angular/router';

import { ConfserviceSharedModule } from 'app/shared/shared.module';
import { ServiceConstraintComponent } from './service-constraint.component';
import { ServiceConstraintDetailComponent } from './service-constraint-detail.component';
import { ServiceConstraintUpdateComponent } from './service-constraint-update.component';
import { ServiceConstraintDeleteDialogComponent } from './service-constraint-delete-dialog.component';
import { serviceConstraintRoute } from './service-constraint.route';

@NgModule({
  imports: [ConfserviceSharedModule, RouterModule.forChild(serviceConstraintRoute)],
  declarations: [
    ServiceConstraintComponent,
    ServiceConstraintDetailComponent,
    ServiceConstraintUpdateComponent,
    ServiceConstraintDeleteDialogComponent,
  ],
  entryComponents: [ServiceConstraintDeleteDialogComponent],
})
export class ConfserviceServiceConstraintModule {}
