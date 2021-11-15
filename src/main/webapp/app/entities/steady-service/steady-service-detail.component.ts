import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';

import { ISteadyService } from 'app/shared/model/steady-service.model';

@Component({
  selector: 'jhi-steady-service-detail',
  templateUrl: './steady-service-detail.component.html',
})
export class SteadyServiceDetailComponent implements OnInit {
  steadyService: ISteadyService | null = null;

  constructor(protected activatedRoute: ActivatedRoute) {}

  ngOnInit(): void {
    this.activatedRoute.data.subscribe(({ steadyService }) => (this.steadyService = steadyService));
  }

  previousState(): void {
    window.history.back();
  }
}
