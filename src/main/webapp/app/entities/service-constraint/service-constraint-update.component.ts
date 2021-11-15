import { Component, OnInit } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import { FormBuilder, Validators } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { Observable } from 'rxjs';

import { IServiceConstraint, ServiceConstraint } from 'app/shared/model/service-constraint.model';
import { ServiceConstraintService } from './service-constraint.service';
import { IService } from 'app/shared/model/service.model';
import { ServiceService } from 'app/entities/service/service.service';

@Component({
  selector: 'jhi-service-constraint-update',
  templateUrl: './service-constraint-update.component.html',
})
export class ServiceConstraintUpdateComponent implements OnInit {
  isSaving = false;
  services: IService[] = [];

  editForm = this.fb.group({
    id: [],
    name: [],
    category: [],
    value: [],
    valueType: [],
    priority: [],
    service: [],
  });

  constructor(
    protected serviceConstraintService: ServiceConstraintService,
    protected serviceService: ServiceService,
    protected activatedRoute: ActivatedRoute,
    private fb: FormBuilder
  ) {}

  ngOnInit(): void {
    this.activatedRoute.data.subscribe(({ serviceConstraint }) => {
      this.updateForm(serviceConstraint);

      this.serviceService.query().subscribe((res: HttpResponse<IService[]>) => (this.services = res.body || []));
    });
  }

  updateForm(serviceConstraint: IServiceConstraint): void {
    this.editForm.patchValue({
      id: serviceConstraint.id,
      name: serviceConstraint.name,
      category: serviceConstraint.category,
      value: serviceConstraint.value,
      valueType: serviceConstraint.valueType,
      priority: serviceConstraint.priority,
      service: serviceConstraint.service,
    });
  }

  previousState(): void {
    window.history.back();
  }

  save(): void {
    this.isSaving = true;
    const serviceConstraint = this.createFromForm();
    if (serviceConstraint.id !== undefined) {
      this.subscribeToSaveResponse(this.serviceConstraintService.update(serviceConstraint));
    } else {
      this.subscribeToSaveResponse(this.serviceConstraintService.create(serviceConstraint));
    }
  }

  private createFromForm(): IServiceConstraint {
    return {
      ...new ServiceConstraint(),
      id: this.editForm.get(['id'])!.value,
      name: this.editForm.get(['name'])!.value,
      category: this.editForm.get(['category'])!.value,
      value: this.editForm.get(['value'])!.value,
      valueType: this.editForm.get(['valueType'])!.value,
      priority: this.editForm.get(['priority'])!.value,
      service: this.editForm.get(['service'])!.value,
    };
  }

  protected subscribeToSaveResponse(result: Observable<HttpResponse<IServiceConstraint>>): void {
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
