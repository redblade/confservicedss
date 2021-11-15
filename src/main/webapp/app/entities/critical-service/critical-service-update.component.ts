import { Component, OnInit } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import { FormBuilder, Validators } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { Observable } from 'rxjs';
import * as moment from 'moment';
import { DATE_TIME_FORMAT } from 'app/shared/constants/input.constants';

import { ICriticalService, CriticalService } from 'app/shared/model/critical-service.model';
import { CriticalServiceService } from './critical-service.service';
import { IService } from 'app/shared/model/service.model';
import { ServiceService } from 'app/entities/service/service.service';

@Component({
  selector: 'jhi-critical-service-update',
  templateUrl: './critical-service-update.component.html',
})
export class CriticalServiceUpdateComponent implements OnInit {
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
    protected criticalServiceService: CriticalServiceService,
    protected serviceService: ServiceService,
    protected activatedRoute: ActivatedRoute,
    private fb: FormBuilder
  ) {}

  ngOnInit(): void {
    this.activatedRoute.data.subscribe(({ criticalService }) => {
      if (!criticalService.id) {
        const today = moment().startOf('day');
        criticalService.timestampCreated = today;
        criticalService.timestampProcessed = today;
      }

      this.updateForm(criticalService);

      this.serviceService.query().subscribe((res: HttpResponse<IService[]>) => (this.services = res.body || []));
    });
  }

  updateForm(criticalService: ICriticalService): void {
    this.editForm.patchValue({
      id: criticalService.id,
      timestampCreated: criticalService.timestampCreated ? criticalService.timestampCreated.format(DATE_TIME_FORMAT) : null,
      timestampProcessed: criticalService.timestampProcessed ? criticalService.timestampProcessed.format(DATE_TIME_FORMAT) : null,
      actionTaken: criticalService.actionTaken,
      score: criticalService.score,
      details: criticalService.details,
      monitoringPeriodSec: criticalService.monitoringPeriodSec,
      service: criticalService.service,
    });
  }

  previousState(): void {
    window.history.back();
  }

  save(): void {
    this.isSaving = true;
    const criticalService = this.createFromForm();
    if (criticalService.id !== undefined) {
      this.subscribeToSaveResponse(this.criticalServiceService.update(criticalService));
    } else {
      this.subscribeToSaveResponse(this.criticalServiceService.create(criticalService));
    }
  }

  private createFromForm(): ICriticalService {
    return {
      ...new CriticalService(),
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

  protected subscribeToSaveResponse(result: Observable<HttpResponse<ICriticalService>>): void {
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
