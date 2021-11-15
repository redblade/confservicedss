import { NgModule } from '@angular/core';
import { RouterModule } from '@angular/router';

import { ConfserviceSharedModule } from 'app/shared/shared.module';
import { ServiceReportComponent } from './service-report.component';
import { ServiceReportDetailComponent } from './service-report-detail.component';
import { ServiceReportUpdateComponent } from './service-report-update.component';
import { ServiceReportDeleteDialogComponent } from './service-report-delete-dialog.component';
import { serviceReportRoute } from './service-report.route';

@NgModule({
  imports: [ConfserviceSharedModule, RouterModule.forChild(serviceReportRoute)],
  declarations: [ServiceReportComponent, ServiceReportDetailComponent, ServiceReportUpdateComponent, ServiceReportDeleteDialogComponent],
  entryComponents: [ServiceReportDeleteDialogComponent],
})
export class ConfserviceServiceReportModule {}
