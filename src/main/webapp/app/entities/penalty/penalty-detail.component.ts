import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';

import { IPenalty } from 'app/shared/model/penalty.model';

@Component({
  selector: 'jhi-penalty-detail',
  templateUrl: './penalty-detail.component.html',
})
export class PenaltyDetailComponent implements OnInit {
  penalty: IPenalty | null = null;

  constructor(protected activatedRoute: ActivatedRoute) {}

  ngOnInit(): void {
    this.activatedRoute.data.subscribe(({ penalty }) => (this.penalty = penalty));
  }

  previousState(): void {
    window.history.back();
  }
}
