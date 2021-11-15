import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';

import { ISlaViolation } from 'app/shared/model/sla-violation.model';

@Component({
  selector: 'jhi-sla-violation-detail',
  templateUrl: './sla-violation-detail.component.html',
})
export class SlaViolationDetailComponent implements OnInit {
  slaViolation: ISlaViolation | null = null;

  constructor(protected activatedRoute: ActivatedRoute) {}

  ngOnInit(): void {
    this.activatedRoute.data.subscribe(({ slaViolation }) => (this.slaViolation = slaViolation));
  }

  previousState(): void {
    window.history.back();
  }
}
