import { NgModule } from '@angular/core';
import { RouterModule } from '@angular/router';

import { ConfserviceSharedModule } from 'app/shared/shared.module';
import { OptimisationReportComponent } from './optimisation-report.component';
import { OptimisationReportDetailComponent } from './optimisation-report-detail.component';
import { optimisationReportRoute } from './optimisation-report.route';

@NgModule({
  imports: [ConfserviceSharedModule, RouterModule.forChild(optimisationReportRoute)],
  declarations: [OptimisationReportComponent, OptimisationReportDetailComponent],
})
export class ConfserviceOptimisationReportModule {}
