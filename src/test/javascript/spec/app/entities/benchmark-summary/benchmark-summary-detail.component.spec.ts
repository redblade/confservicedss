import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute } from '@angular/router';
import { of } from 'rxjs';

import { ConfserviceTestModule } from '../../../test.module';
import { BenchmarkSummaryDetailComponent } from 'app/entities/benchmark-summary/benchmark-summary-detail.component';
import { BenchmarkSummary } from 'app/shared/model/benchmark-summary.model';

describe('Component Tests', () => {
  describe('BenchmarkSummary Management Detail Component', () => {
    let comp: BenchmarkSummaryDetailComponent;
    let fixture: ComponentFixture<BenchmarkSummaryDetailComponent>;
    const route = ({ data: of({ benchmarkSummary: new BenchmarkSummary(123) }) } as any) as ActivatedRoute;

    beforeEach(() => {
      TestBed.configureTestingModule({
        imports: [ConfserviceTestModule],
        declarations: [BenchmarkSummaryDetailComponent],
        providers: [{ provide: ActivatedRoute, useValue: route }],
      })
        .overrideTemplate(BenchmarkSummaryDetailComponent, '')
        .compileComponents();
      fixture = TestBed.createComponent(BenchmarkSummaryDetailComponent);
      comp = fixture.componentInstance;
    });

    describe('OnInit', () => {
      it('Should load benchmarkSummary on init', () => {
        // WHEN
        comp.ngOnInit();

        // THEN
        expect(comp.benchmarkSummary).toEqual(jasmine.objectContaining({ id: 123 }));
      });
    });
  });
});
