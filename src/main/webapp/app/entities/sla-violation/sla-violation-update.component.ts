import { Component, OnInit } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import { FormBuilder, Validators } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { Observable } from 'rxjs';
import * as moment from 'moment';
import { DATE_TIME_FORMAT } from 'app/shared/constants/input.constants';

import { ISlaViolation, SlaViolation } from 'app/shared/model/sla-violation.model';
import { SlaViolationService } from './sla-violation.service';
import { ISla } from 'app/shared/model/sla.model';
import { SlaService } from 'app/entities/sla/sla.service';

@Component({
  selector: 'jhi-sla-violation-update',
  templateUrl: './sla-violation-update.component.html',
})
export class SlaViolationUpdateComponent implements OnInit {
  isSaving = false;
  slas: ISla[] = [];

  editForm = this.fb.group({
    id: [],
    timestamp: [],
    violationName: [],
    severityType: [],
    description: [null, [Validators.maxLength(20000)]],
    status: [],
    sla: [],
  });

  constructor(
    protected slaViolationService: SlaViolationService,
    protected slaService: SlaService,
    protected activatedRoute: ActivatedRoute,
    private fb: FormBuilder
  ) {}

  ngOnInit(): void {
    this.activatedRoute.data.subscribe(({ slaViolation }) => {
      if (!slaViolation.id) {
        const today = moment().startOf('day');
        slaViolation.timestamp = today;
      }

      this.updateForm(slaViolation);

      this.slaService.query().subscribe((res: HttpResponse<ISla[]>) => (this.slas = res.body || []));
    });
  }

  updateForm(slaViolation: ISlaViolation): void {
    this.editForm.patchValue({
      id: slaViolation.id,
      timestamp: slaViolation.timestamp ? slaViolation.timestamp.format(DATE_TIME_FORMAT) : null,
      violationName: slaViolation.violationName,
      severityType: slaViolation.severityType,
      description: slaViolation.description,
      status: slaViolation.status,
      sla: slaViolation.sla,
    });
  }

  previousState(): void {
    window.history.back();
  }

  save(): void {
    this.isSaving = true;
    const slaViolation = this.createFromForm();
    if (slaViolation.id !== undefined) {
      this.subscribeToSaveResponse(this.slaViolationService.update(slaViolation));
    } else {
      this.subscribeToSaveResponse(this.slaViolationService.create(slaViolation));
    }
  }

  private createFromForm(): ISlaViolation {
    return {
      ...new SlaViolation(),
      id: this.editForm.get(['id'])!.value,
      timestamp: this.editForm.get(['timestamp'])!.value ? moment(this.editForm.get(['timestamp'])!.value, DATE_TIME_FORMAT) : undefined,
      violationName: this.editForm.get(['violationName'])!.value,
      severityType: this.editForm.get(['severityType'])!.value,
      description: this.editForm.get(['description'])!.value,
      status: this.editForm.get(['status'])!.value,
      sla: this.editForm.get(['sla'])!.value,
    };
  }

  protected subscribeToSaveResponse(result: Observable<HttpResponse<ISlaViolation>>): void {
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

  trackById(index: number, item: ISla): any {
    return item.id;
  }
}
