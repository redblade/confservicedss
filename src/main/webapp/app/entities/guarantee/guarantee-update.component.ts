import { Component, OnInit } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import { FormBuilder, Validators } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { Observable } from 'rxjs';

import { IGuarantee, Guarantee } from 'app/shared/model/guarantee.model';
import { GuaranteeService } from './guarantee.service';
import { ISla } from 'app/shared/model/sla.model';
import { SlaService } from 'app/entities/sla/sla.service';

@Component({
  selector: 'jhi-guarantee-update',
  templateUrl: './guarantee-update.component.html',
})
export class GuaranteeUpdateComponent implements OnInit {
  isSaving = false;
  slas: ISla[] = [];

  editForm = this.fb.group({
    id: [],
    name: [],
    constraint: [null, [Validators.maxLength(200)]],
    thresholdWarning: [null, [Validators.maxLength(200)]],
    thresholdMild: [null, [Validators.maxLength(200)]],
    thresholdSerious: [null, [Validators.maxLength(200)]],
    thresholdSevere: [null, [Validators.maxLength(200)]],
    thresholdCatastrophic: [null, [Validators.maxLength(200)]],
    sla: [],
  });

  constructor(
    protected guaranteeService: GuaranteeService,
    protected slaService: SlaService,
    protected activatedRoute: ActivatedRoute,
    private fb: FormBuilder
  ) {}

  ngOnInit(): void {
    this.activatedRoute.data.subscribe(({ guarantee }) => {
      this.updateForm(guarantee);

      this.slaService.query().subscribe((res: HttpResponse<ISla[]>) => (this.slas = res.body || []));
    });
  }

  updateForm(guarantee: IGuarantee): void {
    this.editForm.patchValue({
      id: guarantee.id,
      name: guarantee.name,
      constraint: guarantee.constraint,
      thresholdWarning: guarantee.thresholdWarning,
      thresholdMild: guarantee.thresholdMild,
      thresholdSerious: guarantee.thresholdSerious,
      thresholdSevere: guarantee.thresholdSevere,
      thresholdCatastrophic: guarantee.thresholdCatastrophic,
      sla: guarantee.sla,
    });
  }

  previousState(): void {
    window.history.back();
  }

  save(): void {
    this.isSaving = true;
    const guarantee = this.createFromForm();
    if (guarantee.id !== undefined) {
      this.subscribeToSaveResponse(this.guaranteeService.update(guarantee));
    } else {
      this.subscribeToSaveResponse(this.guaranteeService.create(guarantee));
    }
  }

  private createFromForm(): IGuarantee {
    return {
      ...new Guarantee(),
      id: this.editForm.get(['id'])!.value,
      name: this.editForm.get(['name'])!.value,
      constraint: this.editForm.get(['constraint'])!.value,
      thresholdWarning: this.editForm.get(['thresholdWarning'])!.value,
      thresholdMild: this.editForm.get(['thresholdMild'])!.value,
      thresholdSerious: this.editForm.get(['thresholdSerious'])!.value,
      thresholdSevere: this.editForm.get(['thresholdSevere'])!.value,
      thresholdCatastrophic: this.editForm.get(['thresholdCatastrophic'])!.value,
      sla: this.editForm.get(['sla'])!.value,
    };
  }

  protected subscribeToSaveResponse(result: Observable<HttpResponse<IGuarantee>>): void {
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

  trackById(index: number, item: ISla): any {
    return item.id;
  }
}
