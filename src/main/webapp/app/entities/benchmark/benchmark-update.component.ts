import { Component, OnInit } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import { FormBuilder, Validators } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { Observable } from 'rxjs';

import { IBenchmark, Benchmark } from 'app/shared/model/benchmark.model';
import { BenchmarkService } from './benchmark.service';
import { IInfrastructure } from 'app/shared/model/infrastructure.model';
import { InfrastructureService } from 'app/entities/infrastructure/infrastructure.service';
import { IServiceProvider } from 'app/shared/model/service-provider.model';
import { ServiceProviderService } from 'app/entities/service-provider/service-provider.service';

type SelectableEntity = IInfrastructure | IServiceProvider;

@Component({
  selector: 'jhi-benchmark-update',
  templateUrl: './benchmark-update.component.html',
})
export class BenchmarkUpdateComponent implements OnInit {
  isSaving = false;
  infrastructures: IInfrastructure[] = [];
  serviceproviders: IServiceProvider[] = [];

  editForm = this.fb.group({
    id: [],
    name: [null, []],
    category: [],
    infrastructure: [],
    serviceProvider: [],
  });

  constructor(
    protected benchmarkService: BenchmarkService,
    protected infrastructureService: InfrastructureService,
    protected serviceProviderService: ServiceProviderService,
    protected activatedRoute: ActivatedRoute,
    private fb: FormBuilder
  ) {}

  ngOnInit(): void {
    this.activatedRoute.data.subscribe(({ benchmark }) => {
      this.updateForm(benchmark);

      this.infrastructureService.query().subscribe((res: HttpResponse<IInfrastructure[]>) => (this.infrastructures = res.body || []));

      this.serviceProviderService.query().subscribe((res: HttpResponse<IServiceProvider[]>) => (this.serviceproviders = res.body || []));
    });
  }

  updateForm(benchmark: IBenchmark): void {
    this.editForm.patchValue({
      id: benchmark.id,
      name: benchmark.name,
      category: benchmark.category,
      infrastructure: benchmark.infrastructure,
      serviceProvider: benchmark.serviceProvider,
    });
  }

  previousState(): void {
    window.history.back();
  }

  save(): void {
    this.isSaving = true;
    const benchmark = this.createFromForm();
    if (benchmark.id !== undefined) {
      this.subscribeToSaveResponse(this.benchmarkService.update(benchmark));
    } else {
      this.subscribeToSaveResponse(this.benchmarkService.create(benchmark));
    }
  }

  private createFromForm(): IBenchmark {
    return {
      ...new Benchmark(),
      id: this.editForm.get(['id'])!.value,
      name: this.editForm.get(['name'])!.value,
      category: this.editForm.get(['category'])!.value,
      infrastructure: this.editForm.get(['infrastructure'])!.value,
      serviceProvider: this.editForm.get(['serviceProvider'])!.value,
    };
  }

  protected subscribeToSaveResponse(result: Observable<HttpResponse<IBenchmark>>): void {
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
