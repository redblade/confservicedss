import { Component, OnInit } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import { FormBuilder, Validators } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { Observable } from 'rxjs';

import { IInfrastructureProvider, InfrastructureProvider } from 'app/shared/model/infrastructure-provider.model';
import { InfrastructureProviderService } from './infrastructure-provider.service';

@Component({
  selector: 'jhi-infrastructure-provider-update',
  templateUrl: './infrastructure-provider-update.component.html',
})
export class InfrastructureProviderUpdateComponent implements OnInit {
  isSaving = false;

  editForm = this.fb.group({
    id: [],
    name: [null, []],
    organisation: [],
  });

  constructor(
    protected infrastructureProviderService: InfrastructureProviderService,
    protected activatedRoute: ActivatedRoute,
    private fb: FormBuilder
  ) {}

  ngOnInit(): void {
    this.activatedRoute.data.subscribe(({ infrastructureProvider }) => {
      this.updateForm(infrastructureProvider);
    });
  }

  updateForm(infrastructureProvider: IInfrastructureProvider): void {
    this.editForm.patchValue({
      id: infrastructureProvider.id,
      name: infrastructureProvider.name,
      organisation: infrastructureProvider.organisation,
    });
  }

  previousState(): void {
    window.history.back();
  }

  save(): void {
    this.isSaving = true;
    const infrastructureProvider = this.createFromForm();
    if (infrastructureProvider.id !== undefined) {
      this.subscribeToSaveResponse(this.infrastructureProviderService.update(infrastructureProvider));
    } else {
      this.subscribeToSaveResponse(this.infrastructureProviderService.create(infrastructureProvider));
    }
  }

  private createFromForm(): IInfrastructureProvider {
    return {
      ...new InfrastructureProvider(),
      id: this.editForm.get(['id'])!.value,
      name: this.editForm.get(['name'])!.value,
      organisation: this.editForm.get(['organisation'])!.value,
    };
  }

  protected subscribeToSaveResponse(result: Observable<HttpResponse<IInfrastructureProvider>>): void {
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
