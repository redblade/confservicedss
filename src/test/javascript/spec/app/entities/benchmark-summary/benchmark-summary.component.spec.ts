import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of } from 'rxjs';
import { HttpHeaders, HttpResponse } from '@angular/common/http';
import { ActivatedRoute, convertToParamMap } from '@angular/router';

import { ConfserviceTestModule } from '../../../test.module';
import { BenchmarkSummaryComponent } from 'app/entities/benchmark-summary/benchmark-summary.component';
import { BenchmarkSummaryService } from 'app/entities/benchmark-summary/benchmark-summary.service';
import { BenchmarkSummary } from 'app/shared/model/benchmark-summary.model';

describe('Component Tests', () => {
  describe('BenchmarkSummary Management Component', () => {
    let comp: BenchmarkSummaryComponent;
    let fixture: ComponentFixture<BenchmarkSummaryComponent>;
    let service: BenchmarkSummaryService;

    beforeEach(() => {
      TestBed.configureTestingModule({
        imports: [ConfserviceTestModule],
        declarations: [BenchmarkSummaryComponent],
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
        .overrideTemplate(BenchmarkSummaryComponent, '')
        .compileComponents();

      fixture = TestBed.createComponent(BenchmarkSummaryComponent);
      comp = fixture.componentInstance;
      service = fixture.debugElement.injector.get(BenchmarkSummaryService);
    });

    it('Should call load all on init', () => {
      // GIVEN
      const headers = new HttpHeaders().append('link', 'link;link');
      spyOn(service, 'query').and.returnValue(
        of(
          new HttpResponse({
            body: [new BenchmarkSummary(123)],
            headers,
          })
        )
      );

      // WHEN
      comp.ngOnInit();

      // THEN
      expect(service.query).toHaveBeenCalled();
      expect(comp.benchmarkSummaries && comp.benchmarkSummaries[0]).toEqual(jasmine.objectContaining({ id: 123 }));
    });

    it('should load a page', () => {
      // GIVEN
      const headers = new HttpHeaders().append('link', 'link;link');
      spyOn(service, 'query').and.returnValue(
        of(
          new HttpResponse({
            body: [new BenchmarkSummary(123)],
            headers,
          })
        )
      );

      // WHEN
      comp.loadPage(1);

      // THEN
      expect(service.query).toHaveBeenCalled();
      expect(comp.benchmarkSummaries && comp.benchmarkSummaries[0]).toEqual(jasmine.objectContaining({ id: 123 }));
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
