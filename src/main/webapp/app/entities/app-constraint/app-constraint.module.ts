import { NgModule } from '@angular/core';
import { RouterModule } from '@angular/router';

import { ConfserviceSharedModule } from 'app/shared/shared.module';
import { AppConstraintComponent } from './app-constraint.component';
import { AppConstraintDetailComponent } from './app-constraint-detail.component';
import { AppConstraintUpdateComponent } from './app-constraint-update.component';
import { AppConstraintDeleteDialogComponent } from './app-constraint-delete-dialog.component';
import { appConstraintRoute } from './app-constraint.route';

@NgModule({
  imports: [ConfserviceSharedModule, RouterModule.forChild(appConstraintRoute)],
  declarations: [AppConstraintComponent, AppConstraintDetailComponent, AppConstraintUpdateComponent, AppConstraintDeleteDialogComponent],
  entryComponents: [AppConstraintDeleteDialogComponent],
})
export class ConfserviceAppConstraintModule {}
