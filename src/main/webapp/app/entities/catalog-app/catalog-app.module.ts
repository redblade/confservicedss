import { NgModule } from '@angular/core';
import { RouterModule } from '@angular/router';

import { ConfserviceSharedModule } from 'app/shared/shared.module';
import { CatalogAppComponent } from './catalog-app.component';
import { CatalogAppDetailComponent } from './catalog-app-detail.component';
import { CatalogAppUpdateComponent } from './catalog-app-update.component';
import { CatalogAppDeleteDialogComponent } from './catalog-app-delete-dialog.component';
import { catalogAppRoute } from './catalog-app.route';

@NgModule({
  imports: [ConfserviceSharedModule, RouterModule.forChild(catalogAppRoute)],
  declarations: [CatalogAppComponent, CatalogAppDetailComponent, CatalogAppUpdateComponent, CatalogAppDeleteDialogComponent],
  entryComponents: [CatalogAppDeleteDialogComponent],
})
export class ConfserviceCatalogAppModule {}
