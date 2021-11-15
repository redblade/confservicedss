import { NgModule } from '@angular/core';
import { RouterModule } from '@angular/router';

import { ConfserviceSharedModule } from 'app/shared/shared.module';
import { BenchmarkComponent } from './benchmark.component';
import { BenchmarkDetailComponent } from './benchmark-detail.component';
import { BenchmarkUpdateComponent } from './benchmark-update.component';
import { BenchmarkDeleteDialogComponent } from './benchmark-delete-dialog.component';
import { benchmarkRoute } from './benchmark.route';

@NgModule({
  imports: [ConfserviceSharedModule, RouterModule.forChild(benchmarkRoute)],
  declarations: [BenchmarkComponent, BenchmarkDetailComponent, BenchmarkUpdateComponent, BenchmarkDeleteDialogComponent],
  entryComponents: [BenchmarkDeleteDialogComponent],
})
export class ConfserviceBenchmarkModule {}
