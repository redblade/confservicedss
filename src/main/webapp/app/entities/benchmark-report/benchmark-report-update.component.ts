import { Component, OnInit } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import { FormBuilder, Validators } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { Observable } from 'rxjs';
import * as moment from 'moment';
import { DATE_TIME_FORMAT } from 'app/shared/constants/input.constants';

import { IBenchmarkReport, BenchmarkReport } from 'app/shared/model/benchmark-report.model';
import { BenchmarkReportService } from './benchmark-report.service';
import { INode } from 'app/shared/model/node.model';
import { NodeService } from 'app/entities/node/node.service';
import { IBenchmark } from 'app/shared/model/benchmark.model';
import { BenchmarkService } from 'app/entities/benchmark/benchmark.service';

type SelectableEntity = INode | IBenchmark;

@Component({
  selector: 'jhi-benchmark-report-update',
  templateUrl: './benchmark-report-update.component.html',
})
export class BenchmarkReportUpdateComponent implements OnInit {
  isSaving = false;
  nodes: INode[] = [];
  benchmarks: IBenchmark[] = [];

  editForm = this.fb.group({
    id: [],
    time: [],
    metric: [],
    tool: [],
    mean: [],
    interval: [],
    stabilityIndex: [],
    node: [],
    benchmark: [],
  });

  constructor(
    protected benchmarkReportService: BenchmarkReportService,
    protected nodeService: NodeService,
    protected benchmarkService: BenchmarkService,
    protected activatedRoute: ActivatedRoute,
    private fb: FormBuilder
  ) {}

  ngOnInit(): void {
    this.activatedRoute.data.subscribe(({ benchmarkReport }) => {
      if (!benchmarkReport.id) {
        const today = moment().startOf('day');
        benchmarkReport.time = today;
      }

      this.updateForm(benchmarkReport);

      this.nodeService.query().subscribe((res: HttpResponse<INode[]>) => (this.nodes = res.body || []));

      this.benchmarkService.query().subscribe((res: HttpResponse<IBenchmark[]>) => (this.benchmarks = res.body || []));
    });
  }

  updateForm(benchmarkReport: IBenchmarkReport): void {
    this.editForm.patchValue({
      id: benchmarkReport.id,
      time: benchmarkReport.time ? benchmarkReport.time.format(DATE_TIME_FORMAT) : null,
      metric: benchmarkReport.metric,
      tool: benchmarkReport.tool,
      mean: benchmarkReport.mean,
      interval: benchmarkReport.interval,
      stabilityIndex: benchmarkReport.stabilityIndex,
      node: benchmarkReport.node,
      benchmark: benchmarkReport.benchmark,
    });
  }

  previousState(): void {
    window.history.back();
  }

  save(): void {
    this.isSaving = true;
    const benchmarkReport = this.createFromForm();
    if (benchmarkReport.id !== undefined) {
      this.subscribeToSaveResponse(this.benchmarkReportService.update(benchmarkReport));
    } else {
      this.subscribeToSaveResponse(this.benchmarkReportService.create(benchmarkReport));
    }
  }

  private createFromForm(): IBenchmarkReport {
    return {
      ...new BenchmarkReport(),
      id: this.editForm.get(['id'])!.value,
      time: this.editForm.get(['time'])!.value ? moment(this.editForm.get(['time'])!.value, DATE_TIME_FORMAT) : undefined,
      metric: this.editForm.get(['metric'])!.value,
      tool: this.editForm.get(['tool'])!.value,
      mean: this.editForm.get(['mean'])!.value,
      interval: this.editForm.get(['interval'])!.value,
      stabilityIndex: this.editForm.get(['stabilityIndex'])!.value,
      node: this.editForm.get(['node'])!.value,
      benchmark: this.editForm.get(['benchmark'])!.value,
    };
  }

  protected subscribeToSaveResponse(result: Observable<HttpResponse<IBenchmarkReport>>): void {
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
