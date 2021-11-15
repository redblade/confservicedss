import { NgModule } from '@angular/core';
import { RouterModule } from '@angular/router';

import { ConfserviceSharedModule } from 'app/shared/shared.module';
import { AppComponent } from './app.component';
import { AppDetailComponent } from './app-detail.component';
import { AppUpdateComponent } from './app-update.component';
import { AppManageComponent } from './app-manage.component';
import { AppDeleteDialogComponent } from './app-delete-dialog.component';
import { appRoute } from './app.route';

@NgModule({
  imports: [ConfserviceSharedModule, RouterModule.forChild(appRoute)],
  declarations: [AppComponent, AppDetailComponent, AppUpdateComponent, AppManageComponent, AppDeleteDialogComponent],
  entryComponents: [AppDeleteDialogComponent],
})
export class ConfserviceAppModule {}
