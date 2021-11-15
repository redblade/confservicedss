import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';

import { IAppDeploymentOptions } from 'app/shared/model/app-deployment-options.model';

@Component({
  selector: 'jhi-app-deployment-options-detail',
  templateUrl: './app-deployment-options-detail.component.html',
})
export class AppDeploymentOptionsDetailComponent implements OnInit {
  appDeploymentOptions: IAppDeploymentOptions | null = null;

  constructor(protected activatedRoute: ActivatedRoute) {}

  ngOnInit(): void {
    this.activatedRoute.data.subscribe(({ appDeploymentOptions }) => (this.appDeploymentOptions = appDeploymentOptions));
  }

  previousState(): void {
    window.history.back();
  }
}
