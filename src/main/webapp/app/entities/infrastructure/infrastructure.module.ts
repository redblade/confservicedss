import { NgModule } from '@angular/core';
import { RouterModule } from '@angular/router';

import { ConfserviceSharedModule } from 'app/shared/shared.module';
import { InfrastructureComponent } from './infrastructure.component';
import { InfrastructureDetailComponent } from './infrastructure-detail.component';
import { InfrastructureUpdateComponent } from './infrastructure-update.component';
import { InfrastructureDeleteDialogComponent } from './infrastructure-delete-dialog.component';
import { infrastructureRoute } from './infrastructure.route';

@NgModule({
  imports: [ConfserviceSharedModule, RouterModule.forChild(infrastructureRoute)],
  declarations: [
    InfrastructureComponent,
    InfrastructureDetailComponent,
    InfrastructureUpdateComponent,
    InfrastructureDeleteDialogComponent,
  ],
  entryComponents: [InfrastructureDeleteDialogComponent],
})
export class ConfserviceInfrastructureModule {}
