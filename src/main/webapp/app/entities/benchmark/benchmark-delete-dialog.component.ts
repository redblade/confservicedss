import { Component } from '@angular/core';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { JhiEventManager } from 'ng-jhipster';

import { IBenchmark } from 'app/shared/model/benchmark.model';
import { BenchmarkService } from './benchmark.service';

@Component({
  templateUrl: './benchmark-delete-dialog.component.html',
})
export class BenchmarkDeleteDialogComponent {
  benchmark?: IBenchmark;

  constructor(protected benchmarkService: BenchmarkService, public activeModal: NgbActiveModal, protected eventManager: JhiEventManager) {}

  cancel(): void {
    this.activeModal.dismiss();
  }

  confirmDelete(id: number): void {
    this.benchmarkService.delete(id).subscribe(() => {
      this.eventManager.broadcast('benchmarkListModification');
      this.activeModal.close();
    });
  }
}
