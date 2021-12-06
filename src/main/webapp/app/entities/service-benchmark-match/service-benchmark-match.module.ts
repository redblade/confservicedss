import { NgModule } from '@angular/core';
import { RouterModule } from '@angular/router';

import { ConfserviceSharedModule } from 'app/shared/shared.module';
import { ServiceBenchmarkMatchComponent } from './service-benchmark-match.component';
import { ServiceBenchmarkMatchDetailComponent } from './service-benchmark-match-detail.component';
import { serviceBenchmarkMatchRoute } from './service-benchmark-match.route';

@NgModule({
  imports: [ConfserviceSharedModule, RouterModule.forChild(serviceBenchmarkMatchRoute)],
  declarations: [ServiceBenchmarkMatchComponent, ServiceBenchmarkMatchDetailComponent],
})
export class ConfserviceServiceBenchmarkMatchModule {}
