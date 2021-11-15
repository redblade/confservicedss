import { Component, OnInit } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import { FormBuilder, Validators } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { Observable } from 'rxjs';
import * as moment from 'moment';
import { DATE_TIME_FORMAT } from 'app/shared/constants/input.constants';

import { ISteadyService, SteadyService } from 'app/shared/model/steady-service.model';
import { SteadyServiceService } from './steady-service.service';
import { IService } from 'app/shared/model/service.model';
import { ServiceService } from 'app/entities/service/service.service';

@Component({
  selector: 'jhi-steady-service-update',
  templateUrl: './steady-service-update.component.html',
})
export class SteadyServiceUpdateComponent implements OnInit {
  isSaving = false;
  services: IService[] = [];

  editForm = this.fb.group({
    id: [],
    timestampCreated: [],
    timestampProcessed: [],
    actionTaken: [],
    score: [],
    details: [],
    monitoringPeriodSec: [],
    service: [],
  });

  constructor(
    protected steadyServiceService: SteadyServiceService,
    protected serviceService: ServiceService,
    protected activatedRoute: ActivatedRoute,
    private fb: FormBuilder
  ) {}

  ngOnInit(): void {
    this.activatedRoute.data.subscribe(({ steadyService }) => {
      if (!steadyService.id) {
        const today = moment().startOf('day');
        steadyService.timestampCreated = today;
        steadyService.timestampProcessed = today;
      }

      this.updateForm(steadyService);

      this.serviceService.query().subscribe((res: HttpResponse<IService[]>) => (this.services = res.body || []));
    });
  }

  updateForm(steadyService: ISteadyService): void {
    this.editForm.patchValue({
      id: steadyService.id,
      timestampCreated: steadyService.timestampCreated ? steadyService.timestampCreated.format(DATE_TIME_FORMAT) : null,
      timestampProcessed: steadyService.timestampProcessed ? steadyService.timestampProcessed.format(DATE_TIME_FORMAT) : null,
      actionTaken: steadyService.actionTaken,
      score: steadyService.score,
      details: steadyService.details,
      monitoringPeriodSec: steadyService.monitoringPeriodSec,
      service: steadyService.service,
    });
  }

  previousState(): void {
    window.history.back();
  }

  save(): void {
    this.isSaving = true;
    const steadyService = this.createFromForm();
    if (steadyService.id !== undefined) {
      this.subscribeToSaveResponse(this.steadyServiceService.update(steadyService));
    } else {
      this.subscribeToSaveResponse(this.steadyServiceService.create(steadyService));
    }
  }

  private createFromForm(): ISteadyService {
    return {
      ...new SteadyService(),
      id: this.editForm.get(['id'])!.value,
      timestampCreated: this.editForm.get(['timestampCreated'])!.value
        ? moment(this.editForm.get(['timestampCreated'])!.value, DATE_TIME_FORMAT)
        : undefined,
      timestampProcessed: this.editForm.get(['timestampProcessed'])!.value
        ? moment(this.editForm.get(['timestampProcessed'])!.value, DATE_TIME_FORMAT)
        : undefined,
      actionTaken: this.editForm.get(['actionTaken'])!.value,
      score: this.editForm.get(['score'])!.value,
      details: this.editForm.get(['details'])!.value,
      monitoringPeriodSec: this.editForm.get(['monitoringPeriodSec'])!.value,
      service: this.editForm.get(['service'])!.value,
    };
  }

  protected subscribeToSaveResponse(result: Observable<HttpResponse<ISteadyService>>): void {
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
