import { NgModule } from '@angular/core';
import { RouterModule } from '@angular/router';

import { ConfserviceSharedModule } from 'app/shared/shared.module';
import { SteadyServiceComponent } from './steady-service.component';
import { SteadyServiceDetailComponent } from './steady-service-detail.component';
import { SteadyServiceUpdateComponent } from './steady-service-update.component';
import { SteadyServiceDeleteDialogComponent } from './steady-service-delete-dialog.component';
import { steadyServiceRoute } from './steady-service.route';

@NgModule({
  imports: [ConfserviceSharedModule, RouterModule.forChild(steadyServiceRoute)],
  declarations: [SteadyServiceComponent, SteadyServiceDetailComponent, SteadyServiceUpdateComponent, SteadyServiceDeleteDialogComponent],
  entryComponents: [SteadyServiceDeleteDialogComponent],
})
export class ConfserviceSteadyServiceModule {}
