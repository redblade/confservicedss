import { NgModule } from '@angular/core';
import { RouterModule } from '@angular/router';

import { ConfserviceSharedModule } from 'app/shared/shared.module';
import { SlaViolationComponent } from './sla-violation.component';
import { SlaViolationDetailComponent } from './sla-violation-detail.component';
import { SlaViolationUpdateComponent } from './sla-violation-update.component';
import { SlaViolationDeleteDialogComponent } from './sla-violation-delete-dialog.component';
import { slaViolationRoute } from './sla-violation.route';

@NgModule({
  imports: [ConfserviceSharedModule, RouterModule.forChild(slaViolationRoute)],
  declarations: [SlaViolationComponent, SlaViolationDetailComponent, SlaViolationUpdateComponent, SlaViolationDeleteDialogComponent],
  entryComponents: [SlaViolationDeleteDialogComponent],
})
export class ConfserviceSlaViolationModule {}
