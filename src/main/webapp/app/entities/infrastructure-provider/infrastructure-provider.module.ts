import { NgModule } from '@angular/core';
import { RouterModule } from '@angular/router';

import { ConfserviceSharedModule } from 'app/shared/shared.module';
import { InfrastructureProviderComponent } from './infrastructure-provider.component';
import { InfrastructureProviderDetailComponent } from './infrastructure-provider-detail.component';
import { InfrastructureProviderUpdateComponent } from './infrastructure-provider-update.component';
import { InfrastructureProviderDeleteDialogComponent } from './infrastructure-provider-delete-dialog.component';
import { infrastructureProviderRoute } from './infrastructure-provider.route';

@NgModule({
  imports: [ConfserviceSharedModule, RouterModule.forChild(infrastructureProviderRoute)],
  declarations: [
    InfrastructureProviderComponent,
    InfrastructureProviderDetailComponent,
    InfrastructureProviderUpdateComponent,
    InfrastructureProviderDeleteDialogComponent,
  ],
  entryComponents: [InfrastructureProviderDeleteDialogComponent],
})
export class ConfserviceInfrastructureProviderModule {}
