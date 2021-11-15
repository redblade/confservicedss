import { Component, OnInit } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import { FormBuilder, Validators } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';

import { IAppDeploymentOptions, AppDeploymentOptions } from 'app/shared/model/app-deployment-options.model';
import { AppDeploymentOptionsService } from './app-deployment-options.service';
import { IApp } from 'app/shared/model/app.model';
import { AppService } from 'app/entities/app/app.service';

@Component({
  selector: 'jhi-app-deployment-options-update',
  templateUrl: './app-deployment-options-update.component.html',
})
export class AppDeploymentOptionsUpdateComponent implements OnInit {
  isSaving = false;
  apps: IApp[] = [];

  editForm = this.fb.group({
    id: [],
    options: [],
    app: [],
  });

  constructor(
    protected appDeploymentOptionsService: AppDeploymentOptionsService,
    protected appService: AppService,
    protected activatedRoute: ActivatedRoute,
    private fb: FormBuilder
  ) {}

  ngOnInit(): void {
    this.activatedRoute.data.subscribe(({ appDeploymentOptions }) => {
      this.updateForm(appDeploymentOptions);

      this.appService
        .query({ filter: 'appdeploymentoptions-is-null' })
        .pipe(
          map((res: HttpResponse<IApp[]>) => {
            return res.body || [];
          })
        )
        .subscribe((resBody: IApp[]) => {
          if (!appDeploymentOptions.app || !appDeploymentOptions.app.id) {
            this.apps = resBody;
          } else {
            this.appService
              .find(appDeploymentOptions.app.id)
              .pipe(
                map((subRes: HttpResponse<IApp>) => {
                  return subRes.body ? [subRes.body].concat(resBody) : resBody;
                })
              )
              .subscribe((concatRes: IApp[]) => (this.apps = concatRes));
          }
        });
    });
  }

  updateForm(appDeploymentOptions: IAppDeploymentOptions): void {
    this.editForm.patchValue({
      id: appDeploymentOptions.id,
      options: appDeploymentOptions.options,
      app: appDeploymentOptions.app,
    });
  }

  previousState(): void {
    window.history.back();
  }

  save(): void {
    this.isSaving = true;
    const appDeploymentOptions = this.createFromForm();
    if (appDeploymentOptions.id !== undefined) {
      this.subscribeToSaveResponse(this.appDeploymentOptionsService.update(appDeploymentOptions));
    } else {
      this.subscribeToSaveResponse(this.appDeploymentOptionsService.create(appDeploymentOptions));
    }
  }

  private createFromForm(): IAppDeploymentOptions {
    return {
      ...new AppDeploymentOptions(),
      id: this.editForm.get(['id'])!.value,
      options: this.editForm.get(['options'])!.value,
      app: this.editForm.get(['app'])!.value,
    };
  }

  protected subscribeToSaveResponse(result: Observable<HttpResponse<IAppDeploymentOptions>>): void {
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
