import { Component, OnInit } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import { FormBuilder, Validators } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { Observable } from 'rxjs';

import { IInfrastructure, Infrastructure } from 'app/shared/model/infrastructure.model';
import { InfrastructureService } from './infrastructure.service';
import { IInfrastructureProvider } from 'app/shared/model/infrastructure-provider.model';
import { InfrastructureProviderService } from 'app/entities/infrastructure-provider/infrastructure-provider.service';

@Component({
  selector: 'jhi-infrastructure-update',
  templateUrl: './infrastructure-update.component.html',
})
export class InfrastructureUpdateComponent implements OnInit {
  isSaving = false;
  infrastructureproviders: IInfrastructureProvider[] = [];

  editForm = this.fb.group({
    id: [],
    name: [null, []],
    type: [],
    endpoint: [null, []],
    credentials: [null, [Validators.maxLength(2000)]],
    monitoringPlugin: [null, [Validators.maxLength(20000)]],
    properties: [null, [Validators.maxLength(2000)]],
    totalResources: [null, [Validators.maxLength(2000)]],
    infrastructureProvider: [],
  });

  constructor(
    protected infrastructureService: InfrastructureService,
    protected infrastructureProviderService: InfrastructureProviderService,
    protected activatedRoute: ActivatedRoute,
    private fb: FormBuilder
  ) {}

  ngOnInit(): void {
    this.activatedRoute.data.subscribe(({ infrastructure }) => {
      this.updateForm(infrastructure);

      this.infrastructureProviderService
        .query()
        .subscribe((res: HttpResponse<IInfrastructureProvider[]>) => (this.infrastructureproviders = res.body || []));
    });
  }

  updateForm(infrastructure: IInfrastructure): void {
    this.editForm.patchValue({
      id: infrastructure.id,
      name: infrastructure.name,
      type: infrastructure.type,
      endpoint: infrastructure.endpoint,
      credentials: infrastructure.credentials,
      monitoringPlugin: infrastructure.monitoringPlugin,
      properties: infrastructure.properties,
      totalResources: infrastructure.totalResources,
      infrastructureProvider: infrastructure.infrastructureProvider,
    });
  }

  previousState(): void {
    window.history.back();
  }

  save(): void {
    this.isSaving = true;
    const infrastructure = this.createFromForm();
    if (infrastructure.id !== undefined) {
      this.subscribeToSaveResponse(this.infrastructureService.update(infrastructure));
    } else {
      this.subscribeToSaveResponse(this.infrastructureService.create(infrastructure));
    }
  }

  private createFromForm(): IInfrastructure {
    return {
      ...new Infrastructure(),
      id: this.editForm.get(['id'])!.value,
      name: this.editForm.get(['name'])!.value,
      type: this.editForm.get(['type'])!.value,
      endpoint: this.editForm.get(['endpoint'])!.value,
      credentials: this.editForm.get(['credentials'])!.value,
      monitoringPlugin: this.editForm.get(['monitoringPlugin'])!.value,
      properties: this.editForm.get(['properties'])!.value,
      totalResources: this.editForm.get(['totalResources'])!.value,
      infrastructureProvider: this.editForm.get(['infrastructureProvider'])!.value,
    };
  }

  protected subscribeToSaveResponse(result: Observable<HttpResponse<IInfrastructure>>): void {
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

  trackById(index: number, item: IInfrastructureProvider): any {
    return item.id;
  }
}
