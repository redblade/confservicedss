import { NgModule } from '@angular/core';
import { RouterModule } from '@angular/router';

import { ConfserviceSharedModule } from 'app/shared/shared.module';
import { AppReportComponent } from './app-report.component';
import { AppReportDetailComponent } from './app-report-detail.component';
import { AppReportUpdateComponent } from './app-report-update.component';
import { AppReportDeleteDialogComponent } from './app-report-delete-dialog.component';
import { appReportRoute } from './app-report.route';

@NgModule({
  imports: [ConfserviceSharedModule, RouterModule.forChild(appReportRoute)],
  declarations: [AppReportComponent, AppReportDetailComponent, AppReportUpdateComponent, AppReportDeleteDialogComponent],
  entryComponents: [AppReportDeleteDialogComponent],
})
export class ConfserviceAppReportModule {}
