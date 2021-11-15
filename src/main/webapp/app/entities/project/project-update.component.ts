import { Component, OnInit } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import { FormBuilder, Validators } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { Observable } from 'rxjs';

import { IProject, Project } from 'app/shared/model/project.model';
import { ProjectService } from './project.service';
import { IInfrastructure } from 'app/shared/model/infrastructure.model';
import { InfrastructureService } from 'app/entities/infrastructure/infrastructure.service';
import { IServiceProvider } from 'app/shared/model/service-provider.model';
import { ServiceProviderService } from 'app/entities/service-provider/service-provider.service';

type SelectableEntity = IInfrastructure | IServiceProvider;

@Component({
  selector: 'jhi-project-update',
  templateUrl: './project-update.component.html',
})
export class ProjectUpdateComponent implements OnInit {
  isSaving = false;
  infrastructures: IInfrastructure[] = [];
  serviceproviders: IServiceProvider[] = [];

  editForm = this.fb.group({
    id: [],
    name: [null, []],
    group: [],
    properties: [null, [Validators.maxLength(20000)]],
    quotaCpuMillicore: [],
    quotaMemMB: [],
    quotaDiskGB: [],
    credentials: [null, [Validators.maxLength(20000)]],
    enableBenchmark: [],
    privateBenchmark: [],
    infrastructure: [],
    serviceProvider: [],
  });

  constructor(
    protected projectService: ProjectService,
    protected infrastructureService: InfrastructureService,
    protected serviceProviderService: ServiceProviderService,
    protected activatedRoute: ActivatedRoute,
    private fb: FormBuilder
  ) {}

  ngOnInit(): void {
    this.activatedRoute.data.subscribe(({ project }) => {
      this.updateForm(project);

      this.infrastructureService.query().subscribe((res: HttpResponse<IInfrastructure[]>) => (this.infrastructures = res.body || []));

      this.serviceProviderService.query().subscribe((res: HttpResponse<IServiceProvider[]>) => (this.serviceproviders = res.body || []));
    });
  }

  updateForm(project: IProject): void {
    this.editForm.patchValue({
      id: project.id,
      name: project.name,
      group: project.group,
      properties: project.properties,
      quotaCpuMillicore: project.quotaCpuMillicore,
      quotaMemMB: project.quotaMemMB,
      quotaDiskGB: project.quotaDiskGB,
      credentials: project.credentials,
      enableBenchmark: project.enableBenchmark,
      privateBenchmark: project.privateBenchmark,
      infrastructure: project.infrastructure,
      serviceProvider: project.serviceProvider,
    });
  }

  previousState(): void {
    window.history.back();
  }

  save(): void {
    this.isSaving = true;
    const project = this.createFromForm();
    if (project.id !== undefined) {
      this.subscribeToSaveResponse(this.projectService.update(project));
    } else {
      this.subscribeToSaveResponse(this.projectService.create(project));
    }
  }

  private createFromForm(): IProject {
    return {
      ...new Project(),
      id: this.editForm.get(['id'])!.value,
      name: this.editForm.get(['name'])!.value,
      group: this.editForm.get(['group'])!.value,
      properties: this.editForm.get(['properties'])!.value,
      quotaCpuMillicore: this.editForm.get(['quotaCpuMillicore'])!.value,
      quotaMemMB: this.editForm.get(['quotaMemMB'])!.value,
      quotaDiskGB: this.editForm.get(['quotaDiskGB'])!.value,
      credentials: this.editForm.get(['credentials'])!.value,
      enableBenchmark: this.editForm.get(['enableBenchmark'])!.value,
      privateBenchmark: this.editForm.get(['privateBenchmark'])!.value,
      infrastructure: this.editForm.get(['infrastructure'])!.value,
      serviceProvider: this.editForm.get(['serviceProvider'])!.value,
    };
  }

  protected subscribeToSaveResponse(result: Observable<HttpResponse<IProject>>): void {
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
