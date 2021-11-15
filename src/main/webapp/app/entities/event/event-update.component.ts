import { Component, OnInit } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import { FormBuilder, Validators } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { Observable } from 'rxjs';
import * as moment from 'moment';
import { DATE_TIME_FORMAT } from 'app/shared/constants/input.constants';

import { IEvent, Event } from 'app/shared/model/event.model';
import { EventService } from './event.service';
import { IServiceProvider } from 'app/shared/model/service-provider.model';
import { ServiceProviderService } from 'app/entities/service-provider/service-provider.service';

@Component({
  selector: 'jhi-event-update',
  templateUrl: './event-update.component.html',
})
export class EventUpdateComponent implements OnInit {
  isSaving = false;
  serviceproviders: IServiceProvider[] = [];

  editForm = this.fb.group({
    id: [],
    timestamp: [],
    severity: [],
    category: [],
    details: [],
    serviceProvider: [],
  });

  constructor(
    protected eventService: EventService,
    protected serviceProviderService: ServiceProviderService,
    protected activatedRoute: ActivatedRoute,
    private fb: FormBuilder
  ) {}

  ngOnInit(): void {
    this.activatedRoute.data.subscribe(({ event }) => {
      if (!event.id) {
        const today = moment().startOf('day');
        event.timestamp = today;
      }

      this.updateForm(event);

      this.serviceProviderService.query().subscribe((res: HttpResponse<IServiceProvider[]>) => (this.serviceproviders = res.body || []));
    });
  }

  updateForm(event: IEvent): void {
    this.editForm.patchValue({
      id: event.id,
      timestamp: event.timestamp ? event.timestamp.format(DATE_TIME_FORMAT) : null,
      severity: event.severity,
      category: event.category,
      details: event.details,
      serviceProvider: event.serviceProvider,
    });
  }

  previousState(): void {
    window.history.back();
  }

  save(): void {
    this.isSaving = true;
    const event = this.createFromForm();
    if (event.id !== undefined) {
      this.subscribeToSaveResponse(this.eventService.update(event));
    } else {
      this.subscribeToSaveResponse(this.eventService.create(event));
    }
  }

  private createFromForm(): IEvent {
    return {
      ...new Event(),
      id: this.editForm.get(['id'])!.value,
      timestamp: this.editForm.get(['timestamp'])!.value ? moment(this.editForm.get(['timestamp'])!.value, DATE_TIME_FORMAT) : undefined,
      severity: this.editForm.get(['severity'])!.value,
      category: this.editForm.get(['category'])!.value,
      details: this.editForm.get(['details'])!.value,
      serviceProvider: this.editForm.get(['serviceProvider'])!.value,
    };
  }

  protected subscribeToSaveResponse(result: Observable<HttpResponse<IEvent>>): void {
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

  trackById(index: number, item: IServiceProvider): any {
    return item.id;
  }
}
