import { NgModule } from '@angular/core';
import { RouterModule } from '@angular/router';

@NgModule({
  imports: [
    RouterModule.forChild([
      {
        path: 'service-provider',
        loadChildren: () => import('./service-provider/service-provider.module').then(m => m.ConfserviceServiceProviderModule),
      },
      {
        path: 'infrastructure-provider',
        loadChildren: () =>
          import('./infrastructure-provider/infrastructure-provider.module').then(m => m.ConfserviceInfrastructureProviderModule),
      },
      {
        path: 'infrastructure',
        loadChildren: () => import('./infrastructure/infrastructure.module').then(m => m.ConfserviceInfrastructureModule),
      },
      {
        path: 'infrastructure-report',
        loadChildren: () =>
          import('./infrastructure-report/infrastructure-report.module').then(m => m.ConfserviceInfrastructureReportModule),
      },
      {
        path: 'node',
        loadChildren: () => import('./node/node.module').then(m => m.ConfserviceNodeModule),
      },
      {
        path: 'node-report',
        loadChildren: () => import('./node-report/node-report.module').then(m => m.ConfserviceNodeReportModule),
      },
      {
        path: 'benchmark',
        loadChildren: () => import('./benchmark/benchmark.module').then(m => m.ConfserviceBenchmarkModule),
      },
      {
        path: 'benchmark-report',
        loadChildren: () => import('./benchmark-report/benchmark-report.module').then(m => m.ConfserviceBenchmarkReportModule),
      },
      {
        path: 'project',
        loadChildren: () => import('./project/project.module').then(m => m.ConfserviceProjectModule),
      },
      {
        path: 'catalog-app',
        loadChildren: () => import('./catalog-app/catalog-app.module').then(m => m.ConfserviceCatalogAppModule),
      },
      {
        path: 'app',
        loadChildren: () => import('./app/app.module').then(m => m.ConfserviceAppModule),
      },
      {
        path: 'sla',
        loadChildren: () => import('./sla/sla.module').then(m => m.ConfserviceSlaModule),
      },
      {
        path: 'guarantee',
        loadChildren: () => import('./guarantee/guarantee.module').then(m => m.ConfserviceGuaranteeModule),
      },
      {
        path: 'sla-violation',
        loadChildren: () => import('./sla-violation/sla-violation.module').then(m => m.ConfserviceSlaViolationModule),
      },
      {
        path: 'app-constraint',
        loadChildren: () => import('./app-constraint/app-constraint.module').then(m => m.ConfserviceAppConstraintModule),
      },
      {
        path: 'service',
        loadChildren: () => import('./service/service.module').then(m => m.ConfserviceServiceModule),
      },
      {
        path: 'service-constraint',
        loadChildren: () => import('./service-constraint/service-constraint.module').then(m => m.ConfserviceServiceConstraintModule),
      },
      {
        path: 'service-report',
        loadChildren: () => import('./service-report/service-report.module').then(m => m.ConfserviceServiceReportModule),
      },
      {
        path: 'app-deployment-options',
        loadChildren: () =>
          import('./app-deployment-options/app-deployment-options.module').then(m => m.ConfserviceAppDeploymentOptionsModule),
      },
      {
        path: 'critical-service',
        loadChildren: () => import('./critical-service/critical-service.module').then(m => m.ConfserviceCriticalServiceModule),
      },
      {
        path: 'steady-service',
        loadChildren: () => import('./steady-service/steady-service.module').then(m => m.ConfserviceSteadyServiceModule),
      },
      {
        path: 'event',
        loadChildren: () => import('./event/event.module').then(m => m.ConfserviceEventModule),
      },
      {
        path: 'service-optimisation',
        loadChildren: () => import('./service-optimisation/service-optimisation.module').then(m => m.ConfserviceServiceOptimisationModule),
      },
      {
        path: 'benchmark-summary',
        loadChildren: () => import('./benchmark-summary/benchmark-summary.module').then(m => m.ConfserviceBenchmarkSummaryModule),
      },
      /* jhipster-needle-add-entity-route - JHipster will add entity modules routes here */
    ]),
  ],
})
export class ConfserviceEntityModule {}
