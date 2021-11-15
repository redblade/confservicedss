import { NgModule } from '@angular/core';
import { RouterModule } from '@angular/router';

import { ConfserviceSharedModule } from 'app/shared/shared.module';
import { BenchmarkReportComponent } from './benchmark-report.component';
import { BenchmarkReportDetailComponent } from './benchmark-report-detail.component';
import { BenchmarkReportUpdateComponent } from './benchmark-report-update.component';
import { BenchmarkReportDeleteDialogComponent } from './benchmark-report-delete-dialog.component';
import { benchmarkReportRoute } from './benchmark-report.route';

@NgModule({
  imports: [ConfserviceSharedModule, RouterModule.forChild(benchmarkReportRoute)],
  declarations: [
    BenchmarkReportComponent,
    BenchmarkReportDetailComponent,
    BenchmarkReportUpdateComponent,
    BenchmarkReportDeleteDialogComponent,
  ],
  entryComponents: [BenchmarkReportDeleteDialogComponent],
})
export class ConfserviceBenchmarkReportModule {}
