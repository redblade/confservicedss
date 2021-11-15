import { Component, OnInit } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import { FormBuilder, Validators } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { Observable } from 'rxjs';

import { IServiceProvider, ServiceProvider } from 'app/shared/model/service-provider.model';
import { ServiceProviderService } from './service-provider.service';

@Component({
  selector: 'jhi-service-provider-update',
  templateUrl: './service-provider-update.component.html',
})
export class ServiceProviderUpdateComponent implements OnInit {
  isSaving = false;

  editForm = this.fb.group({
    id: [],
    name: [null, []],
    organisation: [],
    preferences: [null, [Validators.maxLength(20000)]],
  });

  constructor(
    protected serviceProviderService: ServiceProviderService,
    protected activatedRoute: ActivatedRoute,
    private fb: FormBuilder
  ) {}

  ngOnInit(): void {
    this.activatedRoute.data.subscribe(({ serviceProvider }) => {
      this.updateForm(serviceProvider);
    });
  }

  updateForm(serviceProvider: IServiceProvider): void {
    this.editForm.patchValue({
      id: serviceProvider.id,
      name: serviceProvider.name,
      organisation: serviceProvider.organisation,
      preferences: serviceProvider.preferences,
    });
  }

  previousState(): void {
    window.history.back();
  }

  save(): void {
    this.isSaving = true;
    const serviceProvider = this.createFromForm();
    if (serviceProvider.id !== undefined) {
      this.subscribeToSaveResponse(this.serviceProviderService.update(serviceProvider));
    } else {
      this.subscribeToSaveResponse(this.serviceProviderService.create(serviceProvider));
    }
  }

  private createFromForm(): IServiceProvider {
    return {
      ...new ServiceProvider(),
      id: this.editForm.get(['id'])!.value,
      name: this.editForm.get(['name'])!.value,
      organisation: this.editForm.get(['organisation'])!.value,
      preferences: this.editForm.get(['preferences'])!.value,
    };
  }

  protected subscribeToSaveResponse(result: Observable<HttpResponse<IServiceProvider>>): void {
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
}
