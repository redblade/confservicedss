import { Component, OnInit } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import { FormBuilder, Validators } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { Observable } from 'rxjs';

import { IService, Service } from 'app/shared/model/service.model';
import { ServiceService } from './service.service';
import { IApp } from 'app/shared/model/app.model';
import { AppService } from 'app/entities/app/app.service';

@Component({
  selector: 'jhi-service-update',
  templateUrl: './service-update.component.html',
})
export class ServiceUpdateComponent implements OnInit {
  isSaving = false;
  apps: IApp[] = [];

  editForm = this.fb.group({
    id: [],
    name: [],
    profile: [],
    priority: [],
    initialConfiguration: [],
    runtimeConfiguration: [],
    deployType: [],
    deployDescriptor: [null, [Validators.maxLength(40000)]],
    status: [],
    app: [],
  });

  constructor(
    protected serviceService: ServiceService,
    protected appService: AppService,
    protected activatedRoute: ActivatedRoute,
    private fb: FormBuilder
  ) {}

  ngOnInit(): void {
    this.activatedRoute.data.subscribe(({ service }) => {
      this.updateForm(service);

      this.appService.query().subscribe((res: HttpResponse<IApp[]>) => (this.apps = res.body || []));
    });
  }

  updateForm(service: IService): void {
    this.editForm.patchValue({
      id: service.id,
      name: service.name,
      profile: service.profile,
      priority: service.priority,
      initialConfiguration: service.initialConfiguration,
      runtimeConfiguration: service.runtimeConfiguration,
      deployType: service.deployType,
      deployDescriptor: service.deployDescriptor,
      status: service.status,
      app: service.app,
    });
  }

  previousState(): void {
    window.history.back();
  }

  save(): void {
    this.isSaving = true;
    const service = this.createFromForm();
    if (service.id !== undefined) {
      this.subscribeToSaveResponse(this.serviceService.update(service));
    } else {
      this.subscribeToSaveResponse(this.serviceService.create(service));
    }
  }

  private createFromForm(): IService {
    return {
      ...new Service(),
      id: this.editForm.get(['id'])!.value,
      name: this.editForm.get(['name'])!.value,
      profile: this.editForm.get(['profile'])!.value,
      priority: this.editForm.get(['priority'])!.value,
      initialConfiguration: this.editForm.get(['initialConfiguration'])!.value,
      runtimeConfiguration: this.editForm.get(['runtimeConfiguration'])!.value,
      deployType: this.editForm.get(['deployType'])!.value,
      deployDescriptor: this.editForm.get(['deployDescriptor'])!.value,
      status: this.editForm.get(['status'])!.value,
      app: this.editForm.get(['app'])!.value,
    };
  }

  protected subscribeToSaveResponse(result: Observable<HttpResponse<IService>>): void {
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

  trackById(index: number, item: IApp): any {
    return item.id;
  }
}
