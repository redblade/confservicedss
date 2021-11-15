import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';

import { ICatalogApp } from 'app/shared/model/catalog-app.model';

@Component({
  selector: 'jhi-catalog-app-detail',
  templateUrl: './catalog-app-detail.component.html',
})
export class CatalogAppDetailComponent implements OnInit {
  catalogApp: ICatalogApp | null = null;

  constructor(protected activatedRoute: ActivatedRoute) {}

  ngOnInit(): void {
    this.activatedRoute.data.subscribe(({ catalogApp }) => (this.catalogApp = catalogApp));
  }

  previousState(): void {
    window.history.back();
  }
}
