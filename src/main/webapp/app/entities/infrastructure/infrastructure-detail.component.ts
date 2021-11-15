import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';

import { IInfrastructure } from 'app/shared/model/infrastructure.model';

@Component({
  selector: 'jhi-infrastructure-detail',
  templateUrl: './infrastructure-detail.component.html',
})
export class InfrastructureDetailComponent implements OnInit {
  infrastructure: IInfrastructure | null = null;

  constructor(protected activatedRoute: ActivatedRoute) {}

  ngOnInit(): void {
    this.activatedRoute.data.subscribe(({ infrastructure }) => (this.infrastructure = infrastructure));
  }

  previousState(): void {
    window.history.back();
  }
}
