import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';

import { IServiceConstraint } from 'app/shared/model/service-constraint.model';

@Component({
  selector: 'jhi-service-constraint-detail',
  templateUrl: './service-constraint-detail.component.html',
})
export class ServiceConstraintDetailComponent implements OnInit {
  serviceConstraint: IServiceConstraint | null = null;

  constructor(protected activatedRoute: ActivatedRoute) {}

  ngOnInit(): void {
    this.activatedRoute.data.subscribe(({ serviceConstraint }) => (this.serviceConstraint = serviceConstraint));
  }

  previousState(): void {
    window.history.back();
  }
}
