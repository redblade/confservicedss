import { Component, OnInit } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import { FormBuilder, Validators } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { Observable } from 'rxjs';

import { IApp, App } from 'app/shared/model/app.model';
import { AppService } from './app.service';
import { IServiceProvider } from 'app/shared/model/service-provider.model';
import { ServiceProviderService } from 'app/entities/service-provider/service-provider.service';
import { ICatalogApp } from 'app/shared/model/catalog-app.model';
import { CatalogAppService } from 'app/entities/catalog-app/catalog-app.service';

type SelectableEntity = IServiceProvider | ICatalogApp;

@Component({
  selector: 'jhi-app-update',
  templateUrl: './app-update.component.html',
})
export class AppUpdateComponent implements OnInit {
  isSaving = false;
  serviceproviders: IServiceProvider[] = [];
  catalogapps: ICatalogApp[] = [];

  editForm = this.fb.group({
    id: [],
    name: [],
    managementType: [],
    status: [],
    appDescriptor: [null, [Validators.maxLength(40000)]],
    serviceProvider: [],
    catalogApp: [],
  });

  constructor(
    protected appService: AppService,
    protected serviceProviderService: ServiceProviderService,
    protected catalogAppService: CatalogAppService,
    protected activatedRoute: ActivatedRoute,
    private fb: FormBuilder
  ) {}

  ngOnInit(): void {
    this.activatedRoute.data.subscribe(({ app }) => {
      this.updateForm(app);

      this.serviceProviderService.query().subscribe((res: HttpResponse<IServiceProvider[]>) => (this.serviceproviders = res.body || []));

      this.catalogAppService.query().subscribe((res: HttpResponse<ICatalogApp[]>) => (this.catalogapps = res.body || []));
    });
  }

  updateForm(app: IApp): void {
    this.editForm.patchValue({
      id: app.id,
      name: app.name,
      managementType: app.managementType,
      status: app.status,
      appDescriptor: app.appDescriptor,
      serviceProvider: app.serviceProvider,
      catalogApp: app.catalogApp,
    });
  }

  previousState(): void {
    window.history.back();
  }

  save(): void {
    this.isSaving = true;
    const app = this.createFromForm();
    if (app.id !== undefined) {
      this.subscribeToSaveResponse(this.appService.update(app));
    } else {
      this.subscribeToSaveResponse(this.appService.create(app));
    }
  }

  private createFromForm(): IApp {
    return {
      ...new App(),
      id: this.editForm.get(['id'])!.value,
      name: this.editForm.get(['name'])!.value,
      managementType: this.editForm.get(['managementType'])!.value,
      status: this.editForm.get(['status'])!.value,
      appDescriptor: this.editForm.get(['appDescriptor'])!.value,
      serviceProvider: this.editForm.get(['serviceProvider'])!.value,
      catalogApp: this.editForm.get(['catalogApp'])!.value,
    };
  }

  protected subscribeToSaveResponse(result: Observable<HttpResponse<IApp>>): void {
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

  trackById(index: number, item: SelectableEntity): any {
    return item.id;
  }
}
