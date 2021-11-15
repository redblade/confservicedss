import { Component, OnInit } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import { FormBuilder, Validators } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { Observable } from 'rxjs';

import { IAppConstraint, AppConstraint } from 'app/shared/model/app-constraint.model';
import { AppConstraintService } from './app-constraint.service';
import { IService } from 'app/shared/model/service.model';
import { ServiceService } from 'app/entities/service/service.service';

@Component({
  selector: 'jhi-app-constraint-update',
  templateUrl: './app-constraint-update.component.html',
})
export class AppConstraintUpdateComponent implements OnInit {
  isSaving = false;
  services: IService[] = [];

  editForm = this.fb.group({
    id: [],
    name: [],
    category: [],
    value: [],
    valueType: [],
    serviceSource: [],
    serviceDestination: [],
  });

  constructor(
    protected appConstraintService: AppConstraintService,
    protected serviceService: ServiceService,
    protected activatedRoute: ActivatedRoute,
    private fb: FormBuilder
  ) {}

  ngOnInit(): void {
    this.activatedRoute.data.subscribe(({ appConstraint }) => {
      this.updateForm(appConstraint);

      this.serviceService.query().subscribe((res: HttpResponse<IService[]>) => (this.services = res.body || []));
    });
  }

  updateForm(appConstraint: IAppConstraint): void {
    this.editForm.patchValue({
      id: appConstraint.id,
      name: appConstraint.name,
      category: appConstraint.category,
      value: appConstraint.value,
      valueType: appConstraint.valueType,
      serviceSource: appConstraint.serviceSource,
      serviceDestination: appConstraint.serviceDestination,
    });
  }

  previousState(): void {
    window.history.back();
  }

  save(): void {
    this.isSaving = true;
    const appConstraint = this.createFromForm();
    if (appConstraint.id !== undefined) {
      this.subscribeToSaveResponse(this.appConstraintService.update(appConstraint));
    } else {
      this.subscribeToSaveResponse(this.appConstraintService.create(appConstraint));
    }
  }

  private createFromForm(): IAppConstraint {
    return {
      ...new AppConstraint(),
      id: this.editForm.get(['id'])!.value,
      name: this.editForm.get(['name'])!.value,
      category: this.editForm.get(['category'])!.value,
      value: this.editForm.get(['value'])!.value,
      valueType: this.editForm.get(['valueType'])!.value,
      serviceSource: this.editForm.get(['serviceSource'])!.value,
      serviceDestination: this.editForm.get(['serviceDestination'])!.value,
    };
  }

  protected subscribeToSaveResponse(result: Observable<HttpResponse<IAppConstraint>>): void {
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

  trackById(index: number, item: IService): any {
    return item.id;
  }
}
