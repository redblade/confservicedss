import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';

import { ISla } from 'app/shared/model/sla.model';

@Component({
  selector: 'jhi-sla-detail',
  templateUrl: './sla-detail.component.html',
})
export class SlaDetailComponent implements OnInit {
  sla: ISla | null = null;

  constructor(protected activatedRoute: ActivatedRoute) {}

  ngOnInit(): void {
    this.activatedRoute.data.subscribe(({ sla }) => (this.sla = sla));
  }

  previousState(): void {
    window.history.back();
  }
}
