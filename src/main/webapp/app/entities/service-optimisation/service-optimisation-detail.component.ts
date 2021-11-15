import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';

import { IServiceOptimisation } from 'app/shared/model/service-optimisation.model';

@Component({
  selector: 'jhi-service-optimisation-detail',
  templateUrl: './service-optimisation-detail.component.html',
})
export class ServiceOptimisationDetailComponent implements OnInit {
  serviceOptimisation: IServiceOptimisation | null = null;

  constructor(protected activatedRoute: ActivatedRoute) {}

  ngOnInit(): void {
    this.activatedRoute.data.subscribe(({ serviceOptimisation }) => (this.serviceOptimisation = serviceOptimisation));
  }

  previousState(): void {
    window.history.back();
  }
}
