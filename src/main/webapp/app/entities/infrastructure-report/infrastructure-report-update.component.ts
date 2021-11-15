import { Component, OnInit } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import { FormBuilder, Validators } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { Observable } from 'rxjs';
import * as moment from 'moment';
import { DATE_TIME_FORMAT } from 'app/shared/constants/input.constants';

import { IInfrastructureReport, InfrastructureReport } from 'app/shared/model/infrastructure-report.model';
import { InfrastructureReportService } from './infrastructure-report.service';
import { IInfrastructure } from 'app/shared/model/infrastructure.model';
import { InfrastructureService } from 'app/entities/infrastructure/infrastructure.service';

@Component({
  selector: 'jhi-infrastructure-report-update',
  templateUrl: './infrastructure-report-update.component.html',
})
export class InfrastructureReportUpdateComponent implements OnInit {
  isSaving = false;
  infrastructures: IInfrastructure[] = [];

  editForm = this.fb.group({
    id: [],
    timestamp: [],
    group: [],
    category: [],
    key: [],
    value: [],
    infrastructure: [],
  });

  constructor(
    protected infrastructureReportService: InfrastructureReportService,
    protected infrastructureService: InfrastructureService,
    protected activatedRoute: ActivatedRoute,
    private fb: FormBuilder
  ) {}

  ngOnInit(): void {
    this.activatedRoute.data.subscribe(({ infrastructureReport }) => {
      if (!infrastructureReport.id) {
        const today = moment().startOf('day');
        infrastructureReport.timestamp = today;
      }

      this.updateForm(infrastructureReport);

      this.infrastructureService.query().subscribe((res: HttpResponse<IInfrastructure[]>) => (this.infrastructures = res.body || []));
    });
  }

  updateForm(infrastructureReport: IInfrastructureReport): void {
    this.editForm.patchValue({
      id: infrastructureReport.id,
      timestamp: infrastructureReport.timestamp ? infrastructureReport.timestamp.format(DATE_TIME_FORMAT) : null,
      group: infrastructureReport.group,
      category: infrastructureReport.category,
      key: infrastructureReport.key,
      value: infrastructureReport.value,
      infrastructure: infrastructureReport.infrastructure,
    });
  }

  previousState(): void {
    window.history.back();
  }

  save(): void {
    this.isSaving = true;
    const infrastructureReport = this.createFromForm();
    if (infrastructureReport.id !== undefined) {
      this.subscribeToSaveResponse(this.infrastructureReportService.update(infrastructureReport));
    } else {
      this.subscribeToSaveResponse(this.infrastructureReportService.create(infrastructureReport));
    }
  }

  private createFromForm(): IInfrastructureReport {
    return {
      ...new InfrastructureReport(),
      id: this.editForm.get(['id'])!.value,
      timestamp: this.editForm.get(['timestamp'])!.value ? moment(this.editForm.get(['timestamp'])!.value, DATE_TIME_FORMAT) : undefined,
      group: this.editForm.get(['group'])!.value,
      category: this.editForm.get(['category'])!.value,
      key: this.editForm.get(['key'])!.value,
      value: this.editForm.get(['value'])!.value,
      infrastructure: this.editForm.get(['infrastructure'])!.value,
    };
  }

  protected subscribeToSaveResponse(result: Observable<HttpResponse<IInfrastructureReport>>): void {
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

  trackById(index: number, item: IInfrastructure): any {
    return item.id;
  }
}
