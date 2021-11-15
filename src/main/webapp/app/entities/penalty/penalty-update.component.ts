import { Component, OnInit } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import { FormBuilder, Validators } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { Observable } from 'rxjs';

import { IPenalty, Penalty } from 'app/shared/model/penalty.model';
import { PenaltyService } from './penalty.service';
import { IGuarantee } from 'app/shared/model/guarantee.model';
import { GuaranteeService } from 'app/entities/guarantee/guarantee.service';

@Component({
  selector: 'jhi-penalty-update',
  templateUrl: './penalty-update.component.html',
})
export class PenaltyUpdateComponent implements OnInit {
  isSaving = false;
  guarantees: IGuarantee[] = [];

  editForm = this.fb.group({
    id: [],
    name: [null, []],
    type: [],
    value: [],
    guarantee: [],
  });

  constructor(
    protected penaltyService: PenaltyService,
    protected guaranteeService: GuaranteeService,
    protected activatedRoute: ActivatedRoute,
    private fb: FormBuilder
  ) {}

  ngOnInit(): void {
    this.activatedRoute.data.subscribe(({ penalty }) => {
      this.updateForm(penalty);

      this.guaranteeService.query().subscribe((res: HttpResponse<IGuarantee[]>) => (this.guarantees = res.body || []));
    });
  }

  updateForm(penalty: IPenalty): void {
    this.editForm.patchValue({
      id: penalty.id,
      name: penalty.name,
      type: penalty.type,
      value: penalty.value,
      guarantee: penalty.guarantee,
    });
  }

  previousState(): void {
    window.history.back();
  }

  save(): void {
    this.isSaving = true;
    const penalty = this.createFromForm();
    if (penalty.id !== undefined) {
      this.subscribeToSaveResponse(this.penaltyService.update(penalty));
    } else {
      this.subscribeToSaveResponse(this.penaltyService.create(penalty));
    }
  }

  private createFromForm(): IPenalty {
    return {
      ...new Penalty(),
      id: this.editForm.get(['id'])!.value,
      name: this.editForm.get(['name'])!.value,
      type: this.editForm.get(['type'])!.value,
      value: this.editForm.get(['value'])!.value,
      guarantee: this.editForm.get(['guarantee'])!.value,
    };
  }

  protected subscribeToSaveResponse(result: Observable<HttpResponse<IPenalty>>): void {
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

  trackById(index: number, item: IGuarantee): any {
    return item.id;
  }
}
