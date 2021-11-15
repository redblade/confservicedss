import { Component, OnInit } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import { FormBuilder, Validators } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { Observable } from 'rxjs';
import * as moment from 'moment';
import { DATE_TIME_FORMAT } from 'app/shared/constants/input.constants';

import { IAppReport, AppReport } from 'app/shared/model/app-report.model';
import { AppReportService } from './app-report.service';
import { IApp } from 'app/shared/model/app.model';
import { AppService } from 'app/entities/app/app.service';

@Component({
  selector: 'jhi-app-report-update',
  templateUrl: './app-report-update.component.html',
})
export class AppReportUpdateComponent implements OnInit {
  isSaving = false;
  apps: IApp[] = [];

  editForm = this.fb.group({
    id: [],
    timestamp: [],
    group: [],
    category: [],
    key: [],
    value: [],
    app: [],
  });

  constructor(
    protected appReportService: AppReportService,
    protected appService: AppService,
    protected activatedRoute: ActivatedRoute,
    private fb: FormBuilder
  ) {}

  ngOnInit(): void {
    this.activatedRoute.data.subscribe(({ appReport }) => {
      if (!appReport.id) {
        const today = moment().startOf('day');
        appReport.timestamp = today;
      }

      this.updateForm(appReport);

      this.appService.query().subscribe((res: HttpResponse<IApp[]>) => (this.apps = res.body || []));
    });
  }

  updateForm(appReport: IAppReport): void {
    this.editForm.patchValue({
      id: appReport.id,
      timestamp: appReport.timestamp ? appReport.timestamp.format(DATE_TIME_FORMAT) : null,
      group: appReport.group,
      category: appReport.category,
      key: appReport.key,
      value: appReport.value,
      app: appReport.app,
    });
  }

  previousState(): void {
    window.history.back();
  }

  save(): void {
    this.isSaving = true;
    const appReport = this.createFromForm();
    if (appReport.id !== undefined) {
      this.subscribeToSaveResponse(this.appReportService.update(appReport));
    } else {
      this.subscribeToSaveResponse(this.appReportService.create(appReport));
    }
  }

  private createFromForm(): IAppReport {
    return {
      ...new AppReport(),
      id: this.editForm.get(['id'])!.value,
      timestamp: this.editForm.get(['timestamp'])!.value ? moment(this.editForm.get(['timestamp'])!.value, DATE_TIME_FORMAT) : undefined,
      group: this.editForm.get(['group'])!.value,
      category: this.editForm.get(['category'])!.value,
      key: this.editForm.get(['key'])!.value,
      value: this.editForm.get(['value'])!.value,
      app: this.editForm.get(['app'])!.value,
    };
  }

  protected subscribeToSaveResponse(result: Observable<HttpResponse<IAppReport>>): void {
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
