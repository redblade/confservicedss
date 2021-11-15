import { NgModule } from '@angular/core';
import { RouterModule } from '@angular/router';

import { ConfserviceSharedModule } from 'app/shared/shared.module';
import { InfrastructureReportComponent } from './infrastructure-report.component';
import { InfrastructureReportDetailComponent } from './infrastructure-report-detail.component';
import { InfrastructureReportUpdateComponent } from './infrastructure-report-update.component';
import { InfrastructureReportDeleteDialogComponent } from './infrastructure-report-delete-dialog.component';
import { infrastructureReportRoute } from './infrastructure-report.route';

@NgModule({
  imports: [ConfserviceSharedModule, RouterModule.forChild(infrastructureReportRoute)],
  declarations: [
    InfrastructureReportComponent,
    InfrastructureReportDetailComponent,
    InfrastructureReportUpdateComponent,
    InfrastructureReportDeleteDialogComponent,
  ],
  entryComponents: [InfrastructureReportDeleteDialogComponent],
})
export class ConfserviceInfrastructureReportModule {}
