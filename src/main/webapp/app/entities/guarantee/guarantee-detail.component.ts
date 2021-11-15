import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';

import { IGuarantee } from 'app/shared/model/guarantee.model';

@Component({
  selector: 'jhi-guarantee-detail',
  templateUrl: './guarantee-detail.component.html',
})
export class GuaranteeDetailComponent implements OnInit {
  guarantee: IGuarantee | null = null;

  constructor(protected activatedRoute: ActivatedRoute) {}

  ngOnInit(): void {
    this.activatedRoute.data.subscribe(({ guarantee }) => (this.guarantee = guarantee));
  }

  previousState(): void {
    window.history.back();
  }
}
