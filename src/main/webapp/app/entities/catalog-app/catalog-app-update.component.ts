import { Component, OnInit } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import { FormBuilder, Validators } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { Observable } from 'rxjs';

import { ICatalogApp, CatalogApp } from 'app/shared/model/catalog-app.model';
import { CatalogAppService } from './catalog-app.service';
import { IServiceProvider } from 'app/shared/model/service-provider.model';
import { ServiceProviderService } from 'app/entities/service-provider/service-provider.service';

@Component({
  selector: 'jhi-catalog-app-update',
  templateUrl: './catalog-app-update.component.html',
})
export class CatalogAppUpdateComponent implements OnInit {
  isSaving = false;
  serviceproviders: IServiceProvider[] = [];

  editForm = this.fb.group({
    id: [],
    name: [null, []],
    appDescriptor: [null, [Validators.maxLength(40000)]],
    serviceProvider: [],
  });

  constructor(
    protected catalogAppService: CatalogAppService,
    protected serviceProviderService: ServiceProviderService,
    protected activatedRoute: ActivatedRoute,
    private fb: FormBuilder
  ) {}

  ngOnInit(): void {
    this.activatedRoute.data.subscribe(({ catalogApp }) => {
      this.updateForm(catalogApp);

      this.serviceProviderService.query().subscribe((res: HttpResponse<IServiceProvider[]>) => (this.serviceproviders = res.body || []));
    });
  }

  updateForm(catalogApp: ICatalogApp): void {
    this.editForm.patchValue({
      id: catalogApp.id,
      name: catalogApp.name,
      appDescriptor: catalogApp.appDescriptor,
      serviceProvider: catalogApp.serviceProvider,
    });
  }

  previousState(): void {
    window.history.back();
  }

  save(): void {
    this.isSaving = true;
    const catalogApp = this.createFromForm();
    if (catalogApp.id !== undefined) {
      this.subscribeToSaveResponse(this.catalogAppService.update(catalogApp));
    } else {
      this.subscribeToSaveResponse(this.catalogAppService.create(catalogApp));
    }
  }

  private createFromForm(): ICatalogApp {
    return {
      ...new CatalogApp(),
      id: this.editForm.get(['id'])!.value,
      name: this.editForm.get(['name'])!.value,
      appDescriptor: this.editForm.get(['appDescriptor'])!.value,
      serviceProvider: this.editForm.get(['serviceProvider'])!.value,
    };
  }

  protected subscribeToSaveResponse(result: Observable<HttpResponse<ICatalogApp>>): void {
    result.subscribe(
      () => this.onSaveSuccess(),
      () => this.onSaveError()
    );
  }

  protected onSaveSuccess(): void {
    this.isSaving = false;
    this.previousState();
  }

  protected onSaveError(): void {
    this.isSaving = false;
  }

  trackById(index: number, item: IServiceProvider): any {
    return item.id;
  }
}
