import { Component, OnInit } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import { FormBuilder, Validators } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { Observable } from 'rxjs';
import * as moment from 'moment';
import { DATE_TIME_FORMAT } from 'app/shared/constants/input.constants';

import { ISla, Sla } from 'app/shared/model/sla.model';
import { SlaService } from './sla.service';
import { IInfrastructureProvider } from 'app/shared/model/infrastructure-provider.model';
import { InfrastructureProviderService } from 'app/entities/infrastructure-provider/infrastructure-provider.service';
import { IServiceProvider } from 'app/shared/model/service-provider.model';
import { ServiceProviderService } from 'app/entities/service-provider/service-provider.service';
import { IService } from 'app/shared/model/service.model';
import { ServiceService } from 'app/entities/service/service.service';

type SelectableEntity = IInfrastructureProvider | IServiceProvider | IService;

@Component({
  selector: 'jhi-sla-update',
  templateUrl: './sla-update.component.html',
})
export class SlaUpdateComponent implements OnInit {
  isSaving = false;
  infrastructureproviders: IInfrastructureProvider[] = [];
  serviceproviders: IServiceProvider[] = [];
  services: IService[] = [];

  editForm = this.fb.group({
    id: [],
    name: [null, []],
    type: [],
    creation: [],
    expiration: [],
    infrastructureProvider: [],
    serviceProvider: [],
    service: [],
  });

  constructor(
    protected slaService: SlaService,
    protected infrastructureProviderService: InfrastructureProviderService,
    protected serviceProviderService: ServiceProviderService,
    protected serviceService: ServiceService,
    protected activatedRoute: ActivatedRoute,
    private fb: FormBuilder
  ) {}

  ngOnInit(): void {
    this.activatedRoute.data.subscribe(({ sla }) => {
      if (!sla.id) {
        const today = moment().startOf('day');
        sla.creation = today;
        sla.expiration = today;
      }

      this.updateForm(sla);

      this.infrastructureProviderService
        .query()
        .subscribe((res: HttpResponse<IInfrastructureProvider[]>) => (this.infrastructureproviders = res.body || []));

      this.serviceProviderService.query().subscribe((res: HttpResponse<IServiceProvider[]>) => (this.serviceproviders = res.body || []));

      this.serviceService.query().subscribe((res: HttpResponse<IService[]>) => (this.services = res.body || []));
    });
  }

  updateForm(sla: ISla): void {
    this.editForm.patchValue({
      id: sla.id,
      name: sla.name,
      type: sla.type,
      creation: sla.creation ? sla.creation.format(DATE_TIME_FORMAT) : null,
      expiration: sla.expiration ? sla.expiration.format(DATE_TIME_FORMAT) : null,
      infrastructureProvider: sla.infrastructureProvider,
      serviceProvider: sla.serviceProvider,
      service: sla.service,
    });
  }

  previousState(): void {
    window.history.back();
  }

  save(): void {
    this.isSaving = true;
    const sla = this.createFromForm();
    if (sla.id !== undefined) {
      this.subscribeToSaveResponse(this.slaService.update(sla));
    } else {
      this.subscribeToSaveResponse(this.slaService.create(sla));
    }
  }

  private createFromForm(): ISla {
    return {
      ...new Sla(),
      id: this.editForm.get(['id'])!.value,
      name: this.editForm.get(['name'])!.value,
      type: this.editForm.get(['type'])!.value,
      creation: this.editForm.get(['creation'])!.value ? moment(this.editForm.get(['creation'])!.value, DATE_TIME_FORMAT) : undefined,
      expiration: this.editForm.get(['expiration'])!.value ? moment(this.editForm.get(['expiration'])!.value, DATE_TIME_FORMAT) : undefined,
      infrastructureProvider: this.editForm.get(['infrastructureProvider'])!.value,
      serviceProvider: this.editForm.get(['serviceProvider'])!.value,
      service: this.editForm.get(['service'])!.value,
    };
  }

  protected subscribeToSaveResponse(result: Observable<HttpResponse<ISla>>): void {
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

  trackById(index: number, item: SelectableEntity): any {
    return item.id;
  }
}
