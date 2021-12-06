import { NgModule } from '@angular/core';
import { RouterModule } from '@angular/router';

import { ConfserviceSharedModule } from 'app/shared/shared.module';
import { BenchmarkSummaryComponent } from './benchmark-summary.component';
import { BenchmarkSummaryDetailComponent } from './benchmark-summary-detail.component';
import { benchmarkSummaryRoute } from './benchmark-summary.route';

@NgModule({
  imports: [ConfserviceSharedModule, RouterModule.forChild(benchmarkSummaryRoute)],
  declarations: [BenchmarkSummaryComponent, BenchmarkSummaryDetailComponent],
})
export class ConfserviceBenchmarkSummaryModule {}
