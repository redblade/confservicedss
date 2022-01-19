import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of } from 'rxjs';
import { HttpHeaders, HttpResponse } from '@angular/common/http';
import { ActivatedRoute, convertToParamMap } from '@angular/router';

import { ConfserviceTestModule } from '../../../test.module';
import { OptimisationReportComponent } from 'app/entities/optimisation-report/optimisation-report.component';
import { OptimisationReportService } from 'app/entities/optimisation-report/optimisation-report.service';
import { OptimisationReport } from 'app/shared/model/optimisation-report.model';

describe('Component Tests', () => {
  describe('OptimisationReport Management Component', () => {
    let comp: OptimisationReportComponent;
    let fixture: ComponentFixture<OptimisationReportComponent>;
    let service: OptimisationReportService;

    beforeEach(() => {
      TestBed.configureTestingModule({
        imports: [ConfserviceTestModule],
        declarations: [OptimisationReportComponent],
        providers: [
          {
            provide: ActivatedRoute,
            useValue: {
              data: of({
                defaultSort: 'id,asc',
              }),
              queryParamMap: of(
                convertToParamMap({
                  page: '1',
                  size: '1',
                  sort: 'id,desc',
                })
              ),
            },
          },
        ],
      })
        .overrideTemplate(OptimisationReportComponent, '')
        .compileComponents();

      fixture = TestBed.createComponent(OptimisationReportComponent);
      comp = fixture.componentInstance;
      service = fixture.debugElement.injector.get(OptimisationReportService);
    });

    it('Should call load all on init', () => {
      // GIVEN
      const headers = new HttpHeaders().append('link', 'link;link');
      spyOn(service, 'query').and.returnValue(
        of(
          new HttpResponse({
            body: [new OptimisationReport(123)],
            headers,
          })
        )
      );

      // WHEN
      comp.ngOnInit();

      // THEN
      expect(service.query).toHaveBeenCalled();
      expect(comp.optimisationReports && comp.optimisationReports[0]).toEqual(jasmine.objectContaining({ id: 123 }));
    });

    it('should load a page', () => {
      // GIVEN
      const headers = new HttpHeaders().append('link', 'link;link');
      spyOn(service, 'query').and.returnValue(
        of(
          new HttpResponse({
            body: [new OptimisationReport(123)],
            headers,
          })
        )
      );

      // WHEN
      comp.loadPage(1);

      // THEN
      expect(service.query).toHaveBeenCalled();
      expect(comp.optimisationReports && comp.optimisationReports[0]).toEqual(jasmine.objectContaining({ id: 123 }));
    });

    it('should calculate the sort attribute for an id', () => {
      // WHEN
      comp.ngOnInit();
      const result = comp.sort();

      // THEN
      expect(result).toEqual(['id,desc']);
    });

    it('should calculate the sort attribute for a non-id attribute', () => {
      // INIT
      comp.ngOnInit();

      // GIVEN
      comp.predicate = 'name';

      // WHEN
      const result = comp.sort();

      // THEN
      expect(result).toEqual(['name,desc', 'id']);
    });
  });
});
