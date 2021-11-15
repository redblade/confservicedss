import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';

import { IAppConstraint } from 'app/shared/model/app-constraint.model';

@Component({
  selector: 'jhi-app-constraint-detail',
  templateUrl: './app-constraint-detail.component.html',
})
export class AppConstraintDetailComponent implements OnInit {
  appConstraint: IAppConstraint | null = null;

  constructor(protected activatedRoute: ActivatedRoute) {}

  ngOnInit(): void {
    this.activatedRoute.data.subscribe(({ appConstraint }) => (this.appConstraint = appConstraint));
  }

  previousState(): void {
    window.history.back();
  }
}
