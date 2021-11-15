import { NgModule } from '@angular/core';
import { RouterModule } from '@angular/router';

import { ConfserviceSharedModule } from 'app/shared/shared.module';
import { ServiceProviderComponent } from './service-provider.component';
import { ServiceProviderDetailComponent } from './service-provider-detail.component';
import { ServiceProviderUpdateComponent } from './service-provider-update.component';
import { ServiceProviderDeleteDialogComponent } from './service-provider-delete-dialog.component';
import { serviceProviderRoute } from './service-provider.route';

@NgModule({
  imports: [ConfserviceSharedModule, RouterModule.forChild(serviceProviderRoute)],
  declarations: [
    ServiceProviderComponent,
    ServiceProviderDetailComponent,
    ServiceProviderUpdateComponent,
    ServiceProviderDeleteDialogComponent,
  ],
  entryComponents: [ServiceProviderDeleteDialogComponent],
})
export class ConfserviceServiceProviderModule {}
