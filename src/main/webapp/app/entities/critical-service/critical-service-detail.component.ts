import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';

import { ICriticalService } from 'app/shared/model/critical-service.model';

@Component({
  selector: 'jhi-critical-service-detail',
  templateUrl: './critical-service-detail.component.html',
})
export class CriticalServiceDetailComponent implements OnInit {
  criticalService: ICriticalService | null = null;

  constructor(protected activatedRoute: ActivatedRoute) {}

  ngOnInit(): void {
    this.activatedRoute.data.subscribe(({ criticalService }) => (this.criticalService = criticalService));
  }

  previousState(): void {
    window.history.back();
  }
}
