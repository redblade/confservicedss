import { Component, OnInit } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import { FormBuilder, Validators } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { Observable } from 'rxjs';
import * as moment from 'moment';
import { DATE_TIME_FORMAT } from 'app/shared/constants/input.constants';

import { IServiceReport, ServiceReport } from 'app/shared/model/service-report.model';
import { ServiceReportService } from './service-report.service';
import { IService } from 'app/shared/model/service.model';
import { ServiceService } from 'app/entities/service/service.service';

@Component({
  selector: 'jhi-service-report-update',
  templateUrl: './service-report-update.component.html',
})
export class ServiceReportUpdateComponent implements OnInit {
  isSaving = false;
  services: IService[] = [];

  editForm = this.fb.group({
    id: [],
    timestamp: [],
    group: [],
    category: [],
    key: [],
    value: [],
    service: [],
  });

  constructor(
    protected serviceReportService: ServiceReportService,
    protected serviceService: ServiceService,
    protected activatedRoute: ActivatedRoute,
    private fb: FormBuilder
  ) {}

  ngOnInit(): void {
    this.activatedRoute.data.subscribe(({ serviceReport }) => {
      if (!serviceReport.id) {
        const today = moment().startOf('day');
        serviceReport.timestamp = today;
      }

      this.updateForm(serviceReport);

      this.serviceService.query().subscribe((res: HttpResponse<IService[]>) => (this.services = res.body || []));
    });
  }

  updateForm(serviceReport: IServiceReport): void {
    this.editForm.patchValue({
      id: serviceReport.id,
      timestamp: serviceReport.timestamp ? serviceReport.timestamp.format(DATE_TIME_FORMAT) : null,
      group: serviceReport.group,
      category: serviceReport.category,
      key: serviceReport.key,
      value: serviceReport.value,
      service: serviceReport.service,
    });
  }

  previousState(): void {
    window.history.back();
  }

  save(): void {
    this.isSaving = true;
    const serviceReport = this.createFromForm();
    if (serviceReport.id !== undefined) {
      this.subscribeToSaveResponse(this.serviceReportService.update(serviceReport));
    } else {
      this.subscribeToSaveResponse(this.serviceReportService.create(serviceReport));
    }
  }

  private createFromForm(): IServiceReport {
    return {
      ...new ServiceReport(),
      id: this.editForm.get(['id'])!.value,
      timestamp: this.editForm.get(['timestamp'])!.value ? moment(this.editForm.get(['timestamp'])!.value, DATE_TIME_FORMAT) : undefined,
      group: this.editForm.get(['group'])!.value,
      category: this.editForm.get(['category'])!.value,
      key: this.editForm.get(['key'])!.value,
      value: this.editForm.get(['value'])!.value,
      service: this.editForm.get(['service'])!.value,
    };
  }

  protected subscribeToSaveResponse(result: Observable<HttpResponse<IServiceReport>>): void {
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
