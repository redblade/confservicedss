import { Component, OnInit } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import { FormBuilder, Validators } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { Observable } from 'rxjs';
import * as moment from 'moment';
import { DATE_TIME_FORMAT } from 'app/shared/constants/input.constants';

import { INodeReport, NodeReport } from 'app/shared/model/node-report.model';
import { NodeReportService } from './node-report.service';
import { INode } from 'app/shared/model/node.model';
import { NodeService } from 'app/entities/node/node.service';

@Component({
  selector: 'jhi-node-report-update',
  templateUrl: './node-report-update.component.html',
})
export class NodeReportUpdateComponent implements OnInit {
  isSaving = false;
  nodes: INode[] = [];

  editForm = this.fb.group({
    id: [],
    timestamp: [],
    category: [],
    key: [],
    value: [],
    node: [],
    nodeDestination: [],
  });

  constructor(
    protected nodeReportService: NodeReportService,
    protected nodeService: NodeService,
    protected activatedRoute: ActivatedRoute,
    private fb: FormBuilder
  ) {}

  ngOnInit(): void {
    this.activatedRoute.data.subscribe(({ nodeReport }) => {
      if (!nodeReport.id) {
        const today = moment().startOf('day');
        nodeReport.timestamp = today;
      }

      this.updateForm(nodeReport);

      this.nodeService.query().subscribe((res: HttpResponse<INode[]>) => (this.nodes = res.body || []));
    });
  }

  updateForm(nodeReport: INodeReport): void {
    this.editForm.patchValue({
      id: nodeReport.id,
      timestamp: nodeReport.timestamp ? nodeReport.timestamp.format(DATE_TIME_FORMAT) : null,
      category: nodeReport.category,
      key: nodeReport.key,
      value: nodeReport.value,
      node: nodeReport.node,
      nodeDestination: nodeReport.nodeDestination,
    });
  }

  previousState(): void {
    window.history.back();
  }

  save(): void {
    this.isSaving = true;
    const nodeReport = this.createFromForm();
    if (nodeReport.id !== undefined) {
      this.subscribeToSaveResponse(this.nodeReportService.update(nodeReport));
    } else {
      this.subscribeToSaveResponse(this.nodeReportService.create(nodeReport));
    }
  }

  private createFromForm(): INodeReport {
    return {
      ...new NodeReport(),
      id: this.editForm.get(['id'])!.value,
      timestamp: this.editForm.get(['timestamp'])!.value ? moment(this.editForm.get(['timestamp'])!.value, DATE_TIME_FORMAT) : undefined,
      category: this.editForm.get(['category'])!.value,
      key: this.editForm.get(['key'])!.value,
      value: this.editForm.get(['value'])!.value,
      node: this.editForm.get(['node'])!.value,
      nodeDestination: this.editForm.get(['nodeDestination'])!.value,
    };
  }

  protected subscribeToSaveResponse(result: Observable<HttpResponse<INodeReport>>): void {
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

  trackById(index: number, item: INode): any {
    return item.id;
  }
}
