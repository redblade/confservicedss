import { NgModule } from '@angular/core';
import { RouterModule } from '@angular/router';

import { ConfserviceSharedModule } from 'app/shared/shared.module';
import { GuaranteeComponent } from './guarantee.component';
import { GuaranteeDetailComponent } from './guarantee-detail.component';
import { GuaranteeUpdateComponent } from './guarantee-update.component';
import { GuaranteeDeleteDialogComponent } from './guarantee-delete-dialog.component';
import { guaranteeRoute } from './guarantee.route';

@NgModule({
  imports: [ConfserviceSharedModule, RouterModule.forChild(guaranteeRoute)],
  declarations: [GuaranteeComponent, GuaranteeDetailComponent, GuaranteeUpdateComponent, GuaranteeDeleteDialogComponent],
  entryComponents: [GuaranteeDeleteDialogComponent],
})
export class ConfserviceGuaranteeModule {}
