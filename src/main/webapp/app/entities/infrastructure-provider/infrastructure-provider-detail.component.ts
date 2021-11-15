import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';

import { IInfrastructureProvider } from 'app/shared/model/infrastructure-provider.model';

@Component({
  selector: 'jhi-infrastructure-provider-detail',
  templateUrl: './infrastructure-provider-detail.component.html',
})
export class InfrastructureProviderDetailComponent implements OnInit {
  infrastructureProvider: IInfrastructureProvider | null = null;

  constructor(protected activatedRoute: ActivatedRoute) {}

  ngOnInit(): void {
    this.activatedRoute.data.subscribe(({ infrastructureProvider }) => (this.infrastructureProvider = infrastructureProvider));
  }

  previousState(): void {
    window.history.back();
  }
}
