import { Component, OnInit } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import { FormBuilder, Validators } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { Observable } from 'rxjs';

import { INode, Node } from 'app/shared/model/node.model';
import { NodeService } from './node.service';
import { IInfrastructure } from 'app/shared/model/infrastructure.model';
import { InfrastructureService } from 'app/entities/infrastructure/infrastructure.service';

@Component({
  selector: 'jhi-node-update',
  templateUrl: './node-update.component.html',
})
export class NodeUpdateComponent implements OnInit {
  isSaving = false;
  infrastructures: IInfrastructure[] = [];

  editForm = this.fb.group({
    id: [],
    name: [],
    ipaddress: [],
    properties: [null, [Validators.maxLength(2000)]],
    features: [null, [Validators.maxLength(20000)]],
    totalResources: [null, [Validators.maxLength(2000)]],
    infrastructure: [],
  });

  constructor(
    protected nodeService: NodeService,
    protected infrastructureService: InfrastructureService,
    protected activatedRoute: ActivatedRoute,
    private fb: FormBuilder
  ) {}

  ngOnInit(): void {
    this.activatedRoute.data.subscribe(({ node }) => {
      this.updateForm(node);

      this.infrastructureService.query().subscribe((res: HttpResponse<IInfrastructure[]>) => (this.infrastructures = res.body || []));
    });
  }

  updateForm(node: INode): void {
    this.editForm.patchValue({
      id: node.id,
      name: node.name,
      ipaddress: node.ipaddress,
      properties: node.properties,
      features: node.features,
      totalResources: node.totalResources,
      infrastructure: node.infrastructure,
    });
  }

  previousState(): void {
    window.history.back();
  }

  save(): void {
    this.isSaving = true;
    const node = this.createFromForm();
    if (node.id !== undefined) {
      this.subscribeToSaveResponse(this.nodeService.update(node));
    } else {
      this.subscribeToSaveResponse(this.nodeService.create(node));
    }
  }

  private createFromForm(): INode {
    return {
      ...new Node(),
      id: this.editForm.get(['id'])!.value,
      name: this.editForm.get(['name'])!.value,
      ipaddress: this.editForm.get(['ipaddress'])!.value,
      properties: this.editForm.get(['properties'])!.value,
      features: this.editForm.get(['features'])!.value,
      totalResources: this.editForm.get(['totalResources'])!.value,
      infrastructure: this.editForm.get(['infrastructure'])!.value,
    };
  }

  protected subscribeToSaveResponse(result: Observable<HttpResponse<INode>>): void {
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
