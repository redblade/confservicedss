import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute } from '@angular/router';
import { of } from 'rxjs';

import { ConfserviceTestModule } from '../../../test.module';
import { OptimisationReportDetailComponent } from 'app/entities/optimisation-report/optimisation-report-detail.component';
import { OptimisationReport } from 'app/shared/model/optimisation-report.model';

describe('Component Tests', () => {
  describe('OptimisationReport Management Detail Component', () => {
    let comp: OptimisationReportDetailComponent;
    let fixture: ComponentFixture<OptimisationReportDetailComponent>;
    const route = ({ data: of({ optimisationReport: new OptimisationReport(123) }) } as any) as ActivatedRoute;

    beforeEach(() => {
      TestBed.configureTestingModule({
        imports: [ConfserviceTestModule],
        declarations: [OptimisationReportDetailComponent],
        providers: [{ provide: ActivatedRoute, useValue: route }],
      })
        .overrideTemplate(OptimisationReportDetailComponent, '')
        .compileComponents();
      fixture = TestBed.createComponent(OptimisationReportDetailComponent);
      comp = fixture.componentInstance;
    });

    describe('OnInit', () => {
      it('Should load optimisationReport on init', () => {
        // WHEN
        comp.ngOnInit();

        // THEN
        expect(comp.optimisationReport).toEqual(jasmine.objectContaining({ id: 123 }));
      });
    });
  });
});
