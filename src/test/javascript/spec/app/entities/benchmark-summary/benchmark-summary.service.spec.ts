import { TestBed, getTestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { BenchmarkSummaryService } from 'app/entities/benchmark-summary/benchmark-summary.service';
import { IBenchmarkSummary, BenchmarkSummary } from 'app/shared/model/benchmark-summary.model';

describe('Service Tests', () => {
  describe('BenchmarkSummary Service', () => {
    let injector: TestBed;
    let service: BenchmarkSummaryService;
    let httpMock: HttpTestingController;
    let elemDefault: IBenchmarkSummary;
    let expectedResult: IBenchmarkSummary | IBenchmarkSummary[] | boolean | null;

    beforeEach(() => {
      TestBed.configureTestingModule({
        imports: [HttpClientTestingModule],
      });
      expectedResult = null;
      injector = getTestBed();
      service = injector.get(BenchmarkSummaryService);
      httpMock = injector.get(HttpTestingController);

      elemDefault = new BenchmarkSummary(0, 0);
    });

    describe('Service methods', () => {
      it('should find an element', () => {
        const returnedFromService = Object.assign({}, elemDefault);

        service.find(123).subscribe(resp => (expectedResult = resp.body));

        const req = httpMock.expectOne({ method: 'GET' });
        req.flush(returnedFromService);
        expect(expectedResult).toMatchObject(elemDefault);
      });

      it('should return a list of BenchmarkSummary', () => {
        const returnedFromService = Object.assign(
          {
            score: 1,
          },
          elemDefault
        );

        const expected = Object.assign({}, returnedFromService);

        service.query().subscribe(resp => (expectedResult = resp.body));

        const req = httpMock.expectOne({ method: 'GET' });
        req.flush([returnedFromService]);
        httpMock.verify();
        expect(expectedResult).toContainEqual(expected);
      });
    });

    afterEach(() => {
      httpMock.verify();
    });
  });
});
