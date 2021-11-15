import { NgModule } from '@angular/core';
import { RouterModule } from '@angular/router';

import { ConfserviceSharedModule } from 'app/shared/shared.module';
import { AppDeploymentOptionsComponent } from './app-deployment-options.component';
import { AppDeploymentOptionsDetailComponent } from './app-deployment-options-detail.component';
import { AppDeploymentOptionsUpdateComponent } from './app-deployment-options-update.component';
import { AppDeploymentOptionsDeleteDialogComponent } from './app-deployment-options-delete-dialog.component';
import { appDeploymentOptionsRoute } from './app-deployment-options.route';

@NgModule({
  imports: [ConfserviceSharedModule, RouterModule.forChild(appDeploymentOptionsRoute)],
  declarations: [
    AppDeploymentOptionsComponent,
    AppDeploymentOptionsDetailComponent,
    AppDeploymentOptionsUpdateComponent,
    AppDeploymentOptionsDeleteDialogComponent,
  ],
  entryComponents: [AppDeploymentOptionsDeleteDialogComponent],
})
export class ConfserviceAppDeploymentOptionsModule {}
