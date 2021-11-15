import { Component, OnInit } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import { FormBuilder, Validators } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';

import { IServiceOptimisation, ServiceOptimisation } from 'app/shared/model/service-optimisation.model';
import { ServiceOptimisationService } from './service-optimisation.service';
import { IService } from 'app/shared/model/service.model';
import { ServiceService } from 'app/entities/service/service.service';

@Component({
  selector: 'jhi-service-optimisation-update',
  templateUrl: './service-optimisation-update.component.html',
})
export class ServiceOptimisationUpdateComponent implements OnInit {
  isSaving = false;
  services: IService[] = [];

  editForm = this.fb.group({
    id: [],
    name: [],
    optimisation: [],
    parameters: [],
    service: [],
  });

  constructor(
    protected serviceOptimisationService: ServiceOptimisationService,
    protected serviceService: ServiceService,
    protected activatedRoute: ActivatedRoute,
    private fb: FormBuilder
  ) {}

  ngOnInit(): void {
    this.activatedRoute.data.subscribe(({ serviceOptimisation }) => {
      this.updateForm(serviceOptimisation);

      this.serviceService
        .query({ filter: 'serviceoptimisation-is-null' })
        .pipe(
          map((res: HttpResponse<IService[]>) => {
            return res.body || [];
          })
        )
        .subscribe((resBody: IService[]) => {
          if (!serviceOptimisation.service || !serviceOptimisation.service.id) {
            this.services = resBody;
          } else {
            this.serviceService
              .find(serviceOptimisation.service.id)
              .pipe(
                map((subRes: HttpResponse<IService>) => {
                  return subRes.body ? [subRes.body].concat(resBody) : resBody;
                })
              )
              .subscribe((concatRes: IService[]) => (this.services = concatRes));
          }
        });
    });
  }

  updateForm(serviceOptimisation: IServiceOptimisation): void {
    this.editForm.patchValue({
      id: serviceOptimisation.id,
      name: serviceOptimisation.name,
      optimisation: serviceOptimisation.optimisation,
      parameters: serviceOptimisation.parameters,
      service: serviceOptimisation.service,
    });
  }

  previousState(): void {
    window.history.back();
  }

  save(): void {
    this.isSaving = true;
    const serviceOptimisation = this.createFromForm();

	if(serviceOptimisation.service !== undefined){
	     serviceOptimisation.name = "opt_" + serviceOptimisation.service.name;
	} 


    if (serviceOptimisation.id !== undefined) {
      this.subscribeToSaveResponse(this.serviceOptimisationService.update(serviceOptimisation));
    } else {
      this.subscribeToSaveResponse(this.serviceOptimisationService.create(serviceOptimisation));
    }
  }

  private createFromForm(): IServiceOptimisation {
    return {
      ...new ServiceOptimisation(),
      id: this.editForm.get(['id'])!.value,
      name: this.editForm.get(['name'])!.value,
      optimisation: this.editForm.get(['optimisation'])!.value,
      parameters: this.editForm.get(['parameters'])!.value,
      service: this.editForm.get(['service'])!.value,
    };
  }

  protected subscribeToSaveResponse(result: Observable<HttpResponse<IServiceOptimisation>>): void {
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
