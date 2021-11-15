import { NgModule } from '@angular/core';
import { RouterModule } from '@angular/router';

import { ConfserviceSharedModule } from 'app/shared/shared.module';
import { NodeComponent } from './node.component';
import { NodeDetailComponent } from './node-detail.component';
import { NodeUpdateComponent } from './node-update.component';
import { NodeDeleteDialogComponent } from './node-delete-dialog.component';
import { nodeRoute } from './node.route';

@NgModule({
  imports: [ConfserviceSharedModule, RouterModule.forChild(nodeRoute)],
  declarations: [NodeComponent, NodeDetailComponent, NodeUpdateComponent, NodeDeleteDialogComponent],
  entryComponents: [NodeDeleteDialogComponent],
})
export class ConfserviceNodeModule {}
