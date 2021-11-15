import { NgModule } from '@angular/core';
import { RouterModule } from '@angular/router';

import { ConfserviceSharedModule } from 'app/shared/shared.module';
import { NodeReportComponent } from './node-report.component';
import { NodeReportDetailComponent } from './node-report-detail.component';
import { NodeReportUpdateComponent } from './node-report-update.component';
import { NodeReportDeleteDialogComponent } from './node-report-delete-dialog.component';
import { nodeReportRoute } from './node-report.route';

@NgModule({
  imports: [ConfserviceSharedModule, RouterModule.forChild(nodeReportRoute)],
  declarations: [NodeReportComponent, NodeReportDetailComponent, NodeReportUpdateComponent, NodeReportDeleteDialogComponent],
  entryComponents: [NodeReportDeleteDialogComponent],
})
export class ConfserviceNodeReportModule {}
